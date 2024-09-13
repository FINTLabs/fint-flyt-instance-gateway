package no.fintlabs.gateway.instance;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.gateway.instance.exception.AbstractInstanceRejectedException;
import no.fintlabs.gateway.instance.exception.FileUploadException;
import no.fintlabs.gateway.instance.exception.IntegrationDeactivatedException;
import no.fintlabs.gateway.instance.exception.NoIntegrationException;
import no.fintlabs.gateway.instance.kafka.InstanceReceivalErrorEventProducerService;
import no.fintlabs.gateway.instance.kafka.IntegrationRequestProducerService;
import no.fintlabs.gateway.instance.kafka.ReceivedInstanceEventProducerService;
import no.fintlabs.gateway.instance.model.Integration;
import no.fintlabs.gateway.instance.model.SourceApplicationIdAndSourceApplicationIntegrationId;
import no.fintlabs.gateway.instance.validation.InstanceValidationException;
import no.fintlabs.gateway.instance.validation.InstanceValidationService;
import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationAuthorizationUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
public class InstanceProcessor<T> {

    private final IntegrationRequestProducerService integrationRequestProducerService;
    private final InstanceValidationService instanceValidationService;
    private final ReceivedInstanceEventProducerService receivedInstanceEventProducerService;
    private final InstanceReceivalErrorEventProducerService instanceReceivalErrorEventProducerService;
    private final FileClient fileClient;
    private final Function<T, Optional<String>> sourceApplicationIntegrationIdFunction;
    private final Function<T, Optional<String>> sourceApplicationInstanceIdFunction;
    private final InstanceMapper<T> instanceMapper;

    @Value("${fint.flyt.instance-gateway.check-integration-exists:true}")
    boolean checkIntegrationExists;

    InstanceProcessor(
            IntegrationRequestProducerService integrationRequestProducerService,
            InstanceValidationService instanceValidationService,
            ReceivedInstanceEventProducerService receivedInstanceEventProducerService,
            InstanceReceivalErrorEventProducerService instanceReceivalErrorEventProducerService,
            FileClient fileClient,
            Function<T, Optional<String>> sourceApplicationIntegrationIdFunction,
            Function<T, Optional<String>> sourceApplicationInstanceIdFunction,
            InstanceMapper<T> instanceMapper
    ) {
        this.integrationRequestProducerService = integrationRequestProducerService;
        this.instanceValidationService = instanceValidationService;
        this.receivedInstanceEventProducerService = receivedInstanceEventProducerService;
        this.instanceReceivalErrorEventProducerService = instanceReceivalErrorEventProducerService;
        this.fileClient = fileClient;
        this.sourceApplicationIntegrationIdFunction = sourceApplicationIntegrationIdFunction;
        this.sourceApplicationInstanceIdFunction = sourceApplicationInstanceIdFunction;
        this.instanceMapper = instanceMapper;
    }

