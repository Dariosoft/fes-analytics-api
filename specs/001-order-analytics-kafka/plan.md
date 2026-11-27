# Plan 001 — Timeline y métricas de ventas desde Kafka

## 1. Resumen

Consumir `orders.events` con dos consumer groups independientes. `TimelineProjection` persiste una
fila por evento en `order_timeline`; `MetricsProjection` actualiza agregados por tienda y moneda en
`sales_metrics`. Dos endpoints de solo lectura exponen ambos read models. La idempotencia se ancla a
`event_id` y el offset se confirma solo después del commit. Cubre RF-1 … RF-18.

## 2. Dependencia y configuración

- `spring-kafka` en `pom.xml`.
- `application.yaml`: `spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}`,
  `enable-auto-commit=false`, `ack-mode=record`.
- Constantes: `orders.events`, grupos `analytics.timeline` y `analytics.metrics`.

## 3. Esquema Flyway

`V1__create_analytics_read_models.sql`:

- `order_timeline`:
  - `event_id UUID PRIMARY KEY`
  - `order_id UUID NOT NULL`
  - `tenant_id UUID NOT NULL`
  - `event_type VARCHAR(50) NOT NULL`
  - `occurred_at TIMESTAMPTZ NOT NULL`
  - `total NUMERIC(19,2) NULL`
  - `currency VARCHAR(3) NULL`
  - `payload JSONB NOT NULL`
  - `created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`
  - índices por `(tenant_id, order_id, occurred_at)` y `(tenant_id, occurred_at)`.
- `sales_metrics`:
  - PK `(tenant_id, currency)`
  - `orders_count BIGINT NOT NULL DEFAULT 0`
  - `total_amount NUMERIC(19,2) NOT NULL DEFAULT 0`
  - `updated_at TIMESTAMPTZ NOT NULL`
- `processed_events` (`event_id UUID PRIMARY KEY`, `consumer_group VARCHAR(50)`, procesado en) para
  idempotencia explícita de métricas si se requiere.

## 4. Consumidores

- `OrderTimelineListener` con `@KafkaListener(topics="orders.events", groupId="analytics.timeline")`.
  - Parsea el envelope; si `eventId`/`orderId`/`tenantId` faltan, log WARN y retorna (offset avanza).
  - Inserta en `order_timeline` con `ON CONFLICT (event_id) DO NOTHING` (idempotencia).
- `SalesMetricsListener` con `@KafkaListener(topics="orders.events", groupId="analytics.metrics")`.
  - Solo `order.placed`: verifica `processed_events` e inserta; si es nueva, hace upsert de
    `sales_metrics` por `(tenant_id, currency)` sumando 1 y el total.
  - Eventos no soportados: log y retorno.
- Ambos usan `AckMode.RECORD`; una excepción de persistencia impide el ACK y Kafka reintenta.

## 5. API

- `GET /analytics/timeline?tenantId=&orderId=` → lista ordenada por `occurred_at, event_id`.
- `GET /analytics/metrics?tenantId=` → lista de métricas por moneda.
- Controladores delgados que delegan en `TimelineService` y `MetricsService`.
- Validación de UUID; 400 ante parámetros inválidos.

## 6. Paquetes (Layered)

- `messaging/kafka/listener/`, `messaging/kafka/model/`, `messaging/kafka/codec/`.
- `service/TimelineService`, `service/MetricsService`.
- `repository/OrderTimelineRepository`, `repository/SalesMetricsRepository`,
  `repository/ProcessedEventRepository`.
- `model/OrderTimelineEntry`, `model/SalesMetric`, `model/ProcessedEvent`.
- `controller/AnalyticsController`.
- `config/KafkaConsumerConfig` solo si hace falta; no declara topics.

## 7. Verificación

- Unitarias de codec, idempotencia y agregación.
- Web tests de los dos endpoints.
- `./mvnw verify`.

## 8. Trazabilidad RF → plan

| RF | Sección |
|---|---|
| RF-1, RF-2 | §2, §4 |
| RF-3, RF-4, RF-5 | §4 |
| RF-6, RF-7 | §4 |
| RF-8, RF-9 | §2, §4 |
| RF-10…RF-12 | §5 |
| RF-13, RF-14 | §3 |
| RF-15, RF-16 | §2 |
| RF-17 | §6 |
| RF-18 | §4 |
