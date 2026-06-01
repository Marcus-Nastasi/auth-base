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
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

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
   public JwtEncoder jwtEncoder(final JWKSource<SecurityContext> jwkSource) {
      return new NimbusJwtEncoder(jwkSource);
   }
}