    public Mono<ResponseEntity<Object>> processInstance(
            Authentication authentication,
            T incomingInstance
    ) {

        InstanceFlowHeaders.InstanceFlowHeadersBuilder instanceFlowHeadersBuilder = InstanceFlowHeaders.builder();

        Long sourceApplicationId;

        List<UUID> fileIds = new ArrayList<>();

        try {
            sourceApplicationId = SourceApplicationAuthorizationUtil.getSourceApplicationId(authentication);

            instanceFlowHeadersBuilder.correlationId(UUID.randomUUID());
            instanceFlowHeadersBuilder.sourceApplicationId(sourceApplicationId);

            instanceFlowHeadersBuilder.fileIds(fileIds);

            Optional<String> sourceApplicationIntegrationIdOptional = sourceApplicationIntegrationIdFunction.apply(incomingInstance);
            Optional<String> sourceApplicationInstanceIdOptional = sourceApplicationInstanceIdFunction.apply(incomingInstance);

            sourceApplicationIntegrationIdOptional.ifPresent(instanceFlowHeadersBuilder::sourceApplicationIntegrationId);
            sourceApplicationInstanceIdOptional.ifPresent(instanceFlowHeadersBuilder::sourceApplicationInstanceId);

            sourceApplicationIntegrationIdOptional.ifPresent(sourceApplicationIntegrationId -> {
                if (!sourceApplicationIntegrationId.isBlank()) {

                    SourceApplicationIdAndSourceApplicationIntegrationId sourceApplicationIdAndSourceApplicationIntegrationId =
                            SourceApplicationIdAndSourceApplicationIntegrationId
                                    .builder()
                                    .sourceApplicationId(sourceApplicationId)
                                    .sourceApplicationIntegrationId(sourceApplicationIntegrationId)
                                    .build();

                    if (checkIntegrationExists) {
                        Integration integration = integrationRequestProducerService
                                .get(sourceApplicationIdAndSourceApplicationIntegrationId)
                                .orElseThrow(() -> new NoIntegrationException(sourceApplicationIdAndSourceApplicationIntegrationId));

                        instanceFlowHeadersBuilder.integrationId(integration.getId());

                        if (integration.getState() == Integration.State.DEACTIVATED) {
                            throw new IntegrationDeactivatedException(integration);
                        }
                    }
                }
            });

            instanceValidationService.validate(incomingInstance).ifPresent((validationErrors) -> {
                throw new InstanceValidationException(validationErrors);
            });

            if (sourceApplicationIntegrationIdOptional.isEmpty()) {
                throw new IllegalStateException("sourceApplicationIntegrationIdOptional is empty, and was not caught in validation");
            }
            if (sourceApplicationInstanceIdOptional.isEmpty()) {
                throw new IllegalStateException("sourceApplicationInstanceIdOptional is empty, and was not caught in validation");
            }

        } catch (InstanceValidationException e) {
            instanceReceivalErrorEventProducerService.publishInstanceValidationErrorEvent(instanceFlowHeadersBuilder.build(), e);

            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Validation error" + (e.getValidationErrors().size() > 1 ? "s:" : ": ") +
                            e.getValidationErrors()
                                    .stream()
                                    .map(error -> "'" + error.getFieldPath() + " " + error.getErrorMessage() + "'")
                                    .toList()
            );
        } catch (NoIntegrationException e) {
            instanceReceivalErrorEventProducerService.publishNoIntegrationFoundErrorEvent(instanceFlowHeadersBuilder.build(), e);

            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    e.getMessage()
            );
        } catch (IntegrationDeactivatedException e) {
            instanceReceivalErrorEventProducerService.publishIntegrationDeactivatedErrorEvent(instanceFlowHeadersBuilder.build(), e);

            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    e.getMessage()
            );
        } catch (RuntimeException e) {
            instanceReceivalErrorEventProducerService.publishGeneralSystemErrorEvent(instanceFlowHeadersBuilder.build());
            throw e;
        }


        return instanceMapper.map(
                        sourceApplicationId,
                        incomingInstance,
                        file -> fileClient.postFile(file)
                                .doOnNext(fileIds::add)
                )
                .doOnNext(instance -> receivedInstanceEventProducerService.publish(
                        instanceFlowHeadersBuilder.build(),
                        instance
                ))
                .thenReturn(ResponseEntity.accepted().build())
                .onErrorResume(AbstractInstanceRejectedException.class, e -> {
                    log.error("Instance receival error");
                    instanceReceivalErrorEventProducerService.publishInstanceRejectedErrorEvent(instanceFlowHeadersBuilder.build(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage()));
                })
                .onErrorResume(e -> e instanceof FileUploadException, e -> {
                    log.error("File upload error");
                    instanceReceivalErrorEventProducerService.publishInstanceFileUploadErrorEvent(instanceFlowHeadersBuilder.build(), (FileUploadException) e);
                    return Mono.just(
                            ResponseEntity
                                    .internalServerError()
                                    .body(e.getMessage())
                    );
                })
                .onErrorResume(e -> {
                    log.error("General system error ", e);
                    instanceReceivalErrorEventProducerService.publishGeneralSystemErrorEvent(instanceFlowHeadersBuilder.build());
                    return Mono.just(ResponseEntity.internalServerError().build());
                });

    }

}
