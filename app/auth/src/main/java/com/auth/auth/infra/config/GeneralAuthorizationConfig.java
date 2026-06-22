package com.auth.auth.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

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
}
