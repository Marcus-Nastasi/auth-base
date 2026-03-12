package com.auth.core.ports.inbound.auth;

public interface AuthUseCasePort {

    String login(String email, String password);
}
