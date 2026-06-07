package com.auth.auth.infra.config;

import com.auth.auth.infra.grant.CustomPasswordGrantAuthenticationConverter;
import com.auth.auth.infra.grant.CustomPasswordGrantAuthenticationProvider;
import com.auth.core.ports.inbound.auth.PasswordEncoderPort;
import com.auth.core.ports.outbound.user.FindUserPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class AuthorizationServerConfig {

   private final FindUserPort findUserPort;
   private final PasswordEncoderPort passwordEncoderPort;
   private final OAuth2AuthorizationService authorizationService;
   private final OAuth2TokenGenerator<OAuth2Token> tokenGenerator;
   private final JwtDecoder jwtDecoder;
   private final String issuer;

   public AuthorizationServerConfig(final FindUserPort findUserPort,
                                    final PasswordEncoderPort passwordEncoderPort,
                                    final OAuth2AuthorizationService authorizationService,
                                    final OAuth2TokenGenerator<OAuth2Token> tokenGenerator,
                                    final JwtDecoder jwtDecoder,
                                    @Value("${spring.security.oauth2.issuer}")
                                     final String issuer) {
      this.findUserPort = findUserPort;
      this.passwordEncoderPort = passwordEncoderPort;
      this.authorizationService = authorizationService;
      this.tokenGenerator = tokenGenerator;
      this.jwtDecoder = jwtDecoder;
      this.issuer = issuer;
   }

   @Bean
   @Order(1)
   public SecurityFilterChain authorizationServerSecurityFilterChain(final HttpSecurity http) {
      final var authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();

      final var converter = new CustomPasswordGrantAuthenticationConverter();
      final var provider = new CustomPasswordGrantAuthenticationProvider(
           findUserPort, passwordEncoderPort, authorizationService, tokenGenerator
      );

      http
           .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
           .with(
               authorizationServerConfigurer,
               configurer -> configurer
                    .authorizationServerSettings(AuthorizationServerSettings.builder().issuer(issuer).build())
                    .tokenEndpoint(endpoint -> endpoint.authenticationProvider(provider).accessTokenRequestConverter(converter))
                    .oidc(oidcConfigurer -> oidcConfigurer.clientRegistrationEndpoint(Customizer.withDefaults()))
           );

      http.exceptionHandling(ex ->
           ex.defaultAuthenticationEntryPointFor(
                new LoginUrlAuthenticationEntryPoint("/oauth2/token"),
                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
           )
      );

      http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder)));

      return http.build();
   }
}
