package com.auth.core.domain;

import com.auth.core.domain.enums.UserRole;
import com.auth.core.domain.enums.UserStatus;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Builder
@ToString
@EqualsAndHashCode(doNotUseGetters = true)
public final class User implements Serializable {

    private UUID id;

    private String email;

    private String cpf;

    private String password;

    private String firstName;

    private String lastName;

    private LocalDate birthDate;

    private UserRole userRole;

    private UserStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime inactivatedAt;

    public static User newUser(final User user, final LocalDateTime moment) {
        user.setStatus(UserStatus.PENDING);
        user.setCreatedAt(moment);
        return user;
    }

    public User update(final User data, final LocalDateTime moment) {
        Optional.ofNullable(data.getEmail()).ifPresent(this::setEmail);
        Optional.ofNullable(data.getCpf()).ifPresent(this::setCpf);
        Optional.ofNullable(data.getFirstName()).ifPresent(this::setFirstName);
        Optional.ofNullable(data.getLastName()).ifPresent(this::setLastName);
        Optional.ofNullable(data.getBirthDate()).ifPresent(this::setBirthDate);
        Optional.ofNullable(data.getStatus()).ifPresent(this::setStatus);

        setUpdatedAt(moment);

        return this;
    }

    public User() {}

    public User(final UUID id,
                final String email,
                final String cpf,
                final String password,
                final String firstName,
                final String lastName,
                final LocalDate birthDate,
                final UserRole userRole,
                final UserStatus status,
                final LocalDateTime createdAt,
                final LocalDateTime updatedAt,
                final LocalDateTime inactivatedAt) {
        this.id = id;
        this.email = email;
        this.cpf = cpf;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.userRole = userRole;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.inactivatedAt = inactivatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getCpf() {
        return cpf;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public UserRole getUserRole() {
        return userRole;
    }

    public UserStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getInactivatedAt() {
        return inactivatedAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setInactivatedAt(LocalDateTime inactivatedAt) {
        this.inactivatedAt = inactivatedAt;
    }
}
