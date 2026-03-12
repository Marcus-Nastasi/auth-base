package com.auth.core.shared;

import java.time.Clock;
import java.time.ZoneId;

public final class Constants {

    private Constants() {}

    public static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");
    public static final Clock CLOCK = Clock.systemDefaultZone().withZone(ZONE_ID);

    // Email constants
    public static final String EMAIL_HOST_KEY = "mail.smtp.host";
    public static final String EMAIL_PORT_KEY = "mail.smtp.port";
    public static final String EMAIL_AUTH_KEY = "mail.smtp.auth";
    public static final String EMAIL_STARTTLS_KEY = "mail.smtp.starttls.enable";
    public static final String MIME_TYPE = "text/html; charset=UTF-8";
}
