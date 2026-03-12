package com.auth.user.adapters.inbound.output;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude
public record UserResponseDto(String email, String cpf) implements Serializable {}
