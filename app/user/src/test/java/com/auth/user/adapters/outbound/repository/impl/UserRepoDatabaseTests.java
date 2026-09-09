package com.auth.user.adapters.outbound.repository.impl;

import com.auth.core.domain.User;
import com.auth.core.domain.enums.UserRole;
import com.auth.core.domain.enums.UserStatus;
import com.auth.user.adapters.outbound.mappers.UserEntityMapper;
import com.auth.user.adapters.outbound.repository.config.JpaDatabaseTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
final class UserRepoDatabaseTests extends JpaDatabaseTestBase {

    private final User user = User.builder()
        .email("dumb@gmail.com")
        .cpf("14408640042")
        .firstName("Bia")
        .lastName("Sanches")
        .status(UserStatus.ACTIVE)
        .userRole(UserRole.USER)
        .password("1234")
        .birthDate(LocalDate.of(1998, 10, 9))
        .createdAt(LocalDate.now().atStartOfDay())
        .build();

    private User mergedUser;

    private UserRepo userRepo;

    @BeforeEach
    void setUp() {
        userRepo = new UserRepo(userJpaRepo, entityManager);

        final var userEntity = UserEntityMapper.INSTANCE.toEntity(user);
        final var merged = entityManager.merge(userEntity);

        mergedUser = UserEntityMapper.INSTANCE.toDomain(merged);
    }

    @Test
    void shouldFindAll() {
        final var rawResp = assertDoesNotThrow(() ->
            userRepo.findAll(0, 10, null, null, null, null, null, null, null));

        assertNotNull(rawResp);

        final var resp = rawResp.iterator().next();

        assertNotNull(resp);
        assertEquals(mergedUser.getEmail(), resp.getEmail());
        assertEquals(mergedUser.getCpf(), resp.getCpf());
        assertEquals(mergedUser.getPassword(), resp.getPassword());
        assertEquals(mergedUser.getFirstName(), resp.getFirstName());
        assertEquals(mergedUser.getStatus(), resp.getStatus());
        assertEquals(mergedUser.getBirthDate(), resp.getBirthDate());
    }

    @Test
    void shouldFindById() {
        final var rawResp = assertDoesNotThrow(() -> userRepo.findById(mergedUser.getId()));

        assertNotNull(rawResp);

        final var resp = rawResp.get();

        assertNotNull(resp);
        assertEquals(mergedUser.getEmail(), resp.getEmail());
        assertEquals(mergedUser.getCpf(), resp.getCpf());
        assertEquals(mergedUser.getPassword(), resp.getPassword());
        assertEquals(mergedUser.getFirstName(), resp.getFirstName());
        assertEquals(mergedUser.getStatus(), resp.getStatus());
        assertEquals(mergedUser.getBirthDate(), resp.getBirthDate());
    }

    @Test
    void shouldFindByEmail() {
        final var rawResp = assertDoesNotThrow(() -> userRepo.findByEmail(mergedUser.getEmail()));

        assertNotNull(rawResp);

        final var resp = rawResp.get();

        assertNotNull(resp);
        assertEquals(mergedUser.getEmail(), resp.getEmail());
        assertEquals(mergedUser.getCpf(), resp.getCpf());
        assertEquals(mergedUser.getPassword(), resp.getPassword());
        assertEquals(mergedUser.getFirstName(), resp.getFirstName());
        assertEquals(mergedUser.getStatus(), resp.getStatus());
        assertEquals(mergedUser.getBirthDate(), resp.getBirthDate());
    }

    @Test
    void shouldFindByCpf() {
        final var rawResp = assertDoesNotThrow(() -> userRepo.findUserByCpf(mergedUser.getCpf()));

        assertNotNull(rawResp);

        final var resp = rawResp.get();

        assertNotNull(resp);
        assertEquals(mergedUser.getEmail(), resp.getEmail());
        assertEquals(mergedUser.getCpf(), resp.getCpf());
        assertEquals(mergedUser.getPassword(), resp.getPassword());
        assertEquals(mergedUser.getFirstName(), resp.getFirstName());
        assertEquals(mergedUser.getStatus(), resp.getStatus());
        assertEquals(mergedUser.getBirthDate(), resp.getBirthDate());
    }

    @Test
    void shouldSaveUser() {
        // delete already created user
        if (mergedUser != null && mergedUser.getId() != null) {
            userJpaRepo.deleteById(mergedUser.getId());

            final var resp = assertDoesNotThrow(() -> userRepo.findById(mergedUser.getId()));

            assertNotNull(resp);
            assertFalse(resp.isPresent());
        }

        final var resp = assertDoesNotThrow(() -> userRepo.save(user));

        assertNotNull(resp);
        assertEquals(user.getCpf(), resp.getCpf());
    }
}
