package com.auth.core.domain.enums;

public enum EmailEventVersion {

    V1("v1");

    private final String value;

    EmailEventVersion(final String value) {
        this.value = value;
    }

    public static EmailEventVersion fromString(final String value) {
        for (final EmailEventVersion eventVersion: EmailEventVersion.values()) {
            if (eventVersion.getValue().equalsIgnoreCase(value)) {
                return eventVersion;
            }
        }
        throw new RuntimeException("");
    }

    public String getValue() {
        return value;
    }
}
