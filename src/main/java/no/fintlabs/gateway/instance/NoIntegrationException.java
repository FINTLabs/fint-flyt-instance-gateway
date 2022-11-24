package no.fintlabs.gateway.instance;

import lombok.Getter;
import no.fintlabs.gateway.instance.model.SourceApplicationIdAndSourceApplicationIntegrationId;

public class NoIntegrationException extends RuntimeException {

    @Getter
    private final SourceApplicationIdAndSourceApplicationIntegrationId sourceApplicationIdAndSourceApplicationIntegrationId;

    public NoIntegrationException(SourceApplicationIdAndSourceApplicationIntegrationId sourceApplicationIdAndSourceApplicationIntegrationId) {
        super("Could not find integration for " + sourceApplicationIdAndSourceApplicationIntegrationId.toString());
        this.sourceApplicationIdAndSourceApplicationIntegrationId = sourceApplicationIdAndSourceApplicationIntegrationId;
    }
}
