package com.auth.auth.infra.grant;

import com.auth.core.domain.User;
import com.auth.core.domain.enums.UserStatus;
import com.auth.core.ports.inbound.auth.PasswordEncoderPort;
import com.auth.core.ports.outbound.user.FindUserPort;
import com.auth.core.shared.Logger;
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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomPasswordGrantAuthenticationProvider implements AuthenticationProvider {

   private static final String LOG_CODE = "CUSTOM-PASSWORD-GRANT-AUTHENTICATION-PROVIDER";

   private final FindUserPort findUserPort;
   private final PasswordEncoderPort passwordEncoderPort;
   private final OAuth2AuthorizationService authorizationService;
   private final OAuth2TokenGenerator<OAuth2Token> tokenGenerator;

   public CustomPasswordGrantAuthenticationProvider(final FindUserPort findUserPort,
                                                    final PasswordEncoderPort passwordEncoderPort,
                                                    final OAuth2AuthorizationService authorizationService,
                                                    final OAuth2TokenGenerator<OAuth2Token> tokenGenerator) {
      this.findUserPort = findUserPort;
      this.passwordEncoderPort = passwordEncoderPort;
      this.authorizationService = authorizationService;
      this.tokenGenerator = tokenGenerator;
   }

   @Override
   @Transactional(
        propagation = Propagation.REQUIRES_NEW,
        isolation = Isolation.READ_COMMITTED,
        timeout = 20,
        rollbackFor = Exception.class
   )
   public Authentication authenticate(@NonNull final Authentication authentication) throws AuthenticationException {
      Logger.info(LOG_CODE, "Initialize providing authentication: ", authentication);

      final var customPasswordGrantAuthenticationToken = (CustomPasswordGrantAuthenticationToken) authentication;
      final AuthorizationGrantType grantType = customPasswordGrantAuthenticationToken.getGrantType();

      SecurityContextHolder.getContext().setAuthentication(customPasswordGrantAuthenticationToken);

      final OAuth2ClientAuthenticationToken clientPrincipal = extractClientPrincipal(customPasswordGrantAuthenticationToken);
      final RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();

      if (unauthorizedGrant(registeredClient, grantType))
         throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);

      final User user = findUserPort.findUserByCpf(customPasswordGrantAuthenticationToken.getCpf())
           .filter(u -> UserStatus.ACTIVE.equals(u.getStatus()))
           .orElseThrow(() -> new OAuth2AuthenticationException(OAuth2ErrorCodes.ACCESS_DENIED));

      if (!passwordEncoderPort.matches(customPasswordGrantAuthenticationToken.getPassword(), user.getPassword()))
         throw new OAuth2AuthenticationException(OAuth2ErrorCodes.ACCESS_DENIED);

      final Set<String> authorizedScopes = resolveScopes(user, registeredClient, customPasswordGrantAuthenticationToken.getScopes());

      final UsernamePasswordAuthenticationToken userPrincipal = new UsernamePasswordAuthenticationToken(
           user.getId().toString(),
           null,
           authorizedScopes.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet())
      );

      final OAuth2AccessToken accessToken = (OAuth2AccessToken) generateToken(
           registeredClient,
           userPrincipal,
           authorizedScopes,
           grantType,
           customPasswordGrantAuthenticationToken,
           false
      );

      final OAuth2RefreshToken refreshToken = (OAuth2RefreshToken) generateToken(
           registeredClient,
           userPrincipal,
           authorizedScopes,
           grantType,
           customPasswordGrantAuthenticationToken,
           true
      );

      final OAuth2Authorization authorization = generateAuthorization(
           registeredClient,
           user,
           grantType,
           authorizedScopes,
           accessToken,
           refreshToken
      );

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
      else throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
   }

   private boolean unauthorizedGrant(final RegisteredClient registeredClient, final AuthorizationGrantType grantType) {
      return registeredClient == null
           || registeredClient.getAuthorizationGrantTypes() == null
           || !registeredClient.getAuthorizationGrantTypes().contains(grantType);
   }

   private Set<String> resolveScopes(final User user,
                                     final RegisteredClient registeredClient,
                                     final Set<String> requestedScopes) {
      final Set<String> userScopes = user.getUserRole().getScopes();

      if (CollectionUtils.isNotEmpty(requestedScopes)) {
         requestedScopes.addAll(userScopes);
         return requestedScopes.stream()
              .filter(s -> userScopes.contains(s) && registeredClient.getScopes().contains(s))
              .collect(Collectors.toSet());
      }

      return userScopes;
   }

   private OAuth2Token generateToken(final RegisteredClient registeredClient,
                                     final UsernamePasswordAuthenticationToken userPrincipal,
                                     final Set<String> authorizedScopes,
                                     final AuthorizationGrantType grantType,
                                     final CustomPasswordGrantAuthenticationToken customPasswordGrantAuthenticationToken,
                                     final boolean isRefresh) {
      final DefaultOAuth2TokenContext accessTokenContext = DefaultOAuth2TokenContext.builder()
           .registeredClient(registeredClient)
           .principal(userPrincipal)
           .authorizationServerContext(AuthorizationServerContextHolder.getContext())
           .authorizedScopes(authorizedScopes)
           .tokenType(isRefresh ? OAuth2TokenType.REFRESH_TOKEN : OAuth2TokenType.ACCESS_TOKEN)
           .authorizationGrantType(grantType)
           .authorizationGrant(customPasswordGrantAuthenticationToken)
           .build();

      final OAuth2Token generatedToken = tokenGenerator.generate(accessTokenContext);

      if (generatedToken == null)
         throw new OAuth2AuthenticationException(OAuth2ErrorCodes.SERVER_ERROR);

      if (isRefresh)
         return new OAuth2RefreshToken(
              generatedToken.getTokenValue(),
              generatedToken.getIssuedAt(),
              generatedToken.getExpiresAt());
      else
         return new OAuth2AccessToken(
              OAuth2AccessToken.TokenType.BEARER,
              generatedToken.getTokenValue(),
              generatedToken.getIssuedAt(),
              generatedToken.getExpiresAt(),
              authorizedScopes);
   }

   private OAuth2Authorization generateAuthorization(final RegisteredClient registeredClient,
                                                     final User user,
                                                     final AuthorizationGrantType grantType,
                                                     final Set<String> authorizedScopes,
                                                     final OAuth2AccessToken accessToken,
                                                     final OAuth2RefreshToken refreshToken) {

      return OAuth2Authorization
           .withRegisteredClient(registeredClient)
           .principalName(user.getId().toString())
           .authorizationGrantType(grantType)
           .authorizedScopes(authorizedScopes)
           .token(accessToken)
           .refreshToken(refreshToken)
           .attribute("user_id", user.getId().toString())
           .attribute("cpf", user.getCpf())
           .build();
   }
}
