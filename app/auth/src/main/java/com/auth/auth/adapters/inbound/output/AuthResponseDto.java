package com.auth.auth.adapters.inbound.output;

import com.auth.core.domain.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponseDto(

        @JsonProperty(value = "access_token")
        String accessToken,

        @JsonProperty(value = "refresh_token")
        String refreshToken,

        @JsonProperty(value = "scopes")
        String scopes
) implements Serializable {}
