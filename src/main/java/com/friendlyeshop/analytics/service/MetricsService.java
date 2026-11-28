package com.friendlyeshop.analytics.service;

import com.friendlyeshop.analytics.messaging.kafka.model.OrderEventEnvelope;
import com.friendlyeshop.analytics.model.SalesMetric;
import com.friendlyeshop.analytics.repository.ProcessedEventRepository;
import com.friendlyeshop.analytics.repository.SalesMetricsRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MetricsService {

    private static final String CONSUMER_GROUP = "analytics.metrics";

    private final ProcessedEventRepository processedEventRepository;

    private final SalesMetricsRepository salesMetricsRepository;

    public MetricsService(ProcessedEventRepository processedEventRepository,
                          SalesMetricsRepository salesMetricsRepository) {
        this.processedEventRepository = processedEventRepository;
        this.salesMetricsRepository = salesMetricsRepository;
    }

    @Transactional
    public boolean recordOrderPlaced(OrderEventEnvelope event) {
        int inserted = processedEventRepository.insertIfAbsent(event.eventId(), CONSUMER_GROUP);
        if (inserted == 0) {
            return false;
        }
        salesMetricsRepository.increment(event.tenantId(), event.currency(), event.total());
        return true;
    }

    @Transactional(readOnly = true)
    public List<SalesMetric> findMetrics(UUID tenantId) {
        return salesMetricsRepository.findByTenantIdOrderByCurrencyAsc(tenantId);
    }
}
