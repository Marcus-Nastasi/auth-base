package com.auth.auth.infra.grant;

import com.auth.auth.helpers.TokenTestHelper;
import com.auth.auth.helpers.UserTestHelper;
import com.auth.core.domain.User;
import com.auth.core.ports.inbound.auth.PasswordEncoderPort;
import com.auth.core.ports.outbound.user.FindUserPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContext;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
final class CustomPasswordGrantAuthenticationProviderTests {

   @Mock
   private FindUserPort findUserPort;
   @Mock
   private PasswordEncoderPort passwordEncoderPort;
   @Mock
   private OAuth2AuthorizationService authorizationService;
   @Mock
   private OAuth2TokenGenerator<OAuth2Token> tokenGenerator;

   @Spy
   @InjectMocks
   private CustomPasswordGrantAuthenticationProvider provider;

   private String cpf;
   private String password;
   private Set<String> scopes;
   private CustomPasswordGrantAuthenticationToken authentication;
   private OAuth2ClientAuthenticationToken oAuth2ClientAuthenticationToken;
   private RegisteredClient registeredClient;
   private User user;
   private OAuth2Token oAuth2Token;

   @BeforeEach
   void setUp() {
      final UUID clientId = UUID.randomUUID();
      cpf = "12345678910";
      password = "123456";
      scopes = Set.of("users.read");

      final AuthorizationServerContext authorizationServerContext = new AuthorizationServerContext() {
         @Override
         public String getIssuer() {
            return "test-issuer";
         }
         @Override
         public AuthorizationServerSettings getAuthorizationServerSettings() {
            return AuthorizationServerSettings.builder().issuer(getIssuer()).build();
         }
      };
      AuthorizationServerContextHolder.setContext(authorizationServerContext);

      oAuth2ClientAuthenticationToken = mock(OAuth2ClientAuthenticationToken.class);
      authentication = new CustomPasswordGrantAuthenticationToken(
           cpf,
           password,
           oAuth2ClientAuthenticationToken,
           scopes,
           Map.of()
      );
      registeredClient = RegisteredClient
           .withId(UUID.randomUUID().toString())
           .clientId(clientId.toString())
           .authorizationGrantTypes(gt ->
                gt.addAll(new HashSet<>(Set.of(new AuthorizationGrantType("urn:custom:grant-type:password")))))
           .scopes(s -> s.addAll(new HashSet<>(Set.of("users.read","users.write","users.admin","users.user"))))
           .build();

      user = UserTestHelper.getUserDomain(UUID.randomUUID(), cpf, "test@test.com");
      user.setPassword(new BCryptPasswordEncoder().encode(password));

      oAuth2Token = TokenTestHelper.getAccessToken();
   }

   @Test
   void shouldThrowOAuth2AuthenticationException() {
      final var auth = new CustomPasswordGrantAuthenticationToken(cpf, password, mock(Authentication.class), scopes, Map.of());
      final var resp = assertThrows(OAuth2AuthenticationException.class, () -> provider.authenticate(auth));
      assertNotNull(resp);
      assertEquals("invalid_client", resp.getError().getErrorCode());
   }

   @Test
   void shouldThrowOAuth2AuthenticationExceptionWhenUnauthorizedGrant() {
      final var resp = assertThrows(OAuth2AuthenticationException.class, () -> provider.authenticate(authentication));
      assertNotNull(resp);
      assertEquals("unauthorized_client", resp.getError().getErrorCode());
   }

   @Test
   void shouldThrowWhenUserNotFound() {
      when(oAuth2ClientAuthenticationToken.getRegisteredClient()).thenReturn(registeredClient);
      when(findUserPort.findUserByCpf(cpf)).thenReturn(Optional.empty());

      final var resp = assertThrows(OAuth2AuthenticationException.class, () -> provider.authenticate(authentication));

      assertNotNull(resp);
      assertEquals("access_denied", resp.getError().getErrorCode());

      verify(findUserPort, times(1)).findUserByCpf(cpf);
   }

   @Test
   void shouldThrowWhenWrongPasswordSent() {
      user.setPassword(password+"0000000123");

      when(oAuth2ClientAuthenticationToken.getRegisteredClient()).thenReturn(registeredClient);
      when(findUserPort.findUserByCpf(cpf)).thenReturn(Optional.of(user));

      final var resp = assertThrows(OAuth2AuthenticationException.class, () -> provider.authenticate(authentication));

      assertNotNull(resp);
      assertEquals("access_denied", resp.getError().getErrorCode());

      verify(findUserPort, times(1)).findUserByCpf(cpf);
      verify(passwordEncoderPort, times(1)).matches(anyString(), anyString());
   }

