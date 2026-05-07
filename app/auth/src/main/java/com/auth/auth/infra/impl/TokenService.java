package com.auth.auth.infra.impl;

import com.auth.auth.infra.util.PemUtils;
import com.auth.core.domain.User;
import com.auth.core.domain.enums.UserRole;
import com.auth.core.exceptions.ForbiddenException;
import com.auth.core.ports.inbound.auth.TokenPort;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
public class TokenService implements TokenPort {

    private final Algorithm algorithm;

    @Getter
    private final RSAPublicKey publicKey;

    @Getter
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
    public String generateAccessToken(final User user) {
        try {
            return JWT.create()
                .withKeyId(kid)
                .withIssuer(issuer)
                .withSubject(user.getId().toString())
                .withClaim("email", user.getEmail())
                .withClaim("cpf", user.getCpf())
                .withClaim("scope", buildScope(user))
                .withClaim("typ", "access")
                .withExpiresAt(accessExpiration())
                .sign(algorithm);
        } catch (IllegalArgumentException | JWTCreationException e) {
            return null;
        }
    }

    public String generateRefreshToken(final User user) {
        try {
            return JWT.create()
                    .withKeyId(kid)
                    .withIssuer(issuer)
                    .withSubject(user.getId().toString())
                    .withClaim("typ", "refresh")
                    .withExpiresAt(refreshExpiration())
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
    public Object getClaim(final String token, final String claim, final Class clazz) throws ForbiddenException {
        try {
            final DecodedJWT d = JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(token);

            return Optional.ofNullable(d.getClaim(claim))
                    .map(c -> c.as(clazz))
                    .orElse(null);
        } catch (JWTVerificationException e) {
            throw new ForbiddenException("");
        }
    }

    @Override
    public String generateEmailConfirmationToken(final User user) {
        try {
            return JWT.create()
                .withKeyId(kid)
                .withIssuer(issuer)
                .withSubject(user.getId().toString())
                .withClaim("email", user.getEmail())
                .withClaim("scope", buildScope(user))
                .withExpiresAt(emailExpiration())
                .sign(algorithm);
        } catch (IllegalArgumentException | JWTCreationException e) {
            return null;
        }
    }

    private String buildScope(final User user) {
        if (UserRole.ADMIN.equals(user.getUserRole())) {
            return "users.read users.write users.admin";
        } else {
            return "users.read users.write users.user";
        }
    }

    public DecodedJWT validateAccessToken(final String token) throws ForbiddenException {
        return validateTokenByType(token, "access");
    }

    public DecodedJWT validateRefreshToken(final String token) throws ForbiddenException {
        return validateTokenByType(token, "refresh");
    }

    private DecodedJWT validateTokenByType(final String token, final String expectedType) throws ForbiddenException {
        try {
            return JWT.require(algorithm)
                .withIssuer(issuer)
                .withClaim("typ", expectedType)
                .build()
                .verify(token);
        } catch (JWTVerificationException e) {
            throw new ForbiddenException("");
        }
    }

    private Instant accessExpiration() {
        return LocalDateTime.now()
                .plusMinutes(15)
                .toInstant(ZoneOffset.of("-03:00"));
    }

    private Instant refreshExpiration() {
        return LocalDateTime.now()
                .plusDays(10)
                .toInstant(ZoneOffset.of("-03:00"));
    }

    private Instant emailExpiration() {
        return LocalDateTime.now()
                .plusMinutes(30)
                .toInstant(ZoneOffset.of("-03:00"));
    }
}
