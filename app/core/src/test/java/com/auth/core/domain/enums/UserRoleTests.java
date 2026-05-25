package com.auth.core.domain.enums;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class UserRoleTests {

   @ParameterizedTest
   @ValueSource(strings = {"", "123456", "jig", "zag", "wip", "V1", "v1"})
   void shouldThrowWhenValueNotFound(final String value) {
      final var resp = assertThrows(IllegalArgumentException.class, () -> UserRole.fromString(value));
      assertNotNull(resp);
      assertTrue(resp.getMessage().contains("Role not found"));
   }

   @ParameterizedTest
   @ValueSource(strings = {"USER", "user", "ADMIN", "admin"})
   void shouldMapCorrectly(final String value) {
      final var resp = assertDoesNotThrow(() -> UserRole.fromString(value));
      assertNotNull(resp);
   }
}
