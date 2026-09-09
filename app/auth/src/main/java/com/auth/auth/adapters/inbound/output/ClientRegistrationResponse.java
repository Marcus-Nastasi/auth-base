package com.auth.auth.adapters.inbound.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Set;

@JsonInclude
public record ClientRegistrationResponse(
        @JsonProperty("app_id")
        String appId,
        @JsonProperty("client_id")
        String clientId,
        @JsonProperty("client_secret")
        String clientSecret,
        @JsonProperty("grant_types")
        Set<String> grantTypes,
        @JsonProperty("scopes")
        Set<String> scopes,
        @JsonProperty("redirect_uris")
        Set<String> redirectUris,
        @JsonProperty("message")
        String message
) implements Serializable {}
