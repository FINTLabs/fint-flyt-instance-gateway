package no.fintlabs.gateway.instance.kafka;

import no.fintlabs.gateway.instance.model.Integration;
import no.fintlabs.gateway.instance.model.SourceApplicationIdAndSourceApplicationIntegrationId;
import no.fintlabs.kafka.consuming.ListenerConfiguration;
import no.fintlabs.kafka.requestreply.RequestProducerRecord;
import no.fintlabs.kafka.requestreply.RequestTemplate;
import no.fintlabs.kafka.requestreply.RequestTemplateFactory;
import no.fintlabs.kafka.requestreply.topic.ReplyTopicService;
import no.fintlabs.kafka.requestreply.topic.configuration.ReplyTopicConfiguration;
import no.fintlabs.kafka.requestreply.topic.name.ReplyTopicNameParameters;
import no.fintlabs.kafka.requestreply.topic.name.RequestTopicNameParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class IntegrationRequestProducerService {

    private final RequestTopicNameParameters requestTopicNameParameters;

    private final RequestTemplate<SourceApplicationIdAndSourceApplicationIntegrationId, Integration> requestTemplate;

    public IntegrationRequestProducerService(
            @Value("${fint.application-id}") String applicationId,
            RequestTemplateFactory requestTemplateFactory,
            ReplyTopicService replyTopicService
    ) {
        ReplyTopicNameParameters replyTopicNameParameters = ReplyTopicNameParameters.builder()
                .applicationId(applicationId)
                .resourceName("integration")
                .build();

        replyTopicService.createOrModifyTopic(
                replyTopicNameParameters,
                ReplyTopicConfiguration.builder()
                        .retentionTime(Duration.ofHours(1))
                        .build()
        );

        this.requestTopicNameParameters = RequestTopicNameParameters.builder()
                .resourceName("integration")
                .parameterName("source-application-id-and-source-application-integration-id")
                .build();

        this.requestTemplate = requestTemplateFactory.createTemplate(
                replyTopicNameParameters,
                SourceApplicationIdAndSourceApplicationIntegrationId.class,
                Integration.class,
                Duration.ofSeconds(15),
                ListenerConfiguration.stepBuilder()
                        .groupIdApplicationDefault()
                        .maxPollRecordsKafkaDefault()
                        .maxPollIntervalKafkaDefault()
                        .continueFromPreviousOffsetOnAssignment()
                        .build()
        );
    }

    public Optional<Integration> get(SourceApplicationIdAndSourceApplicationIntegrationId params) {
        return Optional.ofNullable(
                requestTemplate.requestAndReceive(
                        RequestProducerRecord.<SourceApplicationIdAndSourceApplicationIntegrationId>builder()
                                .topicNameParameters(requestTopicNameParameters)
                                .key(null)
                                .value(params)
                                .build()
                ).value()
        );
    }
}
