package com.auth.auth.infra.impl;

import com.auth.auth.infra.util.PemUtils;
import com.auth.core.domain.User;
import com.auth.core.exceptions.ForbiddenException;
import com.auth.core.ports.inbound.auth.EmailConfirmationTokenPort;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Component("emailConfirmationTokenPortImpl")
public class EmailConfirmationTokenPortImpl implements EmailConfirmationTokenPort {

   private final RSAPrivateKey privateKey;
   private final String kid;
   private final String issuer;

   public EmailConfirmationTokenPortImpl(@Value("${spring.security.key.private.path}") final String privateKeyFileName,
                                         @Value("${spring.security.oauth2.kid}") final String kid,
                                         @Value("${spring.security.oauth2.issuer}") final String issuer) throws Exception {
      this.privateKey = PemUtils.readPrivateKey(privateKeyFileName);
      this.kid = kid;
      this.issuer = issuer;
   }

   @Override
   public String generate(final User user) throws ForbiddenException {
      try {
         final Instant now = Instant.now();
         final Instant expiry = LocalDateTime.now()
              .plusMinutes(20)
              .toInstant(ZoneOffset.UTC);

         final JWTClaimsSet claims = new JWTClaimsSet.Builder()
              .subject(user.getId().toString())
              .issuer(issuer)
              .claim("email", user.getEmail())
              .claim("cpf", user.getCpf())
              .claim("scope", "email.activate")
              .claim("typ", "email_confirmation")
              .issueTime(Date.from(now))
              .expirationTime(Date.from(expiry))
              .build();

         final JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
              .keyID(kid)
              .build();

         final SignedJWT signedJWT = new SignedJWT(header, claims);
         signedJWT.sign(new RSASSASigner(privateKey));

         return signedJWT.serialize();
      } catch (JOSEException e) {
         throw new ForbiddenException("Failed to generate email confirmation token", e);
      }
   }

   @Override
   public void validate(final String token) throws ForbiddenException {
      try {
         final SignedJWT jwt = SignedJWT.parse(token);

         // Verifica assinatura
         if (!jwt.verify(new RSASSAVerifier(PemUtils.readPublicKey("${spring.security.key.public.path}"))))
            throw new ForbiddenException("Invalid token signature");

         // Verifica expiração
         if (jwt.getJWTClaimsSet().getExpirationTime().before(new Date()))
            throw new ForbiddenException("Token expired");

         // Verifica tipo
         final String typ = (String) jwt.getJWTClaimsSet().getClaim("typ");
         if (!"email_confirmation".equals(typ))
            throw new ForbiddenException("Invalid token type");
      } catch (Exception e) {
         throw new ForbiddenException("Invalid token format", e);
      }
   }
}
