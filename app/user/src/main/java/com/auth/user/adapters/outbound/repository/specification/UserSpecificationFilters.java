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
        return (root, query, criteriaBuilder) -> {
            if (email == null) return criteriaBuilder.conjunction();
            final var emailQuery = "%"+Strings.toRootLowerCase(email)+"%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), emailQuery);
        };
    }

    public static Specification<UserEntity> hasCpf(final String cpf) {
        return (root, query, criteriaBuilder) -> {
            if (cpf == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("cpf"), cpf);
        };
    }

    public static Specification<UserEntity> hasFirstName(final String firstName) {
        return (root, query, criteriaBuilder) -> {
            if (firstName == null) return criteriaBuilder.conjunction();
            final var firstNameQuery = "%"+Strings.toRootLowerCase(firstName)+"%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")) , firstNameQuery);
        };
    }

    public static Specification<UserEntity> hasLastName(final String lastName) {
        return (root, query, criteriaBuilder) -> {
            if (lastName == null) return criteriaBuilder.conjunction();
            final var lastNameQuery = "%"+Strings.toRootLowerCase(lastName)+"%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")) , lastNameQuery);
        };
    }

    public static Specification<UserEntity> hasBirthDate(final LocalDate birthDate) {
        return (root, query, criteriaBuilder) -> {
            if (birthDate == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("birthDate"), birthDate);
        };
    }

    public static Specification<UserEntity> hasStatus(final UserStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("status"), status.getStatus());
        };
    }

    public static Specification<UserEntity> hasRole(final UserRole role) {
        return (root, query, criteriaBuilder) -> {
            if (role == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("role"), role.getRole());
        };
    }
}
