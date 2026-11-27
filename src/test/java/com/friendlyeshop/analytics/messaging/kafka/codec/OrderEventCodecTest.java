package com.friendlyeshop.analytics.messaging.kafka.codec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.friendlyeshop.analytics.exception.InvalidOrderEventException;
import com.friendlyeshop.analytics.messaging.kafka.model.OrderEventEnvelope;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class OrderEventCodecTest {

    private static final UUID EVENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID ORDER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private static final UUID TENANT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private static final String VALID_EVENT = """
            {
              "eventId": "11111111-1111-1111-1111-111111111111",
              "eventType": "order.placed",
              "occurredAt": "2026-01-02T03:04:05Z",
              "orderId": "22222222-2222-2222-2222-222222222222",
              "tenantId": "33333333-3333-3333-3333-333333333333",
              "items": [{"sku": "A-1", "quantity": 2}],
              "total": 12.50,
              "currency": "EUR"
            }
            """;

    private OrderEventCodec codec;

    @BeforeEach
    void setUp() {
        codec = new OrderEventCodec(JsonMapper.shared());
    }

    @Test
    void decodesValidEnvelope() {
        OrderEventEnvelope event = codec.decode(VALID_EVENT);

        assertThat(event.eventId()).isEqualTo(EVENT_ID);
        assertThat(event.orderId()).isEqualTo(ORDER_ID);
        assertThat(event.tenantId()).isEqualTo(TENANT_ID);
        assertThat(event.eventType()).isEqualTo("order.placed");
        assertThat(event.occurredAt()).isEqualTo(Instant.parse("2026-01-02T03:04:05Z"));
        assertThat(event.total()).isEqualByComparingTo(new BigDecimal("12.50"));
        assertThat(event.currency()).isEqualTo("EUR");
        assertThat(event.items()).isNotNull();
        assertThat(event.payload()).isEqualTo(VALID_EVENT);
    }

    @Test
    void rejectsMalformedJson() {
        assertThatThrownBy(() -> codec.decode("{not-json"))
                .isInstanceOf(InvalidOrderEventException.class);
    }

    @Test
    void rejectsBlankPayload() {
        assertThatThrownBy(() -> codec.decode(" "))
                .isInstanceOf(InvalidOrderEventException.class);
    }

    @Test
    void rejectsMissingEventId() {
        String payload = VALID_EVENT.replace("\"eventId\": \"11111111-1111-1111-1111-111111111111\",", "");

        assertThatThrownBy(() -> codec.decode(payload))
                .isInstanceOf(InvalidOrderEventException.class)
                .hasMessageContaining("eventId");
    }

    @Test
    void rejectsInvalidUuid() {
        String payload = VALID_EVENT.replace(
                "\"orderId\": \"22222222-2222-2222-2222-222222222222\"",
                "\"orderId\": \"not-a-uuid\"");

        assertThatThrownBy(() -> codec.decode(payload))
                .isInstanceOf(InvalidOrderEventException.class)
                .hasMessageContaining("orderId");
    }

    @Test
    void rejectsInvalidOccurredAt() {
        String payload = VALID_EVENT.replace("2026-01-02T03:04:05Z", "not-an-instant");

        assertThatThrownBy(() -> codec.decode(payload))
                .isInstanceOf(InvalidOrderEventException.class)
                .hasMessageContaining("occurredAt");
    }

    @Test
    void leavesOptionalFieldsNullWhenAbsent() {
        String payload = """
                {
                  "eventId": "11111111-1111-1111-1111-111111111111",
                  "eventType": "order.updated",
                  "occurredAt": "2026-01-02T03:04:05Z",
                  "orderId": "22222222-2222-2222-2222-222222222222",
                  "tenantId": "33333333-3333-3333-3333-333333333333"
                }
                """;

        OrderEventEnvelope event = codec.decode(payload);

        assertThat(event.total()).isNull();
        assertThat(event.currency()).isNull();
        assertThat(event.items()).isNull();
    }
}
