package com.auth.auth.infra.customizer;

import com.auth.core.domain.User;
import com.auth.core.ports.outbound.user.FindUserPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

final class TokenCustomizerTests {

   private FindUserPort findUserPort;
   private OAuth2TokenCustomizer<JwtEncodingContext> customizer;

   @BeforeEach
   void setUp() {
      findUserPort = mock(FindUserPort.class);
      customizer = new TokenCustomizer().jwtCustomizer(findUserPort);
   }

   private JwtEncodingContext buildContext() {
      final var principal = mock(Authentication.class);
      when(principal.getName()).thenReturn("some-principal");

      final var context = mock(JwtEncodingContext.class);
      when(context.getTokenType()).thenReturn(OAuth2TokenType.REFRESH_TOKEN);
      when(context.getPrincipal()).thenReturn(principal);
      when(context.getClaims()).thenReturn(JwtClaimsSet.builder());

      return context;
   }


   @Test
   @DisplayName("não faz nada para token que não é ACCESS_TOKEN")
   void shouldSkipNonAccessToken() {
      final var context = buildContext();
      customizer.customize(context);
      verify(context, never()).getClaims();
   }

   @Test
   @DisplayName("adiciona claims do usuário quando principal é UUID válido")
   void shouldAddUserClaimsWhenPrincipalIsUuid() {
      final var userId = UUID.randomUUID();
      final var user = new User();
      user.setEmail("test@example.com");
      user.setCpf("12345678900");

      when(findUserPort.findById(userId)).thenReturn(Optional.of(user));

      final var auth = mock(Authentication.class);
      final var claimsBuilder = mock(JwtClaimsSet.Builder.class, RETURNS_SELF);
      final var context = mock(JwtEncodingContext.class);

      when(auth.getName()).thenReturn(userId.toString());
      when(context.getPrincipal()).thenReturn(auth);
      when(context.getTokenType()).thenReturn(OAuth2TokenType.ACCESS_TOKEN);
      when(context.getClaims()).thenReturn(claimsBuilder);

      customizer.customize(context);

      verify(claimsBuilder, times(1)).claim("email", "test@example.com");
      verify(claimsBuilder, times(1)).claim("cpf", "12345678900");
      verify(claimsBuilder, times(1)).claim("typ", "access");
   }

   @Test
   @DisplayName("adiciona apenas typ=access quando principal não é UUID (client_credentials)")
   void shouldAddOnlyTypWhenPrincipalIsNotUuid() {
      final var claimsBuilder = mock(JwtClaimsSet.Builder.class, RETURNS_SELF);
      final var context = mock(JwtEncodingContext.class);
      final var auth = mock(Authentication.class);

      when(auth.getName()).thenReturn("my-client-id");
      when(context.getTokenType()).thenReturn(OAuth2TokenType.ACCESS_TOKEN);
      when(context.getPrincipal()).thenReturn(auth);
      when(context.getClaims()).thenReturn(claimsBuilder);

      customizer.customize(context);

      verify(claimsBuilder, times(1)).claim("typ", "access");
      verify(findUserPort, never()).findById(any());
   }

   @Test
   @DisplayName("não adiciona claims quando usuário não encontrado")
   void shouldNotAddClaimsWhenUserNotFound() {
      final var userId = UUID.randomUUID();
      final var auth = mock(Authentication.class);

      when(findUserPort.findById(userId)).thenReturn(Optional.empty());
      when(auth.getName()).thenReturn(userId.toString());

      final var claimsBuilder = mock(JwtClaimsSet.Builder.class, RETURNS_SELF);
      final var context = mock(JwtEncodingContext.class);
      when(context.getTokenType()).thenReturn(OAuth2TokenType.ACCESS_TOKEN);
      when(context.getPrincipal()).thenReturn(auth);
      when(context.getClaims()).thenReturn(claimsBuilder);

      customizer.customize(context);

      verify(claimsBuilder, never()).claim(any(), any());
   }
}
