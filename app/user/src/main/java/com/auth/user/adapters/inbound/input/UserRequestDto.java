package com.auth.user.adapters.inbound.input;

import com.auth.core.domain.enums.UserRole;

import java.io.Serializable;
import java.time.LocalDate;

public record UserRequestDto(
        String email,
        String password,
        String first_name,
        String last_name,
        String cpf,
        LocalDate birth_date,
        UserRole user_role
) implements Serializable {}
