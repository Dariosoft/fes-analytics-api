package com.friendlyeshop.analytics.model.dto.out;

import com.friendlyeshop.analytics.model.OrderTimelineEntry;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderTimelineEntryResponse(
        UUID eventId,
        UUID orderId,
        UUID tenantId,
        String eventType,
        Instant occurredAt,
        BigDecimal total,
        String currency,
        Instant createdAt) {

    public static OrderTimelineEntryResponse from(OrderTimelineEntry entry) {
        return new OrderTimelineEntryResponse(
                entry.getEventId(),
                entry.getOrderId(),
                entry.getTenantId(),
                entry.getEventType(),
                entry.getOccurredAt(),
                entry.getTotal(),
                entry.getCurrency(),
                entry.getCreatedAt());
    }
}
