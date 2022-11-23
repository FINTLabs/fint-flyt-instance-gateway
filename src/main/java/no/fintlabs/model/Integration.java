package no.fintlabs.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Integration {

    public enum State {
        ACTIVE,
        DEACTIVATED
    }

    private long id;

    private Long sourceApplicationId;

    private String sourceApplicationIntegrationId;

    private String destination;

    private State state;

    private Long activeConfigurationId;

}
