package com.auth.auth.adapters.inbound.interceptors;

import com.auth.core.domain.enums.UserRole;
import com.auth.core.exceptions.ForbiddenException;
import com.auth.core.ports.inbound.auth.HttpInterceptor;
import com.auth.core.ports.inbound.auth.TokenPort;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Component("IdEqualsOrAdminInterceptor")
public class IdEqualsOrAdminInterceptor implements HttpInterceptor {

    private final TokenPort tokenPort;

    public IdEqualsOrAdminInterceptor(final TokenPort tokenPort) {
        this.tokenPort = tokenPort;
    }

    @Override
    public void validate(final Object[] data) throws ForbiddenException {
        try {
            if (!(data[0] instanceof UUID id))
                throw new ForbiddenException("");

            if (!(data[1] instanceof String token))
                throw new ForbiddenException("");

            final DecodedJWT d = (DecodedJWT) tokenPort.validate(getToken(token));
            final UUID idFromToken = UUID.fromString(d.getSubject());
            final UserRole userRoleFromToken = extractUserRoleFromScope(d);

            if (!isIdEqualsOrAdmin(id, idFromToken, userRoleFromToken)) {
                throw new ForbiddenException("");
            }
        } catch (IllegalArgumentException e) {
            throw new ForbiddenException("");
        }
    }

    private String getToken(final String tokenHeader) throws ForbiddenException {
        if (tokenHeader == null)
            throw new ForbiddenException("");

        if (tokenHeader.contains("Bearer"))
            return Arrays
                    .stream(tokenHeader.split(" "))
                    .toList()
                    .getLast();

        return tokenHeader;
    }

    private boolean isIdEqualsOrAdmin(final UUID userId,
                                      final UUID idFromToken,
                                      final UserRole userRoleFromToken) {
        if (UserRole.ADMIN.equals(userRoleFromToken))
            return true;

        return userId.equals(idFromToken);
    }

    private UserRole extractUserRoleFromScope(DecodedJWT d) {
        final String scope = Optional.ofNullable(d.getClaim("scope"))
                .map(Claim::asString)
                .orElseThrow(() -> new ForbiddenException(""));

        if (scope.contains("users.admin")) {
            return UserRole.ADMIN;
        } else {
            return UserRole.USER;
        }
    }
}
