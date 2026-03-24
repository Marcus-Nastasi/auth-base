package com.auth.core.exceptions;

import com.auth.core.shared.Errors;

public class NotFoundException extends RuntimeException {

    public NotFoundException() {}

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundException(Throwable cause) {
        super(cause);
    }

    public NotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public NotFoundException(Errors errors) {
        super(errors.getMsg());
    }

    public NotFoundException(Errors errors, Throwable cause) {
        super(errors.getMsg(), cause);
    }

    public NotFoundException(Errors errors, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(errors.getMsg(), cause, enableSuppression, writableStackTrace);
    }
}
