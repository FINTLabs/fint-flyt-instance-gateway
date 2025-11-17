package no.novari.flyt.instance.gateway.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter
@EqualsAndHashCode
@Jacksonized
@Builder

public class InstanceObject {

    @Builder.Default
    private Map<String, String> valuePerKey = new HashMap<>();

    @Builder.Default
    private Map<String, Collection<InstanceObject>> objectCollectionPerKey = new HashMap<>();
}
