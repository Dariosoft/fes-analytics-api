package com.friendlyeshop.analytics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.friendlyeshop.analytics.messaging.kafka.model.OrderEventEnvelope;
import com.friendlyeshop.analytics.model.OrderTimelineEntry;
import com.friendlyeshop.analytics.repository.OrderTimelineRepository;
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
class TimelineServiceTest {

    private static final UUID TENANT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private static final UUID ORDER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock
    private OrderTimelineRepository orderTimelineRepository;

    @InjectMocks
    private TimelineService timelineService;

    @Test
    void reportsProjectionWhenRepositoryInsertsEvent() {
        when(orderTimelineRepository.insertIfAbsent(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(1);

        assertThat(timelineService.project(envelope())).isTrue();
        verify(orderTimelineRepository)
                .insertIfAbsent(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void reportsDuplicateWhenRepositoryInsertsNothing() {
        when(orderTimelineRepository.insertIfAbsent(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(0);

        assertThat(timelineService.project(envelope())).isFalse();
    }

    @Test
    void findsTimelineByTenant() {
        List<OrderTimelineEntry> entries = List.of(new OrderTimelineEntry());
        when(orderTimelineRepository.findByTenantIdOrderByOccurredAtAscEventIdAsc(TENANT_ID))
                .thenReturn(entries);

        assertThat(timelineService.findTimeline(TENANT_ID, null)).isSameAs(entries);
    }

    @Test
    void findsTimelineByTenantAndOrder() {
        List<OrderTimelineEntry> entries = List.of(new OrderTimelineEntry());
        when(orderTimelineRepository.findByTenantIdAndOrderIdOrderByOccurredAtAscEventIdAsc(TENANT_ID, ORDER_ID))
                .thenReturn(entries);

        assertThat(timelineService.findTimeline(TENANT_ID, ORDER_ID)).isSameAs(entries);
    }

    private static OrderEventEnvelope envelope() {
        return new OrderEventEnvelope(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "order.placed",
                Instant.parse("2026-01-02T03:04:05Z"),
                ORDER_ID,
                TENANT_ID,
                null,
                new BigDecimal("12.50"),
                "EUR",
                "{}");
    }
}
