package com.auth.user.usecases;

import com.auth.core.domain.User;
import com.auth.core.domain.enums.UserStatus;
import com.auth.core.exceptions.ForbiddenException;
import com.auth.core.exceptions.NotFoundException;
import com.auth.core.ports.inbound.auth.PasswordEncoderPort;
import com.auth.core.ports.inbound.user.UserUseCasePort;
import com.auth.core.ports.outbound.auth.ConfirmationEmailSenderPort;
import com.auth.core.ports.outbound.user.FindUserPort;
import com.auth.core.ports.outbound.user.SaveUserPort;
import com.auth.core.shared.Constants;
import com.auth.core.shared.Errors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserUseCase implements UserUseCasePort {

    private static final String LOG_CODE = "USER-USE-CASE";

    private final FindUserPort findUserPort;

    private final SaveUserPort saveUserPort;

    private final PasswordEncoderPort passwordEncoderPort;

    private final ConfirmationEmailSenderPort confirmationEmailSenderPort;

    public UserUseCase(final FindUserPort findUserPort,
                       final SaveUserPort saveUserPort,
                       final PasswordEncoderPort passwordEncoderPort,
                       final ConfirmationEmailSenderPort confirmationEmailSenderPort) {
        this.findUserPort = findUserPort;
        this.saveUserPort = saveUserPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.confirmationEmailSenderPort = confirmationEmailSenderPort;
    }

    @Override
    public User findById(final UUID userId) {
        final User user = findUserPort.findById(userId).orElseThrow(NotFoundException::new);

        log.info(String.format("%s: User found by id: %s", LOG_CODE, user.getId()), user);
        return user;
    }

    @Override
    public User findByEmail(final String email) {
        return findUserPort.findByEmail(email).orElseThrow(NotFoundException::new);
    }

    @Override
    @Transactional(propagation = Propagation.NESTED, rollbackFor = {RuntimeException.class, Exception.class})
    public User save(final User user) {
        User processed;

        if (user.getId() == null) {
            processed = create(user);
        } else {
            processed = update(user);
        }

        return processed;
    }

    @Override
    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    public User activate(final String email, final UUID userId) {
        final User user = findUserPort.findByEmail(email).orElseThrow(NotFoundException::new);

        if (!user.getId().equals(userId)) {
            throw new ForbiddenException();
        }

        if (user.getStatus().getCode() == UserStatus.ACTIVE.getCode()) {
            throw new RuntimeException(Errors.USER_ALREADY_ACTIVE.getMsg());
        }

        user.setStatus(UserStatus.ACTIVE);

        return saveUserPort.save(user);
    }

    @Override
    public User inactivate(final String email) {
        final User user = findUserPort.findByEmail(email).orElseThrow(NotFoundException::new);

        user.setStatus(UserStatus.INACTIVE);
        user.setInactivatedAt(LocalDateTime.now(Constants.CLOCK));

        return saveUserPort.save(user);
    }

    @Override
    public void resendEmail(final String email) {
        findUserPort.findByEmail(email).ifPresentOrElse(u -> {
            if (u.getStatus().getCode() == UserStatus.ACTIVE.getCode()) {
                throw new RuntimeException(Errors.USER_ALREADY_ACTIVE.getMsg());
            }

            confirmationEmailSenderPort.send(u);
        }, NotFoundException::new);
    }

    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    private User create(final User user) {
        final LocalDateTime moment = LocalDateTime.now(Constants.CLOCK);
        final Optional<User> existingUser = findUserPort.findUserByCpf(user.getCpf());

        if (existingUser.isPresent()) {
            log.info(String.format("%s: user already exists with cpf %s", LOG_CODE, user.getCpf()));
            throw new RuntimeException(Errors.USER_ALREADY_EXISTS.getMsg());
        }

        user.setPassword(passwordEncoderPort.encode(user.getPassword()));

        final User newUser = saveUserPort.save(User.newUser(user, moment));

        confirmationEmailSenderPort.send(newUser);

        return newUser;
    }

    @Transactional(rollbackFor = {NotFoundException.class, Exception.class})
    private User update(final User user) {
        final User existingUser = findUserPort.findById(user.getId()).orElseThrow(NotFoundException::new);
        final LocalDateTime moment = LocalDateTime.now(Constants.CLOCK);

        return saveUserPort.save(existingUser.update(user, moment));
    }
}
