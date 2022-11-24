package no.fintlabs.gateway.instance;

import no.fintlabs.gateway.instance.model.instance.Instance;
import reactor.core.publisher.Mono;

public interface InstanceMapper<T> {
    Mono<Instance> map(Long sourceApplicationId, T incomingInstance);
}
