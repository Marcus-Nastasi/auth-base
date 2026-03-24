package com.auth.user.usecases;

import com.auth.core.domain.PageResponse;
import com.auth.core.domain.User;
import com.auth.core.domain.enums.UserRole;
import com.auth.core.domain.enums.UserStatus;
import com.auth.core.exceptions.NotFoundException;
import com.auth.user.adapters.inbound.exceptions.UnprocessableEntityException;
import com.auth.core.ports.inbound.auth.PasswordEncoderPort;
import com.auth.core.ports.inbound.user.UserUseCasePort;
import com.auth.core.ports.outbound.auth.ConfirmationEmailSenderPort;
import com.auth.core.ports.outbound.user.FindUserPort;
import com.auth.core.ports.outbound.user.SaveUserPort;
import com.auth.core.shared.Constants;
import com.auth.core.shared.Errors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static java.lang.String.format;

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
    @Transactional(readOnly = true)
    public PageResponse<User> findAll(int page, int size) {
        log.info("Searching all users");

        page = page > 0 ? page : 1;
        size = size > 0 ? size : 10;

        if (size > 50) size = 50;

        final Set<User> users = findUserPort.findAll(page, size);

        if (users == null || users.isEmpty())
            return new PageResponse<>(page, size, 0, HashSet.newHashSet(0));

        return new PageResponse<>(page, size, (page + 1), users);
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(final UUID userId) throws NotFoundException {
        log.info(format("Searching user by id: %s", userId));

        final User user = findUserPort.findById(userId).orElseThrow(NotFoundException::new);

        log.info(format("User found by id: %s", user));

        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(final String email) throws NotFoundException {
        log.info(format("Searching user by email: %s", email));

        return findUserPort.findByEmail(email).orElseThrow(NotFoundException::new);
    }

    @Override
    @Transactional(
        propagation = Propagation.NESTED,
        rollbackFor = {RuntimeException.class, Exception.class}
    )
    public User save(final User user) {
        log.info("User save payload received");

        User processed;

        if (user.getId() == null) {
            log.info("Creating user");
            processed = create(user);
        } else {
            log.info("Updating user");
            processed = update(user);
        }

        return processed;
    }

    @Override
    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    public User activate(final String email, final UUID userId) throws NotFoundException, UnprocessableEntityException {
        log.info(format("Activating user: %s", email));

        final User user = findUserPort.findByEmail(email).orElseThrow(NotFoundException::new);
        log.info(format("User found: %s", user));

//        if (!user.getId().equals(userId)) {
//            log.warn("User found id is different than passed user id");
//            throw new ForbiddenException();
//        }

        if (user.getStatus().getCode() == UserStatus.ACTIVE.getCode()) {
            log.warn("User is already active");
            throw new UnprocessableEntityException(Errors.USER_ALREADY_ACTIVE);
        }

        user.setStatus(UserStatus.ACTIVE);

        log.info("Updating user's status to: [ACTIVE]");

        return saveUserPort.save(user);
    }

    @Override
    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    public User inactivate(final String email) throws NotFoundException {
        log.info(format("Inactivating user: %s", email));

        final User user = findUserPort.findByEmail(email).orElseThrow(NotFoundException::new);
        log.info(format("User found: %s", user));

        user.setStatus(UserStatus.INACTIVE);
        log.info("Setting the UserStatus to INACTIVE");

        user.setInactivatedAt(LocalDateTime.now(Constants.CLOCK));

        return saveUserPort.save(user);
    }

    @Override
    public void resendEmail(final String email) throws UnprocessableEntityException, NotFoundException {
        log.info(format("Resending email: %s", email));

        findUserPort.findByEmail(email).ifPresentOrElse(u -> {
            log.info(format("User is present: %s", u));

            if (u.getStatus().getCode() == UserStatus.ACTIVE.getCode()) {
                log.warn("User already active");
                throw new UnprocessableEntityException(Errors.USER_ALREADY_ACTIVE);
            }

            log.info("Sending confirmation e-mail");
            confirmationEmailSenderPort.send(u);
        }, NotFoundException::new);
    }

    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    private User create(final User user) throws UnprocessableEntityException {
        if (user == null) throw new UnprocessableEntityException(Errors.COULD_NOT_SAVE_USER);

        if (user.getUserRole() == null) user.setUserRole(UserRole.USER);

        log.info(format("Creating user: %s", user));

        final LocalDateTime moment = LocalDateTime.now(Constants.CLOCK);
        final Optional<User> existingUser = findUserPort.findUserByCpf(user.getCpf());

        if (existingUser.isPresent()) {
            log.info(format("%s: user already exists with cpf %s", LOG_CODE, user.getCpf()));
            throw new UnprocessableEntityException(Errors.USER_ALREADY_EXISTS);
        }

        user.setPassword(passwordEncoderPort.encode(user.getPassword()));

        log.info(format("Saving User: %s", user));
        final User newUser = saveUserPort.save(User.newUser(user, moment));

        log.info("Sending confirmation e-mail");
        confirmationEmailSenderPort.send(newUser);

        return newUser;
    }

    @Transactional(rollbackFor = {NotFoundException.class, Exception.class})
    private User update(final User user) {
        log.info(format("Updating User: %s", user));

        final User existingUser = findUserPort.findById(user.getId()).orElseThrow(NotFoundException::new);
        log.info(format("User found: %s", existingUser));

        final LocalDateTime moment = LocalDateTime.now(Constants.CLOCK);

        return saveUserPort.save(existingUser.update(user, moment));
    }
}
