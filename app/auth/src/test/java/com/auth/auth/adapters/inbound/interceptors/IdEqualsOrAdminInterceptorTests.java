package com.auth.auth.adapters.inbound.interceptors;

import com.auth.core.exceptions.ForbiddenException;
import com.auth.core.ports.inbound.auth.TokenPort;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
final class IdEqualsOrAdminInterceptorTests {
//
//    @Mock
//    private TokenPort tokenPort;
//
//    @Spy
//    @InjectMocks
//    private IdEqualsOrAdminInterceptor interceptor;
//
//    @ParameterizedTest
//    @ValueSource(strings = {"dmksadmsalnaklf", "token", "", "null", "3513576c-56d8-404a-9f35-fa1a0f6aac33"})
//    void shouldThrowIfFirstArgNotUUID(final String str) {
//        var result = assertThrows(ForbiddenException.class, () -> interceptor.validate(new Object[]{str, ""}));
//        assertNotNull(result);
//    }
//
//    @ParameterizedTest
//    @ValueSource(chars = {'o', 'p', 'e'})
//    void shouldThrowIfSecondArgNotString(final char charr) {
//        var result = assertThrows(ForbiddenException.class, () -> interceptor.validate(new Object[]{UUID.randomUUID(), charr}));
//        assertNotNull(result);
//    }
//
//
//    @Test
//    void shouldThrowIfSecondArgIsNull() {
//        var result = assertThrows(ForbiddenException.class, () -> interceptor.validate(new Object[]{UUID.randomUUID(), null}));
//        assertNotNull(result);
//    }
//
//    // -------------------------------------------------------------------------
//    // getToken() — Bearer header stripping
//    // -------------------------------------------------------------------------
//
//    @Test
//    void shouldStripBearerPrefixAndPassRawTokenToPort() {
//        final UUID id = UUID.randomUUID();
//        final DecodedJWT decodedJWT = buildDecodedJWT(id.toString(), "users.admin");
//
//        when(tokenPort.validate("raw-jwt-token")).thenReturn(decodedJWT);
//
//        assertDoesNotThrow(() -> interceptor.validate(new Object[]{id, "Bearer raw-jwt-token"}));
//        verify(tokenPort).validate("raw-jwt-token");
//    }
//
//    @Test
//    void shouldThrowForbiddenWhenIdStringNullOnGetToken() {
//        final UUID id = UUID.randomUUID();
//        var resp = assertThrows(ForbiddenException.class, () -> interceptor.validate(new Object[]{id, null}));
//        assertNotNull(resp);
//    }
//
//    @Test
//    void shouldPassTokenDirectlyWhenNoBearerPrefix() {
//        final UUID id = UUID.randomUUID();
//        final DecodedJWT decodedJWT = buildDecodedJWT(id.toString(), "users.admin");
//
//        when(tokenPort.validate("raw-jwt-token")).thenReturn(decodedJWT);
//
//        assertDoesNotThrow(() -> interceptor.validate(new Object[]{id, "raw-jwt-token"}));
//        verify(tokenPort).validate("raw-jwt-token");
//    }
//
//    // -------------------------------------------------------------------------
//    // validate() — IllegalArgumentException path (bad subject UUID)
//    // -------------------------------------------------------------------------
//
//    @Test
//    void shouldThrowForbiddenWhenSubjectIsNotValidUUID() {
//        final UUID id = UUID.randomUUID();
//        final DecodedJWT decodedJWT = mock(DecodedJWT.class);
//
//        when(tokenPort.validate(anyString())).thenReturn(decodedJWT);
//        when(decodedJWT.getSubject()).thenReturn("not-a-valid-uuid");
//
//        var result = assertThrows(ForbiddenException.class, () -> interceptor.validate(new Object[]{id, "some-token"}));
//        assertNotNull(result);
//    }
//
//    // -------------------------------------------------------------------------
//    // extractUserRoleFromScope() — scope claim absent or null string
//    // -------------------------------------------------------------------------
//
//    @Test
//    void shouldThrowForbiddenWhenScopeClaimIsAbsent() {
//        final UUID id = UUID.randomUUID();
//        final DecodedJWT decodedJWT = mock(DecodedJWT.class);
//
//        when(tokenPort.validate(anyString())).thenReturn(decodedJWT);
//        when(decodedJWT.getSubject()).thenReturn(id.toString());
//        when(decodedJWT.getClaim("scope")).thenReturn(null);
//
//        var result = assertThrows(ForbiddenException.class, () -> interceptor.validate(new Object[]{id, "some-token"}));
//        assertNotNull(result);
//    }
//
//    @Test
//    void shouldThrowForbiddenWhenScopeClaimStringIsNull() {
//        final UUID id = UUID.randomUUID();
//        final DecodedJWT decodedJWT = mock(DecodedJWT.class);
//        final Claim claim = mock(Claim.class);
//
//        when(tokenPort.validate(anyString())).thenReturn(decodedJWT);
//        when(decodedJWT.getSubject()).thenReturn(id.toString());
//        when(decodedJWT.getClaim("scope")).thenReturn(claim);
//        when(claim.asString()).thenReturn(null);
//
//        var result = assertThrows(ForbiddenException.class, () -> interceptor.validate(new Object[]{id, "some-token"}));
//        assertNotNull(result);
//    }
//
//    // -------------------------------------------------------------------------
//    // isIdEqualsOrAdmin() — ADMIN role bypasses ID check
//    // -------------------------------------------------------------------------
//
//    @Test
//    void shouldNotThrowWhenRoleIsAdminEvenIfIdsDiffer() {
//        final UUID requestedId = UUID.randomUUID();
//        final UUID tokenSubjectId = UUID.randomUUID(); // intentionally different
//
//        final DecodedJWT decodedJWT = buildDecodedJWT(tokenSubjectId.toString(), "users.admin");
//        when(tokenPort.validate(anyString())).thenReturn(decodedJWT);
//
//        assertDoesNotThrow(() -> interceptor.validate(new Object[]{requestedId, "some-token"}));
//    }
//
//    // -------------------------------------------------------------------------
//    // isIdEqualsOrAdmin() — USER role, ID must match
//    // -------------------------------------------------------------------------
//
//    @Test
//    void shouldNotThrowWhenRoleIsUserAndIdsMatch() {
//        final UUID id = UUID.randomUUID();
//
//        final DecodedJWT decodedJWT = buildDecodedJWT(id.toString(), "users.read");
//        when(tokenPort.validate(anyString())).thenReturn(decodedJWT);
//
//        assertDoesNotThrow(() -> interceptor.validate(new Object[]{id, "some-token"}));
//    }
//
//    @Test
//    void shouldThrowForbiddenWhenRoleIsUserAndIdsDiffer() {
//        final UUID requestedId = UUID.randomUUID();
//        final UUID tokenSubjectId = UUID.randomUUID(); // intentionally different
//
//        final DecodedJWT decodedJWT = buildDecodedJWT(tokenSubjectId.toString(), "users.read");
//        when(tokenPort.validate(anyString())).thenReturn(decodedJWT);
//
//        var result = assertThrows(ForbiddenException.class, () -> interceptor.validate(new Object[]{requestedId, "some-token"}));
//        assertNotNull(result);
//    }
//
//    // -------------------------------------------------------------------------
//    // Helper
//    // -------------------------------------------------------------------------
//
//    private DecodedJWT buildDecodedJWT(final String subject, final String scopeValue) {
//        final DecodedJWT decodedJWT = mock(DecodedJWT.class);
//        final Claim claim = mock(Claim.class);
//
//        when(decodedJWT.getSubject()).thenReturn(subject);
//        when(decodedJWT.getClaim("scope")).thenReturn(claim);
//        when(claim.asString()).thenReturn(scopeValue);
//
//        return decodedJWT;
//    }
}
