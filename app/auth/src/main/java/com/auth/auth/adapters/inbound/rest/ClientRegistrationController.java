package com.auth.auth.adapters.inbound.rest;

import com.auth.auth.adapters.inbound.input.ClientRegistrationRequest;
import com.auth.auth.adapters.inbound.mappers.ClientRegistrationDtoMapper;
import com.auth.core.exceptions.ForbiddenException;
import com.auth.core.ports.inbound.auth.PasswordEncoderPort;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.*;
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
   public ResponseEntity<Object> register(@RequestBody final ClientRegistrationRequest request) {
      final var clientId = UUID.randomUUID();
      final var clientSecret = UUID.randomUUID();

      final RegisteredClient.Builder clientBuilder = RegisteredClient.withId(UUID.randomUUID().toString())
           .clientId(clientId.toString())
           .clientSecret(passwordEncoderPort.encode(clientSecret.toString()))
           .clientName(request.clientName())
           .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
           .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(30))
                .build());

      final var scopesSet = Arrays.stream(request.scopes().split(" ")).collect(Collectors.toSet());

      if (CollectionUtils.isNotEmpty(request.redirectUris())) {
         request.redirectUris().forEach(clientBuilder::redirectUri);
         clientBuilder.scopes(scopes -> scopes.addAll(scopesSet));
         resolveGrantTypes(request.grantTypes()).forEach(clientBuilder::authorizationGrantType);
      } else {
         clientBuilder
              .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
              .scopes(scopes -> scopes.addAll(scopesSet));
      }

      clientBuilder.tokenSettings(TokenSettings.builder()
           .accessTokenTimeToLive(Duration.ofMinutes(30))
           .refreshTokenTimeToLive(Duration.ofDays(10))
           .build());

      final var hasAuthorizationCode = request.grantTypes().contains("authorization_code");

      clientBuilder.clientSettings(ClientSettings.builder()
           .requireProofKey(hasAuthorizationCode)
           .requireAuthorizationConsent(hasAuthorizationCode)
           .build());

      final var client = clientBuilder.build();

      registeredClientRepository.save(client);

      return ResponseEntity.status(HttpStatus.CREATED).body(ClientRegistrationDtoMapper.INSTANCE.response(client, clientSecret));
   }

   private Set<AuthorizationGrantType> resolveGrantTypes(final List<String> stringsGrantType) throws ForbiddenException {
      if (CollectionUtils.isEmpty(stringsGrantType)) return Collections.emptySet();
      return stringsGrantType.stream().filter(Objects::nonNull).map(s ->
           switch (s) {
              case "authorization_code" -> AuthorizationGrantType.AUTHORIZATION_CODE;
              case "client_credentials" -> AuthorizationGrantType.CLIENT_CREDENTIALS;
              case "refresh_token" -> AuthorizationGrantType.REFRESH_TOKEN;
              case "urn:ietf:params:oauth:grant-type:jwt-bearer" -> AuthorizationGrantType.JWT_BEARER;
              case "urn:ietf:params:oauth:grant-type:device_code" -> AuthorizationGrantType.DEVICE_CODE;
              case "urn:ietf:params:oauth:grant-type:token-exchange" -> AuthorizationGrantType.TOKEN_EXCHANGE;
              case "urn:custom:grant-type:password" -> new AuthorizationGrantType("urn:custom:grant-type:password");
              default -> throw new ForbiddenException("Invalid grant type: "+s);
           }
        )
        .collect(Collectors.toSet());
   }
}
