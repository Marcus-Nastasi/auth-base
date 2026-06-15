package com.auth.auth.infra.config;

import com.auth.auth.infra.grant.CustomPasswordGrantAuthenticationToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.UUID;

@Configuration
public class RegisteredClientConfig {

   @Bean
   public RegisteredClientRepository registeredClientRepository(final DataSource dataSource,
                                                                final PasswordEncoder passwordEncoder) {
      final JdbcRegisteredClientRepository repository =
              new JdbcRegisteredClientRepository(new JdbcTemplate(dataSource));

      registerIfAbsent(repository, passwordEncoder, buildAdminClient(passwordEncoder));
      registerIfAbsent(repository, passwordEncoder, buildWebClient(passwordEncoder));
      registerIfAbsent(repository, passwordEncoder, buildServiceClient(passwordEncoder));

      return repository;
   }

   private void registerIfAbsent(final JdbcRegisteredClientRepository repository,
                                 final PasswordEncoder passwordEncoder,
                                 final RegisteredClient client) {
      if (repository.findByClientId(client.getClientId()) == null) repository.save(client);
   }

   @Profile(value = "local")
   private RegisteredClient buildAdminClient(final PasswordEncoder passwordEncoder) {
      return RegisteredClient.withId(UUID.randomUUID().toString())
           .clientId("admin-client")
           .clientSecret(passwordEncoder.encode("admin-secret"))
           .clientName("Admin Client")
           .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
           .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
           .scope("client.create")
           .scope("client.read")
           .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(5))
                .build())
           .build();
   }

   @Profile(value = "local")
   private RegisteredClient buildWebClient(final PasswordEncoder passwordEncoder) {
      return RegisteredClient.withId(UUID.randomUUID().toString())
           .clientId("web-client")
           .clientSecret(passwordEncoder.encode("web-secret"))
           .clientName("Web Client")
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
   }

   @Profile(value = "local")
   private RegisteredClient buildServiceClient(final PasswordEncoder passwordEncoder) {
      return RegisteredClient.withId(UUID.randomUUID().toString())
           .clientId("service-client")
           .clientSecret(passwordEncoder.encode("service-secret"))
           .clientName("Service Client")
           .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
           .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
           .scope("users.read")
           .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(30))
                .build())
           .build();
   }
}
