package com.auth.auth.infra.impl;

import com.auth.auth.helpers.UserTestHelper;
import com.auth.core.domain.User;
import com.auth.core.ports.outbound.user.FindUserPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
final class UserDetailsServiceImplTests {

   @Mock
   private FindUserPort findUserPort;

   @Spy
   @InjectMocks
   private UserDetailsServiceImpl userDetailsService;

   private UUID userId;
   private User user;
   private String cpf;

   @BeforeEach
   void setUp() {
      userId = UUID.randomUUID();
      cpf = "87719506057";
      user = UserTestHelper.getUserDomain(userId, cpf, "test@test.com");
   }

   @Test
   void shouldGetUserDetails() {
      when(findUserPort.findUserByCpf(cpf)).thenReturn(Optional.of(user));
      final UserDetails resp = assertDoesNotThrow(() -> userDetailsService.loadUserByUsername(cpf));
      assertNotNull(resp);
      assertEquals(userId.toString(), resp.getUsername());
      assertTrue(resp.isCredentialsNonExpired());
      assertTrue(resp.isAccountNonExpired());
      assertTrue(resp.isAccountNonLocked());
      assertTrue(resp.isEnabled());
   }

   @Test
   void shouldThrowUsernameNotFoundException() {
      when(findUserPort.findUserByCpf(cpf)).thenReturn(Optional.empty());
      final var resp = assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(cpf));
      assertNotNull(resp);
      assertTrue(resp.getMessage().contains("User not found"));
   }
}
