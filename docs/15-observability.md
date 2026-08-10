# Observability, Metrics & Telemetry

## 1. Spring Boot Actuator & Prometheus Metrics
RouteResQ exposes telemetry at `/actuator/prometheus` using Micrometer.

### Key Custom Metrics

| Metric Name | Type | Description | Tags |
|---|---|---|---|
| `routeresq.optimization.runs` | Counter | Total number of route optimization attempts | `status=SUCCESS|FAILED`, `type=INITIAL|REOPT` |
| `routeresq.optimization.duration` | Timer | Solver execution wall-clock time (ms) | `status=OPTIMAL|FEASIBLE` |
| `routeresq.optimization.score.hard`| Gauge | Final hard constraint score | `run_id` |
| `routeresq.optimization.score.soft`| Gauge | Final soft constraint score | `run_id` |
| `routeresq.incidents.total` | Counter | Total operational incidents triggered | `type=VEHICLE_BREAKDOWN|URGENT_ORDER` |
| `routeresq.simulation.active_vehicles`| Gauge| Number of vehicles currently simulating delivery movement | `depot_id` |

---

## 2. Structured JSON Logging
Logback configured with `logstash-logback-encoder` to produce structured JSON logs with MDC context (`requestId`, `userId`, `optimizationRunId`).

```json
{
  "timestamp": "2026-08-11T10:15:30.123Z",
  "level": "INFO",
  "logger": "com.routeresq.optimization.RouteOptimizationService",
  "message": "Optimization job completed successfully",
  "mdc": {
    "optimizationRunId": "run99999-0000",
    "executionTimeMs": 1450,
    "hardScore": 0,
    "softScore": -41250
  }
}
```