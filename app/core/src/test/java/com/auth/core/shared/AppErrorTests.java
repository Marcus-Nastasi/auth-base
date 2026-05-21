package com.auth.core.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class AppErrorTests {

   @Test
   void shouldCreateAppError() {
      final var appError = assertDoesNotThrow(() -> new AppError("Não pode ser null", "id", "null"));
      assertNotNull(appError);
      assertEquals("Não pode ser null", appError.getMessage());
      assertEquals("id", appError.getField());
      assertEquals("null", appError.getAttempted());
   }

   @Test
   void setters() {
      final var appError = new AppError(null, null, null);
      appError.setMessage("Não pode ser null");
      appError.setField("id");
      appError.setAttempted("null");

      assertNotNull(appError);
      assertEquals("Não pode ser null", appError.getMessage());
      assertEquals("id", appError.getField());
      assertEquals("null", appError.getAttempted());
   }
}
