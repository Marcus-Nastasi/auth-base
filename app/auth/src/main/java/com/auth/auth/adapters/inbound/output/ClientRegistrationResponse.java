package com.auth.auth.adapters.inbound.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.io.Serializable;
import java.util.Set;

@JsonInclude
public record ClientRegistrationResponse(
        @JsonProperty("client_id")
        String clientId,
        @JsonProperty("client_secret")
        String clientSecret,
        @JsonProperty("grant_types")
        Set<AuthorizationGrantType> grantTypes,
        @JsonProperty("scopes")
        Set<String> scopes
) implements Serializable {}
