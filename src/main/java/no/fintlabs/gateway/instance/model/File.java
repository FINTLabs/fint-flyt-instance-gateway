package no.fintlabs.gateway.instance.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.springframework.http.MediaType;

import java.util.StringJoiner;

@Getter
@EqualsAndHashCode
@Jacksonized
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

    @Override
    public String toString() {
        return new StringJoiner(", ", File.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("sourceApplicationId=" + sourceApplicationId)
                .add("sourceApplicationInstanceId='" + sourceApplicationInstanceId + "'")
                .add("type=" + type)
                .add("encoding='" + encoding + "'")
                .toString();
    }
}