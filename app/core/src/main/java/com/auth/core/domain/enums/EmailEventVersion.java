package com.auth.core.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmailEventVersion {

    V1("v1");

    private final String value;

    public static EmailEventVersion fromString(final String value) {
        for (final EmailEventVersion eventVersion: EmailEventVersion.values())
            if (eventVersion.getValue().equalsIgnoreCase(value)) return eventVersion;
        throw new RuntimeException("");
    }
}
