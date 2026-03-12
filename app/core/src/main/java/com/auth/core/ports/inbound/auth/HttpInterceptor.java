package com.auth.core.ports.inbound.auth;

import com.auth.core.exceptions.ForbiddenException;

@FunctionalInterface
public interface HttpInterceptor {

    void validate(final Object[] data) throws ForbiddenException;
}
