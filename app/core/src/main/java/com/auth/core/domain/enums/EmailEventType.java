package com.auth.core.domain.enums;

import lombok.Getter;

@Getter
public enum EmailEventType {

    USER_PENDING_CREATED("UserPendingCreated");

    private final String value;

    EmailEventType(String value) {
        this.value = value;
    }

    public static EmailEventType fromValue(final String value) {
        for (final var eventType: EmailEventType.values())
            if (eventType.getValue().equalsIgnoreCase(value) || eventType.name().equalsIgnoreCase(value))
                return eventType;
        return null;
    }
}
