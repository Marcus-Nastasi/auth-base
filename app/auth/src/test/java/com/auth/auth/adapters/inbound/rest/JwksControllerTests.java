package com.auth.auth.adapters.inbound.rest;

import com.auth.core.ports.inbound.auth.AuthUseCasePort;
import com.auth.core.ports.inbound.auth.TokenPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

final class JwksControllerTests extends ControllerTestBase {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TokenPort tokenPort;

    @MockitoBean
    private AuthUseCasePort authUseCasePort;

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
