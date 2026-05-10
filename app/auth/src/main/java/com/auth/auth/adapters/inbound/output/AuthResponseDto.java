package com.auth.auth.adapters.inbound.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;

import java.io.Serializable;

@Builder(access = AccessLevel.PUBLIC)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponseDto(

        @JsonProperty(value = "access_token")
        String accessToken,

        @JsonProperty(value = "refresh_token")
        String refreshToken,

        @JsonProperty(value = "scopes")
        String scopes
) implements Serializable {}
