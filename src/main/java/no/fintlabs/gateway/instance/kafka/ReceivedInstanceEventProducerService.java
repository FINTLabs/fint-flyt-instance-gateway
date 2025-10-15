package no.fintlabs.gateway.instance.kafka;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.flyt.kafka.instanceflow.headers.InstanceFlowHeaders;
import no.fintlabs.flyt.kafka.instanceflow.producing.InstanceFlowProducerRecord;
import no.fintlabs.flyt.kafka.instanceflow.producing.InstanceFlowTemplate;
import no.fintlabs.flyt.kafka.instanceflow.producing.InstanceFlowTemplateFactory;
import no.fintlabs.gateway.instance.model.instance.InstanceObject;
import no.fintlabs.kafka.topic.name.EventTopicNameParameters;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReceivedInstanceEventProducerService {

    private final InstanceFlowTemplate<InstanceObject> instanceFlowTemplate;
    private final EventTopicNameParameters formDefinitionEventTopicNameParameters;

    public ReceivedInstanceEventProducerService(
            InstanceFlowTemplateFactory instanceFlowTemplateFactory
    ) {
        this.instanceFlowTemplate = instanceFlowTemplateFactory.createTemplate(InstanceObject.class);

        this.formDefinitionEventTopicNameParameters = EventTopicNameParameters.builder()
                .eventName("instance-received")
                .build();
    }

    public void publish(
            InstanceFlowHeaders instanceFlowHeaders,
            InstanceObject instance
    ) {
        instanceFlowTemplate.send(
                InstanceFlowProducerRecord.<InstanceObject>builder()
                        .topicNameParameters(formDefinitionEventTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .value(instance)
                        .build()
        );
    }

}
