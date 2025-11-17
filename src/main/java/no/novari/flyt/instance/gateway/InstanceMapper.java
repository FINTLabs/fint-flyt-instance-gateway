package no.novari.flyt.instance.gateway;

import no.novari.flyt.instance.gateway.model.File;
import no.novari.flyt.instance.gateway.model.InstanceObject;
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
