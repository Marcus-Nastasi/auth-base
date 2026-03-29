package com.auth.core.annotations;

import com.auth.core.validators.ValidCpfValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Constraint(validatedBy = ValidCpfValidator.class)
public @interface ValidCpf {

    String message() default "Invalid cpf field";

    String fieldName() default "cpf";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
