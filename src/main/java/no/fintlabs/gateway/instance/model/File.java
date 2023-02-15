package no.fintlabs.gateway.instance.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.MediaType;

@Data
@Builder
public class File {
    private String name;
    private Long sourceApplicationId;
    private String sourceApplicationInstanceId;
    private MediaType type;
    private String encoding;

    @JsonProperty(value = "contents")
    private String base64Contents;
}
