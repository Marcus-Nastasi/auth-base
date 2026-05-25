package com.auth.core.validators;

import com.auth.core.annotations.ValidEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class ValidEmailValidator implements ConstraintValidator<ValidEmail, String> {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private String message;

    private String fieldName;

    @Override
    public void initialize(final ValidEmail constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);

        this.message = constraintAnnotation.message();
        this.fieldName = constraintAnnotation.fieldName();
    }

    @Override
    public boolean isValid(final String s, final ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();

        if (s == null || s.isBlank()) return true;

        if (s.length() > 77 || !EMAIL_PATTERN.matcher(s).matches()) {
            writeContext(context, message, fieldName);
            return false;
        }

        return true;
    }

    private void writeContext(final ConstraintValidatorContext context,
                              final String message,
                              final String fieldName) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(fieldName)
                .addConstraintViolation();
    }
}
