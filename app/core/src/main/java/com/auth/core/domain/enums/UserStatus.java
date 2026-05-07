package com.auth.core.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {

    PENDING(0, "pending"), ACTIVE(1, "active"), INACTIVE(2, "inactive");

    private final int code;

    private final String status;

    public static UserStatus fromString(final String status) {
        for (final UserStatus value: UserStatus.values())
            if (value.getStatus().equalsIgnoreCase(status)) return value;
        throw new RuntimeException("status not found");
    }

    public static UserStatus fromCode(final int code) {
        for (final UserStatus value: UserStatus.values())
            if (value.getCode() == code) return value;
        throw new RuntimeException("status not found");
    }
}
