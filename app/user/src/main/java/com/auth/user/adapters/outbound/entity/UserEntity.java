package com.auth.user.adapters.outbound.entity;

import com.auth.core.annotations.ValidCpf;
import com.auth.core.domain.enums.UserRole;
import com.auth.core.domain.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Data
@Entity
@Builder
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Email(message = "Invalid email field")
    @NotNull(message = "Email field cannot be null")
    @NotBlank(message = "Email field cannot be blank")
    @Size(max = 77, message = "Email field cannot have more than 77 characters")
    @Column(name = "email", length = 77, nullable = false)
    private String email;

    @ValidCpf(message = "Invalid cpf")
    @NotNull(message = "Cpf field cannot be null")
    @Size(min = 11, max = 14, message = "Cpf field may have minimal 11 chars, and 14 as maximum")
    @Column(name = "cpf", nullable = false)
    private String cpf;

    @NotNull(message = "Password field cannot be null")
    @NotBlank(message = "Password field cannot be blank")
    @Column(name = "password", length = 70, nullable = false)
    private String password;

    @NotNull
    @NotBlank
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, pattern = "yyyy-MM-dd")
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
