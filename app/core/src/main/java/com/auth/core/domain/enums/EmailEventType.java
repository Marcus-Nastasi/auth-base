package com.auth.core.domain.enums;

public enum EmailEventType {

    USER_PENDING_CREATED("UserPendingCreated");

    private final String value;

    EmailEventType(String value) {
        this.value = value;
    }

    public static EmailEventType fromString(String value) {
        for (EmailEventType eventType: EmailEventType.values()) {
            if (eventType.getValue().equalsIgnoreCase(value)) {
                return eventType;
            }
        }
        throw new RuntimeException("");
    }

    public String getValue() {
        return value;
    }
}
