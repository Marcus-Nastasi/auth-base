package com.auth.auth.infra.impl;

import com.auth.auth.infra.util.PemUtils;
import com.auth.core.domain.User;
import com.auth.core.exceptions.ForbiddenException;
import com.auth.core.ports.outbound.auth.PersonalizedTokenPort;
import com.auth.core.shared.Constants;
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
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component("emailConfirmationTokenPortImpl")
public class EmailConfirmationTokenPortImpl implements PersonalizedTokenPort {

   private final RSAPrivateKey privateKey;
   private final RSAPublicKey publicKey;
   private final String kid;
   private final String issuer;

   public EmailConfirmationTokenPortImpl(@Value("${spring.security.key.private.path}") final String privateKeyPath,
                                         @Value("${spring.security.key.public.path}") final String publicKeyPath,
                                         @Value("${spring.security.oauth2.kid}") final String kid,
                                         @Value("${spring.security.oauth2.issuer}") final String issuer) throws Exception {
      this.privateKey = PemUtils.readPrivateKey(privateKeyPath);
      this.publicKey = PemUtils.readPublicKey(publicKeyPath);
      this.kid = kid;
      this.issuer = issuer;
   }

   @Override
   public String generate(final User user) throws ForbiddenException {
      try {
         final Instant now = Instant.now(Constants.CLOCK);
         final Instant expiry = now.plus(20, ChronoUnit.MINUTES);

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
         final var jwt = SignedJWT.parse(token);
         final var rsaVerifier = new RSASSAVerifier(publicKey);

         // Verifica assinatura
         if (!jwt.verify(rsaVerifier))
            throw new ForbiddenException("Invalid token signature");

         final var currentDate = Date.from(Instant.now(Constants.CLOCK));

         // Verifica expiração
         if (jwt.getJWTClaimsSet().getExpirationTime().before(currentDate))
            throw new ForbiddenException("Token expired");

         // Verifica tipo
         final String typ = (String) jwt.getJWTClaimsSet().getClaim("typ");
         final String scope = (String) jwt.getJWTClaimsSet().getClaim("scope");

         if (!"email_confirmation".equalsIgnoreCase(typ))
            throw new ForbiddenException("Invalid token type");
         if (!"email.activate".equalsIgnoreCase(scope))
            throw new ForbiddenException("Invalid scope");

      } catch (Exception e) {
         throw new ForbiddenException("Invalid token format", e);
      }
   }
}
