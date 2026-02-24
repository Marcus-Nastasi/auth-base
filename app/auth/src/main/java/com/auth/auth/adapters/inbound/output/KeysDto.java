package com.auth.auth.adapters.inbound.output;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record KeysDto(JwksDto keys) implements Serializable {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record JwksDto(
            String kty,
            String kid,
            String use,
            String alg,
            String n,
            String e
    ) implements Serializable {}
}
