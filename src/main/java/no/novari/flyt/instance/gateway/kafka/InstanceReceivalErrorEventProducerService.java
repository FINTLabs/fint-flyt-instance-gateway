package no.novari.flyt.instance.gateway.kafka;

import lombok.extern.slf4j.Slf4j;
import no.novari.flyt.instance.gateway.ErrorCode;
import no.novari.flyt.instance.gateway.config.properties.InstanceProcessingEventsConfigurationProperties;
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
import no.novari.flyt.kafka.model.Error;
import no.novari.flyt.kafka.model.ErrorCollection;
import no.novari.kafka.topic.ErrorEventTopicService;
import no.novari.kafka.topic.configuration.EventTopicConfiguration;
import no.novari.kafka.topic.name.ErrorEventTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class InstanceReceivalErrorEventProducerService {

    private final InstanceFlowTemplate<ErrorCollection> instanceFlowTemplate;
    private final InstanceValidationErrorMappingService instanceValidationErrorMappingService;
    private final ErrorEventTopicNameParameters errorEventTopicNameParameters;

    public InstanceReceivalErrorEventProducerService(
            InstanceFlowTemplateFactory instanceFlowTemplateFactory,
            InstanceValidationErrorMappingService instanceValidationErrorMappingService,
            ErrorEventTopicService errorEventTopicService,
            InstanceProcessingEventsConfigurationProperties instanceProcessingEventsConfigurationProperties
    ) {
        this.instanceFlowTemplate = instanceFlowTemplateFactory.createTemplate(ErrorCollection.class);
        this.instanceValidationErrorMappingService = instanceValidationErrorMappingService;

        this.errorEventTopicNameParameters = ErrorEventTopicNameParameters
                .builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build()
                )
                .errorEventName("instance-receival-error")
                .build();

        // Must match setup in `fint-flyt-web-instance-gateway`
        errorEventTopicService.createOrModifyTopic(errorEventTopicNameParameters, EventTopicConfiguration
                .stepBuilder()
                .partitions(instanceProcessingEventsConfigurationProperties.getPartitions())
                .retentionTime(instanceProcessingEventsConfigurationProperties.getRetentionTime())
                .cleanupFrequency(instanceProcessingEventsConfigurationProperties.getCleanupFrequency())
                .build()
        );
    }

    public void publishInstanceValidationErrorEvent(InstanceFlowHeaders instanceFlowHeaders, InstanceValidationException e) {
        instanceFlowTemplate.send(
                InstanceFlowProducerRecord
                        .<ErrorCollection>builder()
                        .topicNameParameters(errorEventTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .value(instanceValidationErrorMappingService.map(e))
                        .build()
        );
    }

    public void publishInstanceRejectedErrorEvent(InstanceFlowHeaders instanceFlowHeaders, AbstractInstanceRejectedException e) {
        instanceFlowTemplate.send(
                InstanceFlowProducerRecord
                        .<ErrorCollection>builder()
                        .topicNameParameters(errorEventTopicNameParameters)
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
                        .topicNameParameters(errorEventTopicNameParameters)
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
                        .topicNameParameters(errorEventTopicNameParameters)
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
                        .topicNameParameters(errorEventTopicNameParameters)
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
                        .topicNameParameters(errorEventTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .value(new ErrorCollection(Error
                                .builder()
                                .errorCode(ErrorCode.GENERAL_SYSTEM_ERROR.getCode())
                                .build()))
                        .build()
        );
    }

}
