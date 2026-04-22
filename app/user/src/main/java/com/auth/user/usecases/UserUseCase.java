package com.auth.user.usecases;

import com.auth.core.domain.PageResponse;
import com.auth.core.domain.User;
import com.auth.core.domain.enums.UserRole;
import com.auth.core.domain.enums.UserStatus;
import com.auth.core.exceptions.InternalException;
import com.auth.core.exceptions.NotFoundException;
import com.auth.core.shared.Logger;
import com.auth.user.adapters.inbound.exceptions.UnprocessableEntityException;
import com.auth.core.ports.inbound.auth.PasswordEncoderPort;
import com.auth.core.ports.inbound.user.UserUseCasePort;
import com.auth.core.ports.outbound.auth.ConfirmationEmailSenderPort;
import com.auth.core.ports.outbound.user.FindUserPort;
import com.auth.core.ports.outbound.user.SaveUserPort;
import com.auth.core.shared.Constants;
import com.auth.core.shared.Errors;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static java.lang.String.format;

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
    @Cacheable(value = "get_users")
    public PageResponse<User> findAll(int page, int size) {
        Logger.info(LOG_CODE, "Searching users");

        page = page >= 0 ? page : 1;
        size = size > 0 ? size : 10;

        if (size > 50) size = 50;

        final Set<User> users = findUserPort.findAll(page, size);

        if (users == null || users.isEmpty()) {
            Logger.info(LOG_CODE, "Not found users on db, returning an empty PageResponse object");
            return new PageResponse<>(page, size, 0, null, Collections.emptySet());
        }

        Logger.info(LOG_CODE, String.format("Users found on page: %s and size: %s", page, size), users);

        return new PageResponse<>(page, size, (page + 1), null, users);
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(final UUID userId) throws NotFoundException {
        Logger.info(LOG_CODE, format("Searching user by id: %s", userId));

        final User user = findUserPort.findById(userId).orElseThrow(NotFoundException::new);

        Logger.info(LOG_CODE, format("User found for id: %s", user.getId()), user);

        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(final String email) throws NotFoundException {
        Logger.info(LOG_CODE, format("Searching user by email: %s", email));

        return findUserPort.findByEmail(email).orElseThrow(NotFoundException::new);
    }

    @Override
    @Transactional(
        propagation = Propagation.NESTED,
        rollbackFor = {RuntimeException.class, Exception.class}
    )
    public User save(final User user) {
        Logger.info(LOG_CODE, "User save payload received");

        User processed;

        if (user.getId() == null) {
            Logger.info(LOG_CODE, "Creating user");
            processed = create(user);
        } else {
            Logger.info(LOG_CODE, "Updating user");
            processed = update(user);
        }

        return processed;
    }

    @Override
    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    public User activate(final String email, final UUID userId) throws NotFoundException, UnprocessableEntityException {
        Logger.info(LOG_CODE, format("Activating user: %s", email));

        final User user = findUserPort.findByEmail(email).orElseThrow(NotFoundException::new);
        Logger.info(LOG_CODE, "User found: ", user);

//        if (!user.getId().equals(userId)) {
//            log.warn("User found id is different than passed user id");
//            throw new ForbiddenException();
//        }

        if (user.getStatus().getCode() == UserStatus.ACTIVE.getCode()) {
            Logger.info(LOG_CODE, "User is already active");
            throw new UnprocessableEntityException(Errors.USER_ALREADY_ACTIVE);
        }

        user.setStatus(UserStatus.ACTIVE);

        Logger.info(LOG_CODE, "Updating user's status to: [ACTIVE]");

        return saveUserPort.save(user);
    }

    @Override
    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    public User inactivate(final String email) throws NotFoundException {
        Logger.info(LOG_CODE, format("Inactivating user: %s", email));

        final User user = findUserPort.findByEmail(email).orElseThrow(NotFoundException::new);
        Logger.info(LOG_CODE, format("User found: %s", user.getId()), user);

        user.setStatus(UserStatus.INACTIVE);
        Logger.info(LOG_CODE, "Setting the UserStatus to [INACTIVE]");

        user.setInactivatedAt(LocalDateTime.now(Constants.CLOCK));

        return saveUserPort.save(user);
    }

    @Override
    public void resendEmail(final String email) throws UnprocessableEntityException, NotFoundException {
        Logger.info(LOG_CODE, format("Resending email: %s", email));

        findUserPort.findByEmail(email).ifPresentOrElse(u -> {
            Logger.info(LOG_CODE, format("User is present: %s", u.getEmail()), u);

            if (u.getStatus().getCode() == UserStatus.ACTIVE.getCode()) {
                Logger.info(LOG_CODE, "User already active");
                throw new UnprocessableEntityException(Errors.USER_ALREADY_ACTIVE);
            }

            Logger.info(LOG_CODE, "Sending confirmation e-mail");
            confirmationEmailSenderPort.send(u);

            Logger.info(LOG_CODE, "E-mail sent successfully");
        }, NotFoundException::new);
    }

    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    private User create(final User user) throws UnprocessableEntityException {
        if (user == null) throw new UnprocessableEntityException(Errors.COULD_NOT_SAVE_USER);

        if (user.getUserRole() == null) user.setUserRole(UserRole.USER);

        Logger.info(LOG_CODE, format("Creating user: %s", user.getEmail()), user);

        final LocalDateTime moment = LocalDateTime.now(Constants.CLOCK);
        final Optional<User> existingUser = findUserPort.findUserByCpf(user.getCpf());

        if (existingUser.isPresent()) {
            Logger.info(LOG_CODE, format("User already exists with cpf: %s", user.getCpf()));
            throw new UnprocessableEntityException(Errors.USER_ALREADY_EXISTS);
        }

        user.setPassword(passwordEncoderPort.encode(user.getPassword()));

        Logger.info(LOG_CODE, format("Saving User: %s", user.getEmail()), user);
        final User newUser = saveUserPort.save(User.newUser(user, moment));

        Logger.info(LOG_CODE, "Sending confirmation e-mail");
        confirmationEmailSenderPort.send(newUser);

        return newUser;
    }

    @Transactional(rollbackFor = {NotFoundException.class, Exception.class})
    private User update(final User user) throws InternalException {
        Logger.info(LOG_CODE, format("Updating user: %s", user.getEmail()), user);

        final User existingUser = findUserPort.findById(user.getId()).orElseThrow(NotFoundException::new);
        Logger.info(LOG_CODE, format("User found: %s", existingUser.getId()), existingUser);

        final LocalDateTime moment = LocalDateTime.now(Constants.CLOCK);

        Logger.info(LOG_CODE, "Updating user...");
        final Optional<User> updated = Optional.ofNullable(saveUserPort.save(existingUser.update(user, moment)));

        if (updated.isPresent()) {
            Logger.info(LOG_CODE, "Successfully updated user: ", updated.get());
            return updated.get();
        } else {
            Logger.error(LOG_CODE, "Failed updating user");
            throw new InternalException(Errors.COULD_NOT_UPDATE_USER);
        }
    }
}
