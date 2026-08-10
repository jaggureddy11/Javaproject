# Technical Interview Preparation Guide

## 1. System Architecture Q&A

### Q1: Why did you choose a Modular Monolith over Microservices?
**Answer**: Microservices introduce network serialization overhead, distributed transaction complexity (Sagas), and deployment friction. RouteResQ requires tight in-memory coupling between order spatial data, fleet capacities, distance matrices, and the Timefold solver engine. A modular monolith in Spring Boot with clear package boundaries (`order`, `fleet`, `optimization`, `incident`) gives us strong domain isolation and high speed without network latency. If scale requires it, individual modules can be extracted later with minimal refactoring.

### Q2: How does your system solve the Vehicle Routing Problem with Time Windows (VRPTW)?
**Answer**: VRPTW is NP-hard. We model it using Timefold Solver 1.x. Our `@PlanningSolution` is `RoutePlanSolution`, and `@PlanningEntity` is `RouteStop`. We use `@PlanningVariable` to link stop sequences. We configure a Construction Heuristic (`FIRST_FIT_DECREASING`) to quickly find an initial feasible state, followed by Local Search (`TABU_SEARCH` / `LATE_ACCEPTANCE`) to explore the solution space while enforcing Hard Constraints (Capacity, Delivery Windows, Shifts) and Soft Constraints (Distance, Duration, Disruption Penalty).

### Q3: How do you handle route stability during mid-day vehicle breakdown re-optimization?
**Answer**: Re-optimizing from scratch can cause "route nervousness" where unaffected drivers see their stop order change completely. We handle this by locking all completed (`DELIVERED`) and in-progress (`IN_TRANSIT`) stops. For remaining future stops, we introduce a **Route Disruption Penalty** soft constraint (`S4`). Changing an existing stop's assigned vehicle or sequence incurs a score penalty, ensuring the solver only changes routes when necessary to restore feasibility.

### Q4: How do WebSockets work in RouteResQ for real-time updates?
**Answer**: We use Spring WebSocket with STOMP broker. Clients subscribe to `/topic/routes` and `/topic/simulation`. When an incident occurs, the `IncidentRecoveryService` triggers re-optimization, persists new route versions to PostgreSQL, and publishes a `RouteUpdatedEvent`. The `NotificationService` intercepts this event and converts it to a STOMP JSON message sent over WebSockets to connected React clients.

### Q5: How is spatial location data handled in PostgreSQL?
**Answer**: We use PostGIS extension with WGS 84 (`EPSG:4326`) `GEOMETRY(Point, 4326)` columns. Spatial indexes are created using `GiST` (`USING GIST (location)`). We compute distances using `ST_DistanceSphere` or in-memory Haversine formulas for high-throughput distance matrix generation.