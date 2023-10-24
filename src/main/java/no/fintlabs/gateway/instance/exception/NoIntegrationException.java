package no.fintlabs.gateway.instance.exception;

import lombok.Getter;
import no.fintlabs.gateway.instance.model.SourceApplicationIdAndSourceApplicationIntegrationId;

@Getter
public class NoIntegrationException extends RuntimeException {

    private final SourceApplicationIdAndSourceApplicationIntegrationId sourceApplicationIdAndSourceApplicationIntegrationId;

    public NoIntegrationException(SourceApplicationIdAndSourceApplicationIntegrationId sourceApplicationIdAndSourceApplicationIntegrationId) {
        super("Could not find integration for " + sourceApplicationIdAndSourceApplicationIntegrationId.getSourceApplicationId());
        this.sourceApplicationIdAndSourceApplicationIntegrationId = sourceApplicationIdAndSourceApplicationIntegrationId;
    }
}
