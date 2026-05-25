package com.auth.core.exceptions;

import com.auth.core.shared.Errors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

final class DomainExceptionTests {

   @Test
   void shouldTestAllTypesOfConstructor() {
      assertDoesNotThrow(() -> new DomainException("just message"));
      assertDoesNotThrow(() -> new DomainException("just message", new RuntimeException("throwable")));
      assertDoesNotThrow(() -> new DomainException("just message", new RuntimeException("throwable")));
      assertDoesNotThrow(() -> new DomainException(new RuntimeException("just throwable")));
      assertDoesNotThrow(() -> new DomainException("message", new RuntimeException("throwable"), true, true));
      assertDoesNotThrow(() -> new DomainException(Errors.COULD_NOT_UPDATE_USER));
      assertDoesNotThrow(() -> new DomainException(Errors.COULD_NOT_UPDATE_USER, new RuntimeException("throwable")));
      assertDoesNotThrow(() -> new DomainException(Errors.COULD_NOT_UPDATE_USER, new RuntimeException("throwable"), true, false));
   }
}
