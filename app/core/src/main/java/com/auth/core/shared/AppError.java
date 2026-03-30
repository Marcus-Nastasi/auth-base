package com.auth.core.shared;

import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PUBLIC)
public final class AppError {

    private String message;

    private String field;

    private String attempted;

    public AppError(final String message, final String field, final String attempted) {
        this.message = message;
        this.field = field;
        this.attempted = attempted;
    }

    public String getMessage() {
        return message;
    }

    public String getField() {
        return field;
    }

    public String getAttempted() {
        return attempted;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setField(String field) {
        this.field = field;
    }

    public void setAttempted(String attempted) {
        this.attempted = attempted;
    }
}
