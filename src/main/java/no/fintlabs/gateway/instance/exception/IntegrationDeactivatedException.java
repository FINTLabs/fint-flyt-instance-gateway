package no.fintlabs.gateway.instance.exception;

import lombok.Getter;
import no.fintlabs.gateway.instance.model.Integration;

@Getter
public class IntegrationDeactivatedException extends RuntimeException {

    private final Integration integration;

    public IntegrationDeactivatedException(Integration integration) {
        super("Integration is deactivated: " + integration);
        this.integration = integration;
    }

}
