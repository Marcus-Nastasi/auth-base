package com.auth.user.adapters.outbound.repository.impl;

import com.auth.core.domain.User;
import com.auth.core.exceptions.InternalException;
import com.auth.core.ports.outbound.user.FindUserPort;
import com.auth.core.ports.outbound.user.SaveUserPort;
import com.auth.core.shared.Errors;
import com.auth.user.adapters.outbound.entity.UserEntity;
import com.auth.user.adapters.outbound.mappers.UserEntityMapper;
import com.auth.user.adapters.outbound.repository.UserJpaRepo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserRepo implements FindUserPort, SaveUserPort {

    private static final String LOG_CODE = "USER-REPO";

    private final UserJpaRepo userJpaRepo;

    @PersistenceContext
    private final EntityManager entityManager;

    public UserRepo(UserJpaRepo userJpaRepo, EntityManager entityManager) {
        this.userJpaRepo = userJpaRepo;
        this.entityManager = entityManager;
    }

    @Override
    public Set<User> findAll(final int page, final int size) {
        return userJpaRepo.findAll(PageRequest.of(page, size))
                .map(UserEntityMapper.INSTANCE::toDomain).stream()
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(final UUID id) {
        return userJpaRepo.findById(id).map(UserEntityMapper.INSTANCE::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(final String email) {
        return userJpaRepo.findByEmail(email).map(UserEntityMapper.INSTANCE::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserByCpf(final String cpf) {
        return userJpaRepo.findUserByCpf(cpf).map(UserEntityMapper.INSTANCE::toDomain);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
    public User save(final User user) throws InternalException {
        try {
            final UserEntity userEntity = UserEntityMapper.INSTANCE.toEntity(user);

            final UserEntity savedUserEntity = entityManager.merge(userEntity);

            if (savedUserEntity == null) {
                throw new InternalException(Errors.COULD_NOT_SAVE_USER);
            }

            entityManager.flush();

            return UserEntityMapper.INSTANCE.toDomain(savedUserEntity);
        } catch (Exception e) {
            log.error(LOG_CODE + ": could not save user, internal error.");
            throw new InternalException(e);
        }
    }
}
