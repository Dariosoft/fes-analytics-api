package com.friendlyeshop.analytics.messaging.kafka.codec;

import com.friendlyeshop.analytics.exception.InvalidOrderEventException;
import com.friendlyeshop.analytics.messaging.kafka.model.OrderEventEnvelope;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Component
public class OrderEventCodec {

    private static final String EVENT_ID = "eventId";

    private static final String ORDER_ID = "orderId";

    private static final String TENANT_ID = "tenantId";

    private static final String EVENT_TYPE = "eventType";

    private static final String OCCURRED_AT = "occurredAt";

    private static final String ITEMS = "items";

    private static final String TOTAL = "total";

    private static final String CURRENCY = "currency";

    private final JsonMapper jsonMapper;

    public OrderEventCodec(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public OrderEventEnvelope decode(String message) {
        JsonNode root = readObject(message);
        return new OrderEventEnvelope(
                requiredUuid(root, EVENT_ID),
                requiredText(root, EVENT_TYPE),
                requiredInstant(root, OCCURRED_AT),
                requiredUuid(root, ORDER_ID),
                requiredUuid(root, TENANT_ID),
                root.get(ITEMS),
                optionalDecimal(root, TOTAL),
                optionalText(root, CURRENCY),
                message);
    }

    private JsonNode readObject(String message) {
        if (message == null || message.isBlank()) {
            throw new InvalidOrderEventException("Order event payload is empty");
        }
        JsonNode root;
        try {
            root = jsonMapper.readTree(message);
        } catch (JacksonException exception) {
            throw new InvalidOrderEventException("Order event payload is not valid JSON", exception);
        }
        if (root == null || !root.isObject()) {
            throw new InvalidOrderEventException("Order event payload must be a JSON object");
        }
        return root;
    }

    private UUID requiredUuid(JsonNode root, String field) {
        String value = requiredText(root, field);
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new InvalidOrderEventException(invalidField(field, "UUID"), exception);
        }
    }

    private Instant requiredInstant(JsonNode root, String field) {
        String value = requiredText(root, field);
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException exception) {
            throw new InvalidOrderEventException(invalidField(field, "instant"), exception);
        }
    }

    private String requiredText(JsonNode root, String field) {
        JsonNode node = root.get(field);
        if (node == null || node.isNull()) {
            throw new InvalidOrderEventException(missingField(field));
        }
        String value = node.asString();
        if (value == null || value.isBlank()) {
            throw new InvalidOrderEventException(blankField(field));
        }
        return value.trim();
    }

    private BigDecimal optionalDecimal(JsonNode root, String field) {
        JsonNode node = root.get(field);
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        if (node.isNumber()) {
            return node.decimalValue();
        }
        String value = node.asString();
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException exception) {
            throw new InvalidOrderEventException(invalidField(field, "number"), exception);
        }
    }

    private String optionalText(JsonNode root, String field) {
        JsonNode node = root.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        String value = node.asString();
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String invalidField(String field, String expected) {
        return "Order event field '" + field + "' is not a valid " + expected;
    }

    private String missingField(String field) {
        return "Order event field '" + field + "' is missing";
    }

    private String blankField(String field) {
        return "Order event field '" + field + "' is blank";
    }
}
