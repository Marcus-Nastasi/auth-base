package com.auth.auth.adapters.inbound.input;

import java.io.Serializable;

public record AuthRequestDto(String cpf, String password) implements Serializable {}
