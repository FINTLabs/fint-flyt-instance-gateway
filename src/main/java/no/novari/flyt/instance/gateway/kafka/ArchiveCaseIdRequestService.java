package no.novari.flyt.instance.gateway.kafka;

import no.novari.flyt.instance.gateway.model.ArchiveCaseIdRequestParams;
import no.novari.kafka.consuming.ListenerConfiguration;
import no.novari.kafka.requestreply.RequestProducerRecord;
import no.novari.kafka.requestreply.RequestTemplate;
import no.novari.kafka.requestreply.RequestTemplateFactory;
import no.novari.kafka.requestreply.topic.ReplyTopicService;
import no.novari.kafka.requestreply.topic.configuration.ReplyTopicConfiguration;
import no.novari.kafka.requestreply.topic.name.ReplyTopicNameParameters;
import no.novari.kafka.requestreply.topic.name.RequestTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class ArchiveCaseIdRequestService {

    private final RequestTemplate<ArchiveCaseIdRequestParams, String> requestTemplate;
    private final RequestTopicNameParameters requestTopicNameParameters;

    public ArchiveCaseIdRequestService(
            @Value("${novari.kafka.application-id}") String applicationId,
            ReplyTopicService replyTopicService,
            RequestTemplateFactory requestTemplateFactory
    ) {
        String resourceName = "archive-instance-id";
        requestTopicNameParameters = RequestTopicNameParameters
                .builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build()
                )
                .resourceName(resourceName)
                .parameterName("source-application-instance-id")
                .build();

        ReplyTopicNameParameters replyTopicNameParameters = ReplyTopicNameParameters
                .builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build()
                )
                .applicationId(applicationId)
                .resourceName(resourceName)
                .build();

        replyTopicService.createOrModifyTopic(
                replyTopicNameParameters,
                ReplyTopicConfiguration
                        .builder()
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
                                RequestProducerRecord
                                        .<ArchiveCaseIdRequestParams>builder()
                                        .topicNameParameters(requestTopicNameParameters)
                                        .key(sourceApplicationInstanceId)
                                        .value(ArchiveCaseIdRequestParams
                                                .builder()
                                                .sourceApplicationId(sourceApplicationId)
                                                .sourceApplicationInstanceId(sourceApplicationInstanceId)
                                                .build()
                                        )
                                        .build()
                        )
                        .value());
    }
}
