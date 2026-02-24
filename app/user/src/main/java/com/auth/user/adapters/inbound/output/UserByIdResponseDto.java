package com.auth.user.adapters.inbound.output;

import com.auth.core.domain.enums.UserRole;
import com.auth.core.domain.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@JsonInclude
public record UserByIdResponseDto(
        UUID id,
        String email,
        String cpf,
        @JsonProperty("first_name")
        String firstName,
        @JsonProperty("last_name")
        String lastName,
        @JsonProperty("birth_date")
        LocalDate birthDate,
        UserRole role,
        UserStatus status,
        @JsonProperty("created_at")
        LocalDateTime createdAt,
        @JsonProperty("updated_at")
        LocalDateTime updatedAt,
        @JsonProperty("inactivated_at")
        LocalDateTime inactivatedAt
) implements Serializable {}
