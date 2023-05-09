package no.fintlabs.gateway.instance.model;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Getter
@EqualsAndHashCode
@Jacksonized
@Builder

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
