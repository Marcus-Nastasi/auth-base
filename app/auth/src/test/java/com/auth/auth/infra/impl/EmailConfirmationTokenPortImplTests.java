package com.auth.auth.infra.impl;

import com.auth.auth.infra.util.PemUtils;
import com.auth.core.domain.User;
import com.auth.core.exceptions.ForbiddenException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
final class EmailConfirmationTokenPortImplTests {

   private static final String PRIVATE_KEY_PATH = "src/test/resources/keys/private-test.pem";
   private static final String PUBLIC_KEY_PATH = "src/test/resources/keys/public-test.pem";
   private static final String KID = "test-kid";
   private static final String ISSUER = "http://localhost:8080";

   private EmailConfirmationTokenPortImpl sut;

   @BeforeEach
   void setUp() throws Exception {
      sut = new EmailConfirmationTokenPortImpl(
           PRIVATE_KEY_PATH,
           PUBLIC_KEY_PATH,
           KID,
           ISSUER
      );
   }

   // -------------------------------------------------------------------------
   // Helpers
   // -------------------------------------------------------------------------
   private User buildUser() {
      final User user = new User();
      user.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
      user.setEmail("test@example.com");
      user.setCpf("12345678900");
      return user;
   }

   // =========================================================================
   // generate()
   // =========================================================================
   @Nested
   @DisplayName("generate()")
   class Generate {

      @Test
      @DisplayName("deve retornar um JWT serializado não nulo e não vazio")
      void shouldReturnNonBlankToken() throws ForbiddenException {
         final String token = sut.generate(buildUser());
         assertThat(token).isNotBlank();
      }

      @Test
      @DisplayName("token deve ter três partes (header.payload.signature)")
      void shouldHaveThreeParts() throws ForbiddenException {
         final String token = sut.generate(buildUser());
         assertThat(token.split("\\.")).hasSize(3);
      }

      @Test
      @DisplayName("header deve conter algoritmo RS256 e kid correto")
      void shouldHaveCorrectHeader() throws Exception {
         final String token = sut.generate(buildUser());
         final SignedJWT jwt = SignedJWT.parse(token);

         assertThat(jwt.getHeader().getAlgorithm().getName()).isEqualTo("RS256");
         assertThat(jwt.getHeader().getKeyID()).isEqualTo(KID);
      }

      @Test
      @DisplayName("claims devem conter subject, issuer, email, cpf, scope e typ")
      void shouldHaveCorrectClaims() throws Exception {
         final User user = buildUser();
         final String token = sut.generate(user);
         final SignedJWT jwt = SignedJWT.parse(token);
         final var claims = jwt.getJWTClaimsSet();

         assertThat(claims.getSubject()).isEqualTo(user.getId().toString());
         assertThat(claims.getIssuer()).isEqualTo(ISSUER);
         assertThat(claims.getStringClaim("email")).isEqualTo(user.getEmail());
         assertThat(claims.getStringClaim("cpf")).isEqualTo(user.getCpf());
         assertThat(claims.getStringClaim("scope")).isEqualTo("email.activate");
         assertThat(claims.getStringClaim("typ")).isEqualTo("email_confirmation");
      }

      @Test
      @DisplayName("expirationTime deve ser aproximadamente 20 minutos após issueTime")
      void shouldExpireIn20Minutes() throws Exception {
         final String token = sut.generate(buildUser());
         final SignedJWT jwt = SignedJWT.parse(token);
         final var claims = jwt.getJWTClaimsSet();

         final long diffMinutes = (claims.getExpirationTime().getTime()
              - claims.getIssueTime().getTime()) / 60_000;

         assertThat(diffMinutes).isEqualTo(20);
      }

      @Test
      @DisplayName("assinatura deve ser verificável com a chave pública")
      void shouldBeVerifiableWithPublicKey() throws Exception {
         final String token = sut.generate(buildUser());
         final SignedJWT jwt = SignedJWT.parse(token);

         // carrega a chave pública via PemUtils do próprio projeto
         final var publicKey = com.auth.auth.infra.util.PemUtils.readPublicKey(PUBLIC_KEY_PATH);
         final var verifier = new RSASSAVerifier(publicKey);

         assertThat(jwt.verify(verifier)).isTrue();
      }

      @Test
      @DisplayName("issueTime deve ser próximo do instante atual")
      void shouldHaveIssueTimeNearNow() throws Exception {
         final Instant before = Instant.now().minusSeconds(2);
         final String token = sut.generate(buildUser());
         final Instant after = Instant.now().plusSeconds(2);

         final Instant iat = SignedJWT.parse(token)
              .getJWTClaimsSet()
              .getIssueTime()
              .toInstant();

         assertThat(iat).isBetween(before, after);
      }
   }

