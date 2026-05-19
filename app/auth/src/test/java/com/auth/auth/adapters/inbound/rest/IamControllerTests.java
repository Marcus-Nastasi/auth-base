package com.auth.auth.adapters.inbound.rest;

import com.auth.auth.adapters.inbound.input.AuthRequestDto;
import com.auth.core.domain.AuthLogin;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.anyString;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

final class IamControllerTests extends ControllerTestBase {

    private AuthLogin authLogin;

    private AuthRequestDto authRequestDto;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        authRequestDto = new AuthRequestDto("username", "pass");
        authLogin = AuthLogin.builder()
            .token("maskldmlsadnaskldmas")
            .refresh("dsjkldnadnklasdnas")
            .scope("user.read")
            .build();
    }

    @Test
    void shouldLogin() throws Exception {
        when(authUseCasePort.login(anyString(), anyString())).thenReturn(authLogin);

        final var request = MockMvcRequestBuilders.post("/api/v1/iam")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequestDto));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.access_token").value("maskldmlsadnaskldmas"))
                .andExpect(jsonPath("$.refresh_token").exists())
                .andExpect(jsonPath("$.refresh_token").value("dsjkldnadnklasdnas"))
                .andExpect(jsonPath("$.scopes").exists())
                .andExpect(jsonPath("$.scopes").value("user.read"));
    }
}
