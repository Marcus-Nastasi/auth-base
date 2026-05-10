package com.auth.user.adapters.outbound.repository.specification;

import com.auth.core.domain.enums.UserRole;
import com.auth.core.domain.enums.UserStatus;
import com.auth.user.adapters.outbound.entity.UserEntity;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public final class UserSpecificationFilters {

    private UserSpecificationFilters() {}

    public static Specification<UserEntity> hasEmail(final String email) {
        if (email == null) return null;
        final var emailQuery = "%"+ Strings.toRootLowerCase(email)+"%";
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("email")) , emailQuery);
    }

    public static Specification<UserEntity> hasCpf(final String cpf) {
        return (root, query, criteriaBuilder) -> cpf != null
                ? criteriaBuilder.equal(root.get("cpf"), cpf) : null;
    }

    public static Specification<UserEntity> hasFirstName(final String firstName) {
        if (firstName == null) return null;
        final var firstNameQuery = "%"+Strings.toRootLowerCase(firstName)+"%";
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")) , firstNameQuery);
    }

    public static Specification<UserEntity> hasLastName(final String lastName) {
        if (lastName == null) return null;
        final var lastNameQuery = "%"+Strings.toRootLowerCase(lastName)+"%";
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")) , lastNameQuery);
    }

    public static Specification<UserEntity> hasBirthDate(final LocalDate birthDate) {
        return (root, query, criteriaBuilder) -> birthDate != null
                ? criteriaBuilder.equal(root.get("birthDate"), birthDate) : null;
    }

    public static Specification<UserEntity> hasStatus(final UserStatus status) {
        return (root, query, criteriaBuilder) -> status != null
                ? criteriaBuilder.equal(root.get("status"), status.getStatus()) : null;
    }

    public static Specification<UserEntity> hasRole(final UserRole role) {
        return (root, query, criteriaBuilder) -> role != null
                ? criteriaBuilder.equal(root.get("role"), role.getRole()) : null;
    }
}