   // =========================================================================
   // validate()
   // =========================================================================
   @Nested
   @DisplayName("validate()")
   class Validate {

      @Test
      @DisplayName("token válido não deve lançar exceção")
      void shouldNotThrowForValidToken() throws ForbiddenException {
         final String token = sut.generate(buildUser());
         assertThatCode(() -> sut.validate(token)).doesNotThrowAnyException();
      }

      @Test
      @DisplayName("token com assinatura adulterada deve lançar ForbiddenException")
      void shouldThrowForTamperedSignature() throws ForbiddenException {
         final String token = sut.generate(buildUser());
         // substitui os últimos 4 chars da assinatura por 'AAAA'
         final String tampered = token.substring(0, token.length() - 4) + "AAAA";

         assertThatThrownBy(() -> sut.validate(tampered)).isInstanceOf(ForbiddenException.class);
      }

      @Test
      @DisplayName("token com typ inválido deve lançar ForbiddenException")
      void shouldThrowForInvalidTyp() throws Exception {
         final User user = buildUser();
         // monta manualmente um token com typ errado
         final Instant now = Instant.now();
         final var claims = new JWTClaimsSet.Builder()
              .subject(user.getId().toString())
              .issuer(ISSUER)
              .claim("email", user.getEmail())
              .claim("cpf", user.getCpf())
              .claim("scope", "email.activate")
              .claim("typ", "wrong_type")
              .issueTime(Date.from(now))
              .expirationTime(Date.from(now.plusSeconds(1200)))
              .build();

         final String token = buildSignedToken(claims);
         assertThatThrownBy(() -> sut.validate(token))
              .isInstanceOf(ForbiddenException.class)
              .hasMessageContaining("Invalid token");
      }

      @Test
      @DisplayName("token com scope inválido deve lançar ForbiddenException")
      void shouldThrowForInvalidScope() throws Exception {
         final User user = buildUser();
         final Instant now = Instant.now();
         final var claims = new JWTClaimsSet.Builder()
              .subject(user.getId().toString())
              .issuer(ISSUER)
              .claim("email", user.getEmail())
              .claim("cpf", user.getCpf())
              .claim("scope", "wrong.scope")
              .claim("typ", "email_confirmation")
              .issueTime(Date.from(now))
              .expirationTime(Date.from(now.plusSeconds(1200)))
              .build();

         final String token = buildSignedToken(claims);
         assertThatThrownBy(() -> sut.validate(token))
              .isInstanceOf(ForbiddenException.class)
              .hasMessageContaining("Invalid");
      }

      @Test
      @DisplayName("token expirado deve lançar ForbiddenException")
      void shouldThrowForExpiredToken() throws Exception {
         final User user = buildUser();
         // token que expirou 1 segundo atrás
         final Instant now = Instant.now();
         final var claims = new JWTClaimsSet.Builder()
              .subject(user.getId().toString())
              .issuer(ISSUER)
              .claim("email", user.getEmail())
              .claim("cpf", user.getCpf())
              .claim("scope", "email.activate")
              .claim("typ", "email_confirmation")
              .issueTime(java.util.Date.from(now.minusSeconds(60)))
              .expirationTime(java.util.Date.from(now.minusSeconds(1)))
              .build();

         final String token = buildSignedToken(claims);
         assertThatThrownBy(() -> sut.validate(token))
              .isInstanceOf(ForbiddenException.class)
              .hasMessageContaining("expired");
      }

      @Test
      @DisplayName("string não JWT deve lançar ForbiddenException")
      void shouldThrowForMalformedToken() {
         assertThatThrownBy(() -> sut.validate("not.a.jwt")).isInstanceOf(ForbiddenException.class);
      }

      @Test
      @DisplayName("string vazia deve lançar ForbiddenException")
      void shouldThrowForBlankToken() {
         assertThatThrownBy(() -> sut.validate("")).isInstanceOf(ForbiddenException.class);
      }

      // ---------------------------------------------------------------------
      // Helper: assina um JWTClaimsSet com a chave privada de teste
      // ---------------------------------------------------------------------
      private String buildSignedToken(final JWTClaimsSet claims) throws Exception {
         final var privateKey = PemUtils.readPrivateKey(PRIVATE_KEY_PATH);
         final var header = new JWSHeader.Builder(com.nimbusds.jose.JWSAlgorithm.RS256)
              .keyID(KID)
              .build();
         final var jwt = new SignedJWT(header, claims);
         jwt.sign(new RSASSASigner(privateKey));
         return jwt.serialize();
      }
   }
}
