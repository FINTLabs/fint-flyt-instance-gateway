package no.fintlabs.gateway.instance.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class File {
    private String name;
    private Long sourceApplicationId;
    private String sourceApplicationInstanceId;
    private String type;
    private String encoding;

    @JsonProperty(value = "contents")
    private String base64Contents;
}
