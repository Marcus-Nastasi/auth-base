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
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
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
        log.info(LOG_CODE, "Searching user by id: ", userId);

        final User user = findUserPort.findById(userId).orElseThrow(NotFoundException::new);

        log.info(LOG_CODE, "%s: User found by id: %s", user);

        return user;
    }

    @Override
    public User findByEmail(final String email) {
        log.info(LOG_CODE, "Searching user by email: ", email);

        return findUserPort.findByEmail(email).orElseThrow(NotFoundException::new);
    }

    @Override
    @Transactional(propagation = Propagation.NESTED, rollbackFor = {RuntimeException.class, Exception.class})
    public User save(final User user) {
        log.info(LOG_CODE, "User save payload received");

        User processed;

        if (user.getId() == null) {
            log.info(LOG_CODE, "Creating user");
            processed = create(user);
        } else {
            log.info(LOG_CODE, "Updating user");
            processed = update(user);
        }

        return processed;
    }

    @Override
    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    public User activate(final String email, final UUID userId) {
        log.info(LOG_CODE, "Activating user: ", email);

        final User user = findUserPort.findByEmail(email).orElseThrow(NotFoundException::new);
        log.info(LOG_CODE, "User found: ", user);

        if (!user.getId().equals(userId)) {
            log.warn(LOG_CODE, "User found id is different than passed user id");
            throw new ForbiddenException();
        }

        if (user.getStatus().getCode() == UserStatus.ACTIVE.getCode()) {
            log.warn(LOG_CODE, "User is already active");
            throw new RuntimeException(Errors.USER_ALREADY_ACTIVE.getMsg());
        }

        user.setStatus(UserStatus.ACTIVE);

        log.info(LOG_CODE, "Updating user's status to: [ACTIVE]");

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
