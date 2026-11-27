package com.friendlyeshop.analytics.repository;

import com.friendlyeshop.analytics.model.SalesMetric;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SalesMetricsRepository extends JpaRepository<SalesMetric, SalesMetric.SalesMetricId> {

    @Modifying
    @Query(value = """
            INSERT INTO sales_metrics (tenant_id, currency, orders_count, total_amount, updated_at)
            VALUES (:tenantId, :currency, 1, :total, NOW())
            ON CONFLICT (tenant_id, currency) DO UPDATE
            SET orders_count = sales_metrics.orders_count + 1,
                total_amount = sales_metrics.total_amount + EXCLUDED.total_amount,
                updated_at = NOW()
            """, nativeQuery = true)
    int increment(@Param("tenantId") UUID tenantId,
                  @Param("currency") String currency,
                  @Param("total") BigDecimal total);

    List<SalesMetric> findByTenantIdOrderByCurrencyAsc(UUID tenantId);
}
