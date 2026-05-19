package com.auth.auth.adapters.inbound.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

final class JwksControllerTests extends ControllerTestBase {

    private KeyPair keyPair;

    @BeforeEach
    void setUp() throws Exception {
        final var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keyPair = generator.generateKeyPair();
    }

    @Test
    void shouldReturnSuccess() throws Exception {
        when(tokenPort.getPublicKey()).thenReturn(keyPair.getPublic());
        when(tokenPort.getKid()).thenReturn("my-key-id");

        mockMvc.perform(get("/.well-known/jwks.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].keys.kty").value("RSA"))
                .andExpect(jsonPath("$[0].keys.kid").value("my-key-id"))
                .andExpect(jsonPath("$[0].keys.use").value("sig"))
                .andExpect(jsonPath("$[0].keys.alg").value("RS256"))
                .andExpect(jsonPath("$[0].keys.n").isNotEmpty())
                .andExpect(jsonPath("$[0].keys.e").isNotEmpty());
    }
}
