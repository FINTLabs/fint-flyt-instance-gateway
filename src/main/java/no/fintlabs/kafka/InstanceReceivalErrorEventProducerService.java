package no.fintlabs.kafka;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.ErrorCode;
import no.fintlabs.IntegrationDeactivatedException;
import no.fintlabs.NoIntegrationException;
import no.fintlabs.flyt.kafka.event.error.InstanceFlowErrorEventProducer;
import no.fintlabs.flyt.kafka.event.error.InstanceFlowErrorEventProducerRecord;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.event.error.Error;
import no.fintlabs.kafka.event.error.ErrorCollection;
import no.fintlabs.kafka.event.error.topic.ErrorEventTopicNameParameters;
import no.fintlabs.kafka.event.error.topic.ErrorEventTopicService;
import no.fintlabs.validation.InstanceValidationErrorMappingService;
import no.fintlabs.validation.InstanceValidationException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class InstanceReceivalErrorEventProducerService {

    private final InstanceFlowErrorEventProducer instanceFlowErrorEventProducer;
    private final InstanceValidationErrorMappingService instanceValidationErrorMappingService;
    private final ErrorEventTopicNameParameters instanceProcessingErrorTopicNameParameters;

    public InstanceReceivalErrorEventProducerService(
            ErrorEventTopicService errorEventTopicService,
            InstanceFlowErrorEventProducer instanceFlowErrorEventProducer,
            InstanceValidationErrorMappingService instanceValidationErrorMappingService
    ) {
        this.instanceFlowErrorEventProducer = instanceFlowErrorEventProducer;
        this.instanceValidationErrorMappingService = instanceValidationErrorMappingService;

        this.instanceProcessingErrorTopicNameParameters = ErrorEventTopicNameParameters.builder()
                .errorEventName("instance-receival-error")
                .build();

        errorEventTopicService.ensureTopic(instanceProcessingErrorTopicNameParameters, 0);
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
