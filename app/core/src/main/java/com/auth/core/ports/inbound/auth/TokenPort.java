package com.auth.core.ports.inbound.auth;

import com.auth.core.domain.User;

public interface TokenPort {

    Object getClaim(String token, String claim, Class clazz);

    Object getPublicKey();

    String getKid();

    String generateAccessToken(User user);

    String generateRefreshToken(User user);

    String generateEmailConfirmationToken(User user);

    Object validate(String s);
}
