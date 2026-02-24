package com.auth.auth.usecases;

import com.auth.core.domain.User;
import com.auth.core.exceptions.ForbiddenException;
import com.auth.core.exceptions.NotFoundException;
import com.auth.core.ports.inbound.auth.AuthUseCasePort;
import com.auth.core.ports.inbound.auth.PasswordEncoderPort;
import com.auth.core.ports.inbound.auth.TokenPort;
import com.auth.core.ports.outbound.user.FindUserPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
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
    public String login(final String email, final String password) {
        final User user = findUserPort.findUserByCpf(email).orElseThrow(NotFoundException::new);

        isPasswordEqual(password, user);

        return tokenPort.generate(user);
    }

    private void isPasswordEqual(final String password, final User user) {
        if (Boolean.FALSE.equals(passwordEncoderPort.matches(password, user.getPassword()))) {
            throw new ForbiddenException();
        }
    }
}
