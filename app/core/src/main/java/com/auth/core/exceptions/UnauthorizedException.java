package com.auth.core.exceptions;

import com.auth.core.shared.Errors;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {}

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedException(Throwable cause) {
        super(cause);
    }

    public UnauthorizedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public UnauthorizedException(Errors errors) {
        super(errors.getMsg());
    }

    public UnauthorizedException(Errors errors, Throwable cause) {
        super(errors.getMsg(), cause);
    }

    public UnauthorizedException(Errors errors, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(errors.getMsg(), cause, enableSuppression, writableStackTrace);
    }
}
