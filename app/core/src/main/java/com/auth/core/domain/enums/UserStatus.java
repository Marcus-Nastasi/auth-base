package com.auth.core.domain.enums;

public enum UserStatus {

    PENDING(0, "pending"), ACTIVE(1, "active"), INACTIVE(2, "inactive");

    private final int code;

    private final String status;

    UserStatus(final int code, final String status) {
        this.code = code;
        this.status = status;
    }

    public static UserStatus fromString(final String status) {
        for (UserStatus value: UserStatus.values()) {
            if (value.getStatus().equalsIgnoreCase(status)) {
                return value;
            }
        }
        throw new RuntimeException("status not found");
    }

    public static UserStatus fromCode(final int code) {
        for (UserStatus value: UserStatus.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new RuntimeException("status not found");
    }

    public int getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }
}
