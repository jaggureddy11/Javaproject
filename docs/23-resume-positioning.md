# Resume Positioning & Professional Bullet Points

## Version 1: Backend Engineering Focused
- **RouteResQ — Real-Time Constraint-Based Last-Mile Delivery Optimization Platform**
  - Architected and built a production-grade modular monolith in **Java 21, Spring Boot 3.3, and PostgreSQL/PostGIS** solving the Vehicle Routing Problem with Time Windows (VRPTW) using **Timefold Solver**.
  - Engineered hard/soft constraint models (capacity, delivery windows, driver shifts, route disruption penalty) resulting in a **21.3% reduction in total fleet route distance** over a Nearest-Neighbor baseline.
  - Developed real-time incident recovery manager using **Spring ApplicationEvents and STOMP WebSockets** to automatically re-route unassigned orders within **< 2 seconds** during simulated vehicle breakdowns.
  - Implemented high-performance spatial queries with PostGIS (`EPSG:4326`, `GiST` spatial indexing) and integrated Testcontainers for automated database integration testing.

---

## Version 2: Full-Stack / Software Engineering Focused
- **RouteResQ — Logistics Operations & Dynamic Fleet Recovery Control Center**
  - Designed and built a full-stack real-time logistics platform using **Spring Boot 3.3, React 18, TypeScript, and Leaflet.js**.
  - Created interactive mission-control map UI streaming live vehicle movements, stop status updates, and route polylines over **STOMP WebSockets**.
  - Implemented dual optimization solver engine comparing greedy baseline heuristics against Timefold constraint algorithms, rendering side-by-side performance benchmarks.
  - Package infrastructure using **Docker Compose** multi-stage builds and Nginx reverse proxy with full unit/integration test coverage.

---

## Version 3: ATS-Optimized Bullet Points
- Applied **Java 21, Spring Boot, PostGIS, Timefold Solver, Redis, and WebSockets** to construct a real-time last-mile logistics routing system.
- Designed database schema using PostgreSQL with spatial geometry types (`Point`), Flyway migrations, and JPA spatial queries.
- Created real-time event-driven architecture delivering live route recalculations with $< 500$ ms WebSocket broadcast latency.
- Authored 25 comprehensive technical architecture documents including ERDs, REST API OpenAPI specifications, and risk mitigation registers.