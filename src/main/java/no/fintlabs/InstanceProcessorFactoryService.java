package no.fintlabs;

import no.fintlabs.kafka.InstanceReceivalErrorEventProducerService;
import no.fintlabs.kafka.IntegrationRequestProducerService;
import no.fintlabs.kafka.ReceivedInstanceEventProducerService;
import no.fintlabs.validation.InstanceValidationService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Function;

@Service
public class InstanceProcessorFactoryService {

    private final IntegrationRequestProducerService integrationRequestProducerService;
    private final InstanceValidationService instanceValidationService;
    private final ReceivedInstanceEventProducerService receivedInstanceEventProducerService;
    private final InstanceReceivalErrorEventProducerService instanceReceivalErrorEventProducerService;

    public InstanceProcessorFactoryService(
            IntegrationRequestProducerService integrationRequestProducerService,
            InstanceValidationService instanceValidationService,
            ReceivedInstanceEventProducerService receivedInstanceEventProducerService,
            InstanceReceivalErrorEventProducerService instanceReceivalErrorEventProducerService
    ) {
        this.integrationRequestProducerService = integrationRequestProducerService;
        this.instanceValidationService = instanceValidationService;
        this.receivedInstanceEventProducerService = receivedInstanceEventProducerService;
        this.instanceReceivalErrorEventProducerService = instanceReceivalErrorEventProducerService;
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
                sourceApplicationIntegrationIdFunction,
                sourceApplicationInstanceIdFunction,
                instanceMapper
        );
    }
}
