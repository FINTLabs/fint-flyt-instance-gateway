package no.fintlabs.gateway.instance;

import no.fintlabs.gateway.instance.model.instance.InstanceObject;
import reactor.core.publisher.Mono;

public interface InstanceMapper<T> {
    Mono<InstanceObject> map(Long sourceApplicationId, T incomingInstance);
}
