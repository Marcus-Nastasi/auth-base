package com.auth.auth.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class FormLoginConfig {

   @Bean
   @Order(3)
   public SecurityFilterChain defaultSecurityFilterChain(final HttpSecurity http) {
      http
        .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login").permitAll()  // ← login é público
                .anyRequest().authenticated())
        .formLogin(form -> form
                .loginPage("/login")                    // ← customiza página de login
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/oauth2/authorize")  // ← redireciona para authorize após sucesso
                .permitAll())
        .csrf(Customizer.withDefaults());               // ← mantém CSRF

      return http.build();
   }
}
