package com.auth.core.ports.outbound.user;

import com.auth.core.domain.User;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface FindUserPort {

    Set<User> findAll(int page, int size);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    Optional<User> findUserByCpf(String cpf);
}
