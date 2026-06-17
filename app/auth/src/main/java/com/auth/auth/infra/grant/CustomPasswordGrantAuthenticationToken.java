package com.auth.auth.infra.grant;

import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

import java.util.Map;
import java.util.Set;

@Getter
public class CustomPasswordGrantAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

   public static final AuthorizationGrantType GRANT_TYPE = new AuthorizationGrantType("urn:custom:grant-type:password");

   private final String cpf;
   private final String password;
   private final Set<String> scopes;

   public CustomPasswordGrantAuthenticationToken(final String cpf,
                                                 final String password,
                                                 final Authentication clientPrincipal,
                                                 final Set<String> scopes,
                                                 final Map<String, Object> additionalParameters) {
      super(GRANT_TYPE, clientPrincipal, additionalParameters);
      this.cpf = cpf;
      this.password = password;
      this.scopes = scopes;
   }
}
