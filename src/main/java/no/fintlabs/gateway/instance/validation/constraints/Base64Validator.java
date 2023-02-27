package no.fintlabs.gateway.instance.validation.constraints;

import org.springframework.util.Base64Utils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class Base64Validator implements ConstraintValidator<ValidBase64, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || canBeDecoded(value);
    }

    private boolean canBeDecoded(String value) {
        try {
            Base64Utils.decodeFromString(value);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

}
