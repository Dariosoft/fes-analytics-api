package com.friendlyeshop.analytics.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.friendlyeshop.analytics.model.SalesMetric;
import com.friendlyeshop.analytics.service.MetricsService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MetricsController.class)
class MetricsControllerTest {

    private static final UUID TENANT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MetricsService metricsService;

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
    void rejectsInvalidTenantId() throws Exception {
        mockMvc.perform(get("/analytics/metrics").param("tenantId", "not-a-uuid"))
                .andExpect(status().isBadRequest());
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
