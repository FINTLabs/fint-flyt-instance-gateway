package no.fintlabs.gateway.instance;

import no.fintlabs.gateway.instance.model.instance.InstanceElement;
import reactor.core.publisher.Mono;

public interface InstanceMapper<T> {
    Mono<InstanceElement> map(Long sourceApplicationId, T incomingInstance);
}
