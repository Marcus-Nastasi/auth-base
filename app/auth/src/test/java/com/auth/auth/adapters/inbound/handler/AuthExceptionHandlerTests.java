package com.auth.auth.adapters.inbound.handler;

import com.auth.core.exceptions.ForbiddenException;
import com.auth.core.exceptions.UnauthorizedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

final class AuthExceptionHandlerTests {

   private final AuthExceptionHandler handler = new AuthExceptionHandler();

   @Test
   @DisplayName("UnauthorizedException deve retornar 401 sem body")
   void shouldReturn401ForUnauthorizedException() {
      final var response = handler.handleUnauthorizedException(new UnauthorizedException("unauthorized"));

      assertEquals(response.getStatusCode(), HttpStatus.UNAUTHORIZED);
      assertFalse(response.hasBody());
   }

   @Test
   @DisplayName("ForbiddenException deve retornar 403 sem body")
   void shouldReturn403ForForbiddenException() {
      final var response = handler.handleForbiddenException(new ForbiddenException("forbidden"));

      assertEquals(response.getStatusCode(), HttpStatus.FORBIDDEN);
      assertFalse(response.hasBody());
   }

   @Test
   @DisplayName("UnauthorizedException com mensagem não deve vazar no body")
   void shouldNotExposeMessageOnUnauthorized() {
      final var response = handler.handleUnauthorizedException(new UnauthorizedException("sensitive info"));

      assertNull(response.getBody());
   }

   @Test
   @DisplayName("ForbiddenException com mensagem não deve vazar no body")
   void shouldNotExposeMessageOnForbidden() {
      final var response = handler.handleForbiddenException(new ForbiddenException("sensitive info"));

      assertNull(response.getBody());
   }

   @Test
   @DisplayName("ForbiddenException com causa não deve alterar o status")
   void shouldReturn403EvenWithCause() {
      final var cause = new RuntimeException("root cause");
      final var response = handler.handleForbiddenException(new ForbiddenException("msg", cause));

      assertEquals(response.getStatusCode(), HttpStatus.FORBIDDEN);
   }
}
