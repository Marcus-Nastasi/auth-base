package com.auth.user.adapters.outbound.entity;

import com.auth.core.domain.enums.UserRole;
import com.auth.core.domain.enums.UserStatus;
import com.auth.core.exceptions.InternalException;
import com.auth.core.shared.AppError;
import com.auth.core.shared.Errors;
import com.auth.core.shared.Logger;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static java.lang.String.format;

@Data
@Entity
@Table(name = "users")
public class UserEntity implements UserDetails, Serializable {

    private static final SimpleGrantedAuthority ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");
    private static final SimpleGrantedAuthority USER = new SimpleGrantedAuthority("ROLE_USER");

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @NotNull
    @NotBlank
    @Size(max = 77)
    @Column(name = "email", length = 77, nullable = false)
    private String email;

    @NotNull
    @Size(min = 11, max = 14)
    @Column(name = "cpf", nullable = false)
    private String cpf;

    @NotNull
    @NotBlank
    @Column(name = "password", nullable = false)
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        switch (this.userRole) {
            case UserRole.ADMIN: return List.of(ADMIN, USER);
            case UserRole.USER: return List.of(USER);
            default: {
                final var msg = format("UserRole does not equals permitted values. Permitted values are: %s",
                                       Arrays.toString(UserRole.values()));
                Logger.error("USER-ENTITY", msg, Map.of("errors", List.of(AppError.builder()
                        .message(msg)
                        .field("UserRole")
                        .attempted(this.userRole.getRole()))));

                throw new InternalException(msg);
            }
        }
    }

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "inactivated_at")
    private LocalDateTime inactivatedAt;

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getPassword() {
        return password;
    }
}
