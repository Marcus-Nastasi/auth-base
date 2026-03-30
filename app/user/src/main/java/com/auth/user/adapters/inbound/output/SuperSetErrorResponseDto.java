package com.auth.user.adapters.inbound.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.io.Serializable;

@Builder
@JsonInclude
public record SuperSetErrorResponseDto<T>(T errors) implements Serializable {}
