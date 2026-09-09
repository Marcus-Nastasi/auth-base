package com.auth.auth.adapters.inbound.input;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Set;

@JsonInclude
public record ClientRegistrationRequest(
     @JsonProperty("client_name")
     String clientName,
     @JsonProperty("scopes")
     Set<String> scopes,
     @JsonProperty("grant_types")
     Set<String> grantTypes,
     @JsonProperty("redirect_uris")
     Set<String> redirectUris
) implements Serializable {}
