package com.auth.core.ports.inbound.auth;

public interface PasswordEncoderPort {

    String encode(String password);

    boolean matches(String raw, String encoded);
}
