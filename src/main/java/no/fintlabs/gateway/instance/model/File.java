package no.fintlabs.gateway.instance.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;

@Data
@Builder
public class File {
    private String name;
    private Long sourceApplicationId;
    private String sourceApplicationInstanceId;
    @JsonSerialize(using = ToStringSerializer.class)
    private MediaType type;
    private String encoding;

    @JsonProperty(value = "contents")
    private String base64Contents;
}