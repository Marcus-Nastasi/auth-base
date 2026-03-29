package com.auth.core.annotations;

import com.auth.core.validators.ValidEmailValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Constraint(validatedBy = ValidEmailValidator.class)
public @interface ValidEmail {

    String message() default "Invalid e-mail field";

    String fieldName() default "email";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
