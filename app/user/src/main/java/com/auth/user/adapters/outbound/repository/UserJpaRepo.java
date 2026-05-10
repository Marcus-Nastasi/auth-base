package com.auth.user.adapters.outbound.repository;

import com.auth.user.adapters.outbound.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepo extends JpaRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {

    Optional<UserEntity> findByEmail(String email);

    @Query(value = "SELECT u FROM UserEntity u WHERE u.cpf = :cpf AND u.inactivatedAt IS NULL")
    Optional<UserEntity> findUserByCpf(String cpf);
}
