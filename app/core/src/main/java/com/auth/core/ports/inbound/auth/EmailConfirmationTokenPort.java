package com.auth.core.ports.inbound.auth;

import com.auth.core.domain.User;
import com.auth.core.exceptions.ForbiddenException;

public interface EmailConfirmationTokenPort {

   String generate(final User user) throws Exception;

   void validate(final String token) throws ForbiddenException;
}
