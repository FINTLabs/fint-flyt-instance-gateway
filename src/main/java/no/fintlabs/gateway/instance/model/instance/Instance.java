package no.fintlabs.gateway.instance.model.instance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Instance {
    private String sourceApplicationInstanceUri;
    private Map<String, InstanceElement> fieldPerKey;
    private List<Document> documents;
}
