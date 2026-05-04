package com.auth.core.domain;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthLogin implements Serializable {

    @Serial
    private static final long serialVersionUID = 28347398013140L;

    private String token;

    private String refresh;

    private String scope;
}
