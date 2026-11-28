package com.friendlyeshop.analytics.service;

import com.friendlyeshop.analytics.messaging.kafka.model.OrderEventEnvelope;
import com.friendlyeshop.analytics.model.OrderTimelineEntry;
import com.friendlyeshop.analytics.repository.OrderTimelineRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TimelineService {

    private final OrderTimelineRepository orderTimelineRepository;

    public TimelineService(OrderTimelineRepository orderTimelineRepository) {
        this.orderTimelineRepository = orderTimelineRepository;
    }

    @Transactional
    public boolean project(OrderEventEnvelope event) {
        int inserted = orderTimelineRepository.insertIfAbsent(
                event.eventId(),
                event.orderId(),
                event.tenantId(),
                event.eventType(),
                event.occurredAt(),
                event.total(),
                event.currency(),
                event.payload());
        return inserted > 0;
    }

    @Transactional(readOnly = true)
    public List<OrderTimelineEntry> findTimeline(UUID tenantId, UUID orderId) {
        if (orderId == null) {
            return orderTimelineRepository.findByTenantIdOrderByOccurredAtAscEventIdAsc(tenantId);
        }
        return orderTimelineRepository.findByTenantIdAndOrderIdOrderByOccurredAtAscEventIdAsc(tenantId, orderId);
    }
}
