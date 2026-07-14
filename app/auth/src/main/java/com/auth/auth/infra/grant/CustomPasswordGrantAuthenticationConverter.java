package com.auth.auth.infra.grant;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomPasswordGrantAuthenticationConverter implements AuthenticationConverter {

   @Override
   public Authentication convert(final HttpServletRequest request) {
      final var grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
      if (!CustomPasswordGrantAuthenticationToken.GRANT_TYPE.getValue().equals(grantType))
         return null;

      final Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();
      if (clientPrincipal == null)
         throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);

      final var cpf = request.getParameter("cpf");
      final var password = request.getParameter("password");

      if (!StringUtils.hasText(cpf) || !StringUtils.hasText(password))
         throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST);

      final var scopeParam = request.getParameter(OAuth2ParameterNames.SCOPE);
      final Set<String> scopes = StringUtils.hasText(scopeParam)
              ? Stream.of(scopeParam.split(" ")).collect(Collectors.toSet())
              : Collections.emptySet();

      final var additionalParameters = new HashMap<String, Object>(Map.of("cpf", cpf));

      return new CustomPasswordGrantAuthenticationToken(cpf, password, clientPrincipal, scopes, additionalParameters);
   }
}
