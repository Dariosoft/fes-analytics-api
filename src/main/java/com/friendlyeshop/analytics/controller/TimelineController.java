package com.friendlyeshop.analytics.controller;

import com.friendlyeshop.analytics.model.dto.out.OrderTimelineEntryResponse;
import com.friendlyeshop.analytics.service.TimelineService;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/timeline")
public class TimelineController {

    private final TimelineService timelineService;

    public TimelineController(TimelineService timelineService) {
        this.timelineService = timelineService;
    }

    @GetMapping
    public List<OrderTimelineEntryResponse> timeline(@RequestParam UUID tenantId,
                                                     @RequestParam(required = false) UUID orderId) {
        return timelineService.findTimeline(tenantId, orderId).stream()
                .map(OrderTimelineEntryResponse::from)
                .toList();
    }
}
