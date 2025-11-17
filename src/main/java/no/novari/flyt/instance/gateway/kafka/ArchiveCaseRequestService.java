package no.novari.flyt.instance.gateway.kafka;

import no.fint.model.resource.arkiv.noark.SakResource;
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
public class ArchiveCaseRequestService {

    private final RequestTemplate<String, SakResource> requestTemplate;
    private final RequestTopicNameParameters requestTopicNameParameters;

    public ArchiveCaseRequestService(
            @Value("${fint.kafka.application-id}") String applicationId,
            ReplyTopicService replyTopicService,
            RequestTemplateFactory requestTemplateFactory
    ) {
        String topicName = "arkiv-noark-sak-with-filtered-journalposts";
        requestTopicNameParameters = RequestTopicNameParameters
                .builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .builder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build()
                )
                .resourceName(topicName)
                .parameterName("archive-instance-id")
                .build();

        ReplyTopicNameParameters replyTopicNameParameters = ReplyTopicNameParameters
                .builder()
                .topicNamePrefixParameters(TopicNamePrefixParameters
                        .builder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build()
                )
                .applicationId(applicationId)
                .resourceName(topicName)
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
                String.class,
                SakResource.class,
                Duration.ofSeconds(60),
                ListenerConfiguration
                        .stepBuilder()
                        .groupIdApplicationDefault()
                        .maxPollRecordsKafkaDefault()
                        .maxPollIntervalKafkaDefault()
                        .continueFromPreviousOffsetOnAssignment()
                        .build()
        );
    }

    public Optional<SakResource> getByArchiveCaseId(String archiveCaseId) {
        return Optional.ofNullable(
                requestTemplate.requestAndReceive(
                                RequestProducerRecord.<String>builder()
                                        .topicNameParameters(requestTopicNameParameters)
                                        .key(null)
                                        .value(archiveCaseId)
                                        .build()
                        )
                        .value());
    }
}
