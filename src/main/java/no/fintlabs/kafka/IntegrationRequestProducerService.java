package no.fintlabs.kafka;


import no.fintlabs.kafka.common.topic.TopicCleanupPolicyParameters;
import no.fintlabs.kafka.requestreply.RequestProducer;
import no.fintlabs.kafka.requestreply.RequestProducerConfiguration;
import no.fintlabs.kafka.requestreply.RequestProducerFactory;
import no.fintlabs.kafka.requestreply.RequestProducerRecord;
import no.fintlabs.kafka.requestreply.topic.ReplyTopicNameParameters;
import no.fintlabs.kafka.requestreply.topic.ReplyTopicService;
import no.fintlabs.kafka.requestreply.topic.RequestTopicNameParameters;
import no.fintlabs.model.Integration;
import no.fintlabs.model.SourceApplicationIdAndSourceApplicationIntegrationId;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class IntegrationRequestProducerService {

    private final RequestTopicNameParameters requestTopicNameParameters;

    private final RequestProducer<SourceApplicationIdAndSourceApplicationIntegrationId, Integration> requestProducer;

    public IntegrationRequestProducerService(
            @Value("${fint.application-id}") String applicationId,
            RequestProducerFactory requestProducerFactory,
            ReplyTopicService replyTopicService
    ) {
        ReplyTopicNameParameters replyTopicNameParameters = ReplyTopicNameParameters.builder()
                .applicationId(applicationId)
                .resource("integration")
                .build();

        replyTopicService.ensureTopic(replyTopicNameParameters, 0, TopicCleanupPolicyParameters.builder().build());

        this.requestTopicNameParameters = RequestTopicNameParameters.builder()
                .resource("integration")
                .parameterName("source-application-id-and-source-application-integration-id")
                .build();

        this.requestProducer = requestProducerFactory.createProducer(
                replyTopicNameParameters,
                SourceApplicationIdAndSourceApplicationIntegrationId.class,
                Integration.class,
                RequestProducerConfiguration
                        .builder()
                        .defaultReplyTimeout(Duration.ofSeconds(15))
                        .build()
        );
    }

    public Optional<Integration> get(SourceApplicationIdAndSourceApplicationIntegrationId sourceApplicationIdAndSourceApplicationIntegrationId) {
        return requestProducer.requestAndReceive(
                RequestProducerRecord.<SourceApplicationIdAndSourceApplicationIntegrationId>builder()
                        .topicNameParameters(requestTopicNameParameters)
                        .value(sourceApplicationIdAndSourceApplicationIntegrationId)
                        .build()
        ).map(ConsumerRecord::value);
    }
}
