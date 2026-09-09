package com.auth.auth.infra.grant;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

final class CustomPasswordGrantAuthenticationConverterTests {

   private CustomPasswordGrantAuthenticationConverter converter;

   // RegisteredClient's vars
   private UUID appId;
   private UUID clientId;
   private UUID clientSecret;
   private RegisteredClient.Builder registeredClient;

   private ClientAuthenticationMethod clientAuthenticationMethod;
   private Authentication authentication;
   private HttpServletRequest request;

   @BeforeEach
   void setUp() {
      appId = UUID.randomUUID();
      clientId = UUID.randomUUID();
      clientSecret = UUID.randomUUID();
      converter = new CustomPasswordGrantAuthenticationConverter();

      registeredClient = RegisteredClient
           .withId(appId.toString())
           .clientId(clientId.toString())
           .clientSecret(clientSecret.toString());

      request = mock(HttpServletRequest.class);
   }

   @AfterEach
   void shutDown() {
      SecurityContextHolder.clearContext();
   }

   @Test
   void shouldReturnNullIfWrongGrant() {
      when(request.getParameter(OAuth2ParameterNames.GRANT_TYPE)).thenReturn("ABC");
      final var result = assertDoesNotThrow(() -> converter.convert(request));
      assertNull(result);
   }

   @Test
   void shouldThrowExceptionWhenNullAuth() {
      SecurityContextHolder.getContext().setAuthentication(null);

      when(request.getParameter(OAuth2ParameterNames.GRANT_TYPE))
              .thenReturn(CustomPasswordGrantAuthenticationToken.GRANT_TYPE.getValue());
      when(request.getParameter("cpf")).thenReturn(null);
      when(request.getParameter("password")).thenReturn(null);

      final var result = assertThrows(OAuth2AuthenticationException.class, () -> converter.convert(request));

      assertNotNull(result);
      assertEquals(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT, result.getError().getErrorCode());
   }

   @Test
   @DisplayName("should throw when client principal not instance of OAuth2ClientAuthenticationToken")
   void shouldThrowExceptionWhenClientPrincipalNotInstance() {
      SecurityContextHolder.getContext().setAuthentication(mock(Authentication.class));

      when(request.getParameter(OAuth2ParameterNames.GRANT_TYPE)).thenReturn(CustomPasswordGrantAuthenticationToken.GRANT_TYPE.getValue());

      final var result = assertThrows(OAuth2AuthenticationException.class, () -> converter.convert(request));

      assertNotNull(result);
      assertEquals(OAuth2ErrorCodes.INVALID_CLIENT, result.getError().getErrorCode());
   }

   @Test
   @DisplayName("should throw when registered client is null")
   void shouldThrowWhenRegisteredClientNull() {
      authentication = mock(OAuth2ClientAuthenticationToken.class);

      when(request.getParameter(OAuth2ParameterNames.GRANT_TYPE)).thenReturn(CustomPasswordGrantAuthenticationToken.GRANT_TYPE.getValue());
      when(authentication.getPrincipal()).thenReturn(null);

      SecurityContextHolder.getContext().setAuthentication(authentication);

      final var result = assertThrows(OAuth2AuthenticationException.class, () -> converter.convert(request));

      assertNotNull(result);
      assertEquals(OAuth2ErrorCodes.INVALID_CLIENT, result.getError().getErrorCode());
   }

   @Nested
   class GrantType {

      @Test
      @DisplayName("should throw when requested grant is not present in client's grant")
      void shouldThrowWhenRequestedGrantNotPresentInClientGrant() {
         clientAuthenticationMethod = new ClientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue());
         registeredClient.authorizationGrantTypes(authorizationGrantTypes ->
                 authorizationGrantTypes.addAll(Set.of(AuthorizationGrantType.CLIENT_CREDENTIALS, AuthorizationGrantType.JWT_BEARER, AuthorizationGrantType.REFRESH_TOKEN)))
                 .clientAuthenticationMethod(clientAuthenticationMethod);
         authentication = new OAuth2ClientAuthenticationToken(registeredClient.build(), clientAuthenticationMethod, null);

         when(request.getParameter(OAuth2ParameterNames.GRANT_TYPE)).thenReturn(CustomPasswordGrantAuthenticationToken.GRANT_TYPE.getValue());

         SecurityContextHolder.getContext().setAuthentication(authentication);

         final var result = assertThrows(OAuth2AuthenticationException.class, () -> converter.convert(request));

