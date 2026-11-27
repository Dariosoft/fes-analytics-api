package com.friendlyeshop.analytics.controller;

import com.friendlyeshop.analytics.model.dto.out.OrderTimelineEntryResponse;
import com.friendlyeshop.analytics.model.dto.out.SalesMetricResponse;
import com.friendlyeshop.analytics.service.MetricsService;
import com.friendlyeshop.analytics.service.TimelineService;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnalyticsController {

    private final TimelineService timelineService;

    private final MetricsService metricsService;

    public AnalyticsController(TimelineService timelineService, MetricsService metricsService) {
        this.timelineService = timelineService;
        this.metricsService = metricsService;
    }

    @GetMapping("/timeline")
    public List<OrderTimelineEntryResponse> timeline(@RequestParam UUID tenantId,
                                                     @RequestParam(required = false) UUID orderId) {
        return timelineService.findTimeline(tenantId, orderId).stream()
                .map(OrderTimelineEntryResponse::from)
                .toList();
    }

    @GetMapping("/metrics")
    public List<SalesMetricResponse> metrics(@RequestParam UUID tenantId) {
        return metricsService.findMetrics(tenantId).stream()
                .map(SalesMetricResponse::from)
                .toList();
    }
}
