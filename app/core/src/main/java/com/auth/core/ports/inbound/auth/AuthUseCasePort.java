package com.auth.core.ports.inbound.auth;

import com.auth.core.domain.AuthLogin;

public interface AuthUseCasePort {

    AuthLogin login(String email, String password);
}
