package com.auth.user.adapters.inbound.mappers;

import com.auth.user.helpers.UserTestHelper;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

final class UserResponseMapperTests {

   private final UserResponseMapper mapper = UserResponseMapper.INSTANCE;

   @Test
   void shouldMap() {
      final var userId = UUID.randomUUID();
      final var cpf = "04432842024";
      final var email = "test@gmail.com";
      final var user = UserTestHelper.getUserDomain(userId, cpf, email);

      final var resp = assertDoesNotThrow(() -> mapper.toUserByIdResponse(user));

      assertNotNull(resp);
      assertEquals(email, resp.email());
      assertEquals(user.getFirstName(), resp.firstName());
      assertEquals(user.getLastName(), resp.lastName());
      assertEquals(cpf, resp.cpf());
      assertEquals(user.getBirthDate(), resp.birthDate());
      assertEquals(user.getUserRole(), resp.role());
   }
}
