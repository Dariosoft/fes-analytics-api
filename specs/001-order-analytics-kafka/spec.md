# Spec 001 — Timeline y métricas de ventas desde Kafka

## Contexto y objetivo

El panel del vendedor necesita historial de pedidos y métricas de ventas sin consultar las tablas de
`order-api`. `analytics-api` consume el log de dominio `orders.events`, proyecta un timeline por
pedido y agrega métricas por tienda, y las expone por HTTP para que `panel-api` las proxye. Es un
consumidor de solo lectura: nunca es dueño del pedido ni escribe tablas de otros dominios.

## Usuarios / actores

- Vendedor que consulta historial y métricas en el panel.
- `panel-api`, que proxya las consultas.
- `order-api`, productor de `orders.events`.
- Equipo de operaciones, que monitorea offsets y lag.

## Historias de usuario

- H1: Como vendedor quiero ver el historial de eventos de un pedido.
- H2: Como vendedor quiero ver métricas agregadas de ventas de mi tienda.
- H3: Como analítica quiero consumir el mismo log de eventos sin afectar a otros consumidores.
- H4: Como operador quiero tolerar reentregas y eventos fuera de orden sin duplicar datos.

## Requisitos funcionales (criterios de aceptación en EARS)

- RF-1: EL SISTEMA consumirá el topic `orders.events` con el grupo `analytics.timeline`.
- RF-2: EL SISTEMA consumirá el topic `orders.events` con el grupo `analytics.metrics`.
- RF-3: CUANDO reciba un evento con `eventId` y `orderId` utilizables, EL SISTEMA registrará el evento en el timeline del pedido.
- RF-4: SI recibe un `eventId` ya proyectado, ENTONCES EL SISTEMA no duplicará la fila del timeline.
- RF-5: EL SISTEMA ordenará el timeline por `occurredAt` y desempatará por `eventId`.
- RF-6: CUANDO reciba un `order.placed`, EL SISTEMA incrementará las métricas de pedidos y de total por moneda de su tienda.
- RF-7: SI recibe un `eventId` ya contabilizado, ENTONCES EL SISTEMA no volverá a sumar en las métricas.
- RF-8: EL SISTEMA confirmará el offset solo después de persistir la proyección del evento.
- RF-9: SI la persistencia falla, ENTONCES EL SISTEMA no confirmará el offset para permitir el reintento.
- RF-10: EL SISTEMA expondrá `GET /analytics/timeline` filtrado por `tenantId` y, opcionalmente, por `orderId`.
- RF-11: EL SISTEMA expondrá `GET /analytics/metrics` filtrado por `tenantId`.
- RF-12: EL SISTEMA devolverá el timeline en orden cronológico ascendente.
- RF-13: EL SISTEMA persistirá únicamente en la base `analytics`.
- RF-14: EL SISTEMA no escribirá tablas de pedidos, pagos, productos ni cuentas.
- RF-15: EL SISTEMA conservará los health checks de Actuator.
- RF-16: EL SISTEMA obtendrá los brokers desde `KAFKA_BOOTSTRAP_SERVERS` y la base desde `DATABASE_*`.
- RF-17: EL SISTEMA no declarará topics ni consumirá otros eventos distintos de `orders.events`.
- RF-18: SI recibe un evento malformado o de un tipo no soportado, ENTONCES EL SISTEMA lo registrará como advertencia y continuará sin bloquear el offset.

## Requisitos no funcionales

- Idempotencia por `eventId`.
- Consumo con offset manual.
- Dos consumer groups independientes sobre el mismo topic.
- Solo lectura de otros dominios.

## Casos límite

- Evento duplicado: no duplica timeline ni métricas.
- Evento fuera de orden: el timeline se lee ordenado por `occurredAt`.
- Evento sin `tenantId` o sin `orderId`: se descarta con log y se confirma.
- Evento con tipo no soportado: se ignora con log y se confirma.
- Reinicio con offset pendiente: se reprocesa sin duplicar.

## Fuera de alcance

- Feed en vivo y notificaciones.
- Publicar pedidos.
- Declarar topics o topología.
- Proxy del panel.
- Cambiar `/analytics` más allá de los dos endpoints.
- Autenticación nueva.

## Criterios de finalización

- `./mvnw verify` en verde.
- Demo: publicar `order.placed` y ver la fila del timeline y las métricas; reenviar el mismo evento y verificar que no se duplica.

## Dudas abiertas

Ninguna.
