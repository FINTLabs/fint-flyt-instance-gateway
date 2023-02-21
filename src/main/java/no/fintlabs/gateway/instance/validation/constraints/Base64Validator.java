package no.fintlabs.gateway.instance.validation.constraints;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Base64;

public class Base64Validator implements ConstraintValidator<ValidBase64, String> {
    @Override
    public void initialize(ValidBase64 validBase64) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext cxt) {
        if (value == null) {
            return false;
        }

        try {
            Base64.getDecoder().decode(value);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

}