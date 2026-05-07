package com.auth.core.validators;

import com.auth.core.annotations.ValidCpf;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class ValidCpfValidator implements ConstraintValidator<ValidCpf, String> {

    private static final Pattern DIGITS = Pattern.compile("\\d+");

    private String message;

    private String fieldName;

    @Override
    public void initialize(final ValidCpf constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);

        this.message = constraintAnnotation.message();
        this.fieldName = constraintAnnotation.fieldName();
    }


    @Override
    public boolean isValid(final String s, final ConstraintValidatorContext context) {
        if (s == null || s.length() != 11) {
            writeContext(context, message, fieldName);
            return false;
        }

        if (!isNumeric(s)) {
            writeContext(context, message, fieldName);
            return false;
        }

        return isValidCpf(s);
    }

    private boolean isNumeric(final String s) {
        return s != null && DIGITS.matcher(s).matches();
    }

    /**
     * Validates a CPF number using the modulus 11 algorithm.
     *
     * <p>The CPF must be composed of 11 digits. This method first checks whether the input
     * is a sequence of identical digits (which is invalid), then calculates the two
     * verifying digits using the standard modulus 11 algorithm. If both calculated digits
     * match the last two digits of the input, the CPF is considered valid.</p>
     *
     * @param cpf the CPF number as a numeric string (only digits)
     * @return true if the CPF is valid, false otherwise
     */
    private static boolean isValidCpf(final String cpf) {
        // reject equal digit sequence
        if (cpf.chars().distinct().count() == 1) return false;

        final int[] digits = cpf.chars().map(c -> c - '0').toArray();

        // 1º verifying digit
        int sum = 0;

        for (int i = 0; i < 9; i++) {
            sum += digits[i] * (10 - i);
        }

        int remainder = sum % 11;
        final int check1 = (remainder < 2) ? 0 : 11 - remainder;
        if (digits[9] != check1) return false;

        // 2º verifying digit
        sum = 0;

        for (int i = 0; i < 10; i++) {
            sum += digits[i] * (11 - i);
        }

        remainder = sum % 11;
        final int check2 = (remainder < 2) ? 0 : 11 - remainder;
        return digits[10] == check2;
    }

    private void writeContext(final ConstraintValidatorContext context,
                              final String message,
                              final String fieldName) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(fieldName)
                .addConstraintViolation();
    }
}
