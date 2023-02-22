package no.fintlabs.gateway.instance.validation.constraints;

import org.springframework.util.Base64Utils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class Base64Validator implements ConstraintValidator<ValidBase64, String> {

    @Override
    public void initialize(ValidBase64 constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty() || value.isBlank()) {
            return false;
        }

        try {
            Base64Utils.decodeFromString(value);
        } catch (IllegalArgumentException e) {
            return false;
        }

        int numPaddingChars = countPaddingChars(value);
        return numPaddingChars >= 1 && numPaddingChars <= 2;
    }

    private int countPaddingChars(String value) {
        int numPaddingChars = 0;
        int index = value.length() - 1;
        while (index >= 0 && value.charAt(index) == '=') {
            numPaddingChars++;
            index--;
        }
        return numPaddingChars;
    }

}
