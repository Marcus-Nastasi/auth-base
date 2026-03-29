package com.auth.user.adapters.inbound.input;

import com.auth.core.domain.enums.UserRole;
import com.auth.core.annotations.ValidEmail;
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

        @Size(min = 11, max = 14)
        @JsonProperty(value = "cpf")
        String cpf,

        @JsonProperty(value = "birth_date")
        LocalDate birthDate,

        @JsonProperty(value = "user_role")
        UserRole userRole
) implements Serializable {}
