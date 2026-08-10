# Event Model & Domain Messaging

## 1. Domain Event Architecture
RouteResQ implements an event-driven domain architecture using Spring `ApplicationEventPublisher`. Domain events decouple core entity mutations from WebSocket notifications, audit logs, and analytical metrics.

```mermaid
graph TD
    DomainAction[Vehicle Breakdown Action] --> Pub[ApplicationEventPublisher]
    Pub --> Evt[VehicleBreakdownEvent]
    
    Evt --> H1[IncidentRecoveryHandler]
    Evt --> H2[AuditLogHandler]
    Evt --> H3[WebSocketNotificationHandler]
    Evt --> H4[MicrometerMetricsHandler]
    
    H1 --> Reopt[Trigger Timefold Re-optimization]
    H2 --> DB[(Insert AuditLog)]
    H3 --> WS[STOMP Push /topic/incidents]
    H4 --> Metrics[Increment routeresq.incidents.total]
```

---

## 2. Domain Event Catalog

| Event Name | Publisher | Payload Summary | Listeners |
|---|---|---|---|
| `OrderCreatedEvent` | OrderService | `orderId`, `depotId`, `weightKg`, `location` | MetricCounter, WSBroadcaster |
| `OptimizationCompletedEvent` | RouteOptimizationService | `runId`, `hardScore`, `softScore`, `durationMs` | AuditLogger, AnalyticsService |
| `VehicleBreakdownEvent` | IncidentService | `vehicleId`, `location`, `timestamp` | RecoveryHandler, WSBroadcaster |
| `DeliveryCompletedEvent` | SimulationService | `orderId`, `vehicleId`, `actualArrivalTime` | OrderService, MetricCounter |