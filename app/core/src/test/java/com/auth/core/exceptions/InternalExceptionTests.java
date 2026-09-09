package com.auth.core.exceptions;

import com.auth.core.shared.Errors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

final class InternalExceptionTests {

   @Test
   void shouldTestAllTypesOfConstructor() {
      assertDoesNotThrow(() -> new InternalException("just message"));
      assertDoesNotThrow(() -> new InternalException("just message", new RuntimeException("throwable")));
      assertDoesNotThrow(() -> new InternalException("just message", new RuntimeException("throwable")));
      assertDoesNotThrow(() -> new InternalException(new RuntimeException("just throwable")));
      assertDoesNotThrow(() -> new InternalException("message", new RuntimeException("throwable"), true, true));
      assertDoesNotThrow(() -> new InternalException(Errors.COULD_NOT_UPDATE_USER));
      assertDoesNotThrow(() -> new InternalException(Errors.COULD_NOT_UPDATE_USER, new RuntimeException("throwable")));
      assertDoesNotThrow(() -> new InternalException(Errors.COULD_NOT_UPDATE_USER, new RuntimeException("throwable"), true, false));
   }
}
