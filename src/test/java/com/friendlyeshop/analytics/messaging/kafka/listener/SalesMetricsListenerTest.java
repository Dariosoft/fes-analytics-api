package com.friendlyeshop.analytics.messaging.kafka.listener;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.friendlyeshop.analytics.exception.InvalidOrderEventException;
import com.friendlyeshop.analytics.messaging.kafka.codec.OrderEventCodec;
import com.friendlyeshop.analytics.messaging.kafka.model.OrderEventEnvelope;
import com.friendlyeshop.analytics.service.MetricsService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SalesMetricsListenerTest {

    private static final String PAYLOAD = "{\"eventId\":\"11111111-1111-1111-1111-111111111111\"}";

    @Mock
    private OrderEventCodec orderEventCodec;

    @Mock
    private MetricsService metricsService;

    @InjectMocks
    private SalesMetricsListener listener;

    @Test
    void ignoresMalformedEvent() {
        when(orderEventCodec.decode(PAYLOAD)).thenThrow(new InvalidOrderEventException("bad payload"));

        listener.onOrderEvent(PAYLOAD);

        verify(metricsService, never()).recordOrderPlaced(any());
    }

    @Test
    void ignoresUnsupportedEventType() {
        when(orderEventCodec.decode(PAYLOAD)).thenReturn(envelope("order.updated", new BigDecimal("12.50"), "EUR"));

        listener.onOrderEvent(PAYLOAD);

        verify(metricsService, never()).recordOrderPlaced(any());
    }

    @Test
    void ignoresOrderPlacedWithoutTotalOrCurrency() {
        when(orderEventCodec.decode(PAYLOAD)).thenReturn(envelope("order.placed", null, "EUR"));

        listener.onOrderEvent(PAYLOAD);

        verify(metricsService, never()).recordOrderPlaced(any());
    }

    @Test
    void recordsOrderPlacedEvent() {
        OrderEventEnvelope event = envelope("order.placed", new BigDecimal("12.50"), "EUR");
        when(orderEventCodec.decode(PAYLOAD)).thenReturn(event);
        when(metricsService.recordOrderPlaced(event)).thenReturn(true);

        listener.onOrderEvent(PAYLOAD);

        verify(metricsService).recordOrderPlaced(event);
    }

    @Test
    void keepsOffsetWhenEventWasAlreadyCounted() {
        OrderEventEnvelope event = envelope("order.placed", new BigDecimal("12.50"), "EUR");
        when(orderEventCodec.decode(PAYLOAD)).thenReturn(event);
        when(metricsService.recordOrderPlaced(event)).thenReturn(false);

        listener.onOrderEvent(PAYLOAD);

        verify(metricsService).recordOrderPlaced(event);
    }

    @Test
    void propagatesPersistenceFailureSoOffsetIsNotAcknowledged() {
        OrderEventEnvelope event = envelope("order.placed", new BigDecimal("12.50"), "EUR");
        when(orderEventCodec.decode(PAYLOAD)).thenReturn(event);
        when(metricsService.recordOrderPlaced(event)).thenThrow(new IllegalStateException("database down"));

        assertThatThrownBy(() -> listener.onOrderEvent(PAYLOAD))
                .isInstanceOf(IllegalStateException.class);
    }

    private static OrderEventEnvelope envelope(String eventType, BigDecimal total, String currency) {
        return new OrderEventEnvelope(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                eventType,
                Instant.parse("2026-01-02T03:04:05Z"),
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                null,
                total,
                currency,
                PAYLOAD);
    }
}
