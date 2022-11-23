package no.fintlabs.validation;

import lombok.Getter;

import java.util.List;

public class InstanceValidationException extends RuntimeException {

    @Getter
    private final List<InstanceValidationService.Error> validationErrors;

    public InstanceValidationException(List<InstanceValidationService.Error> validationErrors) {
        super("Instance validation error(s): " + validationErrors);
        this.validationErrors = validationErrors;
    }
}
