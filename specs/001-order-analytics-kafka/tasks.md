# Tasks 001 — Timeline y métricas de ventas desde Kafka

## Fase A — Base y esquema

- [x] **T1. Agregar `spring-kafka` y configuración de consumidor.** Cubre RF-1, RF-2, RF-16, RF-17.
  Done when: `./mvnw compile` pasa y `KAFKA_BOOTSTRAP_SERVERS` tiene default.
- [x] **T2. Crear `V1__create_analytics_read_models.sql` y entidades.** Cubre RF-3, RF-6, RF-13.
  Done when: Flyway aplica e Hibernate valida `order_timeline`, `sales_metrics` y `processed_events`.

## Fase B — Consumidores

- [x] **T3. Codec del envelope `orders.events`.** Cubre RF-3, RF-18.
  Done when: parsea eventos válidos y descarta con log los malformados.
- [x] **T4. Implementar `OrderTimelineListener`.** Cubre RF-1, RF-3, RF-4, RF-5, RF-8, RF-9.
  Done when: inserta idempotente por `eventId` y no confirma si falla la persistencia.
- [x] **T5. Implementar `SalesMetricsListener`.** Cubre RF-2, RF-6, RF-7, RF-8, RF-9.
  Done when: suma una sola vez por `eventId` y actualiza por `(tenantId, currency)`.

## Fase C — API y pruebas

- [x] **T6. Implementar `GET /analytics/timeline`.** Cubre RF-10, RF-12.
  Done when: filtra por `tenantId`/`orderId` y ordena por `occurredAt`.
- [x] **T7. Implementar `GET /analytics/metrics`.** Cubre RF-11.
  Done when: devuelve agregados de la tienda.
- [x] **T8. Unitarias y web tests.** Cubre RF-3…RF-12, RF-18.
  Done when: cubren duplicados, fuera de orden, malformados y ambos endpoints.
- [x] **T9. Verificar `./mvnw verify` y demo manual.** Cubre RF-13…RF-16.
  Done when: publicar un evento se ve en timeline y métricas sin duplicar al reenviar.
