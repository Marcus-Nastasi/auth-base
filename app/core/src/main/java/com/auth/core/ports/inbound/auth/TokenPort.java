package com.auth.core.ports.inbound.auth;

import com.auth.core.domain.User;

public interface TokenPort {

    Object getPublicKey();

    String getKid();

    String generate(User user);

    Object validate(String s);

    String emailConfirmation(User user);
}
