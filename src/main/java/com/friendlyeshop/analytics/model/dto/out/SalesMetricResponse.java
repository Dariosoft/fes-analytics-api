package com.friendlyeshop.analytics.model.dto.out;

import com.friendlyeshop.analytics.model.SalesMetric;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SalesMetricResponse(
        UUID tenantId,
        String currency,
        long ordersCount,
        BigDecimal totalAmount,
        Instant updatedAt) {

    public static SalesMetricResponse from(SalesMetric metric) {
        return new SalesMetricResponse(
                metric.getId().getTenantId(),
                metric.getId().getCurrency(),
                metric.getOrdersCount(),
                metric.getTotalAmount(),
                metric.getUpdatedAt());
    }
}
