package com.friendlyeshop.analytics.messaging.kafka.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import tools.jackson.databind.JsonNode;

public record OrderEventEnvelope(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        UUID orderId,
        UUID tenantId,
        JsonNode items,
        BigDecimal total,
        String currency,
        String payload) {
}
