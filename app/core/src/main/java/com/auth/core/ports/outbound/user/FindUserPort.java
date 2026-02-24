package com.auth.core.ports.outbound.user;

import com.auth.core.domain.User;

import java.util.Optional;
import java.util.UUID;

public interface FindUserPort {

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    Optional<User> findUserByCpf(String cpf);
}
