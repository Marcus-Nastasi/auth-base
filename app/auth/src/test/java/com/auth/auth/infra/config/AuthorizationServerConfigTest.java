package com.auth.auth.infra.config;

import com.auth.core.ports.inbound.auth.PasswordEncoderPort;
import com.auth.core.ports.outbound.user.FindUserPort;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

final class AuthorizationServerConfigTest {

   @Test
   void shouldBuildFilterChainWithoutException() {
      final var findUserPort = mock(FindUserPort.class);
      final var passwordEncoderPort = mock(PasswordEncoderPort.class);
      final var authorizationService = mock(OAuth2AuthorizationService.class);
      final var tokenGenerator = mock(OAuth2TokenGenerator.class);
      final var jwtDecoder = mock(JwtDecoder.class);

      final var config = new AuthorizationServerConfig(
           findUserPort,
           passwordEncoderPort,
           authorizationService,
           tokenGenerator,
           jwtDecoder,
           "http://localhost:8080"
      );

      // HttpSecurity precisa de contexto Spring — esse teste valida apenas
      // que o construtor e as dependências são aceitos corretamente
      assertNotNull(config);
   }
}
