package com.auth.auth.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

@Configuration
public class FormLoginConfig {

   @Bean
   @Order(3)
   public SecurityFilterChain defaultSecurityFilterChain(final HttpSecurity http) {
      final var requestCache = new HttpSessionRequestCache();
      requestCache.setMatchingRequestParameterName("continue");

      http
        .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/error").permitAll()
                .anyRequest().authenticated())
        .formLogin(Customizer.withDefaults())
        .requestCache(cacheConfigure -> cacheConfigure.requestCache(requestCache))
        .csrf(Customizer.withDefaults());
        //.logout(Customizer.withDefaults())
//        .formLogin(form -> form
//                .loginPage("/login")
//                .loginProcessingUrl("/login")
//                .defaultSuccessUrl("/oauth2/authorize")  // ← redireciona para authorize após sucesso
//                .permitAll())

      return http.build();
   }
}
