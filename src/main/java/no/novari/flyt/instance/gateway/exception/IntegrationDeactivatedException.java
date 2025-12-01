package no.novari.flyt.instance.gateway.exception;

import lombok.Getter;
import no.novari.flyt.instance.gateway.model.Integration;

@Getter
public class IntegrationDeactivatedException extends RuntimeException {

    private final Integration integration;

    public IntegrationDeactivatedException(Integration integration) {
        super("Integration is deactivated: " + integration);
        this.integration = integration;
    }

}
