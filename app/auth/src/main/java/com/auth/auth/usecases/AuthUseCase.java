package com.auth.auth.usecases;

import com.auth.core.domain.AuthLogin;
import com.auth.core.domain.User;
import com.auth.core.exceptions.ForbiddenException;
import com.auth.core.exceptions.NotFoundException;
import com.auth.core.ports.inbound.auth.AuthUseCasePort;
import com.auth.core.ports.inbound.auth.PasswordEncoderPort;
import com.auth.core.ports.inbound.auth.TokenPort;
import com.auth.core.ports.outbound.user.FindUserPort;
import com.auth.core.shared.Logger;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

@Component
public class AuthUseCase implements AuthUseCasePort {

    private static final String LOG_CODE = "AUTH_USE_CASE";

    private final FindUserPort findUserPort;

    private final TokenPort tokenPort;

    private final PasswordEncoderPort passwordEncoderPort;

    public AuthUseCase(final FindUserPort findUserPort,
                       final TokenPort tokenPort,
                       final PasswordEncoderPort passwordEncoderPort) {
        this.findUserPort = findUserPort;
        this.tokenPort = tokenPort;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    @Override
    public AuthLogin login(final String email, final String password) {
        Logger.info(LOG_CODE, format("Payload received: %s", email));

        final User user = findUserPort.findUserByCpf(email).orElseThrow(NotFoundException::new);

        Logger.info(LOG_CODE, format("User found: %s", user.getId()), user);

        isPasswordEqual(password, user);
        Logger.info(LOG_CODE, "Generating token");

        final String accessToken = tokenPort.generateAccessToken(user);
        final String refreshToken = tokenPort.generateRefreshToken(user);
        final String scopes = (String) tokenPort.getClaim(accessToken, "scope", String.class);

        return AuthLogin.builder()
                .token(accessToken)
                .refresh(refreshToken)
                .scope(scopes)
                .build();
    }

    private void isPasswordEqual(final String password, final User user) {
        if (Boolean.FALSE.equals(passwordEncoderPort.matches(password, user.getPassword()))) {
            throw new ForbiddenException("");
        }
    }
}
