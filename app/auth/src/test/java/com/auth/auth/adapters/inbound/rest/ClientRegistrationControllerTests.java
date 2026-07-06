package com.auth.auth.adapters.inbound.rest;

import com.auth.auth.adapters.inbound.input.ClientRegistrationRequest;
import com.auth.core.ports.inbound.auth.PasswordEncoderPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

final class ClientRegistrationControllerTests extends ControllerTestBase {

   @MockitoBean
   private RegisteredClientRepository registeredClientRepository;
   @MockitoBean
   private PasswordEncoderPort passwordEncoderPort;

   private ClientRegistrationRequest clientRegistrationRequest;

   @BeforeEach
   void setUp() {
      clientRegistrationRequest = new ClientRegistrationRequest(
           "fake-client",
           Set.of("users.read", "users.write"),
           Set.of("client_credentials", "refresh_token"),
           null
      );
   }

   @Test
   @DisplayName("Should return 401 without scope")
   void shouldReturn401WhenUnauthenticated() throws Exception {
      mockMvc.perform(post("/api/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clientRegistrationRequest)))
           .andExpect(status().isForbidden());
   }

   @Test
   @DisplayName("Should return 401 with wrong scope")
   @WithMockUser(authorities = {"SCOPE_users.read", "SCOPE_users.write", "SCOPE_client.read"})
   void shouldReturn401WhenInvalidScope() throws Exception {
      mockMvc.perform(post("/api/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clientRegistrationRequest)))
           .andExpect(status().isForbidden());
   }

   @Test
   @DisplayName("Should create client")
   @WithMockUser(authorities = {"SCOPE_client.create"})
   void shouldReturnOk() throws Exception {
      when(passwordEncoderPort.encode(anyString())).thenReturn("xyx");
      doNothing().when(registeredClientRepository).save(any(RegisteredClient.class));

      mockMvc.perform(post("/api/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clientRegistrationRequest))
                .with(csrf()))
           .andExpect(status().isCreated());
   }
}
