package com.auth.auth.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.*;

@Configuration
public class GeneralAuthorizationConfig {

   @Bean
   public OAuth2AuthorizationService authorizationService() {
      return new InMemoryOAuth2AuthorizationService();
   }

   @Bean
   public AuthorizationServerSettings authorizationServerSettings(@Value("${spring.security.oauth2.issuer}")
                                                                  final String issuer) {
      return AuthorizationServerSettings.builder().issuer(issuer).build();
   }

   @Bean
   public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
   }

   @Bean
   public OAuth2TokenGenerator<OAuth2Token> tokenGenerator(final JwtEncoder jwtEncoder,
                                                           final OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer) {
      final JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
      jwtGenerator.setJwtCustomizer(jwtCustomizer);

      final OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();

      return new DelegatingOAuth2TokenGenerator(jwtGenerator, refreshTokenGenerator);
   }
}
