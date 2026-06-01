package com.auth.auth.infra.config;

import com.auth.auth.infra.grant.CustomPasswordGrantAuthenticationToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.util.UUID;

@Configuration
public class RegisteredClientConfig {

   @Bean
   public RegisteredClientRepository registeredClientRepository(final PasswordEncoder passwordEncoder) {

      // Client para login de usuário (frontend / mobile)
      final RegisteredClient webClient = RegisteredClient.withId(UUID.randomUUID().toString())
           .clientId("web-client")
           .clientSecret(passwordEncoder.encode("web-secret"))
           .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
           .authorizationGrantType(CustomPasswordGrantAuthenticationToken.GRANT_TYPE)
           .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
           .scope("users.read")
           .scope("users.write")
           .scope("users.user")
           .scope("users.admin")
           .tokenSettings(TokenSettings.builder()
                   .accessTokenTimeToLive(Duration.ofMinutes(15))
                   .refreshTokenTimeToLive(Duration.ofDays(10))
                   .reuseRefreshTokens(false)
                   .build())
           .build();

      // Client para comunicação machine-to-machine (microsserviços)
      final RegisteredClient serviceClient = RegisteredClient.withId(UUID.randomUUID().toString())
           .clientId("service-client")
           .clientSecret(passwordEncoder.encode("service-secret"))
           .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
           .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
           .scope("users.read")
           .tokenSettings(TokenSettings.builder()
                   .accessTokenTimeToLive(Duration.ofMinutes(30))
                   .build())
           .build();

      return new InMemoryRegisteredClientRepository(webClient, serviceClient);
   }
}
