package com.auth.auth.infra.grant;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomPasswordGrantAuthenticationConverter implements AuthenticationConverter {

   @Override
   public Authentication convert(final HttpServletRequest request) throws OAuth2AuthenticationException {
      final var grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
      if (!CustomPasswordGrantAuthenticationToken.GRANT_TYPE.getValue().equalsIgnoreCase(grantType))
         return null;

      final Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();
      if (clientPrincipal == null)
         throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);

      RegisteredClient registeredClient = null;
      if (clientPrincipal instanceof OAuth2ClientAuthenticationToken)
         registeredClient = ((OAuth2ClientAuthenticationToken) clientPrincipal).getRegisteredClient();
      else throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);

      if (registeredClient == null) throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);

      final var clientGrantType = registeredClient.getAuthorizationGrantTypes();
      checkGrantTypes(grantType, clientGrantType);

      final var cpf = request.getParameter("cpf");
      final var password = request.getParameter("password");

      if (!StringUtils.hasText(cpf) || !StringUtils.hasText(password))
         throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST);

      final var scopeParam = request.getParameter(OAuth2ParameterNames.SCOPE);
      final Set<String> requestedScopes = StringUtils.hasText(scopeParam)
              ? Stream.of(scopeParam.split(" "))
                  .filter(StringUtils::hasText)
                  .collect(Collectors.toSet())
              : Collections.emptySet();

      final var clientScopes = registeredClient.getScopes();

      checkClientScopes(clientScopes, requestedScopes);

      return new CustomPasswordGrantAuthenticationToken(cpf, password, clientPrincipal, requestedScopes, Map.of("cpf", cpf));
   }

   private void checkGrantTypes(final String requestGrantType, final Set<AuthorizationGrantType> clientGrantTypes) throws OAuth2AuthenticationException {
      final var clientGrantTypeStringSet = clientGrantTypes.stream()
              .filter(Objects::nonNull)
              .map(AuthorizationGrantType::getValue)
              .collect(Collectors.toSet());

      if (!clientGrantTypeStringSet.contains(requestGrantType))
         throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_GRANT);
   }

   private void checkClientScopes(final Set<String> clientScopes, final Set<String> requestedScopes) throws OAuth2AuthenticationException {
      if (CollectionUtils.isEmpty(clientScopes))
         throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INSUFFICIENT_SCOPE);

      requestedScopes.forEach(scope -> {
         if (!clientScopes.contains(scope))
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INSUFFICIENT_SCOPE);
      });
   }
}
