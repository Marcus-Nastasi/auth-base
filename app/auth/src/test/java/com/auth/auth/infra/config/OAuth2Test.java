package com.auth.auth.infra.config;

import com.auth.auth.adapters.inbound.input.ClientRegistrationRequest;
import com.auth.auth.helpers.UserTestHelper;
import com.auth.core.domain.User;
import com.auth.core.ports.inbound.auth.PasswordEncoderPort;
import com.auth.core.ports.outbound.user.FindUserPort;
import org.apache.commons.codec.EncoderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
final class OAuth2Test {

   private static final String TOKEN_BASE_URL = "/oauth2/token";

   @Autowired
   private MockMvc mockMvc;

   @MockitoBean
   private RegisteredClientRepository registeredClientRepository;
   @MockitoBean
   private PasswordEncoderPort passwordEncoderPort;
   @MockitoBean
   private FindUserPort findUserPort;
   @MockitoBean
   private OAuth2AuthorizationService authorizationService;
   @MockitoBean
   private OAuth2TokenGenerator<OAuth2Token> tokenGenerator;

   private ClientRegistrationRequest clientRegistrationRequest;

   private User user;
   private UUID id;
   private String cpf;
   private String email;
   private OAuth2Token oAuth2Token;
   private UUID clientId;
   private UUID clientSecret;
   private String headerAuthBasic;
   private String headerAuthBasicBase64;

   @BeforeEach
   void setUp() throws EncoderException {
      clientId = UUID.randomUUID();
      clientSecret = UUID.randomUUID();
      headerAuthBasic = String.format("%s:%s", clientId, clientSecret);
      headerAuthBasicBase64 = Arrays.toString(Base64.getEncoder().encode(headerAuthBasic.getBytes(StandardCharsets.UTF_8)));

      clientRegistrationRequest = new ClientRegistrationRequest(
              "fake-client",
              Set.of("users.read", "users.write"),
              Set.of("client_credentials", "refresh_token"),
              null
      );

      id = UUID.randomUUID();
      cpf = "85816295047";
      email = "test@gmail.com";

      user = UserTestHelper.getUserDomain(id, cpf, email);

      oAuth2Token = new OAuth2AccessToken(
           OAuth2AccessToken.TokenType.BEARER,
           "eyJraWQiOiI2QjM3RmhON250dlNzdUpWR1FBcDFGek5uakxqdFJZem52LUg1SWQ3Z2FJIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiI4OTYzMzgzNS00YTQyLTQ3MDctYWZlNi1kNjdkYTgyMjE0ZmIiLCJhdWQiOiJiM2VjNDFiZi05MTA4LTQ2YTUtYmViZi1kNzNmM2I2ODVhNjMiLCJuYmYiOjE3ODI3MTA2NzQsInNjb3BlIjpbInVzZXJzLmFkbWluIiwib3BlbmlkIiwidXNlcnMudXNlcnMiLCJ1c2Vycy5yZWFkIiwiZW1haWwiLCJ1c2Vycy53cml0ZSJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAiLCJjcGYiOiI0Mzc0MzUwODg4NSIsInR5cCI6ImFjY2VzcyIsImV4cCI6MTc4MjcxMjQ3NCwiaWF0IjoxNzgyNzEwNjc0LCJqdGkiOiIwYWE0MDZmMC1iYjNjLTRiODctOGQwOC03MTAzMjg4MDVjMzAiLCJlbWFpbCI6InZpbm5pZS5uc3RzQGdtYWlsLmNvbSJ9.Wbnft6Hl6_Jp881KNCYc8w_6ogtY8g1SJ3eJYcIHJ__KFES16w8wg4T4mkTqyueyR1I5NCASKS1cRuKoPLBdbA4cEcP6e3n1jecXQbPx5TEMPKaSB1VOiRngYF9u6-XHCBgmNuZdR4ny3f0D2xb6AtXZIs3LUE2geAoWJNjBtq4JenhgFleQHagbeniV_oRfiMRcA7zLIRlmSBHCgUtUwbRz15KnHzCFXJLpBM1bFhiMWyP0lcQbv4VF8lKVEqSbIDCwdMJ8dVKWw4_qQSEUNSFZQFDjDDzv79NEnaOQu-HeTqdNSWbQdaGJi6wA64T0pUjFYJCf-tPy7dNT-5R0xg",
           Instant.now(),
           Instant.now().plus(2, ChronoUnit.MINUTES),
           Set.of("users.read", "users.write", "users.admin")
      );
   }

   @Test
   @DisplayName("Should not return token and return 401")
   void shouldReturn401() throws Exception {
      when(findUserPort.findUserByCpf(anyString())).thenReturn(Optional.of(user));
      when(passwordEncoderPort.matches(anyString(), anyString())).thenReturn(true);
      when(tokenGenerator.generate(any(DefaultOAuth2TokenContext.class))).thenReturn(oAuth2Token);
      doNothing().when(authorizationService).save(any(OAuth2Authorization.class));

      mockMvc.perform(post(TOKEN_BASE_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("Authorization", String.format("Basic %s", HttpHeaders.encodeBasicAuth(clientId.toString(), clientSecret.toString(), StandardCharsets.UTF_8)))
                .param("grant_type", "urn:custom:grant-type:password")
                .param("cpf", cpf)
                .param("password", "12345678")
                .param("scope", "users.read users.write users.admin")
                .with(csrf()))
           .andExpect(status().isUnauthorized());
   }
}
