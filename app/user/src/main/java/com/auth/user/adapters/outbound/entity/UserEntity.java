package com.auth.user.adapters.outbound.entity;

import com.auth.core.annotations.ValidCpf;
import com.auth.core.domain.enums.UserRole;
import com.auth.core.domain.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Data
@Entity
@Table(name = "users")
public class UserEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @NotNull
    @NotBlank
    @Size(max = 77)
    @Email(message = "Email invalid")
    @Column(name = "email", length = 77, nullable = false)
    private String email;

    @NotNull
    @Size(min = 11, max = 14)
    @ValidCpf(message = "Cpf cannot be null")
    @Column(name = "cpf", nullable = false)
    private String cpf;

    @NotNull
    @NotBlank
    @Column(name = "password", length = 70, nullable = false)
    private String password;

    @NotNull
    @NotBlank
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @NotNull
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false)
    private UserStatus status;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private UserRole userRole;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "inactivated_at")
    private LocalDateTime inactivatedAt;
}
