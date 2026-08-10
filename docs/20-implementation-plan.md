# 4-Week Master Implementation Plan

## Week 1: Foundation, Data Model & Auth (Checkpoints 0 - 4)
- **Day 1**: Project initialization, Maven/Vite configuration, Docker Compose (`postgres-postgis`, `redis`).
- **Day 2**: Flyway database migrations (`V1__init_schema.sql`), spatial column configuration, JPA entity mappings.
- **Day 3**: Spring Security JWT authentication setup, user/role domain, RBAC filter chain, auth controller endpoints.
- **Day 4**: Order, Vehicle, Driver, Depot REST CRUD controllers, spatial DTO converters, request validations.
- **Day 5**: Realistic seed data generator (Depots, Vehicles, 100+ spatially coherent Orders in Chicago area).

## Week 2: Constraint Optimization Engine & Baseline (Checkpoints 5 - 6)
- **Day 6**: Timefold Solver Spring Boot integration, `@PlanningSolution` (`RoutePlanSolution`), `@PlanningEntity` (`RouteStop`).
- **Day 7**: Implementation of Hard Constraints (Vehicle Capacity H1, Delivery Time Windows H2, Driver Shifts H3).
- **Day 8**: Implementation of Soft Constraints (Total Distance S1, Duration S2, Fleet Count S3).
- **Day 9**: Implementation of Nearest-Neighbor Greedy Baseline algorithm and comparative audit matrix.
- **Day 10**: Automated benchmark test runner for 10, 50, 100 order solver verification.

## Week 3: Real-Time Operations, Map UI & Incident Recovery (Checkpoints 7 - 10)
- **Day 11**: React dashboard setup, Leaflet/Mapbox central map, depot/order markers, vehicle route polylines.
- **Day 12**: Delivery day simulation engine (`SimulationService`), vehicle location movement interpolation loop.
- **Day 13**: Incident recovery framework (`IncidentService`), vehicle breakdown handler, stop locking logic.
- **Day 14**: Route Disruption Penalty (`S4`) soft constraint tuning to prevent unnecessary route sequence changes.
- **Day 15**: Spring STOMP WebSockets implementation (`/ws-net`), client subscriptions, live map update pushes.

## Week 4: Analytics, Hardening, Docker & Polish (Checkpoints 11 - 15)
- **Day 16**: Analytics dashboard frontend components, comparative metric charts, vehicle load gauges.
- **Day 17**: Unit & integration test completion (Timefold `ConstraintVerifier`, Testcontainers PostgreSQL/PostGIS).
- **Day 18**: Spring Boot Actuator telemetry, Prometheus metrics export (`routeresq_*`), structured JSON logging.
- **Day 19**: Production Dockerfile multi-stage builds, Nginx reverse proxy, `docker-compose.yml` hardening.
- **Day 20**: Final polish, quality gate evaluation, demo script verification, comprehensive README creation.