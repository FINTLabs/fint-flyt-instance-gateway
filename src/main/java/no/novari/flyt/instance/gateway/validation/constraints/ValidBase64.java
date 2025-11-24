package no.novari.flyt.instance.gateway.validation.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Constraint(validatedBy = Base64Validator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBase64 {
    String message() default "Invalid base64-encoded string";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
