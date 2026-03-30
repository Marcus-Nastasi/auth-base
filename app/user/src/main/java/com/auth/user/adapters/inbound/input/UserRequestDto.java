package com.auth.user.adapters.inbound.input;

import com.auth.core.domain.enums.UserRole;
import com.auth.core.annotations.ValidEmail;
import com.auth.core.annotations.ValidCpf;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDate;

@JsonInclude
public record UserRequestDto(

        @JsonProperty(value = "email")
        @ValidEmail(message = "Invalid email")
        String email,

        @Size(min = 8, max = 77)
        @JsonProperty(value = "password")
        String password,

        @Size(min = 1, max = 77)
        @JsonProperty(value = "first_name")
        String firstName,

        @Size(min = 1, max = 77)
        @JsonProperty(value = "last_name")
        String lastName,

        @JsonProperty(value = "cpf")
        @ValidCpf(message = "Invalid cpf")
        @Size(min = 11, max = 14, message = "Size must be between 11 and 14 chars")
        String cpf,

        @JsonProperty(value = "birth_date")
        LocalDate birthDate,

        @JsonProperty(value = "user_role")
        UserRole userRole
) implements Serializable {}
