package com.auth.auth.adapters.inbound.rest;

import com.auth.auth.adapters.inbound.input.ClientRegistrationRequest;
import com.auth.core.ports.inbound.auth.PasswordEncoderPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/clients")
public class ClientRegistrationController {

   private final RegisteredClientRepository registeredClientRepository;
   private final PasswordEncoderPort passwordEncoderPort;

   public ClientRegistrationController(final RegisteredClientRepository registeredClientRepository,
                                       final PasswordEncoderPort passwordEncoderPort) {
      this.registeredClientRepository = registeredClientRepository;
      this.passwordEncoderPort = passwordEncoderPort;
   }

   @PostMapping
   @PreAuthorize("hasAuthority('SCOPE_client.create')")
   public ResponseEntity<?> register(@RequestBody final ClientRegistrationRequest request) {
      final var clientId = UUID.randomUUID();
      final var clientSecret = UUID.randomUUID();

      final RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
           .clientId(clientId.toString())
           .clientSecret(passwordEncoderPort.encode(clientSecret.toString()))
           .clientName(request.clientName())
           .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
           .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
           .scopes(scopes -> scopes.addAll(Arrays.stream(request.scopes().split(" ")).collect(Collectors.toSet())))
           .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(30))
                .build())
           .build();

      registeredClientRepository.save(client);

      return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
           "client_id",
           client.getClientId(),
           "client_secret",
           clientSecret,
           "grant_types",
           client.getAuthorizationGrantTypes(),
           "scopes",
           client.getScopes()
      ));
   }
}
