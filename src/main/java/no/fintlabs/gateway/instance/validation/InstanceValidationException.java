package no.fintlabs.gateway.instance.validation;

import lombok.Getter;

import java.util.List;

@Getter
public class InstanceValidationException extends RuntimeException {

    private final List<InstanceValidationService.Error> validationErrors;

    public InstanceValidationException(List<InstanceValidationService.Error> validationErrors) {
        super("Instance validation error(s): " + validationErrors);
        this.validationErrors = validationErrors;
    }
}
