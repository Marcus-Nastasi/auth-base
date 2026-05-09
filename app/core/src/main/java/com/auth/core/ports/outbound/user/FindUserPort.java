package com.auth.core.ports.outbound.user;

import com.auth.core.domain.User;
import com.auth.core.domain.enums.UserRole;
import com.auth.core.domain.enums.UserStatus;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface FindUserPort {

    Set<User> findAll(int page,
                      int size,
                      String email,
                      String cpf,
                      String firstName,
                      String lastName,
                      LocalDate birthDate,
                      UserStatus status,
                      UserRole userRole);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    Optional<User> findUserByCpf(String cpf);
}
