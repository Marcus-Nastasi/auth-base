package com.auth.core.domain.enums;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class EmailEventTypeTests {

   @ParameterizedTest
   @ValueSource(strings = {"", "123456", "jig", "zag", "wip"})
   void shouldNotThrowWhenValueNotFound(final String value) {
      final var resp = assertDoesNotThrow(() -> EmailEventType.fromValue(value));
      assertNull(resp);
   }

   @ParameterizedTest
   @ValueSource(strings = {"USER_PENDING_CREATED", "user_pending_created", "UserPendingCreated"})
   void shouldMapCorrectly(final String value) {
      final var resp = assertDoesNotThrow(() -> EmailEventType.fromValue(value));
      assertNotNull(resp);
   }
}
