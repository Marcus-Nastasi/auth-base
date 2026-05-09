package com.auth.core.ports.inbound.user;

import com.auth.core.domain.PageResponse;
import com.auth.core.domain.User;
import com.auth.core.domain.enums.UserRole;
import com.auth.core.domain.enums.UserStatus;

import java.time.LocalDate;
import java.util.UUID;

public interface UserUseCasePort {

    PageResponse<User> findAll(int page,
                               int size,
                               String email,
                               String cpf,
                               String firstName,
                               String lastName,
                               LocalDate birthDate,
                               UserStatus status,
                               UserRole userRole);

    User findById(UUID userId);

    User findByEmail(String email);

    User save(User user);

    User activate(String email, UUID userId);

    User inactivate(String email);

    void resendEmail(String email);
}
