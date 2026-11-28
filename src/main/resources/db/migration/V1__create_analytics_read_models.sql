CREATE TABLE order_timeline (
    event_id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    total NUMERIC(19, 2) NULL,
    currency VARCHAR(3) NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_order_timeline_tenant_order_occurred
    ON order_timeline (tenant_id, order_id, occurred_at);

CREATE INDEX idx_order_timeline_tenant_occurred
    ON order_timeline (tenant_id, occurred_at);

CREATE TABLE sales_metrics (
    tenant_id UUID NOT NULL,
    currency VARCHAR(3) NOT NULL,
    orders_count BIGINT NOT NULL DEFAULT 0,
    total_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (tenant_id, currency)
);

CREATE INDEX idx_sales_metrics_tenant ON sales_metrics (tenant_id);

CREATE TABLE processed_events (
    event_id UUID PRIMARY KEY,
    consumer_group VARCHAR(50) NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_processed_events_consumer_group ON processed_events (consumer_group);
