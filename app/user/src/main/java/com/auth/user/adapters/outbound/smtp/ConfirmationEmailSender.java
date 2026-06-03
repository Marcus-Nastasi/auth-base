package com.auth.user.adapters.outbound.smtp;

import com.auth.core.domain.User;
import com.auth.core.ports.outbound.auth.ConfirmationEmailSenderPort;
import com.auth.core.shared.Constants;
import com.auth.core.shared.Logger;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

@Service
@ConditionalOnProperty(value = "spring.mail.enable", havingValue = "true")
public class ConfirmationEmailSender implements ConfirmationEmailSenderPort {

    private static final String LOG_CODE = "CONFIRMATION-EMAIL-SENDER";

    private final String host;

    private final String port;

    private final String username;

    private final String password;

    private final String team;
    private final OAuth2TokenGenerator<?> tokenGenerator;

    public ConfirmationEmailSender(
                                   @Value("${spring.mail.host}") final String host,
                                   @Value("${spring.mail.port}") final String port,
                                   @Value("${spring.mail.username}") final String username,
                                   @Value("${spring.mail.password}") final String password,
                                   @Value("${spring.mail.team}") final String team,
                                   OAuth2TokenGenerator<?> tokenGenerator) {
        this.host     = host;
        this.port     = port;
        this.username = username;
        this.password = password;
        this.team     = team;
        this.tokenGenerator = tokenGenerator;
    }

    @Override
    public void send(final User data) {
        try {
            var auth = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication());

            final OAuth2ClientAuthenticationToken clientPrincipal = extractClientPrincipal(auth);
            final RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();

            final var token = tokenGenerator.generate(DefaultOAuth2TokenContext.builder()
                    .registeredClient(registeredClient)
                    .principal(auth)
                    .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                    .authorizedScopes(Set.of("email.activate"))
                    .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrant(auth)
                    .build());

            final Properties props = setProperties();
            final Session session = createSession(props);

            final Message message = getMessage(session, data, token.getTokenValue());

            Transport.send(message);
        } catch (UnsupportedEncodingException | MessagingException e) {
            Logger.error(LOG_CODE, e.getMessage(), e);
        }
    }

    private Properties setProperties() {
        final Properties props = new Properties();
        props.put(Constants.EMAIL_HOST_KEY, host);
        props.put(Constants.EMAIL_PORT_KEY, port);
        props.put(Constants.EMAIL_AUTH_KEY, Boolean.TRUE.toString().toLowerCase());
        props.put(Constants.EMAIL_STARTTLS_KEY, Boolean.TRUE.toString().toLowerCase());
        return props;
    }

    private Session createSession(final Properties props) {
        return Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    private Message getMessage(final Session session,
                               final User user,
                               final String token) throws UnsupportedEncodingException, MessagingException {

        final Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username, team));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(user.getEmail()));
        message.setSubject("Roots. io - Confirme seu e-mail");
        message.setContent(getMessage(user.getFirstName(), token), Constants.MIME_TYPE);
        return message;
    }

    private String getMessage(String name, String token) {
        return String.format("""
            <h3>Olá %s, tudo bem?</h3>
            <h4>Clique para confirmar seu e-mail: <a href="http://localhost:8080/api/v1/user/activate?token=%s">clique aqui</a></h4>
        """, name, token);
    }

    // TODO: remove this and create a port and impl to handle personalized token creation
    private OAuth2ClientAuthenticationToken extractClientPrincipal(final Authentication authentication) throws OAuth2AuthenticationException {
        if (authentication.getPrincipal() instanceof OAuth2ClientAuthenticationToken authenticationToken)
            return authenticationToken;
        else
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
    }
}
