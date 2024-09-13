package no.fintlabs.gateway.instance;

import no.fintlabs.gateway.instance.model.File;
import no.fintlabs.gateway.instance.model.instance.InstanceObject;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.function.Function;

public interface InstanceMapper<T> {
    Mono<InstanceObject> map(
            Long sourceApplicationId,
            T incomingInstance,
            Function<File, Mono<UUID>> persistFile
    );
}
