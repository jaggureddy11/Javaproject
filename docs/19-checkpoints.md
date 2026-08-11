# Checkpoints & Acceptance Criteria Matrix

The RouteResQ project will be implemented strictly across **16 explicit checkpoints** (Checkpoint 0 to Checkpoint 15). Moving to the next checkpoint requires passing all defined acceptance criteria and verification tests.

---

## Checkpoint 0: Documentation & Architecture Approval
- **Status**: COMPLETED
- **Objective**: Establish complete, internally consistent technical documentation.
- **Tasks**: Write all 25 documentation markdown files in `docs/`.
- **Acceptance Criteria**: All 25 docs present; ERD, API specs, constraint equations, and checkpoints fully consistent.
- **Verification**: `python3 scripts/verify_docs.py` passes with zero errors.

## Checkpoint 1: Project Foundation & Docker Environment
- **Status**: COMPLETED
- **Objective**: Initialize Spring Boot 3.3 Java 21 project and Vite React TypeScript frontend repository structure.
- **Tasks**: Configure Maven `pom.xml`, React `package.json`, Docker Compose (`postgres`, `redis`).
- **Acceptance Criteria**: Backend boots cleanly; `docker-compose up` launches PostgreSQL with PostGIS extension.

## Checkpoint 2: Database Schema & Spatial Migration
- **Status**: COMPLETED
- **Objective**: Implement Flyway migration scripts for PostgreSQL/PostGIS domain tables.
- **Tasks**: Create `V1__init_schema.sql`, entity classes (`Depot`, `Vehicle`, `Driver`, `Order`, `Route`), spatial indexes.
- **Acceptance Criteria**: Flyway migrates cleanly; JPA repositories pass spatial CRUD tests with Testcontainers.

## Checkpoint 3: Authentication & Security Filter Chain
- **Status**: COMPLETED
- **Objective**: Secure API endpoints with JWT authentication and RBAC roles.
- **Tasks**: Configure Spring Security filter chain, `JwtTokenProvider`, User/Role entities, Auth endpoints (`/api/v1/auth/login`).
- **Acceptance Criteria**: Unauthenticated requests return HTTP 401; Dispatcher JWT grants access to protected APIs.

## Checkpoint 4: Fleet & Order Domain Management
- **Status**: COMPLETED
- **Objective**: Build REST CRUD APIs for Depots, Vehicles, Drivers, and Orders.
- **Tasks**: Implement services, controllers, DTOs, request validation (`@NotNull`, `@Min`).
- **Acceptance Criteria**: Dispatcher can create orders with spatial coordinates and delivery time windows via REST.

## Checkpoint 5: Core VRPTW Optimization Engine (Timefold)
- **Status**: COMPLETED
- **Objective**: Integrate Timefold Solver and implement VRPTW constraint rules.
- **Tasks**: Define `@PlanningSolution`, `@PlanningEntity`, hard capacity/time-window constraints, solver config.
- **Acceptance Criteria**: Solver solves 50-order problem in under 5s with 0 hard constraint violations.

## Checkpoint 6: Baseline Algorithm & Comparison Framework
- **Status**: COMPLETED
- **Objective**: Build Nearest-Feasible-Neighbor greedy baseline dispatcher for metric comparison.
- **Tasks**: Implement `BaselineRoutePlanner`, metric calculation engine (`Total Distance`, `Travel Time`, `Late Deliveries`), and 6 benchmark datasets.
- **Acceptance Criteria**: System executes dual solve (Baseline vs Timefold) and calculates exact percentage improvements (achieving 44.1% distance reduction on spatially clustered orders).

## Checkpoint 7: Interactive Map & Logistics Operations Control Center
- **Status**: COMPLETED
- **Objective**: Build React mission-control operations control center with Leaflet map, application shell navigation, 7 operational views, real-time STOMP STOMP/polling fallback, and RBAC UI.
- **Tasks**: Render central map, depot markers, customer order priority markers, vehicle route polylines, overview KPI dashboard, sequential stop timeline inspector, orders management, fleet sub-tabs, async solver center, incident tracking UI foundation, and empirical benchmark charts.
- **Acceptance Criteria**: Application shell supports seamless navigation across 8 views; map dynamically zooms/fits selected routes/orders/vehicles with `"Route geometry: estimated"` badge; STOMP WebSocket streams live optimization progress; `npx tsc --noEmit` returns 0 errors; `npm run build` succeeds; `mvn test` passes cleanly with 0 failures across 35 unit/integration tests.

