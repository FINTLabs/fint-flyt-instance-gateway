package no.novari.flyt.instance.gateway.validation;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.springframework.stereotype.Service;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InstanceValidationService {

    @Getter
    @EqualsAndHashCode
    @Jacksonized
    @Builder
    public static class Error {
        private final String fieldPath;
        private final String errorMessage;
    }

    private final Validator fieldValidator;

    public InstanceValidationService(ValidatorFactory validatorFactory) {
        this.fieldValidator = validatorFactory.getValidator();
    }

    public Optional<List<Error>> validate(Object instance) {
        List<Error> errors = fieldValidator.validate(instance)
                .stream()
                .map(constraintViolation -> Error
                        .builder()
                        .fieldPath(constraintViolation.getPropertyPath().toString())
                        .errorMessage(constraintViolation.getMessage())
                        .build()
                )
                .sorted(Comparator.comparing(Error::getFieldPath))
                .collect(Collectors.toList());

        return errors.isEmpty()
                ? Optional.empty()
                : Optional.of(errors);
    }

}
