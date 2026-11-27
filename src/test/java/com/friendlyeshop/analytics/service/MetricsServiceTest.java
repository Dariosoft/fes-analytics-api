package com.friendlyeshop.analytics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.friendlyeshop.analytics.messaging.kafka.model.OrderEventEnvelope;
import com.friendlyeshop.analytics.model.SalesMetric;
import com.friendlyeshop.analytics.repository.ProcessedEventRepository;
import com.friendlyeshop.analytics.repository.SalesMetricsRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MetricsServiceTest {

    private static final UUID EVENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID TENANT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Mock
    private SalesMetricsRepository salesMetricsRepository;

    @InjectMocks
    private MetricsService metricsService;

    @Test
    void incrementsMetricsWhenEventIsNew() {
        OrderEventEnvelope event = envelope();
        when(processedEventRepository.insertIfAbsent(EVENT_ID, "analytics.metrics")).thenReturn(1);

        assertThat(metricsService.recordOrderPlaced(event)).isTrue();
        verify(salesMetricsRepository).increment(TENANT_ID, "EUR", new BigDecimal("12.50"));
    }

    @Test
    void skipsMetricsWhenEventWasAlreadyProcessed() {
        OrderEventEnvelope event = envelope();
        when(processedEventRepository.insertIfAbsent(EVENT_ID, "analytics.metrics")).thenReturn(0);

        assertThat(metricsService.recordOrderPlaced(event)).isFalse();
        verify(salesMetricsRepository, never()).increment(any(), any(), any());
    }

    @Test
    void propagatesFailureFromMetricsIncrement() {
        OrderEventEnvelope event = envelope();
        when(processedEventRepository.insertIfAbsent(EVENT_ID, "analytics.metrics")).thenReturn(1);
        when(salesMetricsRepository.increment(TENANT_ID, "EUR", new BigDecimal("12.50")))
                .thenThrow(new IllegalStateException("database down"));

        assertThatThrownBy(() -> metricsService.recordOrderPlaced(event))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void findsMetricsByTenant() {
        List<SalesMetric> metrics = List.of(new SalesMetric());
        when(salesMetricsRepository.findByTenantIdOrderByCurrencyAsc(TENANT_ID)).thenReturn(metrics);

        assertThat(metricsService.findMetrics(TENANT_ID)).isSameAs(metrics);
    }

    private static OrderEventEnvelope envelope() {
        return new OrderEventEnvelope(
                EVENT_ID,
                "order.placed",
                Instant.parse("2026-01-02T03:04:05Z"),
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                TENANT_ID,
                null,
                new BigDecimal("12.50"),
                "EUR",
                "{}");
    }
}
