package no.novari.flyt.instance.gateway.kafka;

import lombok.extern.slf4j.Slf4j;
import no.novari.flyt.instance.gateway.model.InstanceObject;
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders;
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowProducerRecord;
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowTemplate;
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowTemplateFactory;
import no.novari.kafka.topic.name.EventTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
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

        this.formDefinitionEventTopicNameParameters = EventTopicNameParameters
                .builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .builder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build()
                )
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
