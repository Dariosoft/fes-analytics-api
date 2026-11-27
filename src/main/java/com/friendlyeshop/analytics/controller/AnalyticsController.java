package com.friendlyeshop.analytics.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnalyticsController {

    /** Placeholder so /analytics keeps responding until the projections and metrics land. */
    @GetMapping
    public String analytics() {
        return "Analytics API is up and running.";
    }
}
