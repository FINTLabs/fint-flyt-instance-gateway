package no.fintlabs.gateway.instance.kafka;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.noark.SakResource;
import no.fintlabs.kafka.common.topic.TopicCleanupPolicyParameters;
import no.fintlabs.kafka.requestreply.RequestProducer;
import no.fintlabs.kafka.requestreply.RequestProducerConfiguration;
import no.fintlabs.kafka.requestreply.RequestProducerFactory;
import no.fintlabs.kafka.requestreply.RequestProducerRecord;
import no.fintlabs.kafka.requestreply.topic.ReplyTopicNameParameters;
import no.fintlabs.kafka.requestreply.topic.ReplyTopicService;
import no.fintlabs.kafka.requestreply.topic.RequestTopicNameParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@Slf4j
public class ArchiveCaseRequestService {

    private final RequestProducer<String, SakResource> requestProducer;
    private final RequestTopicNameParameters requestTopicNameParameters;

    public ArchiveCaseRequestService(
            @Value("${fint.kafka.application-id}") String applicationId,
            ReplyTopicService replyTopicService,
            RequestProducerFactory requestProducerFactory
    ) {
        String topicName = "arkiv.noark.sak-with-filtered-journalposts";
        requestTopicNameParameters = RequestTopicNameParameters.builder()
                .resource(topicName)
                .parameterName("mappeid")
                .build();

        ReplyTopicNameParameters replyTopicNameParameters = ReplyTopicNameParameters.builder()
                .applicationId(applicationId)
                .resource(topicName)
                .build();

        replyTopicService.ensureTopic(replyTopicNameParameters, 0, TopicCleanupPolicyParameters.builder().build());

        this.requestProducer = requestProducerFactory.createProducer(
                replyTopicNameParameters,
                String.class,
                SakResource.class,
                RequestProducerConfiguration
                        .builder()
                        .defaultReplyTimeout(Duration.ofSeconds(60))
                        .build()
        );
    }

    public Optional<SakResource> getByArchiveCaseId(String archiveCaseId) {
        return requestProducer.requestAndReceive(
                RequestProducerRecord.<String>builder()
                        .topicNameParameters(requestTopicNameParameters)
                        .value(archiveCaseId)
                        .build()
        ).map(ConsumerRecord::value);
    }

}
