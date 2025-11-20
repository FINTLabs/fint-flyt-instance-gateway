package no.novari.flyt.instance.gateway.config;

import no.novari.flyt.instance.gateway.config.properties.InstanceProcessingEventsConfigurationProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AutoConfiguration
@EnableConfigurationProperties({
        InstanceProcessingEventsConfigurationProperties.class
})
public class KafkaConfiguration {
}
