package no.novari.flyt.instance.gateway;

import no.novari.flyt.instance.gateway.kafka.InstanceReceivalErrorEventProducerService;
import no.novari.flyt.instance.gateway.kafka.IntegrationRequestProducerService;
import no.novari.flyt.instance.gateway.kafka.ReceivedInstanceEventProducerService;
import no.novari.flyt.instance.gateway.validation.InstanceValidationService;
import no.novari.flyt.resourceserver.security.client.sourceapplication.SourceApplicationAuthorizationService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Function;

@Service
public class InstanceProcessorFactoryService {

    private final IntegrationRequestProducerService integrationRequestProducerService;
    private final InstanceValidationService instanceValidationService;
    private final ReceivedInstanceEventProducerService receivedInstanceEventProducerService;
    private final InstanceReceivalErrorEventProducerService instanceReceivalErrorEventProducerService;
    private final SourceApplicationAuthorizationService sourceApplicationAuthorizationService;
    private final FileClient fileClient;

    public InstanceProcessorFactoryService(
            IntegrationRequestProducerService integrationRequestProducerService,
            InstanceValidationService instanceValidationService,
            ReceivedInstanceEventProducerService receivedInstanceEventProducerService,
            InstanceReceivalErrorEventProducerService instanceReceivalErrorEventProducerService,
            SourceApplicationAuthorizationService sourceApplicationAuthorizationService,
            FileClient fileClient
    ) {
        this.integrationRequestProducerService = integrationRequestProducerService;
        this.instanceValidationService = instanceValidationService;
        this.receivedInstanceEventProducerService = receivedInstanceEventProducerService;
        this.instanceReceivalErrorEventProducerService = instanceReceivalErrorEventProducerService;
        this.sourceApplicationAuthorizationService = sourceApplicationAuthorizationService;
        this.fileClient = fileClient;
    }

    public <T> InstanceProcessor<T> createInstanceProcessor(
            String sourceApplicationIntegrationId,
            Function<T, Optional<String>> sourceApplicationInstanceIdFunction,
            InstanceMapper<T> instanceMapper
    ) {
        return createInstanceProcessor(
                incomingInstance -> Optional.ofNullable(sourceApplicationIntegrationId),
                sourceApplicationInstanceIdFunction,
                instanceMapper
        );
    }

    public <T> InstanceProcessor<T> createInstanceProcessor(
            Function<T, Optional<String>> sourceApplicationIntegrationIdFunction,
            Function<T, Optional<String>> sourceApplicationInstanceIdFunction,
            InstanceMapper<T> instanceMapper
    ) {
        return new InstanceProcessor<>(
                integrationRequestProducerService,
                instanceValidationService,
                receivedInstanceEventProducerService,
                instanceReceivalErrorEventProducerService,
                sourceApplicationAuthorizationService,
                fileClient,
                sourceApplicationIntegrationIdFunction,
                sourceApplicationInstanceIdFunction,
                instanceMapper
        );
    }
}
