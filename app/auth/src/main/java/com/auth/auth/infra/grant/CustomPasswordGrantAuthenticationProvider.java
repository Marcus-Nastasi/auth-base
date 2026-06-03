package com.auth.auth.infra.grant;

import com.auth.core.domain.User;
import com.auth.core.domain.enums.UserRole;
import com.auth.core.ports.inbound.auth.PasswordEncoderPort;
import com.auth.core.ports.outbound.user.FindUserPort;
import lombok.NonNull;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomPasswordGrantAuthenticationProvider implements AuthenticationProvider {

   private final FindUserPort findUserPort;
   private final PasswordEncoderPort passwordEncoderPort;
   private final OAuth2AuthorizationService authorizationService;
   private final OAuth2TokenGenerator<?> tokenGenerator;

   public CustomPasswordGrantAuthenticationProvider(final FindUserPort findUserPort,
                                                    final PasswordEncoderPort passwordEncoderPort,
                                                    final OAuth2AuthorizationService authorizationService,
                                                    final OAuth2TokenGenerator<?> tokenGenerator) {
      this.findUserPort = findUserPort;
      this.passwordEncoderPort = passwordEncoderPort;
      this.authorizationService = authorizationService;
      this.tokenGenerator = tokenGenerator;
   }

   @Override
   @Transactional(readOnly = true)
   public Authentication authenticate(@NonNull final Authentication authentication) throws AuthenticationException {
      final var token = CustomPasswordGrantAuthenticationToken.class.cast(authentication);
      SecurityContextHolder.getContext().setAuthentication(token);

      final OAuth2ClientAuthenticationToken clientPrincipal = extractClientPrincipal(token);
      final RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();

      if (registeredClient == null
              || registeredClient.getAuthorizationGrantTypes() == null
              || !registeredClient.getAuthorizationGrantTypes().contains(token.getGrantType()))
         throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);

      final User user = findUserPort.findUserByCpf(token.getCpf())
              .orElseThrow(() -> new OAuth2AuthenticationException(OAuth2ErrorCodes.ACCESS_DENIED));
      if (user.getInactivatedAt() != null)
         throw new OAuth2AuthenticationException(OAuth2ErrorCodes.ACCESS_DENIED);

      if (!passwordEncoderPort.matches(token.getPassword(), user.getPassword()))
         throw new OAuth2AuthenticationException(OAuth2ErrorCodes.ACCESS_DENIED);

      final Set<String> authorizedScopes = resolveScopes(user, registeredClient, token.getScopes());

      final UsernamePasswordAuthenticationToken userPrincipal = new UsernamePasswordAuthenticationToken(
           user.getId().toString(),
           null,
           authorizedScopes.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet())
      );

      // Gera contexto para criação dos tokens
      final DefaultOAuth2TokenContext accessTokenContext = DefaultOAuth2TokenContext.builder()
              .registeredClient(registeredClient)
              .principal(userPrincipal)
              .authorizationServerContext(AuthorizationServerContextHolder.getContext())
              .authorizedScopes(authorizedScopes)
              .tokenType(OAuth2TokenType.ACCESS_TOKEN)
              .authorizationGrantType(token.getGrantType())
              .authorizationGrant(token)
              .build();

      // Gera access token
      final OAuth2Token generatedAccessToken = tokenGenerator.generate(accessTokenContext);
      if (generatedAccessToken == null)
         throw new OAuth2AuthenticationException(OAuth2ErrorCodes.SERVER_ERROR);

      final OAuth2AccessToken accessToken = new OAuth2AccessToken(
              OAuth2AccessToken.TokenType.BEARER,
              generatedAccessToken.getTokenValue(),
              generatedAccessToken.getIssuedAt(),
              generatedAccessToken.getExpiresAt(),
              authorizedScopes);

      final DefaultOAuth2TokenContext refreshTokenContext = DefaultOAuth2TokenContext.builder()
              .registeredClient(registeredClient)
              .principal(userPrincipal)
              .authorizationServerContext(AuthorizationServerContextHolder.getContext())
              .authorizedScopes(authorizedScopes)
              .tokenType(OAuth2TokenType.REFRESH_TOKEN)
              .authorizationGrantType(token.getGrantType())
              .authorizationGrant(token)
              .build();

      final OAuth2Token generatedRefreshToken = tokenGenerator.generate(refreshTokenContext);
      if (generatedRefreshToken == null)
         throw new OAuth2AuthenticationException(OAuth2ErrorCodes.SERVER_ERROR);

      final OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(
           generatedRefreshToken.getTokenValue(),
           generatedRefreshToken.getIssuedAt(),
           generatedRefreshToken.getExpiresAt()
      );
      //}

      // Persiste a autorização (em memória por agora)
      final OAuth2Authorization authorization = OAuth2Authorization
              .withRegisteredClient(registeredClient)
              .principalName(user.getId().toString())
              .authorizationGrantType(token.getGrantType())
              .authorizedScopes(authorizedScopes)
              .token(accessToken)
              .refreshToken(refreshToken)
              .attribute("user_id", user.getId().toString())
              .attribute("cpf", user.getCpf())
              .build();

      authorizationService.save(authorization);

      return new OAuth2AccessTokenAuthenticationToken(
              registeredClient, clientPrincipal, accessToken, refreshToken, Collections.emptyMap());
   }

   @Override
   public boolean supports(@NonNull final Class<?> authentication) {
      return CustomPasswordGrantAuthenticationToken.class.isAssignableFrom(authentication);
   }

   private OAuth2ClientAuthenticationToken extractClientPrincipal(final Authentication authentication) throws OAuth2AuthenticationException {
      if (authentication.getPrincipal() instanceof OAuth2ClientAuthenticationToken authenticationToken)
         return authenticationToken;
      else
         throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
   }

   private Set<String> resolveScopes(final User user,
                                     final RegisteredClient registeredClient,
                                     final Set<String> requestedScopes) {
      final Set<String> userScopes = UserRole.ADMIN.equals(user.getUserRole())
           ? Set.of("users.read", "users.write", "users.admin")
           : Set.of("users.read", "users.write", "users.user");

      if (CollectionUtils.isNotEmpty(requestedScopes)) {
         requestedScopes.addAll(userScopes);
         return requestedScopes.stream()
              .filter(userScopes::contains)
              .filter(s -> registeredClient.getScopes().contains(s))
              .collect(Collectors.toSet());
      }

      return userScopes;
   }
}
