package com.auth.core.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.io.Serializable;
import java.util.Collection;

@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public record PageResponse<T>(
        @JsonProperty("page")
        int page,
        @JsonProperty("size")
        int size,
        @JsonProperty("next_page")
        int nextPage,
        @JsonProperty("next_page_link")
        String nextPageLink,
        @JsonProperty("data")
        Collection<T> data
) implements Serializable {}
