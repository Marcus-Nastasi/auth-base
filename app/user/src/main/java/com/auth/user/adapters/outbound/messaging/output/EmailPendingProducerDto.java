package com.auth.user.adapters.outbound.messaging.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.io.Serializable;
import java.time.Instant;

@JsonInclude
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record EmailPendingProducerDto(
        @JsonProperty("event_type")
        String eventType,
        @JsonProperty("event_version")
        String eventVersion,
        @JsonProperty("event_id")
        String eventId,
        @JsonProperty("occurred_at")
        Instant occurredAt,
        EmailPendingData data
) implements Serializable {

    @JsonInclude
    public record EmailPendingData(
            @JsonProperty("user_id")
            String userId,
            String email,
            @JsonProperty("first_name")
            String firstName,
            @JsonProperty("confirmation_token")
            String confirmationToken
    ) implements Serializable {}
}
