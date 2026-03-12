package com.auth.user.adapters.inbound.input;

import com.auth.core.domain.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDate;

@JsonInclude
public record UserRequestDto(
        String email,
        String password,
        @JsonProperty(value = "first_name")
        String firstName,
        @JsonProperty(value = "last_name")
        String lastName,
        String cpf,
        @JsonProperty(value = "birth_date")
        LocalDate birthDate,
        @JsonProperty(value = "user_role")
        UserRole userRole
) implements Serializable {}