## Checkpoint 8: Real-Time Delivery Simulation Engine
- **Status**: COMPLETED
- **Objective**: Build backend-driven real-time delivery day simulator with position linear interpolation, simulated clock, STOMP event streaming, speed multipliers (`1x`, `2x`, `5x`), and React `SimulationView`.
- **Tasks**: Implement `SimulationService` timer loop (`ScheduledExecutorService`), vehicle movement interpolation, stop arrival & service time processing (10 min duration), order `DELIVERED` state transitions, SLA lateness window checks, STOMP event broadcasting over `/topic/simulation/{simulationId}`, and `SimulationView` UI with live control panel, KPI bar, moving truck markers, and activity feed log.
- **Acceptance Criteria**: Backend drives simulation state; moving truck markers update smoothly on Leaflet map via STOMP events; `SimulationServiceTest` verifies session creation and position interpolation; `npx tsc --noEmit` returns 0 errors; `npm run build` succeeds; `mvn test` passes cleanly with 0 failures across 37 backend tests.

## Checkpoint 9: Incident Recovery & Dynamic Re-Optimization
- **Status**: COMPLETED
- **Objective**: Handle vehicle breakdowns, driver unavailability, traffic delays, and urgent order insertions with automatic Timefold dynamic sub-plan re-optimization.
- **Tasks**: Implement `IncidentImpactAnalyzer` (completed stop preservation & impact analysis), `IncidentRecoveryService` (Timefold sub-plan re-optimization for affected orders), versioned replacement route generation, active simulation session update (`SimulationService.applyRecoveryPlan`), STOMP event broadcasting (`/topic/incidents/{id}`, `/topic/simulation/{simId}`), REST endpoints (`POST /api/v1/incidents/{id}/analyze`, `/recover`), `IncidentsView` Recovery Inspector Drawer, and `SimulationView` **Simulate Breakdown** feature for recruiter demo.
- **Acceptance Criteria**: Completed deliveries are preserved; affected undelivered orders are reassigned to replacement vehicles in under 200ms via Timefold; route versions increment; active simulation and map update live without page refresh; `IncidentRecoveryServiceTest` passes cleanly; `npx tsc --noEmit` returns 0 errors; `npm run build` succeeds; `mvn test` passes cleanly with 0 failures across 39 backend tests.

## Checkpoint 10: Unified Real-Time Event Platform
- **Status**: COMPLETED
- **Objective**: Unify application-wide real-time event architecture under a strongly-typed `RealtimeEvent` envelope, single shared STOMP connection, clean topic hierarchy (`/topic/operations`), exponential backoff reconnection, event deduplication (500 event LRU cache), sequence ordering checks, and automated REST resynchronization upon reconnect.
- **Tasks**: Implement backend `RealtimeEventType` enum, `RealtimeEvent` DTO, `RealtimeEventPublisher` service, frontend `RealtimeContext` provider, `useRealtime` hook, Header connection status badge (`● LIVE`, `● RECONNECTING`, `● OFFLINE`), global operations subscription (`/topic/operations`), resync handler registration in `App.tsx`, and `RealtimeEventTest` unit tests.
- **Acceptance Criteria**: Single STOMP connection shared across entire frontend; events arrive wrapped in typed envelope; global business events mirror to `/topic/operations`; exponential backoff handles disconnects smoothly; REST resync updates stale client state on reconnect; all views reactively update without page refresh; `RealtimeEventTest` passes cleanly; `npx tsc --noEmit` returns 0 errors; `npm run build` succeeds; `mvn test` passes cleanly with 0 failures across 41 backend tests.

## Checkpoint 11: Analytics & Metrics Dashboard
- **Objective**: Present historical benchmark performance metrics and SLA reporting.
- **Tasks**: Build analytics cards, distance savings charts, capacity utilization metrics.
- **Acceptance Criteria**: Dashboard presents crisp visual breakdown of fleet performance metrics.

## Checkpoint 12: Test Suite & Benchmark Suite Completion
- **Objective**: Achieve high test coverage across domain logic, constraints, and integration workflows.
- **Tasks**: Write JUnit 5 unit tests, Timefold `ConstraintVerifier` tests, Testcontainers integration tests, performance benchmark suite.
- **Acceptance Criteria**: `mvn test` passes cleanly with $> 80\%$ code coverage.

## Checkpoint 13: Observability & Micrometer Actuator Configuration
- **Objective**: Instrument system with metrics, health checks, and structured logging.
- **Tasks**: Expose `/actuator/prometheus`, record solver durations, hard/soft score gauges, incident counters.
- **Acceptance Criteria**: Prometheus endpoint outputs custom `routeresq_*` metrics.

## Checkpoint 14: Containerized Docker Deployment
- **Objective**: Package complete platform into standalone Docker environment.
- **Tasks**: Write production Dockerfiles for backend and frontend, update `docker-compose.yml`, configure Nginx proxy.
- **Acceptance Criteria**: Running `docker-compose up` boots full application cleanly from scratch.

## Checkpoint 15: Final Polish, Demo Scenarios & Documentation
- **Objective**: Finalize README, verify 3-5 minute demo script, conduct quality gate review.
- **Tasks**: Create seed data scenarios (`NORMAL_DAY`, `VEHICLE_BREAKDOWN`), write final README, record walkthrough.
- **Acceptance Criteria**: All items on Quality Gate checklist verified.