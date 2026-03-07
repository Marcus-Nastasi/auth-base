package com.auth.user.adapters.inbound.exceptions;

import com.auth.core.exceptions.DomainException;
import com.auth.core.shared.Errors;

public class UnprocessableEntityException extends DomainException {

    public UnprocessableEntityException(String message) {
        super(message);
    }

    public UnprocessableEntityException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnprocessableEntityException(Throwable cause) {
        super(cause);
    }

    public UnprocessableEntityException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public UnprocessableEntityException(Errors errors) {
        super(errors);
    }

    public UnprocessableEntityException(Errors errors, Throwable cause) {
        super(errors, cause);
    }

    public UnprocessableEntityException(Errors errors, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(errors, cause, enableSuppression, writableStackTrace);
    }
}
