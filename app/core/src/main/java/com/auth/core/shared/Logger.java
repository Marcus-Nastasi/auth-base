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

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Getter
    private enum Severity implements Serializable {

        INFO("INFO"), WARN("WARN"), DEBUG("DEBUG"), ERROR("ERROR");

        private final String name;

        Severity(final String name) {
            this.name = name;
        }
    }

    @Builder
    @RequiredArgsConstructor
    @JsonInclude(JsonInclude.Include.ALWAYS)
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
        private final Instant time;
    }


    public static void info(final String logCode, final String message) {
        final LoggerJson loggerJson = LoggerJson.builder()
                .severity(Severity.INFO)
                .logCode(logCode)
                .message(message)
                .time(LocalDateTime.now(Constants.CLOCK).toInstant(Constants.ZONE_OFFSET))
                .build();

        String jsonLog = null;
        try {
            jsonLog = objectMapper.writeValueAsString(loggerJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        log.info(jsonLog);
    };

    public static void info(final String logCode, final String message, final Object payload) {
        final LoggerJson loggerJson = LoggerJson.builder()
                .severity(Severity.INFO)
                .logCode(logCode)
                .message(message)
                .payload(payload)
                .time(LocalDateTime.now(Constants.CLOCK).toInstant(Constants.ZONE_OFFSET))
                .build();

        String jsonLog = null;
        try {
            jsonLog = objectMapper.writeValueAsString(loggerJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        log.info(jsonLog);
    };

    public static void info(final String logCode, final String message, final Object payload, final Throwable throwable) {
        final LoggerJson loggerJson = LoggerJson.builder()
                .severity(Severity.INFO)
                .logCode(logCode)
                .message(message)
                .payload(payload)
                .exception(throwable != null ? Arrays.toString(throwable.getStackTrace()) : "")
                .time(LocalDateTime.now(Constants.CLOCK).toInstant(Constants.ZONE_OFFSET))
                .build();

        String jsonLog = null;
        try {
            jsonLog = objectMapper.writeValueAsString(loggerJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        log.info(jsonLog);
    };
}
