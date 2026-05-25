package com.auth.core.exceptions;

import com.auth.core.shared.Errors;

import java.io.Serializable;

public class DomainException extends RuntimeException implements Serializable {

    public DomainException() {}

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }

    public DomainException(Throwable cause) {
        super(cause);
    }

    public DomainException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public DomainException(Errors errors) {
        super(errors.getMsg());
    }

    public DomainException(Errors errors, Throwable cause) {
        super(errors.getMsg(), cause);
    }

    public DomainException(Errors errors, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(errors.getMsg(), cause, enableSuppression, writableStackTrace);
    }
}
