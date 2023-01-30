package no.fintlabs.gateway.instance.model.instance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InstanceElement {
    private Map<String, String> valuePerKey = new HashMap<>();

    private Map<String, Collection<InstanceElement>> elementCollectionPerKey = new HashMap<>();
}
