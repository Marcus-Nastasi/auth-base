package com.auth.core.exceptions;

import com.auth.core.shared.Errors;

import java.io.Serializable;

public class InternalException extends DomainException implements Serializable {

    public InternalException(String message) {
        super(message);
    }

    public InternalException(String message, Throwable cause) {
        super(message, cause);
    }

    public InternalException(Throwable cause) {
        super(cause);
    }

    public InternalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public InternalException(Errors errors) {
        super(errors.getMsg());
    }

    public InternalException(Errors errors, Throwable cause) {
        super(errors.getMsg(), cause);
    }

    public InternalException(Errors errors, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(errors.getMsg(), cause, enableSuppression, writableStackTrace);
    }
}
