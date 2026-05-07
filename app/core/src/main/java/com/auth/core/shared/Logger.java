package com.auth.core.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
public final class Logger {

    public Logger() {}

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Getter
    @RequiredArgsConstructor
    private enum Severity implements Serializable {

        INFO("INFO"), WARN("WARN"), DEBUG("DEBUG"), ERROR("ERROR");

        private final String name;
    }

    @Builder
    @JsonInclude
    @RequiredArgsConstructor
    private static class LoggerJson implements Serializable {

        @Serial
        private static final long serialVersionUID = 71892739218302L;

        @JsonProperty("severity")
        private final Severity severity;

        @JsonProperty("log_code")
        private final String logCode;

        @JsonProperty("message")
        private final String message;

        @JsonProperty("payload")
        private final Object payload;

        @JsonProperty("exception")
        private final String exception;

        @JsonProperty("time")
        private final Instant timestamp;
    }

    /**
     */
    private static void defaultLogger(final Severity severity,
                                      final String logCode,
                                      final String message,
                                      final Object payload,
                                      final Throwable throwable) {
        final LoggerJson loggerJson = LoggerJson.builder()
                .severity(severity)
                .logCode(logCode)
                .message(message)
                .payload(payload)
                .exception(throwable != null ? Arrays.toString(throwable.getStackTrace()) : null)
                .timestamp(LocalDateTime.now(Constants.CLOCK).toInstant(Constants.ZONE_OFFSET))
                .build();

        String jsonLog = null;
        try {
            jsonLog = objectMapper.writeValueAsString(loggerJson);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        switch (severity) {
            case INFO: log.info(jsonLog);
                break;
            case ERROR: log.error(jsonLog);
                break;
            case WARN: log.warn(jsonLog);
                break;
            case DEBUG: log.debug(jsonLog);
                break;
            default: {
                log.info("severity missing on logger, using default 'INFO'");
                log.info(jsonLog);
            }
        }
    }

    // info
    public static void info(final String logCode, final String message) {
        defaultLogger(Severity.INFO, logCode, message, null, null);
    };

    public static void info(final String logCode, final String message, final Object payload) {
        defaultLogger(Severity.INFO, logCode, message, payload, null);
    };

    public static void info(final String logCode, final String message, final Object payload, final Throwable throwable) {
        defaultLogger(Severity.INFO, logCode, message, payload, throwable);
    };

    // error
    public static void error(final String logCode, final String message) {
        defaultLogger(Severity.ERROR, logCode, message, null, null);
    };

    public static void error(final String logCode, final String message, final Object payload) {
        defaultLogger(Severity.ERROR, logCode, message, payload, null);
    };

    public static void error(final String logCode, final String message, final Object payload, final Throwable throwable) {
        defaultLogger(Severity.ERROR, logCode, message, payload, throwable);
    };

    // warn
    public static void warn(final String logCode, final String message) {
        defaultLogger(Severity.WARN, logCode, message, null, null);
    };

    public static void warn(final String logCode, final String message, final Object payload) {
        defaultLogger(Severity.WARN, logCode, message, payload, null);
    };

    public static void warn(final String logCode, final String message, final Object payload, final Throwable throwable) {
        defaultLogger(Severity.WARN, logCode, message, payload, throwable);
    };

    // debug
    public static void debug(final String logCode, final String message) {
        defaultLogger(Severity.DEBUG, logCode, message, null, null);
    };

    public static void debug(final String logCode, final String message, final Object payload) {
        defaultLogger(Severity.DEBUG, logCode, message, payload, null);
    };

    public static void debug(final String logCode, final String message, final Object payload, final Throwable throwable) {
        defaultLogger(Severity.DEBUG, logCode, message, payload, throwable);
    };
}
