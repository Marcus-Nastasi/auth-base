package com.auth.core.exceptions;

import com.auth.core.shared.Errors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

final class NotFoundExceptionTests {

   @Test
   void shouldTestAllTypesOfConstructor() {
      assertDoesNotThrow(() -> new NotFoundException());
      assertDoesNotThrow(() -> new NotFoundException("just message"));
      assertDoesNotThrow(() -> new NotFoundException("just message", new RuntimeException("throwable")));
      assertDoesNotThrow(() -> new NotFoundException("just message", new RuntimeException("throwable")));
      assertDoesNotThrow(() -> new NotFoundException(new RuntimeException("just throwable")));
      assertDoesNotThrow(() -> new NotFoundException("message", new RuntimeException("throwable"), true, true));
      assertDoesNotThrow(() -> new NotFoundException(Errors.COULD_NOT_UPDATE_USER));
      assertDoesNotThrow(() -> new NotFoundException(Errors.COULD_NOT_UPDATE_USER, new RuntimeException("throwable")));
      assertDoesNotThrow(() -> new NotFoundException(Errors.COULD_NOT_UPDATE_USER, new RuntimeException("throwable"), true, false));
   }
}
