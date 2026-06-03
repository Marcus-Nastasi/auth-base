package com.auth.auth.adapters.inbound.input;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@JsonInclude
public record ClientRegistrationRequest(
     @JsonProperty("client_name")
     String clientName,
     @JsonProperty("grant_types")
     List<String> grantTypes,
     @JsonProperty("scopes")
     String scopes
) implements Serializable {}
