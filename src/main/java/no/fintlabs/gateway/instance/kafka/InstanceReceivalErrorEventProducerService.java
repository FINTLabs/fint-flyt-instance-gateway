package no.fintlabs.gateway.instance.kafka;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.flyt.kafka.event.error.InstanceFlowErrorEventProducer;
import no.fintlabs.flyt.kafka.event.error.InstanceFlowErrorEventProducerRecord;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.gateway.instance.ErrorCode;
import no.fintlabs.gateway.instance.exception.AbstractInstanceRejectedException;
import no.fintlabs.gateway.instance.exception.FileUploadException;
import no.fintlabs.gateway.instance.exception.IntegrationDeactivatedException;
import no.fintlabs.gateway.instance.exception.NoIntegrationException;
import no.fintlabs.gateway.instance.validation.InstanceValidationErrorMappingService;
import no.fintlabs.gateway.instance.validation.InstanceValidationException;
import no.fintlabs.kafka.event.error.Error;
import no.fintlabs.kafka.event.error.ErrorCollection;
import no.fintlabs.kafka.event.error.topic.ErrorEventTopicNameParameters;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class InstanceReceivalErrorEventProducerService {

    private final InstanceFlowErrorEventProducer instanceFlowErrorEventProducer;
    private final InstanceValidationErrorMappingService instanceValidationErrorMappingService;
    private final ErrorEventTopicNameParameters instanceProcessingErrorTopicNameParameters;

    public InstanceReceivalErrorEventProducerService(
            InstanceFlowErrorEventProducer instanceFlowErrorEventProducer,
            InstanceValidationErrorMappingService instanceValidationErrorMappingService
    ) {
        this.instanceFlowErrorEventProducer = instanceFlowErrorEventProducer;
        this.instanceValidationErrorMappingService = instanceValidationErrorMappingService;

        this.instanceProcessingErrorTopicNameParameters = ErrorEventTopicNameParameters.builder()
                .errorEventName("instance-receival-error")
                .build();
    }

    public void publishInstanceValidationErrorEvent(InstanceFlowHeaders instanceFlowHeaders, InstanceValidationException e) {
        instanceFlowErrorEventProducer.send(
                InstanceFlowErrorEventProducerRecord
                        .builder()
                        .topicNameParameters(instanceProcessingErrorTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .errorCollection(instanceValidationErrorMappingService.map(e))
                        .build()
        );
    }

    public void publishInstanceRejectedErrorEvent(InstanceFlowHeaders instanceFlowHeaders, AbstractInstanceRejectedException e) {
        instanceFlowErrorEventProducer.send(
                InstanceFlowErrorEventProducerRecord
                        .builder()
                        .topicNameParameters(instanceProcessingErrorTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .errorCollection(new ErrorCollection(Error
                                .builder()
                                .errorCode(ErrorCode.INSTANCE_REJECTED_ERROR.getCode())
                                .args(Map.of("message", e.getMessage()))
                                .build()
                        ))
                        .build()
        );
    }

    public void publishInstanceFileUploadErrorEvent(InstanceFlowHeaders instanceFlowHeaders, FileUploadException e) {
        instanceFlowErrorEventProducer.send(
                InstanceFlowErrorEventProducerRecord
                        .builder()
                        .topicNameParameters(instanceProcessingErrorTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .errorCollection(new ErrorCollection(Error
                                .builder()
                                .errorCode(ErrorCode.FILE_UPLOAD_ERROR.getCode())
                                .args(Map.of(
                                                "name", e.getFile().getName(),
                                                "mediatype", e.getFile().getType().toString()
                                        )
                                )
                                .build()
                        ))
                        .build()
        );
    }

    public void publishNoIntegrationFoundErrorEvent(InstanceFlowHeaders instanceFlowHeaders, NoIntegrationException e) {
        instanceFlowErrorEventProducer.send(
                InstanceFlowErrorEventProducerRecord
                        .builder()
                        .topicNameParameters(instanceProcessingErrorTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .errorCollection(new ErrorCollection(Error
                                .builder()
                                .errorCode(ErrorCode.NO_INTEGRATION_FOUND_ERROR.getCode())
                                .args(Map.of(
                                        "sourceApplicationId",
                                        String.valueOf(e.getSourceApplicationIdAndSourceApplicationIntegrationId().getSourceApplicationId()),
                                        "sourceApplicationIntegrationId",
                                        e.getSourceApplicationIdAndSourceApplicationIntegrationId().getSourceApplicationIntegrationId()
                                ))
                                .build()
                        ))
                        .build()
        );
    }

    public void publishIntegrationDeactivatedErrorEvent(InstanceFlowHeaders instanceFlowHeaders, IntegrationDeactivatedException e) {
        instanceFlowErrorEventProducer.send(
                InstanceFlowErrorEventProducerRecord
                        .builder()
                        .topicNameParameters(instanceProcessingErrorTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .errorCollection(new ErrorCollection(Error
                                .builder()
                                .errorCode(ErrorCode.INTEGRATION_DEACTIVATED_ERROR.getCode())
                                .args(Map.of(
                                        "sourceApplicationId",
                                        String.valueOf(e.getIntegration().getSourceApplicationId()),
                                        "sourceApplicationIntegrationId",
                                        e.getIntegration().getSourceApplicationIntegrationId()
                                ))
                                .build()
                        ))
                        .build()
        );
    }

    public void publishGeneralSystemErrorEvent(InstanceFlowHeaders instanceFlowHeaders) {
        instanceFlowErrorEventProducer.send(
                InstanceFlowErrorEventProducerRecord
                        .builder()
                        .topicNameParameters(instanceProcessingErrorTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .errorCollection(new ErrorCollection(Error
                                .builder()
                                .errorCode(ErrorCode.GENERAL_SYSTEM_ERROR.getCode())
                                .build()))
                        .build()
        );
    }

}
