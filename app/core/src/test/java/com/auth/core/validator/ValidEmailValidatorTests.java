package com.auth.core.validator;

import com.auth.core.annotations.ValidEmail;
import com.auth.core.validators.ValidEmailValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidEmailValidatorTests {

   private ValidEmailValidator validator;

   @Mock
   private ConstraintValidatorContext context;

   @Mock
   private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

   @Mock
   private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

   @Mock
   private ValidEmail annotation;

   @BeforeEach
   void setUp() {
      validator = new ValidEmailValidator();

      when(annotation.message()).thenReturn("Invalid email field");
      when(annotation.fieldName()).thenReturn("email");

      validator.initialize(annotation);
   }

   // -------------------------------------------------------
   // disableDefaultConstraintViolation — sempre chamado
   // -------------------------------------------------------

   @Test
   void shouldAlwaysDisableDefaultConstraintViolation() {
      validator.isValid(null, context);

      verify(context).disableDefaultConstraintViolation();
   }

   // -------------------------------------------------------
   // Casos que retornam true sem chamar writeContext
   // -------------------------------------------------------

   @Test
   void shouldReturnTrue_whenEmailIsNull() {
      assertTrue(validator.isValid(null, context));

      verify(context, never()).buildConstraintViolationWithTemplate(anyString());
   }

   @ParameterizedTest
   @ValueSource(strings = {
        "",       // vazio
        " ",      // só espaço
        "   ",    // vários espaços
        "\t",     // tab
        "\n"      // newline
   })
   void shouldReturnTrue_whenEmailIsBlank(String email) {
      assertTrue(validator.isValid(email, context));

      verify(context, never()).buildConstraintViolationWithTemplate(anyString());
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
   void shouldReturnFalse_whenEmailExceedsMaxLength() {
      // 78 caracteres: local(68) + "@" + domínio(5) + ".com" = 78
      final String longEmail = "a".repeat(78);
      mockContextForInvalidCases();

      assertFalse(validator.isValid(longEmail, context));
   }

   @Test
   void shouldReturnTrue_whenEmailIsExactlyMaxLength() {
      final String exactMax = "a".repeat(66).concat("@gmail.com");
      assertEquals(76, exactMax.length());

      assertTrue(validator.isValid(exactMax, context));
   }

   @ParameterizedTest
   @ValueSource(strings = {
        "plainaddress",           // sem @
        "@missinglocal.com",      // sem parte local
        "missing@",               // sem domínio
        "missing.domain@",        // sem domínio após @
        "user@.com",              // domínio começa com ponto
        "user@domain.",           // TLD vazio
        "user@domain.c",          // TLD com menos de 2 letras
        "user @domain.com",       // espaço na parte local
        "user@dom ain.com",       // espaço no domínio
        "user@@domain.com",       // dois @
        "user@domain@domain.com"  // dois @
   })
   void shouldReturnFalse_whenEmailFormatIsInvalid(String email) {
      mockContextForInvalidCases();

      assertFalse(validator.isValid(email, context));
   }

   // -------------------------------------------------------
   // Emails válidos
   // -------------------------------------------------------

   @ParameterizedTest
   @ValueSource(strings = {
        "user@example.com",
        "user.name@example.com",
        "user+tag@example.com",
        "user%name@example.com",
        "user-name@example.co.uk",
        "123@example.com",
        "user@subdomain.example.com",
        "USER@EXAMPLE.COM",           // maiúsculas
        "user@example.travel"         // TLD longo
   })
   void shouldReturnTrue_whenEmailIsValid(String email) {
      assertTrue(validator.isValid(email, context));
   }

   // -------------------------------------------------------
   // Verifica que writeContext usa mensagem e campo corretos
   // -------------------------------------------------------

   @Test
   void shouldWriteViolationWithCorrectMessageAndFieldName_whenEmailIsInvalid() {
      mockContextForInvalidCases();

      validator.isValid("not-an-email", context);

      verify(context).buildConstraintViolationWithTemplate("Invalid email field");
      verify(violationBuilder).addPropertyNode("email");
      verify(nodeBuilder).addConstraintViolation();
   }

   @Test
   void shouldUseCustomMessageAndFieldName_whenConfiguredViaAnnotation() {
      when(annotation.message()).thenReturn("E-mail inválido");
      when(annotation.fieldName()).thenReturn("emailUsuario");
      validator.initialize(annotation);

      mockContextForInvalidCases();

      validator.isValid("not-an-email", context);

      verify(context).buildConstraintViolationWithTemplate("E-mail inválido");
      verify(violationBuilder).addPropertyNode("emailUsuario");
   }
}
