package no.fintlabs.validation;

import no.fintlabs.ErrorCode;
import no.fintlabs.kafka.event.error.Error;
import no.fintlabs.kafka.event.error.ErrorCollection;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InstanceValidationErrorMappingService {

    public ErrorCollection map(InstanceValidationException instanceValidationException) {
        return new ErrorCollection(
                instanceValidationException.getValidationErrors()
                        .stream()
                        .map(validationError -> Error
                                .builder()
                                .errorCode(ErrorCode.INSTANCE_VALIDATION_ERROR.getCode())
                                .args(Map.of(
                                        "fieldPath", validationError.getFieldPath(),
                                        "errorMessage", validationError.getErrorMessage()
                                ))
                                .build()
                        )
                        .collect(Collectors.toList())
        );
    }

}
