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

## Checkpoint 7: Interactive Map & Dashboard UI
- **Objective**: Build React mission-control dashboard with Leaflet/Mapbox integration.
- **Tasks**: Render central map, depot markers, customer order markers, vehicle route polylines, sidebar panels.
- **Acceptance Criteria**: Map displays clean visual routes; sidebar displays vehicle loads and stop sequences.

## Checkpoint 8: Delivery Simulation Engine
- **Objective**: Build real-time delivery day simulator.
- **Tasks**: Implement `SimulationService` timer loop, step vehicle locations along route polylines, emit location events.
- **Acceptance Criteria**: Simulation smoothly moves truck markers across map at selectable speed multipliers (1x, 2x, 5x).

## Checkpoint 9: Incident Recovery & Dynamic Re-Optimization
- **Objective**: Handle vehicle breakdowns and urgent orders with automatic route re-optimization.
- **Tasks**: Implement `IncidentRecoveryService`, locked stop logic, `S4: Disruption Penalty` soft constraint.
- **Acceptance Criteria**: Triggering vehicle breakdown re-assigns uncompleted orders to active vehicles in under 5s.

## Checkpoint 10: Real-Time WebSocket STOMP Pushes
- **Objective**: Broadcast live route updates to UI via STOMP WebSockets.
- **Tasks**: Configure Spring WebSocket message broker, STOMP endpoint `/ws-net`, React WebSocket client listeners.
- **Acceptance Criteria**: Route re-optimization instantly updates UI map and stop lists without manual page refresh.

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