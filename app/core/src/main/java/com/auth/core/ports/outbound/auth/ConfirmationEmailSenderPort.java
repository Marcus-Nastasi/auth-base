package com.auth.core.ports.outbound.auth;

import com.auth.core.domain.User;

@FunctionalInterface
public interface ConfirmationEmailSenderPort {

    void send(User user);
}
