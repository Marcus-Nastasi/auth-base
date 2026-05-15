package com.auth.auth.usecases;

import com.auth.core.domain.User;
import com.auth.core.exceptions.ForbiddenException;
import com.auth.core.exceptions.NotFoundException;
import com.auth.core.ports.inbound.auth.PasswordEncoderPort;
import com.auth.core.ports.inbound.auth.TokenPort;
import com.auth.core.ports.outbound.user.FindUserPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
final class AuthUseCaseTests {

    @Mock
    private FindUserPort findUserPort;

    @Mock
    private TokenPort tokenPort;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @Spy
    @InjectMocks
    private AuthUseCase authUseCase;

    private User user;
    private String claims;

    private String accessToken;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .password("123456")
                .inactivatedAt(null)
                .build();

        claims = "users.read users.write users.admin";

        accessToken = "eyJraWQiOiJhdXRoLWJhc2Uta2lkIiwiYWxnIjoiUlMyNTYiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2lhbS5sb2NhbCIsInN1YiI6ImM5ZGU2MzI5LWIwNTItNGY5MS04M2U4LTc1OGY1MDc2NzIwYSIsImVtYWlsIjoidmlubmllLm5zdHNAZ21haWwuY29tIiwiY3BmIjoiNDM3LjQzNS4wODgtODUiLCJzY29wZSI6InVzZXJzLnJlYWQgdXNlcnMud3JpdGUgdXNlcnMuYWRtaW4iLCJ0eXAiOiJhY2Nlc3MiLCJleHAiOjE3NzgzOTYxNDR9.stro4kz7rWlACnxjMyCMgKFJU6rbXxn7WkcAuKtcfxF6ptmEWF3qCDbk7cg2v0nNqBWnCnYb4XWfY4sau0LopjvYkVtutgpmj2vULSLw8Xgu9oP583w0TeaAWsYuIcX-HiIE4mhDGjjsQcpsoukTbTgoQR0KQJ06C_GLdCwXEDhXmt7QjIeRwqF0_1lBfLhqmnojwqtEPl24rwpm-K8fKkidGBIL2nKsFMekLYRny3SoGZMltvjIy-R5C2-8X2pF6SkoL9lRuy_KD8MTQMBPCzIOomVTI1BFYRy5XjoJeUinpCqa0k_xA3cDMnlHpr6mFr1dgGrPblMsE3N4qZGPRQ";
    }

    @Test
    void shouldLogIn() {
        when(findUserPort.findUserByCpf(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoderPort.matches(anyString(), anyString())).thenReturn(true);
        when(tokenPort.generateAccessToken(any(User.class))).thenReturn(accessToken);
        when(tokenPort.generateRefreshToken(any(User.class))).thenReturn(accessToken);
        when(tokenPort.getClaim(anyString(), any(), any())).thenReturn(claims);

        final var response = assertDoesNotThrow(() -> authUseCase.login("fake@gmail.com", "123456"));

        assertNotNull(response);
        assertEquals(accessToken, response.getToken());
        assertEquals(accessToken, response.getRefresh());
        assertEquals(claims, response.getScope());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserEmpty() {
        when(findUserPort.findUserByCpf(anyString())).thenReturn(Optional.empty());
        final var response = assertThrows(NotFoundException.class, () -> authUseCase.login("fake@gmail.com", "123456"));
        assertNotNull(response);
    }


    @Test
    void shouldThrowForbiddenExceptionWhenUserInactive() {
        user.setInactivatedAt(LocalDateTime.now());
        when(findUserPort.findUserByCpf(anyString())).thenReturn(Optional.of(user));
        final var response = assertThrows(ForbiddenException.class, () -> authUseCase.login("fake@gmail.com", "123456"));
        assertNotNull(response);
    }

    @Test
    void shouldThrowForbiddenExceptionWhenPasswordNotEqual() {
        when(findUserPort.findUserByCpf(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoderPort.matches(anyString(), anyString())).thenReturn(false);
        final var response = assertThrows(ForbiddenException.class, () -> authUseCase.login("fake@gmail.com", "123456"));
        assertNotNull(response);
    }
}
