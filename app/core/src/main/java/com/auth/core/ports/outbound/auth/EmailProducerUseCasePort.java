package com.auth.core.ports.outbound.auth;

import com.auth.core.domain.User;

public interface EmailProducerUseCasePort {

    void send(User user);
}
