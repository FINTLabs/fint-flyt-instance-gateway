package no.fintlabs.gateway.instance.kafka;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.flyt.kafka.event.InstanceFlowEventProducer;
import no.fintlabs.flyt.kafka.event.InstanceFlowEventProducerFactory;
import no.fintlabs.flyt.kafka.event.InstanceFlowEventProducerRecord;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.gateway.instance.model.instance.InstanceObject;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReceivedInstanceEventProducerService {

    private final InstanceFlowEventProducer<InstanceObject> instanceProducer;
    private final EventTopicNameParameters formDefinitionEventTopicNameParameters;

    public ReceivedInstanceEventProducerService(
            InstanceFlowEventProducerFactory instanceFlowEventProducerFactory
    ) {
        this.instanceProducer = instanceFlowEventProducerFactory.createProducer(InstanceObject.class);
        this.formDefinitionEventTopicNameParameters = EventTopicNameParameters.builder()
                .eventName("instance-received")
                .build();
    }

    public void publish(
            InstanceFlowHeaders instanceFlowHeaders,
            InstanceObject instance
    ) {
        instanceProducer.send(
                InstanceFlowEventProducerRecord.<InstanceObject>builder()
                        .topicNameParameters(formDefinitionEventTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .value(instance)
                        .build()
        );
    }

}
