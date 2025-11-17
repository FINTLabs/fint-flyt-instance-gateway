package no.novari.flyt.instance.gateway.kafka;

import lombok.extern.slf4j.Slf4j;
import no.novari.flyt.instance.gateway.ErrorCode;
import no.novari.flyt.instance.gateway.exception.AbstractInstanceRejectedException;
import no.novari.flyt.instance.gateway.exception.FileUploadException;
import no.novari.flyt.instance.gateway.exception.IntegrationDeactivatedException;
import no.novari.flyt.instance.gateway.exception.NoIntegrationException;
import no.novari.flyt.instance.gateway.validation.InstanceValidationErrorMappingService;
import no.novari.flyt.instance.gateway.validation.InstanceValidationException;
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders;
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowProducerRecord;
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowTemplate;
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowTemplateFactory;
import no.novari.kafka.model.Error;
import no.novari.kafka.model.ErrorCollection;
import no.novari.kafka.topic.name.ErrorEventTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class InstanceReceivalErrorEventProducerService {

    private final InstanceFlowTemplate<ErrorCollection> instanceFlowTemplate;
    private final InstanceValidationErrorMappingService instanceValidationErrorMappingService;
    private final ErrorEventTopicNameParameters instanceProcessingErrorTopicNameParameters;

    public InstanceReceivalErrorEventProducerService(
            InstanceFlowTemplateFactory instanceFlowTemplateFactory,
            InstanceValidationErrorMappingService instanceValidationErrorMappingService
    ) {
        this.instanceFlowTemplate = instanceFlowTemplateFactory.createTemplate(ErrorCollection.class);
        this.instanceValidationErrorMappingService = instanceValidationErrorMappingService;

        this.instanceProcessingErrorTopicNameParameters = ErrorEventTopicNameParameters
                .builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build()
                )
                .errorEventName("instance-receival-error")
                .build();
    }

    public void publishInstanceValidationErrorEvent(InstanceFlowHeaders instanceFlowHeaders, InstanceValidationException e) {
        instanceFlowTemplate.send(
                InstanceFlowProducerRecord
                        .<ErrorCollection>builder()
                        .topicNameParameters(instanceProcessingErrorTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .value(instanceValidationErrorMappingService.map(e))
                        .build()
        );
    }

    public void publishInstanceRejectedErrorEvent(InstanceFlowHeaders instanceFlowHeaders, AbstractInstanceRejectedException e) {
        instanceFlowTemplate.send(
                InstanceFlowProducerRecord
                        .<ErrorCollection>builder()
                        .topicNameParameters(instanceProcessingErrorTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .value(new ErrorCollection(Error
                                .builder()
                                .errorCode(ErrorCode.INSTANCE_REJECTED_ERROR.getCode())
                                .args(Map.of("message", e.getMessage()))
                                .build()
                        ))
                        .build()
        );
    }

    public void publishInstanceFileUploadErrorEvent(InstanceFlowHeaders instanceFlowHeaders, FileUploadException e) {
        instanceFlowTemplate.send(
                InstanceFlowProducerRecord
                        .<ErrorCollection>builder()
                        .topicNameParameters(instanceProcessingErrorTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .value(new ErrorCollection(Error
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
        instanceFlowTemplate.send(
                InstanceFlowProducerRecord
                        .<ErrorCollection>builder()
                        .topicNameParameters(instanceProcessingErrorTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .value(new ErrorCollection(Error
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
        instanceFlowTemplate.send(
                InstanceFlowProducerRecord
                        .<ErrorCollection>builder()
                        .topicNameParameters(instanceProcessingErrorTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .value(new ErrorCollection(Error
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
        instanceFlowTemplate.send(
                InstanceFlowProducerRecord
                        .<ErrorCollection>builder()
                        .topicNameParameters(instanceProcessingErrorTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .value(new ErrorCollection(Error
                                .builder()
                                .errorCode(ErrorCode.GENERAL_SYSTEM_ERROR.getCode())
                                .build()))
                        .build()
        );
    }

}
