package com.friendlyeshop.analytics.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.friendlyeshop.analytics.model.OrderTimelineEntry;
import com.friendlyeshop.analytics.model.SalesMetric;
import com.friendlyeshop.analytics.service.MetricsService;
import com.friendlyeshop.analytics.service.TimelineService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AnalyticsController.class)
class AnalyticsControllerTest {

    private static final UUID TENANT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private static final UUID ORDER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TimelineService timelineService;

    @MockitoBean
    private MetricsService metricsService;

    @Test
    void returnsTimelineByTenant() throws Exception {
        OrderTimelineEntry entry = timelineEntry();
        when(timelineService.findTimeline(TENANT_ID, null)).thenReturn(List.of(entry));

        mockMvc.perform(get("/analytics/timeline").param("tenantId", TENANT_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventId").value(entry.getEventId().toString()))
                .andExpect(jsonPath("$[0].orderId").value(ORDER_ID.toString()))
                .andExpect(jsonPath("$[0].eventType").value("order.placed"))
                .andExpect(jsonPath("$[0].currency").value("EUR"));
    }

    @Test
    void returnsTimelineFilteredByOrder() throws Exception {
        when(timelineService.findTimeline(TENANT_ID, ORDER_ID)).thenReturn(List.of(timelineEntry()));

        mockMvc.perform(get("/analytics/timeline")
                        .param("tenantId", TENANT_ID.toString())
                        .param("orderId", ORDER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").value("order.placed"));

        verify(timelineService).findTimeline(TENANT_ID, ORDER_ID);
    }

    @Test
    void rejectsInvalidTenantIdOnTimeline() throws Exception {
        mockMvc.perform(get("/analytics/timeline").param("tenantId", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requiresTenantIdOnTimeline() throws Exception {
        mockMvc.perform(get("/analytics/timeline"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsMetricsByTenant() throws Exception {
        when(metricsService.findMetrics(TENANT_ID)).thenReturn(List.of(salesMetric()));

        mockMvc.perform(get("/analytics/metrics").param("tenantId", TENANT_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tenantId").value(TENANT_ID.toString()))
                .andExpect(jsonPath("$[0].currency").value("EUR"))
                .andExpect(jsonPath("$[0].ordersCount").value(3))
                .andExpect(jsonPath("$[0].totalAmount").exists());
    }

    @Test
    void rejectsInvalidTenantIdOnMetrics() throws Exception {
        mockMvc.perform(get("/analytics/metrics").param("tenantId", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    private static OrderTimelineEntry timelineEntry() {
        OrderTimelineEntry entry = new OrderTimelineEntry();
        entry.setEventId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        entry.setOrderId(ORDER_ID);
        entry.setTenantId(TENANT_ID);
        entry.setEventType("order.placed");
        entry.setOccurredAt(Instant.parse("2026-01-02T03:04:05Z"));
        entry.setTotal(new BigDecimal("12.50"));
        entry.setCurrency("EUR");
        entry.setPayload("{}");
        entry.setCreatedAt(Instant.parse("2026-01-02T03:04:06Z"));
        return entry;
    }

    private static SalesMetric salesMetric() {
        SalesMetric metric = new SalesMetric();
        metric.setId(new SalesMetric.SalesMetricId(TENANT_ID, "EUR"));
        metric.setOrdersCount(3);
        metric.setTotalAmount(new BigDecimal("37.50"));
        metric.setUpdatedAt(Instant.parse("2026-01-02T03:04:05Z"));
        return metric;
    }
}
