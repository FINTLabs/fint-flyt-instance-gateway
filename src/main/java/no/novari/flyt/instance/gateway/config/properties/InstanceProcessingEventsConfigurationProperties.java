package no.novari.flyt.instance.gateway.config.properties;

import lombok.Getter;
import lombok.Setter;
import no.novari.kafka.topic.configuration.EventCleanupFrequency;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "novari.flyt.instance-gateway.kafka.topic.instance-receival-error")
public class InstanceProcessingEventsConfigurationProperties {
    private Duration retentionTime = Duration.ofDays(7);
    private EventCleanupFrequency cleanupFrequency = EventCleanupFrequency.NORMAL;
    private int partitions = 1;
}
