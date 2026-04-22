package com.auth.auth.infra.impl;

import com.auth.auth.infra.util.PemUtils;
import com.auth.core.domain.User;
import com.auth.core.exceptions.ForbiddenException;
import com.auth.core.ports.inbound.auth.TokenPort;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService implements TokenPort {

    private final Algorithm algorithm;

    private final RSAPublicKey publicKey;

    @Value("${spring.security.oauth2.kid}")
    private String kid;

    @Value("${spring.security.oauth2.issuer}")
    private String issuer;

    public TokenService(@Value("${spring.security.key.public.path}") String publicKeyPath,
                        @Value("${spring.security.key.private.path}") String privateKeyPath) throws RuntimeException {
        try {
            this.publicKey = PemUtils.readPublicKey(publicKeyPath);
            final RSAPrivateKey privateKey = PemUtils.readPrivateKey(privateKeyPath);

            this.algorithm = Algorithm.RSA256(publicKey, privateKey);
        } catch (final Exception e) {
            throw new RuntimeException("Unable to get private and public keys");
        }
    }

    @Override
    public String generate(final User user) {
        try {
            return JWT.create()
                .withKeyId(kid)
                .withIssuer(issuer)
                .withSubject(user.getId().toString())
                .withClaim("email", user.getEmail())
                .withClaim("cpf", user.getCpf())
                .withClaim("role", user.getUserRole().getRole())
                .withExpiresAt(exp())
                .sign(algorithm);
        } catch (IllegalArgumentException | JWTCreationException e) {
            return null;
        }
    }

    @Override
    public DecodedJWT validate(final String token) throws ForbiddenException {
        try {
            return JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(token);
        } catch (JWTVerificationException e) {
            throw new ForbiddenException("");
        }
    }

    @Override
    public String emailConfirmation(final User user) {
        try {
            return JWT.create()
                .withKeyId(kid)
                .withIssuer(issuer)
                .withSubject(user.getId().toString())
                .withClaim("email", user.getEmail())
                .withExpiresAt(emailExpiration())
                .sign(algorithm);
        } catch (IllegalArgumentException | JWTCreationException e) {
            return null;
        }
    }

    private Instant exp() {
        return LocalDateTime.now()
                .plusHours(10)
                .toInstant(ZoneOffset.of("-03:00"));
    }

    private Instant emailExpiration() {
        return LocalDateTime.now()
                .plusHours(1)
                .toInstant(ZoneOffset.of("-03:00"));
    }

    private Instant expRefresh() {
        return LocalDateTime.now()
                .plusDays(10)
                .toInstant(ZoneOffset.of("-03:00"));
    }

    public Object getPublicKey() {
        return publicKey;
    }

    public String getKid() {
        return kid;
    }
}
