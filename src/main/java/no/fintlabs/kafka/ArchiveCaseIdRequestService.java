package no.fintlabs.kafka;

import no.fintlabs.kafka.common.topic.TopicCleanupPolicyParameters;
import no.fintlabs.kafka.requestreply.RequestProducer;
import no.fintlabs.kafka.requestreply.RequestProducerConfiguration;
import no.fintlabs.kafka.requestreply.RequestProducerFactory;
import no.fintlabs.kafka.requestreply.RequestProducerRecord;
import no.fintlabs.kafka.requestreply.topic.ReplyTopicNameParameters;
import no.fintlabs.kafka.requestreply.topic.ReplyTopicService;
import no.fintlabs.kafka.requestreply.topic.RequestTopicNameParameters;
import no.fintlabs.model.ArchiveCaseIdRequestParams;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class ArchiveCaseIdRequestService {

    private final RequestProducer<ArchiveCaseIdRequestParams, String> caseIdRequestProducer;
    private final RequestTopicNameParameters requestTopicNameParameters;

    public ArchiveCaseIdRequestService(
            @Value("${fint.kafka.application-id}") String applicationId,
            ReplyTopicService replyTopicService,
            RequestProducerFactory requestProducerFactory
    ) {
        requestTopicNameParameters = RequestTopicNameParameters
                .builder()
                .resource("archive.instance.id")
                .parameterName("source-application-instance-id")
                .build();

        ReplyTopicNameParameters replyTopicNameParameters = ReplyTopicNameParameters
                .builder()
                .applicationId(applicationId)
                .resource("archive.instance.id")
                .build();

        replyTopicService.ensureTopic(replyTopicNameParameters, 0, TopicCleanupPolicyParameters.builder().build());

        caseIdRequestProducer = requestProducerFactory.createProducer(
                replyTopicNameParameters,
                ArchiveCaseIdRequestParams.class,
                String.class,
                RequestProducerConfiguration
                        .builder()
                        .defaultReplyTimeout(Duration.ofSeconds(10))
                        .build()
        );
    }

    public Optional<String> getArchiveCaseId(Long sourceApplicationId, String sourceApplicationInstanceId) {
        return caseIdRequestProducer.requestAndReceive(
                RequestProducerRecord
                        .<ArchiveCaseIdRequestParams>builder()
                        .topicNameParameters(requestTopicNameParameters)
                        .value(ArchiveCaseIdRequestParams
                                .builder()
                                .sourceApplicationId(sourceApplicationId)
                                .sourceApplicationInstanceId(sourceApplicationInstanceId)
                                .build()
                        )
                        .build()
        ).map(ConsumerRecord::value);
    }
}