   @Test
   void shouldResolveOneScope() {
      when(oAuth2ClientAuthenticationToken.getRegisteredClient()).thenReturn(registeredClient);
      when(findUserPort.findUserByCpf(cpf)).thenReturn(Optional.of(user));
      when(passwordEncoderPort.matches(any(), any())).thenReturn(true);
      when(tokenGenerator.generate(any(OAuth2TokenContext.class))).thenReturn(oAuth2Token);
      doNothing().when(authorizationService).save(any());

      final var resp = assertDoesNotThrow(() -> provider.authenticate(authentication));

      assertNotNull(resp);
      assertInstanceOf(OAuth2AccessTokenAuthenticationToken.class, resp);

      final var transformed = (OAuth2AccessTokenAuthenticationToken) resp;

      assertNotNull(transformed);
      assertEquals(oAuth2Token.getTokenValue(), transformed.getAccessToken().getTokenValue());
      assertFalse(transformed.getAccessToken().getScopes().isEmpty());
      assertTrue(transformed.getAccessToken().getScopes().contains("users.read"));

      verify(findUserPort, times(1)).findUserByCpf(cpf);
      verify(passwordEncoderPort, times(1)).matches(anyString(), anyString());
      verify(tokenGenerator, times(2)).generate(any(OAuth2TokenContext.class));
      verify(authorizationService, times(1)).save(any());
   }

   @Test
   void shouldResolveUserScopesWhenScopesRequestedNull() {
      authentication.setScopes(Collections.emptySet());

      when(oAuth2ClientAuthenticationToken.getRegisteredClient()).thenReturn(registeredClient);
      when(findUserPort.findUserByCpf(cpf)).thenReturn(Optional.of(user));
      when(passwordEncoderPort.matches(any(), any())).thenReturn(true);
      when(tokenGenerator.generate(any(OAuth2TokenContext.class))).thenReturn(oAuth2Token);
      doNothing().when(authorizationService).save(any());

      final var resp = assertDoesNotThrow(() -> provider.authenticate(authentication));

      assertNotNull(resp);
      assertInstanceOf(OAuth2AccessTokenAuthenticationToken.class, resp);

      final var transformed = (OAuth2AccessTokenAuthenticationToken) resp;

      assertNotNull(transformed);
      assertEquals(oAuth2Token.getTokenValue(), transformed.getAccessToken().getTokenValue());
      assertFalse(transformed.getAccessToken().getScopes().isEmpty());
      assertTrue(transformed.getAccessToken().getScopes().contains("users.read"));
      assertTrue(transformed.getAccessToken().getScopes().contains("users.write"));
      assertTrue(transformed.getAccessToken().getScopes().contains("users.admin"));

      verify(findUserPort, times(1)).findUserByCpf(cpf);
      verify(passwordEncoderPort, times(1)).matches(anyString(), anyString());
      verify(tokenGenerator, times(2)).generate(any(OAuth2TokenContext.class));
      verify(authorizationService, times(1)).save(any());
   }

   @Test
   void shouldThrowWhenNullGeneratedToken() {
      when(oAuth2ClientAuthenticationToken.getRegisteredClient()).thenReturn(registeredClient);
      when(findUserPort.findUserByCpf(cpf)).thenReturn(Optional.of(user));
      when(passwordEncoderPort.matches(any(), any())).thenReturn(true);
      when(tokenGenerator.generate(any(OAuth2TokenContext.class))).thenReturn(null);

      final var resp = assertThrows(OAuth2AuthenticationException.class, () -> provider.authenticate(authentication));

      assertNotNull(resp);
      assertEquals("server_error", resp.getError().getErrorCode());

      verify(findUserPort, times(1)).findUserByCpf(cpf);
      verify(passwordEncoderPort, times(1)).matches(anyString(), anyString());
      verify(tokenGenerator, times(1)).generate(any(OAuth2TokenContext.class));
      verify(authorizationService, times(0)).save(any());
   }

   @Test
   void shouldSupportAndNotSupport() {
      assertTrue(provider.supports(authentication.getClass()));
      assertFalse(provider.supports(mock(Authentication.class).getClass()));
   }
}
