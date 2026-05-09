package com.auth.user.usecases;

import com.auth.core.domain.User;
import com.auth.core.domain.enums.UserRole;
import com.auth.core.domain.enums.UserStatus;
import com.auth.core.exceptions.NotFoundException;
import com.auth.core.ports.inbound.auth.PasswordEncoderPort;
import com.auth.core.ports.outbound.auth.ConfirmationEmailSenderPort;
import com.auth.core.ports.outbound.user.FindUserPort;
import com.auth.core.ports.outbound.user.SaveUserPort;
import com.auth.user.helpers.UserTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
final class UserUseCaseTests {

    @Mock
    private FindUserPort findUserPort;

    @Mock
    private SaveUserPort saveUserPort;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @Mock
    private ConfirmationEmailSenderPort confirmationEmailSenderPort;

    @Spy
    @InjectMocks
    private UserUseCase useCase;

    private static UUID userId;
    private static String cpf;
    private static String email;
    private static User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        cpf = "12345678910";
        email = "larara.larari@gmail.com";
        user = UserTestHelper.getUserDomain(userId, cpf, email);
    }

    @Test
    void shouldGetAllUsers() {
        when(findUserPort.findAll(anyInt(), anyInt(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Set.of(user));

        final var result = useCase.findAll(1, 10, "email@email.com", "12345677890", "Jef", "Jones", LocalDate.of(2000, 4, 10), UserStatus.ACTIVE, UserRole.USER);
        final var resultUser = result.data().iterator().next();

        assertNotNull(result);
        assertEquals(userId, resultUser.getId());
        assertEquals(cpf, resultUser.getCpf());
        assertEquals(email, resultUser.getEmail());
    }

    @Test
    void shouldReturnPageResponseEmpty() {
        when(findUserPort.findAll(anyInt(), anyInt(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(null);

        final var result = useCase.findAll(1, 10, "email@email.com", "12345677890", "Jef", "Jones", LocalDate.of(2000, 4, 10), UserStatus.ACTIVE, UserRole.USER);

        assertNotNull(result);
        assertTrue(result.data().isEmpty());
    }

    @Test
    void shouldFindById() {
        when(findUserPort.findById(any(UUID.class))).thenReturn(Optional.of(user));

        final var result = useCase.findById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals(cpf, result.getCpf());
        assertEquals(email, result.getEmail());
    }

    @Test
    void shouldThrowNotFoundWhenFindById() {
        when(findUserPort.findById(any(UUID.class))).thenReturn(Optional.empty());

        final var result = assertThrows(NotFoundException.class, () -> useCase.findById(userId));

        assertNotNull(result);
    }

    @Test
    void shouldFindByEmail() {
        when(findUserPort.findByEmail(anyString())).thenReturn(Optional.of(user));

        final var result = useCase.findByEmail(email);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals(cpf, result.getCpf());
        assertEquals(email, result.getEmail());
    }

    @Test
    void shouldThrowNotFoundWhenFindByEmail() {
        when(findUserPort.findByEmail(anyString())).thenReturn(Optional.empty());

        final var result = assertThrows(NotFoundException.class, () -> useCase.findByEmail(email));

        assertNotNull(result);
    }
}
