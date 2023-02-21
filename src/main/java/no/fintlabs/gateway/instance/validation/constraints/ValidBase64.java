package no.fintlabs.gateway.instance.validation.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Constraint(validatedBy = Base64Validator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBase64 {
    String message() default "Invalid base64 string";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
