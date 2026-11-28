package com.friendlyeshop.analytics.controller;

import com.friendlyeshop.analytics.model.dto.out.SalesMetricResponse;
import com.friendlyeshop.analytics.service.MetricsService;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/metrics")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping
    public List<SalesMetricResponse> metrics(@RequestParam UUID tenantId) {
        return metricsService.findMetrics(tenantId).stream()
                .map(SalesMetricResponse::from)
                .toList();
    }
}
