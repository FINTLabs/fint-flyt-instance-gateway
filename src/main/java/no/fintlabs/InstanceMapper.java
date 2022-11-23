package no.fintlabs;

import no.fintlabs.model.instance.Instance;
import reactor.core.publisher.Mono;

public interface InstanceMapper<T> {
    Mono<Instance> map(Long sourceApplicationId, T incomingInstance);
}
