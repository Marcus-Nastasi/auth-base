package com.auth.user.adapters.outbound.repository.impl;

import com.auth.core.domain.User;
import com.auth.core.domain.enums.UserRole;
import com.auth.core.domain.enums.UserStatus;
import com.auth.core.exceptions.InternalException;
import com.auth.core.ports.outbound.user.FindUserPort;
import com.auth.core.ports.outbound.user.SaveUserPort;
import com.auth.core.shared.Errors;
import com.auth.core.shared.Logger;
import com.auth.user.adapters.outbound.entity.UserEntity;
import com.auth.user.adapters.outbound.mappers.UserEntityMapper;
import com.auth.user.adapters.outbound.repository.UserJpaRepo;
import com.auth.user.adapters.outbound.repository.specification.UserSpecificationFilters;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class UserRepo implements FindUserPort, SaveUserPort {

    private static final String LOG_CODE = "USER-REPO";

    private final UserJpaRepo userJpaRepo;

    @PersistenceContext
    private final EntityManager entityManager;

    public UserRepo(final UserJpaRepo userJpaRepo, final EntityManager entityManager) {
        this.userJpaRepo = userJpaRepo;
        this.entityManager = entityManager;
    }

    @Override
    public Set<User> findAll(final int page,
                             final int size,
                             final String email,
                             final String cpf,
                             final String firstName,
                             final String lastName,
                             final LocalDate birthDate,
                             final UserStatus status,
                             final UserRole userRole) {
        final Specification<UserEntity> spec = Specification
                .where(UserSpecificationFilters.hasEmail(email))
                .and(UserSpecificationFilters.hasCpf(cpf))
                .and(UserSpecificationFilters.hasFirstName(firstName))
                .and(UserSpecificationFilters.hasLastName(lastName))
                .and(UserSpecificationFilters.hasBirthDate(String.valueOf(birthDate)))
                .and(UserSpecificationFilters.hasStatus(status))
                .and(UserSpecificationFilters.hasRole(userRole));

        return userJpaRepo.findAll(spec, PageRequest.of(page, size, Sort.by("createdAt").ascending()))
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
            Logger.error(LOG_CODE, "Could not save user, internal error", e.getMessage(), e);
            throw new InternalException(e);
        }
    }
}