         assertNotNull(result);
         assertEquals(OAuth2ErrorCodes.INVALID_GRANT, result.getError().getErrorCode());
      }

   }

   @Test
   @DisplayName("should throw when request cpf is null")
   void shouldThrowWhenRequestCpfNull() {
      clientAuthenticationMethod = new ClientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue());
      registeredClient.authorizationGrantTypes(authorizationGrantTypes ->
                      authorizationGrantTypes.add(CustomPasswordGrantAuthenticationToken.GRANT_TYPE))
              .clientAuthenticationMethod(clientAuthenticationMethod);
      authentication = new OAuth2ClientAuthenticationToken(registeredClient.build(), clientAuthenticationMethod, null);

      when(request.getParameter(OAuth2ParameterNames.GRANT_TYPE)).thenReturn(CustomPasswordGrantAuthenticationToken.GRANT_TYPE.getValue());
      when(request.getParameter("cpf")).thenReturn(null);
      when(request.getParameter("password")).thenReturn("1234");

      SecurityContextHolder.getContext().setAuthentication(authentication);

      final var result = assertThrows(OAuth2AuthenticationException.class, () -> converter.convert(request));

      assertNotNull(result);
      assertEquals(OAuth2ErrorCodes.INVALID_REQUEST, result.getError().getErrorCode());
   }

   @Test
   @DisplayName("should throw when request password is null")
   void shouldThrowWhenRequestPasswordNull() {
      clientAuthenticationMethod = new ClientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue());
      registeredClient.authorizationGrantTypes(authorizationGrantTypes ->
                      authorizationGrantTypes.add(CustomPasswordGrantAuthenticationToken.GRANT_TYPE))
              .clientAuthenticationMethod(clientAuthenticationMethod);
      authentication = new OAuth2ClientAuthenticationToken(registeredClient.build(), clientAuthenticationMethod, null);

      when(request.getParameter(OAuth2ParameterNames.GRANT_TYPE)).thenReturn(CustomPasswordGrantAuthenticationToken.GRANT_TYPE.getValue());
      when(request.getParameter("cpf")).thenReturn("1234");
      when(request.getParameter("password")).thenReturn(null);

      SecurityContextHolder.getContext().setAuthentication(authentication);

      final var result = assertThrows(OAuth2AuthenticationException.class, () -> converter.convert(request));

      assertNotNull(result);
      assertEquals(OAuth2ErrorCodes.INVALID_REQUEST, result.getError().getErrorCode());
   }

   @Nested
   class ClientScopes {

      @Test
      @DisplayName("should throw when client scopes null/empty")
      void shouldThrowWhenClientScopesEmpty() {
         clientAuthenticationMethod = new ClientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue());
         registeredClient.authorizationGrantTypes(authorizationGrantTypes ->
                         authorizationGrantTypes.add(CustomPasswordGrantAuthenticationToken.GRANT_TYPE))
                 .clientAuthenticationMethod(clientAuthenticationMethod);
         authentication = new OAuth2ClientAuthenticationToken(registeredClient.build(), clientAuthenticationMethod, null);

         when(request.getParameter(OAuth2ParameterNames.GRANT_TYPE)).thenReturn(CustomPasswordGrantAuthenticationToken.GRANT_TYPE.getValue());
         when(request.getParameter("cpf")).thenReturn("1234");
         when(request.getParameter("password")).thenReturn("1234");

         SecurityContextHolder.getContext().setAuthentication(authentication);

         final var result = assertThrows(OAuth2AuthenticationException.class, () -> converter.convert(request));

         assertNotNull(result);
         assertEquals(OAuth2ErrorCodes.INSUFFICIENT_SCOPE, result.getError().getErrorCode());
      }

      @Test
      @DisplayName("should throw when requested scopes not included in client scopes")
      void shouldThrowWhenRequestedScopesNotIncluded() {
         clientAuthenticationMethod = new ClientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue());
         registeredClient.authorizationGrantTypes(authorizationGrantTypes ->
                         authorizationGrantTypes.add(CustomPasswordGrantAuthenticationToken.GRANT_TYPE))
                 .clientAuthenticationMethod(clientAuthenticationMethod)
                 .scopes(s -> s.addAll(Set.of("email", "openid")));
         authentication = new OAuth2ClientAuthenticationToken(registeredClient.build(), clientAuthenticationMethod, null);

         when(request.getParameter(OAuth2ParameterNames.GRANT_TYPE)).thenReturn(CustomPasswordGrantAuthenticationToken.GRANT_TYPE.getValue());
         when(request.getParameter(OAuth2ParameterNames.SCOPE)).thenReturn("email openid users.read");
         when(request.getParameter("cpf")).thenReturn("1234");
         when(request.getParameter("password")).thenReturn("1234");

         SecurityContextHolder.getContext().setAuthentication(authentication);

         final var result = assertThrows(OAuth2AuthenticationException.class, () -> converter.convert(request));

         assertNotNull(result);
         assertEquals(OAuth2ErrorCodes.INSUFFICIENT_SCOPE, result.getError().getErrorCode());
      }

   }

   @Test
   @DisplayName("should pass")
   void shouldPassOk() {
      clientAuthenticationMethod = new ClientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue());
      registeredClient.authorizationGrantTypes(authorizationGrantTypes ->
                      authorizationGrantTypes.add(CustomPasswordGrantAuthenticationToken.GRANT_TYPE))
              .clientAuthenticationMethod(clientAuthenticationMethod)
              .scopes(s -> s.addAll(Set.of("email", "openid", "users.read")));
      authentication = new OAuth2ClientAuthenticationToken(registeredClient.build(), clientAuthenticationMethod, null);

      when(request.getParameter(OAuth2ParameterNames.GRANT_TYPE)).thenReturn(CustomPasswordGrantAuthenticationToken.GRANT_TYPE.getValue());
      when(request.getParameter(OAuth2ParameterNames.SCOPE)).thenReturn("email openid users.read");
      when(request.getParameter("cpf")).thenReturn("1234");
      when(request.getParameter("password")).thenReturn("1234");

      SecurityContextHolder.getContext().setAuthentication(authentication);

      final var result = assertDoesNotThrow(() -> converter.convert(request));

      assertNotNull(result);
      assertInstanceOf(CustomPasswordGrantAuthenticationToken.class, result);

      final var customAuth = (CustomPasswordGrantAuthenticationToken) result;
      assertEquals("1234", customAuth.getCpf());
      assertTrue(customAuth.getScopes().containsAll(Set.of("email", "openid", "users.read")));

      assertInstanceOf(OAuth2ClientAuthenticationToken.class, result.getPrincipal());

      final var auth = (OAuth2ClientAuthenticationToken) result.getPrincipal();
      assertEquals(appId.toString(), auth.getRegisteredClient().getId());

      assertEquals(clientId.toString(), auth.getRegisteredClient().getClientId());
      assertEquals(clientSecret.toString(), auth.getRegisteredClient().getClientSecret());
   }
}
