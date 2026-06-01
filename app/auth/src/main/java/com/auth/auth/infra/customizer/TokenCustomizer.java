package com.auth.auth.infra.customizer;

import com.auth.core.ports.outbound.user.FindUserPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.UUID;

@Configuration
public class TokenCustomizer {

   @Bean
   public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer(final FindUserPort findUserPort) {
      return context -> {
         if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) return;

         final String principalName = context.getPrincipal().getName();

         try {
            final UUID userId = UUID.fromString(principalName);
            findUserPort.findById(userId).ifPresent(user ->
                    context.getClaims()
                         .claim("email", user.getEmail())
                         .claim("cpf", user.getCpf())
                         .claim("typ", "access"));
         } catch (IllegalArgumentException ignored) {
            // client_credentials: principal é o client_id, não um UUID de usuário
            context.getClaims().claim("typ", "access");
         }
      };
   }
}
