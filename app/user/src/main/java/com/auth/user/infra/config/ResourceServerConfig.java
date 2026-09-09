package com.auth.user.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ResourceServerConfig {

   @Bean
   @Order(2)
   public SecurityFilterChain resourceServerSecurityFilterChain(final HttpSecurity http) {
      return http
           .securityMatcher("/api/v1/users/**")
           .csrf(AbstractHttpConfigurer::disable)
           .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/users/activate", "/api/v1/users/resend").permitAll()
                .anyRequest().authenticated())
           .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
           .build();
   }
}
