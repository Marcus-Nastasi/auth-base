package com.auth.core.domain.enums;

import com.auth.core.shared.Errors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {

    ADMIN("ADMIN"),
    USER("USER");

    private final String role;

    public static UserRole fromString(final String role) {
        for (final UserRole value: UserRole.values())
            if (value.getRole().equalsIgnoreCase(role))
                return value;
        throw new IllegalArgumentException(Errors.ROLE_NOT_FOUND.getMsg());
    }
}
