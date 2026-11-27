package com.friendlyeshop.analytics.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sales_metrics")
@Getter
@Setter
@NoArgsConstructor
public class SalesMetric {

    @EmbeddedId
    private SalesMetricId id;

    @Column(name = "orders_count", nullable = false)
    private long ordersCount;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class SalesMetricId implements Serializable {

        @Column(name = "tenant_id", nullable = false)
        private UUID tenantId;

        @Column(name = "currency", nullable = false, length = 3)
        private String currency;
    }
}
