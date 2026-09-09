package com.auth.auth.infra.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

final  class PasswordEncoderPortImplTests {

    private PasswordEncoderPortImpl passwordEncoderPort;
    private String encoded;

    @BeforeEach
    void setUp() {
        passwordEncoderPort = new PasswordEncoderPortImpl();
        encoded = passwordEncoderPort.encode("123456");
    }

    @Test
    void shouldEncode() {
        final var response = Assertions.assertDoesNotThrow(() -> passwordEncoderPort.encode("123456"));
        Assertions.assertNotNull(response);
        Assertions.assertFalse(StringUtils.isBlank(response));
    }

    @Test
    void shouldMatch() {
        final var response = Assertions.assertDoesNotThrow(() -> passwordEncoderPort.matches("123456", encoded));
        Assertions.assertNotNull(response);
        Assertions.assertTrue(response);
    }

    @Test
    void shouldNotMatch() {
        final var response = Assertions.assertDoesNotThrow(() -> passwordEncoderPort.matches("knlnksalm", encoded));
        Assertions.assertNotNull(response);
        Assertions.assertFalse(response);
    }
}
