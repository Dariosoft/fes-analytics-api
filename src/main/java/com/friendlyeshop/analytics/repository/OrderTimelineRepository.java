package com.friendlyeshop.analytics.repository;

import com.friendlyeshop.analytics.model.OrderTimelineEntry;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderTimelineRepository extends JpaRepository<OrderTimelineEntry, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO order_timeline (event_id, order_id, tenant_id, event_type, occurred_at,
                                        total, currency, payload, created_at)
            VALUES (:eventId, :orderId, :tenantId, :eventType, :occurredAt,
                    :total, :currency, CAST(:payload AS jsonb), NOW())
            ON CONFLICT (event_id) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("eventId") UUID eventId,
                       @Param("orderId") UUID orderId,
                       @Param("tenantId") UUID tenantId,
                       @Param("eventType") String eventType,
                       @Param("occurredAt") Instant occurredAt,
                       @Param("total") BigDecimal total,
                       @Param("currency") String currency,
                       @Param("payload") String payload);

    List<OrderTimelineEntry> findByTenantIdOrderByOccurredAtAscEventIdAsc(UUID tenantId);

    List<OrderTimelineEntry> findByTenantIdAndOrderIdOrderByOccurredAtAscEventIdAsc(UUID tenantId, UUID orderId);
}
