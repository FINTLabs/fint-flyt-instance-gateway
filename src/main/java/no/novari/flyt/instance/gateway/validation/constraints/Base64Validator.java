package no.novari.flyt.instance.gateway.validation.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Base64;

public class Base64Validator implements ConstraintValidator<ValidBase64, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || canBeDecoded(value);
    }

    private boolean canBeDecoded(String value) {
        try {
            Base64.getDecoder().decode(value);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

}
