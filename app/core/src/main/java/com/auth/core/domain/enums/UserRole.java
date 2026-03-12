package com.auth.core.domain.enums;

import com.auth.core.shared.Errors;

public enum UserRole {

    ADMIN("admin"),
    USER("user");

    private final String role;

    UserRole(final String role) {
        this.role = role;
    }

    public static UserRole fromString(final String role) {
        for (UserRole value: UserRole.values()) {
            if (value.getRole().equalsIgnoreCase(role)) {
                return value;
            }
        }
        throw new IllegalArgumentException(Errors.ROLE_NOT_FOUND.getMsg());
    }

    public String getRole() {
        return role;
    }
}
