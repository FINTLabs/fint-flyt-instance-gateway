package no.novari.flyt.instance.gateway.exception;

import lombok.Getter;
import no.novari.flyt.instance.gateway.model.SourceApplicationIdAndSourceApplicationIntegrationId;

@Getter
public class NoIntegrationException extends RuntimeException {

    private final SourceApplicationIdAndSourceApplicationIntegrationId sourceApplicationIdAndSourceApplicationIntegrationId;

    public NoIntegrationException(SourceApplicationIdAndSourceApplicationIntegrationId sourceApplicationIdAndSourceApplicationIntegrationId) {
        super("Could not find integration for " + sourceApplicationIdAndSourceApplicationIntegrationId.getSourceApplicationId());
        this.sourceApplicationIdAndSourceApplicationIntegrationId = sourceApplicationIdAndSourceApplicationIntegrationId;
    }
}
