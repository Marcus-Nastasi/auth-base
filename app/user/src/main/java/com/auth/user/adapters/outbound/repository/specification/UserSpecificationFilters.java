package com.auth.user.adapters.outbound.repository.specification;

import com.auth.core.domain.enums.UserRole;
import com.auth.core.domain.enums.UserStatus;
import com.auth.user.adapters.outbound.entity.UserEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class UserSpecificationFilters {

    public static Specification<UserEntity> hasEmail(final String email) {
        return (root, query, criteriaBuilder) -> email != null
                ? criteriaBuilder.equal(root.get("email"), email) : null;
    }

    public static Specification<UserEntity> hasCpf(final String cpf) {
        return (root, query, criteriaBuilder) -> cpf != null
                ? criteriaBuilder.equal(root.get("cpf"), cpf) : null;
    }

    public static Specification<UserEntity> hasFirstName(final String firstName) {
        return (root, query, criteriaBuilder) -> firstName != null
                ? criteriaBuilder.equal(root.get("firstName"), firstName) : null;
    }

    public static Specification<UserEntity> hasLastName(final String lastName) {
        return (root, query, criteriaBuilder) -> lastName != null
                ? criteriaBuilder.equal(root.get("lastName"), lastName) : null;
    }

    public static Specification<UserEntity> hasBirthDate(final String birthDate) {
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
