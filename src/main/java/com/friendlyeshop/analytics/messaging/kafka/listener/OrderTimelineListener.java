package com.friendlyeshop.analytics.messaging.kafka.listener;

import com.friendlyeshop.analytics.exception.InvalidOrderEventException;
import com.friendlyeshop.analytics.messaging.kafka.codec.OrderEventCodec;
import com.friendlyeshop.analytics.messaging.kafka.model.OrderEventEnvelope;
import com.friendlyeshop.analytics.service.TimelineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderTimelineListener {

    private static final Logger LOG = LoggerFactory.getLogger(OrderTimelineListener.class);

    private final OrderEventCodec orderEventCodec;

    private final TimelineService timelineService;

    public OrderTimelineListener(OrderEventCodec orderEventCodec, TimelineService timelineService) {
        this.orderEventCodec = orderEventCodec;
        this.timelineService = timelineService;
    }

    @KafkaListener(topics = "orders.events", groupId = "analytics.timeline")
    public void onOrderEvent(String message) {
        OrderEventEnvelope event;
        try {
            event = orderEventCodec.decode(message);
        } catch (InvalidOrderEventException exception) {
            LOG.warn("Skipping order timeline event: {}", exception.getMessage());
            return;
        }
        boolean inserted = timelineService.project(event);
        if (!inserted) {
            LOG.debug("Ignoring duplicate timeline event {}", event.eventId());
        }
    }
}
