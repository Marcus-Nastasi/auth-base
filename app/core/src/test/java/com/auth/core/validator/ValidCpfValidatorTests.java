package com.auth.core.validator;

import com.auth.core.annotations.ValidCpf;
import com.auth.core.validators.ValidCpfValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidCpfValidatorTests {

    private ValidCpfValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

    @Mock
    private ValidCpf annotation;

    @BeforeEach
    void setUp() {
        validator = new ValidCpfValidator();

        // configura o mock da annotation
        when(annotation.message()).thenReturn("Invalid cpf field");
        when(annotation.fieldName()).thenReturn("cpf");

        validator.initialize(annotation);
    }

    // -------------------------------------------------------
    // Casos inválidos — disparam o writeContext
    // -------------------------------------------------------

    private void mockContextForInvalidCases() {
        when(context.buildConstraintViolationWithTemplate(anyString()))
                .thenReturn(violationBuilder);
        when(violationBuilder.addPropertyNode(anyString()))
                .thenReturn(nodeBuilder);
    }

    @Test
    void shouldReturnFalse_whenCpfIsNull() {
        mockContextForInvalidCases();

        assertFalse(validator.isValid(null, context));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",           // vazio
        "123",        // curto demais
        "1234567891011", // longo demais
        "1234567890", // 10 dígitos
        "123456789012" // 12 dígitos
    })
    void shouldReturnFalse_whenLengthIsNot11(String cpf) {
        mockContextForInvalidCases();

        assertFalse(validator.isValid(cpf, context));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "1234567891a",  // letra no final
        "abcdefghijk",  // só letras
        "123.456.789-09" // com máscara (tamanho != 11)
    })
    void shouldReturnFalse_whenCpfContainsNonDigits(String cpf) {
        mockContextForInvalidCases();

        assertFalse(validator.isValid(cpf, context));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "00000000000",
        "11111111111",
        "22222222222",
        "99999999999"
    })
    void shouldReturnFalse_whenAllDigitsAreEqual(String cpf) {
        // dígitos iguais passam pelo isNumeric mas falham no isValidCpf
        // writeContext NÃO é chamado nesse caminho, então sem mock de context
        assertFalse(validator.isValid(cpf, context));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "15076989011", // dígito verificador 1 errado: seria 15076989091
        "15076989093", // dígito verificador 2 errado
        "00000000190"  // matematicamente inválido
    })
    void shouldReturnFalse_whenVerifyingDigitsAreWrong(String cpf) {
        assertFalse(validator.isValid(cpf, context));
    }

    // -------------------------------------------------------
    // Casos válidos
    // -------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {
        "52998224725",
        "11144477735",
        "71428793860",
        "87748248800"
    })
    void shouldReturnTrue_whenCpfIsValid(String cpf) {
        assertTrue(validator.isValid(cpf, context));
    }

    // -------------------------------------------------------
    // Verifica que o writeContext escreve na violação correta
    // -------------------------------------------------------

    @Test
    void shouldWriteViolationWithCorrectMessageAndFieldName_whenCpfIsNull() {
        mockContextForInvalidCases();

        validator.isValid(null, context);

        verify(context).buildConstraintViolationWithTemplate("Invalid cpf field");
        verify(violationBuilder).addPropertyNode("cpf");
        verify(nodeBuilder).addConstraintViolation();
    }

    @Test
    void shouldUseCustomMessageAndFieldName_whenConfiguredViaAnnotation() {
        when(annotation.message()).thenReturn("CPF inválido");
        when(annotation.fieldName()).thenReturn("documento");
        validator.initialize(annotation);

        mockContextForInvalidCases();

        validator.isValid(null, context);

        verify(context).buildConstraintViolationWithTemplate("CPF inválido");
        verify(violationBuilder).addPropertyNode("documento");
    }
}
