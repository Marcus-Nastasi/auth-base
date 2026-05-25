package com.auth.core.domain.enums;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class EmailEventVersionTests {

   @ParameterizedTest
   @ValueSource(strings = {"", "123456", "jig", "zag", "wip"})
   void shouldNotThrowWhenValueNotFound(final String value) {
      final var resp = assertDoesNotThrow(() -> EmailEventVersion.fromString(value));
      assertNull(resp);
   }

   @ParameterizedTest
   @ValueSource(strings = {"V1", "v1"})
   void shouldMapCorrectly(final String value) {
      final var resp = assertDoesNotThrow(() -> EmailEventVersion.fromString(value));
      assertNotNull(resp);
   }
}
