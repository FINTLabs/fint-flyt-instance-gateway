package no.fintlabs.kafka;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.flyt.kafka.event.InstanceFlowEventProducer;
import no.fintlabs.flyt.kafka.event.InstanceFlowEventProducerFactory;
import no.fintlabs.flyt.kafka.event.InstanceFlowEventProducerRecord;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;
import no.fintlabs.kafka.event.topic.EventTopicService;
import no.fintlabs.model.instance.Instance;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReceivedInstanceEventProducerService {

    private final InstanceFlowEventProducer<Instance> instanceProducer;
    private final EventTopicNameParameters formDefinitionEventTopicNameParameters;

    public ReceivedInstanceEventProducerService(
            InstanceFlowEventProducerFactory instanceFlowEventProducerFactory,
            EventTopicService eventTopicService
    ) {
        this.instanceProducer = instanceFlowEventProducerFactory.createProducer(Instance.class);
        this.formDefinitionEventTopicNameParameters = EventTopicNameParameters.builder()
                .eventName("instance-received")
                .build();
        eventTopicService.ensureTopic(formDefinitionEventTopicNameParameters, 15778463000L);
    }

    public void publish(
            InstanceFlowHeaders instanceFlowHeaders,
            Instance instance
    ) {
        instanceProducer.send(
                InstanceFlowEventProducerRecord.<Instance>builder()
                        .topicNameParameters(formDefinitionEventTopicNameParameters)
                        .instanceFlowHeaders(instanceFlowHeaders)
                        .value(instance)
                        .build()
        );
    }

}
