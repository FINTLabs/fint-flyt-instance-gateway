package no.fintlabs;

import lombok.Getter;
import no.fintlabs.model.Integration;

public class IntegrationDeactivatedException extends RuntimeException {

    @Getter
    private final Integration integration;

    public IntegrationDeactivatedException(Integration integration) {
        super("Integration is disabled: " + integration);
        this.integration = integration;
    }

}
