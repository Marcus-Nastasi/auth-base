package com.auth.core.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

final class ForbiddenExceptionTests {

   @Test
   void shouldTestAllTypesOfConstructor() {
      assertDoesNotThrow(() -> new ForbiddenException("just message"));
      assertDoesNotThrow(() -> new ForbiddenException("just message", new RuntimeException("throwable")));
      assertDoesNotThrow(() -> new ForbiddenException("just message", new RuntimeException("throwable")));
      assertDoesNotThrow(() -> new ForbiddenException(new RuntimeException("just throwable")));
      assertDoesNotThrow(() -> new ForbiddenException("message", new RuntimeException("throwable"), true, true));
   }
}
