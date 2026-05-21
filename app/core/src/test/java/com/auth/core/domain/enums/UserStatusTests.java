package com.auth.core.domain.enums;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class UserStatusTests {

   @ParameterizedTest
   @ValueSource(strings = {"", "123456", "jig", "zag", "wip", "V1", "v1"})
   void shouldNotThrowWhenValueNotFound(final String value) {
      final var resp = assertDoesNotThrow(() -> UserStatus.fromString(value));
      assertNull(resp);
   }

   @ParameterizedTest
   @ValueSource(strings = {"PENDING", "pending", "ACTIVE", "active", "INACTIVE", "inactive"})
   void shouldMapCorrectlyFromString(final String value) {
      final var resp = assertDoesNotThrow(() -> UserStatus.fromString(value));
      assertNotNull(resp);
   }

   @ParameterizedTest
   @ValueSource(ints = {3,4,5,6,7,8,9})
   void shouldNotThrowWhenValueNotFoundFromCode(final int code) {
      final var resp = assertDoesNotThrow(() -> UserStatus.fromCode(code));
      assertNull(resp);
   }

   @ParameterizedTest
   @ValueSource(ints = {0, 1, 2})
   void shouldMapCorrectlyFromCode(final int code) {
      final var resp = assertDoesNotThrow(() -> UserStatus.fromCode(code));
      assertNotNull(resp);
   }
}
