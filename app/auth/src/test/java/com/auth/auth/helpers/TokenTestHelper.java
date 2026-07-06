package com.auth.auth.helpers;

import com.auth.core.shared.Constants;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public final class TokenTestHelper {

   private TokenTestHelper() {}

   public static OAuth2AccessToken getAccessToken() {
      return new OAuth2AccessToken(
           OAuth2AccessToken.TokenType.BEARER,
           "daklnfkdanfla120938102dkadmaslk",
           Instant.now(Constants.CLOCK),
           Instant.now(Constants.CLOCK).plus(10, ChronoUnit.SECONDS)
      );
   }
}
