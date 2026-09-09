package com.auth.core.domain;

import com.auth.core.helpers.UserTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

final class UserTests {

    private UUID id;
    private String cpf;
    private String email;
    private User user;

    @BeforeEach
    void setUp() {
        user = UserTestHelper.getUserDomain(id, cpf, email);
    }

    @Test
    void shouldUseNoArgsConstructor() {
        assertNotNull(assertDoesNotThrow(() -> new User()));
    }

    @Test
    void shouldCreateUser() {
        final var resp = assertDoesNotThrow(() -> User.newUser(user, LocalDateTime.now()));

        assertNotNull(resp);
        assertEquals(user.getCpf(), resp.getCpf());
        assertEquals(user.getEmail(), resp.getEmail());
    }

    @Test
    void shouldUpdateUser() {
        final var user2 = UserTestHelper.getUserDomain(user.getId(), "30550400079", "fake@gmail.com");

        final var resp = assertDoesNotThrow(() -> user.update(user2, LocalDateTime.now()));

        assertNotNull(resp);
        assertEquals(user2.getCpf(), user.getCpf());
        assertEquals(user2.getEmail(), user.getEmail());
    }
}
