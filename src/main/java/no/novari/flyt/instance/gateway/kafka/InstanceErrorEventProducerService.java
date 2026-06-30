package no.novari.flyt.instance.gateway.kafka;

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
import no.novari.flyt.kafka.model.Error;
import no.novari.flyt.kafka.model.ErrorCollection;
import no.novari.flyt.kafka.model.InstanceErrorEvent;
import no.novari.flyt.kafka.model.InstanceErrorOrigin;
import no.novari.kafka.topic.name.ErrorEventTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class InstanceErrorEventProducerService {

    private final InstanceFlowTemplate<InstanceErrorEvent> instanceFlowTemplate;
    private final InstanceValidationErrorMappingService instanceValidationErrorMappingService;
    private final ErrorEventTopicNameParameters errorEventTopicNameParameters;

    public InstanceErrorEventProducerService(
            InstanceFlowTemplateFactory instanceFlowTemplateFactory,
            InstanceValidationErrorMappingService instanceValidationErrorMappingService
    ) {
        this.instanceFlowTemplate = instanceFlowTemplateFactory.createTemplate(InstanceErrorEvent.class);
        this.instanceValidationErrorMappingService = instanceValidationErrorMappingService;

        this.errorEventTopicNameParameters = ErrorEventTopicNameParameters
                .builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build()
                )
                .errorEventName("instance-error")
                .build();
    }

    private void send(InstanceFlowHeaders instanceFlowHeaders, ErrorCollection errors) {
        instanceFlowTemplate.send(
                InstanceFlowProducerRecord
                        .<InstanceErrorEvent>builder()
                        .topicNameParameters(errorEventTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .value(new InstanceErrorEvent(InstanceErrorOrigin.RECEIVAL, errors))
                        .build()
        );
    }

    public void publishInstanceValidationErrorEvent(InstanceFlowHeaders instanceFlowHeaders, InstanceValidationException e) {
        send(instanceFlowHeaders, instanceValidationErrorMappingService.map(e));
    }

    public void publishInstanceRejectedErrorEvent(InstanceFlowHeaders instanceFlowHeaders, AbstractInstanceRejectedException e) {
        send(instanceFlowHeaders, new ErrorCollection(Error
                .builder()
                .errorCode(ErrorCode.INSTANCE_REJECTED_ERROR.getCode())
                .args(Map.of("message", e.getMessage()))
                .build()
        ));
    }

    public void publishInstanceFileUploadErrorEvent(InstanceFlowHeaders instanceFlowHeaders, FileUploadException e) {
        send(instanceFlowHeaders, new ErrorCollection(Error
                .builder()
                .errorCode(ErrorCode.FILE_UPLOAD_ERROR.getCode())
                .args(Map.of(
                        "name", e.getFile().getName(),
                        "mediatype", e.getFile().getType().toString()
                ))
                .build()
        ));
    }

    public void publishNoIntegrationFoundErrorEvent(InstanceFlowHeaders instanceFlowHeaders, NoIntegrationException e) {
        send(instanceFlowHeaders, new ErrorCollection(Error
                .builder()
                .errorCode(ErrorCode.NO_INTEGRATION_FOUND_ERROR.getCode())
                .args(Map.of(
                        "sourceApplicationId",
                        String.valueOf(e.getSourceApplicationIdAndSourceApplicationIntegrationId().getSourceApplicationId()),
                        "sourceApplicationIntegrationId",
                        e.getSourceApplicationIdAndSourceApplicationIntegrationId().getSourceApplicationIntegrationId()
                ))
                .build()
        ));
    }

    public void publishIntegrationDeactivatedErrorEvent(InstanceFlowHeaders instanceFlowHeaders, IntegrationDeactivatedException e) {
        send(instanceFlowHeaders, new ErrorCollection(Error
                .builder()
                .errorCode(ErrorCode.INTEGRATION_DEACTIVATED_ERROR.getCode())
                .args(Map.of(
                        "sourceApplicationId",
                        String.valueOf(e.getIntegration().getSourceApplicationId()),
                        "sourceApplicationIntegrationId",
                        e.getIntegration().getSourceApplicationIntegrationId()
                ))
                .build()
        ));
    }

    public void publishGeneralSystemErrorEvent(InstanceFlowHeaders instanceFlowHeaders) {
        send(instanceFlowHeaders, new ErrorCollection(Error
                .builder()
                .errorCode(ErrorCode.GENERAL_SYSTEM_ERROR.getCode())
                .build()
        ));
    }

}
