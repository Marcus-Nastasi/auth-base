package com.auth.user.usecases;

import com.auth.core.domain.User;
import com.auth.core.domain.enums.UserRole;
import com.auth.core.domain.enums.UserStatus;
import com.auth.core.exceptions.ForbiddenException;
import com.auth.core.exceptions.InternalException;
import com.auth.core.exceptions.NotFoundException;
import com.auth.core.ports.inbound.auth.PasswordEncoderPort;
import com.auth.core.ports.outbound.auth.ConfirmationEmailSenderPort;
import com.auth.core.ports.outbound.user.FindUserPort;
import com.auth.core.ports.outbound.user.SaveUserPort;
import com.auth.core.shared.Errors;
import com.auth.user.adapters.inbound.exceptions.UnprocessableEntityException;
import com.auth.user.helpers.UserTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.Mockito.*;

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

    @Nested
    final class FindAll {

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

            verify(findUserPort, times(1)).findAll(anyInt(), anyInt(), any(), any(), any(), any(), any(), any(), any());
        }


        @Test
        void shouldReturnPageResponseEmpty() {
            when(findUserPort.findAll(anyInt(), anyInt(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(null);

            final var result = useCase.findAll(1, 10, "email@email.com", "12345677890", "Jef", "Jones", LocalDate.of(2000, 4, 10), UserStatus.ACTIVE, UserRole.USER);

            assertNotNull(result);
            assertTrue(result.data().isEmpty());

            verify(findUserPort, times(1)).findAll(anyInt(), anyInt(), any(), any(), any(), any(), any(), any(), any());
        }
    }

    @Nested
    final class FindById {

        @Test
        void shouldFindById() {
            when(findUserPort.findById(any(UUID.class))).thenReturn(Optional.of(user));

            final var result = useCase.findById(userId);

            assertNotNull(result);
            assertEquals(userId, result.getId());
            assertEquals(cpf, result.getCpf());
            assertEquals(email, result.getEmail());

            verify(findUserPort, times(1)).findById(any(UUID.class));
        }

        @Test
        void shouldThrowNotFoundWhenFindById() {
            when(findUserPort.findById(any(UUID.class))).thenReturn(Optional.empty());

            final var result = assertThrows(NotFoundException.class, () -> useCase.findById(userId));

            assertNotNull(result);

            verify(findUserPort, times(1)).findById(any(UUID.class));
        }
    }

    @Nested
    final class FindByEmail {

        @Test
        void shouldFindByEmail() {
            when(findUserPort.findByEmail(anyString())).thenReturn(Optional.of(user));

            final var result = useCase.findByEmail(email);

            assertNotNull(result);
            assertEquals(userId, result.getId());
            assertEquals(cpf, result.getCpf());
            assertEquals(email, result.getEmail());

            verify(findUserPort, times(1)).findByEmail(anyString());
        }

        @Test
        void shouldThrowNotFoundWhenFindByEmail() {
            when(findUserPort.findByEmail(anyString())).thenReturn(Optional.empty());

            final var result = assertThrows(NotFoundException.class, () -> useCase.findByEmail(email));

            assertNotNull(result);

            verify(findUserPort, times(1)).findByEmail(anyString());
        }
    }

    @Nested
    final class Activate {

        @Test
        void shouldReturnNullWhenEmailOrUserIdEmpty() {
            final var resp = assertDoesNotThrow(() -> useCase.activate(null, null));
            assertNull(resp);
            verify(findUserPort, times(0)).findByEmail(anyString());
            verify(saveUserPort, times(0)).save(any(User.class));
        }

        @Test
        void shouldThrowWhenUserNotFound() {
            when(findUserPort.findByEmail(anyString())).thenReturn(Optional.empty());

            final var resp = assertThrows(NotFoundException.class, () -> useCase.activate(email, userId));

            assertNotNull(resp);

            verify(findUserPort, times(1)).findByEmail(anyString());
            verify(saveUserPort, times(0)).save(any(User.class));
        }

        @Test
        void shouldThrowWhenUserIdDiff() {
            when(findUserPort.findByEmail(anyString())).thenReturn(Optional.of(User.builder().id(UUID.randomUUID()).build()));

            final var resp = assertThrows(ForbiddenException.class, () -> useCase.activate(email, userId));

            assertNotNull(resp);

            verify(findUserPort, times(1)).findByEmail(anyString());
            verify(saveUserPort, times(0)).save(any(User.class));
        }

        @Test
        void shouldThrowWhenUserAlreadyActive() {
            when(findUserPort.findByEmail(anyString()))
                    .thenReturn(Optional.of(User.builder().id(userId).status(UserStatus.ACTIVE).build()));

            final var resp = assertThrows(UnprocessableEntityException.class, () -> useCase.activate(email, userId));

            assertNotNull(resp);
            assertEquals(Errors.USER_ALREADY_ACTIVE.getMsg(), resp.getMessage());

            verify(findUserPort, times(1)).findByEmail(anyString());
            verify(saveUserPort, times(0)).save(any(User.class));
        }

        @Test
        void shouldActivate() {
            user.setStatus(UserStatus.PENDING);

            when(findUserPort.findByEmail(anyString())).thenReturn(Optional.of(user));
            when(saveUserPort.save(any(User.class))).thenReturn(user);

            final var resp = assertDoesNotThrow(() -> useCase.activate(email, userId));

            assertNotNull(resp);
            assertEquals(UserStatus.ACTIVE, resp.getStatus());

            verify(findUserPort, times(1)).findByEmail(anyString());
            verify(saveUserPort, times(1)).save(any(User.class));
        }
    }

    @Nested
    final class Inactivate {

        @Test
        void shouldThrowWhenUserNotFound() {
            when(findUserPort.findByEmail(anyString())).thenReturn(Optional.empty());

            final var resp = assertThrows(NotFoundException.class, () -> useCase.inactivate(email));

            assertNotNull(resp);

            verify(findUserPort, times(1)).findByEmail(anyString());
            verify(saveUserPort, times(0)).save(any(User.class));
        }

        @Test
        void shouldInactivate() {
            user.setStatus(UserStatus.ACTIVE);

            when(findUserPort.findByEmail(anyString())).thenReturn(Optional.of(user));
            when(saveUserPort.save(any(User.class))).thenReturn(user);

            final var resp = assertDoesNotThrow(() -> useCase.inactivate(email));

            assertNotNull(resp);
            assertEquals(UserStatus.INACTIVE, resp.getStatus());

            verify(findUserPort, times(1)).findByEmail(anyString());
            verify(saveUserPort, times(1)).save(any(User.class));
        }
    }

    @Nested
    final class ResendEmail {

        @Test
        void shouldThrowWhenUserNotFound() {
            when(findUserPort.findByEmail(anyString())).thenReturn(Optional.empty());

            final var resp = assertThrows(NotFoundException.class, () -> useCase.resendEmail(email));

            assertNotNull(resp);

            verify(findUserPort, times(1)).findByEmail(anyString());
            verify(confirmationEmailSenderPort, times(0)).send(any(User.class));
        }

        @Test
        void shouldThrowWhenUserActive() {
            user.setStatus(UserStatus.ACTIVE);

            when(findUserPort.findByEmail(anyString())).thenReturn(Optional.of(user));

            final var resp = assertThrows(UnprocessableEntityException.class, () -> useCase.resendEmail(email));

            assertNotNull(resp);
            assertEquals(Errors.USER_ALREADY_ACTIVE.getMsg(), resp.getMessage());

            verify(findUserPort, times(1)).findByEmail(anyString());
            verify(confirmationEmailSenderPort, times(0)).send(any(User.class));
        }

        @Test
        void shouldResend() {
            user.setStatus(UserStatus.PENDING);

            when(findUserPort.findByEmail(anyString())).thenReturn(Optional.of(user));

            assertDoesNotThrow(() -> useCase.resendEmail(email));

            verify(findUserPort, times(1)).findByEmail(anyString());
            verify(confirmationEmailSenderPort, times(1)).send(any(User.class));
        }
    }

    @Nested
    final class Create {

        @BeforeEach
        void setUp() {
            user.setId(null);
        }

        @Test
        void shouldThrowWhenUserNull() {
            final var resp = assertThrows(UnprocessableEntityException.class, () -> useCase.save(null));

            assertNotNull(resp);
            assertEquals(Errors.USER_NULL.getMsg(), resp.getMessage());

            verify(findUserPort, times(0)).findUserByCpf(anyString());
        }

        @Test
        void shouldThrowWhenUserExists() {
            when(findUserPort.findUserByCpf(anyString())).thenReturn(Optional.of(User.builder().build()));

            final var resp = assertThrows(UnprocessableEntityException.class, () -> useCase.save(user));

            assertNotNull(resp);
            assertEquals(Errors.USER_ALREADY_EXISTS.getMsg(), resp.getMessage());

            verify(findUserPort, times(1)).findUserByCpf(anyString());
        }

        @Test
        void shouldSave() {
            when(findUserPort.findUserByCpf(anyString())).thenReturn(Optional.empty());
            when(passwordEncoderPort.encode(anyString())).thenReturn("12345678");
            when(saveUserPort.save(any(User.class))).thenReturn(user);
            doNothing().when(confirmationEmailSenderPort).send(any(User.class));

            final var resp = assertDoesNotThrow(() -> useCase.save(user));

            assertNotNull(resp);

            verify(findUserPort, times(1)).findUserByCpf(anyString());
            verify(passwordEncoderPort, times(1)).encode(anyString());
            verify(saveUserPort, times(1)).save(any(User.class));
        }
    }

    @Nested
    final class Update {

        @Test
        void shouldThrowWhenUserNotFound() {
            final var resp = assertThrows(NotFoundException.class, () -> useCase.save(user));

            assertNotNull(resp);

            verify(findUserPort, times(1)).findById(any(UUID.class));
            verify(saveUserPort, times(0)).save(any(User.class));
        }

        @Test
        void shouldThrowWhenErrorToSave() {
            when(findUserPort.findById(userId)).thenReturn(Optional.of(user));
            when(saveUserPort.save(any(User.class))).thenReturn(null);

            final var resp = assertThrows(InternalException.class, () -> useCase.save(user));

            assertNotNull(resp);

            verify(findUserPort, times(1)).findById(any(UUID.class));
            verify(saveUserPort, times(1)).save(any(User.class));
        }

        @Test
        void shouldSave() {
            when(findUserPort.findById(userId)).thenReturn(Optional.of(user));
            when(saveUserPort.save(any(User.class))).thenReturn(user);

            final var resp = assertDoesNotThrow(() -> useCase.save(user));

            assertNotNull(resp);

            verify(findUserPort, times(1)).findById(any(UUID.class));
            verify(saveUserPort, times(1)).save(any(User.class));
        }
    }
}
