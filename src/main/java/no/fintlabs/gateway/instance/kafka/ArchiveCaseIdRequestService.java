package no.fintlabs.gateway.instance.kafka;

import no.fintlabs.gateway.instance.model.ArchiveCaseIdRequestParams;
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
public class ArchiveCaseIdRequestService {

    private final RequestTemplate<ArchiveCaseIdRequestParams, String> requestTemplate;
    private final RequestTopicNameParameters requestTopicNameParameters;

    public ArchiveCaseIdRequestService(
            @Value("${fint.kafka.application-id}") String applicationId,
            ReplyTopicService replyTopicService,
            RequestTemplateFactory requestTemplateFactory
    ) {
        String resourceName = "archive-instance-id";
        requestTopicNameParameters = RequestTopicNameParameters
                .builder()
                .resourceName(resourceName)
                .parameterName("source-application-instance-id")
                .build();

        ReplyTopicNameParameters replyTopicNameParameters = ReplyTopicNameParameters
                .builder()
                .applicationId(applicationId)
                .resourceName(resourceName)
                .build();

        replyTopicService.createOrModifyTopic(
                replyTopicNameParameters,
                ReplyTopicConfiguration.builder()
                        .retentionTime(Duration.ofHours(1))
                        .build()
        );

        requestTemplate = requestTemplateFactory.createTemplate(
                replyTopicNameParameters,
                ArchiveCaseIdRequestParams.class,
                String.class,
                Duration.ofSeconds(10),
                ListenerConfiguration
                        .stepBuilder()
                        .groupIdApplicationDefault()
                        .maxPollRecordsKafkaDefault()
                        .maxPollIntervalKafkaDefault()
                        .continueFromPreviousOffsetOnAssignment()
                        .build()
        );
    }

    public Optional<String> getArchiveCaseId(Long sourceApplicationId, String sourceApplicationInstanceId) {
        return Optional.ofNullable(
                requestTemplate.requestAndReceive(
                                RequestProducerRecord.<ArchiveCaseIdRequestParams>builder()
                                        .topicNameParameters(requestTopicNameParameters)
                                        .key(sourceApplicationInstanceId)
                                        .value(ArchiveCaseIdRequestParams.builder()
                                                .sourceApplicationId(sourceApplicationId)
                                                .sourceApplicationInstanceId(sourceApplicationInstanceId)
                                                .build()
                                        )
                                        .build()
                        )
                        .value());
    }
}
