package com.auth.auth.adapters.inbound.mappers;

import com.auth.auth.adapters.inbound.output.ClientRegistrationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

final class ClientRegistrationDtoMapperTests {

   private ClientRegistrationDtoMapper mapper;

   @BeforeEach
   void setUp() {
      mapper = ClientRegistrationDtoMapper.INSTANCE;
   }

   // -------------------------------------------------------------------------
   // Helpers
   // -------------------------------------------------------------------------
   private RegisteredClient.Builder baseClientBuilder() {
      return RegisteredClient.withId(UUID.randomUUID().toString())
           .id(UUID.randomUUID().toString())
           .clientId("client-abc")
           .clientIdIssuedAt(Instant.now())
           .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
           .redirectUri("https://app.example.com/callback")
           .scope("openid")
           .scope("email");
   }

   // =========================================================================
   // response()
   // =========================================================================
   @Nested
   @DisplayName("response()")
   class Response {

      @Test
      @DisplayName("deve mapear appId corretamente")
      void shouldMapAppId() {
         final RegisteredClient client = baseClientBuilder()
                 .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                 .build();

         final ClientRegistrationResponse response = mapper.response(client, UUID.randomUUID());

         assertThat(response.appId()).isEqualTo(client.getId());
      }

      @Test
      @DisplayName("deve mapear clientId corretamente")
      void shouldMapClientId() {
         final RegisteredClient client = baseClientBuilder()
              .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
              .build();

         final ClientRegistrationResponse response = mapper.response(client, UUID.randomUUID());

         assertThat(response.clientId()).isEqualTo("client-abc");
      }

      @Test
      @DisplayName("deve mapear clientSecret (UUID) como string")
      void shouldMapClientSecret() {
         final RegisteredClient client = baseClientBuilder()
              .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
              .build();
         final UUID secret = UUID.randomUUID();

         final ClientRegistrationResponse response = mapper.response(client, secret);

         assertThat(response.clientSecret()).isEqualTo(secret.toString());
      }

      @Test
      @DisplayName("clientSecret nulo deve resultar em clientSecret nulo no response")
      void shouldMapNullClientSecret() {
         final RegisteredClient client = baseClientBuilder()
              .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
              .build();

         final ClientRegistrationResponse response = mapper.response(client, null);

         assertThat(response.clientSecret()).isNull();
      }

      @Test
      @DisplayName("deve mapear scopes corretamente")
      void shouldMapScopes() {
         final RegisteredClient client = baseClientBuilder()
              .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
              .build();

         final ClientRegistrationResponse response = mapper.response(client, UUID.randomUUID());

         assertThat(response.scopes()).containsExactlyInAnyOrder("openid", "email");
      }

      @Test
      @DisplayName("deve mapear redirectUris corretamente")
      void shouldMapRedirectUris() {
         final RegisteredClient client = baseClientBuilder()
              .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
              .build();

         final ClientRegistrationResponse response = mapper.response(client, UUID.randomUUID());

         assertThat(response.redirectUris()).containsExactly("https://app.example.com/callback");
      }

      @Test
      @DisplayName("deve mapear um único grantType corretamente")
      void shouldMapSingleGrantType() {
         final RegisteredClient client = baseClientBuilder()
              .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
              .build();

         final ClientRegistrationResponse response = mapper.response(client, UUID.randomUUID());

         assertThat(response.grantTypes()).containsExactly("authorization_code");
      }

      @Test
      @DisplayName("deve mapear múltiplos grantTypes corretamente")
      void shouldMapMultipleGrantTypes() {
         final RegisteredClient client = baseClientBuilder()
              .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
              .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
              .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
              .build();

         final ClientRegistrationResponse response = mapper.response(client, UUID.randomUUID());

         assertThat(response.grantTypes()).containsExactlyInAnyOrder(
              "authorization_code", "refresh_token", "client_credentials"
         );
      }

      @Test
      @DisplayName("deve mapear corretamente um RegisteredClient completo")
      void shouldMapFullObjectCorrectly() {
         final UUID secret = UUID.randomUUID();
         final RegisteredClient client = baseClientBuilder()
              .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
              .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
              .redirectUri("https://app.example.com/other-callback")
              .scope("users.read")
              .build();

         final ClientRegistrationResponse response = mapper.response(client, secret);

         assertThat(response.clientId()).isEqualTo("client-abc");
         assertThat(response.clientSecret()).isEqualTo(secret.toString());
         assertThat(response.grantTypes()).containsExactlyInAnyOrder("authorization_code", "refresh_token");
         assertThat(response.scopes()).containsExactlyInAnyOrder("openid", "email", "users.read");
         assertThat(response.redirectUris()).containsExactlyInAnyOrder(
              "https://app.example.com/callback",
              "https://app.example.com/other-callback"
         );
      }

      @Test
      @DisplayName("deve mapear default message")
      void shouldMapDefaultMessage() {
         final RegisteredClient client = baseClientBuilder()
                 .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                 .build();

         final ClientRegistrationResponse response = mapper.response(client, UUID.randomUUID());

         assertThat(response.message()).isNotNull();
         assertThat(response.message()).isEqualTo("Save your client_secret in a safe place, you cannot consult it later");
      }
   }

   // =========================================================================
   // getGrantTypes()
   // =========================================================================
   @Nested
   @DisplayName("getGrantTypes()")
   class GetGrantTypes {

      @Test
      @DisplayName("conjunto nulo deve retornar conjunto vazio")
      void shouldReturnEmptySetForNullInput() {
         final Set<String> result = mapper.getGrantTypes(null);
         assertThat(result).isEmpty();
      }

      @Test
      @DisplayName("conjunto vazio deve retornar conjunto vazio")
      void shouldReturnEmptySetForEmptyInput() {
         final Set<String> result = mapper.getGrantTypes(Collections.emptySet());
         assertThat(result).isEmpty();
      }

      @Test
      @DisplayName("conjunto com um único grantType deve mapear corretamente")
      void shouldMapSingleValue() {
         final Set<String> result = mapper.getGrantTypes(
                 Set.of(AuthorizationGrantType.CLIENT_CREDENTIALS)
         );
         assertThat(result).containsExactly("client_credentials");
      }

      @Test
      @DisplayName("conjunto com múltiplos grantTypes deve mapear todos corretamente")
      void shouldMapMultipleValues() {
         final Set<String> result = mapper.getGrantTypes(
              Set.of(
                   AuthorizationGrantType.AUTHORIZATION_CODE,
                   AuthorizationGrantType.REFRESH_TOKEN,
                   AuthorizationGrantType.CLIENT_CREDENTIALS
              )
         );
         assertThat(result).containsExactlyInAnyOrder(
              "authorization_code", "refresh_token", "client_credentials"
         );
      }

      @Test
      @DisplayName("conjunto contendo elemento nulo deve ignorá-lo (filter Objects::nonNull)")
      void shouldIgnoreNullElementsInSet() {
         final Set<AuthorizationGrantType> grantTypes =
                 new HashSet<>(Set.of(AuthorizationGrantType.AUTHORIZATION_CODE));
         grantTypes.add(null);

         final Set<String> result = mapper.getGrantTypes(grantTypes);

         assertThat(result).containsExactly("authorization_code");
      }

      @Test
      @DisplayName("custom AuthorizationGrantType deve ser mapeado pelo valor (getValue)")
      void shouldMapCustomGrantType() {
         final AuthorizationGrantType custom = new AuthorizationGrantType("custom_grant");

         final Set<String> result = mapper.getGrantTypes(Set.of(custom));

         assertThat(result).containsExactly("custom_grant");
      }
   }
}
