package com.auth.auth.infra.config;

import com.auth.auth.infra.util.PemUtils;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.token.*;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class KeyConfig {

   @Bean
   public JWKSource<SecurityContext> jwkSource(@Value("${spring.security.key.public.path}") final String publicKeyPath,
                                               @Value("${spring.security.key.private.path}") final String privateKeyPath,
                                               @Value("${spring.security.oauth2.kid}") final String kid) throws Exception {
      final RSAPublicKey publicKey = PemUtils.readPublicKey(publicKeyPath);
      final RSAPrivateKey privateKey = PemUtils.readPrivateKey(privateKeyPath);

      final RSAKey rsaKey = new RSAKey.Builder(publicKey)
           .privateKey(privateKey)
           .keyID(kid)
           .build();

      return new ImmutableJWKSet<>(new JWKSet(rsaKey));
   }

   @Bean
   public JwtEncoder jwtEncoder(@Value("${spring.security.key.private.path}") final String privateKeyPath,
                                @Value("${spring.security.key.public.path}") final String publicKeyPath) throws Exception {
      final var privateKey = PemUtils.readPrivateKey(privateKeyPath);
      final var publicKey = PemUtils.readPublicKey(publicKeyPath);
      return NimbusJwtEncoder.withKeyPair(publicKey, privateKey).build();
   }

   @Bean
   public JwtDecoder jwtDecoder(@Value("${spring.security.key.public.path}") final String publicKeyPath) throws Exception {
      return NimbusJwtDecoder.withPublicKey(PemUtils.readPublicKey(publicKeyPath)).build();
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
