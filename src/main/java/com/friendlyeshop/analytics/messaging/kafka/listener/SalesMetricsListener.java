package com.friendlyeshop.analytics.messaging.kafka.listener;

import com.friendlyeshop.analytics.exception.InvalidOrderEventException;
import com.friendlyeshop.analytics.messaging.kafka.codec.OrderEventCodec;
import com.friendlyeshop.analytics.messaging.kafka.model.OrderEventEnvelope;
import com.friendlyeshop.analytics.service.MetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SalesMetricsListener {

    private static final String ORDER_PLACED = "order.placed";

    private static final Logger LOG = LoggerFactory.getLogger(SalesMetricsListener.class);

    private final OrderEventCodec orderEventCodec;

    private final MetricsService metricsService;

    public SalesMetricsListener(OrderEventCodec orderEventCodec, MetricsService metricsService) {
        this.orderEventCodec = orderEventCodec;
        this.metricsService = metricsService;
    }

    @KafkaListener(topics = "orders.events", groupId = "analytics.metrics")
    public void onOrderEvent(String message) {
        OrderEventEnvelope event;
        try {
            event = orderEventCodec.decode(message);
        } catch (InvalidOrderEventException exception) {
            LOG.warn("Skipping sales metrics event: {}", exception.getMessage());
            return;
        }
        if (!ORDER_PLACED.equals(event.eventType())) {
            LOG.warn("Ignoring unsupported event type {} for sales metrics", event.eventType());
            return;
        }
        if (event.total() == null || event.currency() == null) {
            LOG.warn("Skipping order.placed event {} without total or currency", event.eventId());
            return;
        }
        boolean recorded = metricsService.recordOrderPlaced(event);
        if (!recorded) {
            LOG.debug("Ignoring duplicate metrics event {}", event.eventId());
        }
    }
}
