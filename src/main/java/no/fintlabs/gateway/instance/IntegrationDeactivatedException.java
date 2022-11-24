package no.fintlabs.gateway.instance;

import lombok.Getter;
import no.fintlabs.gateway.instance.model.Integration;

public class IntegrationDeactivatedException extends RuntimeException {

    @Getter
    private final Integration integration;

    public IntegrationDeactivatedException(Integration integration) {
        super("Integration is disabled: " + integration);
        this.integration = integration;
    }

}
