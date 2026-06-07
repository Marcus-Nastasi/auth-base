package com.auth.core.domain.enums;

import com.auth.core.shared.Errors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum UserRole {

    ADMIN("ADMIN", Set.of("users.read", "users.write", "users.admin")),
    USER("USER", Set.of("users.read", "users.write", "users.user"));

    private final String role;
    private final Set<String> scopes;

    public static UserRole fromString(final String role) {
        for (final UserRole value: UserRole.values())
            if (value.getRole().equalsIgnoreCase(role))
                return value;
        throw new IllegalArgumentException(Errors.ROLE_NOT_FOUND.getMsg());
    }
}
