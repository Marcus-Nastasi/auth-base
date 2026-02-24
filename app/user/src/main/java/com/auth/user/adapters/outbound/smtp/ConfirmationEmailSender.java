package com.auth.user.adapters.outbound.smtp;

import com.auth.core.domain.User;
import com.auth.core.ports.inbound.auth.TokenPort;
import com.auth.core.ports.outbound.auth.ConfirmationEmailSenderPort;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

@Slf4j
@Service
@ConditionalOnProperty(value = "spring.mail.enable", havingValue = "true")
public class ConfirmationEmailSender implements ConfirmationEmailSenderPort {

    private final String host;

    private final String port;

    private final String username;

    private final String password;

    private final String team;

    private final TokenPort tokenPort;

    public ConfirmationEmailSender(final TokenPort tokenPort) throws DotenvException {
        final Dotenv dotenv = Dotenv.load();

        this.host = dotenv.get("MAIL_HOST");
        this.port = dotenv.get("MAIL_PORT");
        this.username = dotenv.get("MAIL_USERNAME");
        this.password = dotenv.get("MAIL_PASSWORD");
        this.team = dotenv.get("MAIL_TEAM");

        this.tokenPort = tokenPort;
    }

    @Override
    public void send(final User data) {
        try {
            final Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            final Session session = Session.getInstance(props,
                    new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });

            final Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username, team));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(data.getEmail()));
            message.setSubject("Roots. io - Confirme seu e-mail");
            message.setContent(getMessage(data.getFirstName(), tokenPort.emailConfirmation(data)), "text/html; charset=UTF-8");

            Transport.send(message);
        } catch (UnsupportedEncodingException | MessagingException e) {
            log.error(e.getMessage(), e);
        }
    }

    private String getMessage(String name, String token) {
        return String.format("""
            <h3>Olá %s, tudo bem?</h3>
            <h4>Clique para confirmar seu e-mail: <a href="http://localhost:8080/api/v1/user/activate?token=%s">clique aqui</a></h4>
        """, name, token);
    }
}
