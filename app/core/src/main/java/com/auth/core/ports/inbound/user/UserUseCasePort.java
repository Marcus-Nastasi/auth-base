package com.auth.core.ports.inbound.user;

import com.auth.core.domain.User;

import java.util.UUID;

public interface UserUseCasePort {

    User findById(UUID userId);

    User findByEmail(String email);

    User save(User user);

    User activate(String email, UUID userId);

    User inactivate(String email);

    void resendEmail(String email);
}
