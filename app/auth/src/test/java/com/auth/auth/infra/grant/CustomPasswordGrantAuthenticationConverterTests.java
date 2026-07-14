package com.auth.auth.infra.grant;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class CustomPasswordGrantAuthenticationConverterTests {

   private CustomPasswordGrantAuthenticationConverter converter;

   private Authentication authentication;
   private HttpServletRequest request;

   @BeforeEach
   void setUp() {
      converter = new CustomPasswordGrantAuthenticationConverter();
      authentication = new CustomPasswordGrantAuthenticationToken(
           "12345678910",
           "123456",
           mock(Authentication.class),
           Set.of("users.read"),
           Collections.emptyMap()
      );
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
   void shouldThrowExceptionWhenNullCredentials() {
      SecurityContextHolder.getContext().setAuthentication(authentication);

      when(request.getParameter(OAuth2ParameterNames.GRANT_TYPE))
              .thenReturn(CustomPasswordGrantAuthenticationToken.GRANT_TYPE.getValue());
      when(request.getParameter("cpf")).thenReturn(null);
      when(request.getParameter("password")).thenReturn(null);

      final var result = assertThrows(OAuth2AuthenticationException.class, () -> converter.convert(request));

      assertNotNull(result);
      assertEquals(OAuth2ErrorCodes.INVALID_REQUEST, result.getError().getErrorCode());
   }

   @Test
   void shouldThrowExceptionWhenEmptyCredentials() {
      SecurityContextHolder.getContext().setAuthentication(authentication);

      when(request.getParameter(OAuth2ParameterNames.GRANT_TYPE))
              .thenReturn(CustomPasswordGrantAuthenticationToken.GRANT_TYPE.getValue());
      when(request.getParameter("cpf")).thenReturn("");
      when(request.getParameter("password")).thenReturn("");

      final var result = assertThrows(OAuth2AuthenticationException.class, () -> converter.convert(request));

      assertNotNull(result);
      assertEquals(OAuth2ErrorCodes.INVALID_REQUEST, result.getError().getErrorCode());
   }

   @Test
   void shouldGetEmptyScopes() {
      SecurityContextHolder.getContext().setAuthentication(authentication);

      when(request.getParameter(OAuth2ParameterNames.GRANT_TYPE)).thenReturn(CustomPasswordGrantAuthenticationToken.GRANT_TYPE.getValue());
      when(request.getParameter("cpf")).thenReturn("12345678910");
      when(request.getParameter("password")).thenReturn("123456");
      when(request.getParameter(OAuth2ParameterNames.SCOPE)).thenReturn("");

      final var result = assertDoesNotThrow(() -> converter.convert(request));

      assertNotNull(result);
   }

   @Test
   void shouldGetWithScopes() {
      SecurityContextHolder.getContext().setAuthentication(authentication);

      when(request.getParameter(OAuth2ParameterNames.GRANT_TYPE)).thenReturn(CustomPasswordGrantAuthenticationToken.GRANT_TYPE.getValue());
      when(request.getParameter("cpf")).thenReturn("12345678910");
      when(request.getParameter("password")).thenReturn("123456");
      when(request.getParameter(OAuth2ParameterNames.SCOPE)).thenReturn("users.read users.write");

      final var result = assertDoesNotThrow(() -> converter.convert(request));

      assertNotNull(result);
      assertInstanceOf(CustomPasswordGrantAuthenticationToken.class, result);

      final var converted = (CustomPasswordGrantAuthenticationToken) result;

      assertEquals("12345678910", converted.getCpf());
      assertEquals("123456", converted.getPassword());
      assertEquals("users.read users.write", String.join(" ", converted.getScopes()));
   }
}
