# Technical Requirements Document (TRD)

## 1. Architectural Style: Modular Monolith
RouteResQ is designed as a **Modular Monolith** in Java 21 using Spring Boot 3.3. Monolithic packaging eliminates distributed system network latency, remote service deployment overhead, and distributed transaction complexity while enforcing strict internal module boundaries via Java package scoping and Spring bean isolation.

```
com.routeresq
├── auth/           # Spring Security, JWT tokens, RBAC filters
├── user/           # User account domain & credentials management
├── fleet/          # Vehicle, Driver, Depot domain models & repositories
├── order/          # Order, DeliveryAddress, PostGIS spatial mapping
├── optimization/   # Timefold Solver domain model, constraints, solvers
├── baseline/       # Greedy Nearest-Neighbor comparison algorithm
├── incident/       # Incident handling, breakdown recovery logic
├── simulation/     # Delivery simulation thread & event triggers
├── routing/        # Distance/Time Matrix calculators (Haversine & OSRM)
├── notification/   # WebSocket STOMP handlers & push messaging
├── analytics/      # Metric aggregation & benchmark comparative analysis
└── shared/         # Common DTOs, exceptions, domain events, utilities
```

---

## 2. Technology Stack & Compatibility Matrix

| Category | Technology / Library | Version | Selection Rationale |
|---|---|---|---|
| **Runtime Language** | Java OpenJDK | 21 (LTS) | Virtual Threads (Project Loom), Record Types, Pattern Matching. |
| **Framework** | Spring Boot | 3.3.x | Modern Spring 6 core, native Actuator, Spring Data JPA, WebSockets. |
| **Constraint Solver**| Timefold Solver | 1.11.x | Open-source successor to OptaPlanner; high performance solver for VRPTW. |
| **Database** | PostgreSQL | 16.x | Enterprise ACID database with robust spatial capabilities. |
| **Spatial Engine** | PostGIS | 3.4.x | Native geometry types (`Point`), spatial indexing (`GiST`), `ST_DistanceSphere`. |
| **ORM / Data** | Hibernate Spatial | 6.5.x | JPA spatial mappings for PostGIS `Geometry` types. |
| **Caching** | Redis | 7.2.x | High-speed cache for distance matrices & active simulation states. |
| **Real-time Push** | Spring WebSocket + STOMP| 3.3.x | Lightweight, standardized pub/sub over WebSocket connections. |
| **Database Migration**| Flyway | 10.x | Version-controlled SQL migration scripts (`V1__...sql`). |
| **Testing** | JUnit 5, Mockito, Testcontainers | Latest | Containerized integration tests against real PostgreSQL/PostGIS. |
| **Frontend Framework**| React | 18.3.x | Component-based UI with Virtual DOM and rich mapping ecosystem. |
| **Frontend Language** | TypeScript | 5.4.x | Type safety across API payloads and domain models. |
| **Map Rendering** | Leaflet.js / React-Leaflet | 4.x | High-performance open-source map rendering & marker animations. |
| **Styling** | Vanilla CSS / CSS Modules | Standard | Custom mission-control dark/light design system. |

---

## 3. Core Component Architectures

### 3.1 Distance & Duration Provider (`RoutingProvider`)
To avoid external network API dependency failures during local benchmarking, `RoutingProvider` uses an adapter pattern:
- **`HaversineRoutingProvider`**: Fast in-memory spatial distance calculation using great-circle formula with configurable average speed (40 km/h urban).
- **`OsrmRoutingProvider`**: Pluggable provider invoking local or remote OSRM engine for real road network routing.

### 3.2 Timefold Integration Architecture
- Uses `timefold-solver-spring-boot-starter`.
- `@PlanningSolution`: `RoutePlanSolution` containing list of `Vehicle` entities and `RouteStop` entities.
- `@PlanningEntity`: `RouteStop` representing customer order visits with `@PlanningVariable` linking to previous stop or vehicle.
- Shadow Variables: `@PiggybackShadowVariable` calculates arrival times and cumulative weight load at each stop.

---

## 4. Resilience & Error Handling
- **Solver Timeout Safety**: Timefold `SolverManager` configures a hard termination limit (e.g., 10 seconds). If no optimal solution is reached, the best feasible solution found so far is returned.
- **Global Exception Handling**: `@RestControllerAdvice` translates domain exceptions into standard RFC 7807 `ProblemDetails` JSON responses.