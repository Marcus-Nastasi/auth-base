package com.auth.user.helpers;

import com.auth.core.domain.User;
import com.auth.core.domain.enums.UserRole;
import com.auth.core.domain.enums.UserStatus;

import java.time.LocalDate;
import java.util.UUID;

public final class UserTestHelper {

    private UserTestHelper() {}

    public static User getUserDomain(final UUID id, final String cpf, final String email) {
        return User.builder()
                .id(id)
                .cpf(cpf)
                .email(email)
                .userRole(UserRole.ADMIN)
                .birthDate(LocalDate.of(2001, 10, 26))
                .createdAt(LocalDate.now().atStartOfDay())
                .firstName("Mark")
                .lastName("Oliveira")
                .status(UserStatus.ACTIVE)
                .password("xyz")
                .build();
    }
}
