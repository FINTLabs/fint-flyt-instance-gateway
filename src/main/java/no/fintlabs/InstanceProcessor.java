package no.fintlabs;

import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.InstanceReceivalErrorEventProducerService;
import no.fintlabs.kafka.IntegrationRequestProducerService;
import no.fintlabs.kafka.ReceivedInstanceEventProducerService;
import no.fintlabs.model.Integration;
import no.fintlabs.model.SourceApplicationIdAndSourceApplicationIntegrationId;
import no.fintlabs.resourceserver.security.client.ClientAuthorizationUtil;
import no.fintlabs.validation.InstanceValidationException;
import no.fintlabs.validation.InstanceValidationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class InstanceProcessor<T> {

    private final IntegrationRequestProducerService integrationRequestProducerService;
    private final InstanceValidationService instanceValidationService;
    private final ReceivedInstanceEventProducerService receivedInstanceEventProducerService;
    private final InstanceReceivalErrorEventProducerService instanceReceivalErrorEventProducerService;
    private final Function<T, Optional<String>> sourceApplicationIntegrationIdFunction;
    private final Function<T, Optional<String>> sourceApplicationInstanceIdFunction;
    private final InstanceMapper<T> instanceMapper;

    public InstanceProcessor(
            IntegrationRequestProducerService integrationRequestProducerService,
            InstanceValidationService instanceValidationService,
            ReceivedInstanceEventProducerService receivedInstanceEventProducerService,
            InstanceReceivalErrorEventProducerService instanceReceivalErrorEventProducerService,
            Function<T, Optional<String>> sourceApplicationIntegrationIdFunction,
            Function<T, Optional<String>> sourceApplicationInstanceIdFunction,
            InstanceMapper<T> instanceMapper
    ) {
        this.integrationRequestProducerService = integrationRequestProducerService;
        this.instanceValidationService = instanceValidationService;
        this.receivedInstanceEventProducerService = receivedInstanceEventProducerService;
        this.instanceReceivalErrorEventProducerService = instanceReceivalErrorEventProducerService;
        this.sourceApplicationIntegrationIdFunction = sourceApplicationIntegrationIdFunction;
        this.sourceApplicationInstanceIdFunction = sourceApplicationInstanceIdFunction;
        this.instanceMapper = instanceMapper;
    }

    public Mono<ResponseEntity<?>> processInstance(
            Authentication authentication,
            T incomingInstance
    ) {

        InstanceFlowHeaders.InstanceFlowHeadersBuilder instanceFlowHeadersBuilder = InstanceFlowHeaders.builder();

        try {
            Long sourceApplicationId = ClientAuthorizationUtil.getSourceApplicationId(authentication);

            instanceFlowHeadersBuilder.correlationId(UUID.randomUUID());
            instanceFlowHeadersBuilder.sourceApplicationId(sourceApplicationId);

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

                    Integration integration = integrationRequestProducerService
                            .get(sourceApplicationIdAndSourceApplicationIntegrationId)
                            .orElseThrow(() -> new NoIntegrationException(sourceApplicationIdAndSourceApplicationIntegrationId));

                    instanceFlowHeadersBuilder.integrationId(integration.getId());

                    if (integration.getState() == Integration.State.DEACTIVATED) {
                        throw new IntegrationDeactivatedException(integration);
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

            return instanceMapper.map(sourceApplicationId, incomingInstance)
                    .doOnNext(instance -> receivedInstanceEventProducerService.publish(
                            instanceFlowHeadersBuilder.build(),
                            instance
                    )).thenReturn(ResponseEntity.accepted().build());

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

    }

}
