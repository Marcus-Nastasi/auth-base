package com.auth.user.adapters.inbound.mappers;

import com.auth.core.domain.enums.UserRole;
import com.auth.user.adapters.inbound.input.UserRequestDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

final class UserRequestMapperTests {

   private final UserRequestMapper mapper = UserRequestMapper.INSTANCE;

   @Test
   void shouldMap() {
      final var cpf = "04432842024";
      final var email = "test@gmail.com";
      final var userRequestDto = new UserRequestDto(
              email, "12345678", "Marc", "Rol", cpf, LocalDate.of(2000, 10, 2), UserRole.ADMIN
      );

      final var resp = assertDoesNotThrow(() -> mapper.toDomain(userRequestDto));

      assertNotNull(resp);
      assertEquals(email, resp.getEmail());
      assertEquals("12345678", resp.getPassword());
      assertEquals("Marc", resp.getFirstName());
      assertEquals("Rol", resp.getLastName());
      assertEquals(cpf, resp.getCpf());
      assertEquals(LocalDate.of(2000, 10, 2), resp.getBirthDate());
      assertEquals(UserRole.ADMIN, resp.getUserRole());
      assertNull(resp.getCreatedAt());
      assertNull(resp.getInactivatedAt());
      assertNull(resp.getStatus());
   }
}
