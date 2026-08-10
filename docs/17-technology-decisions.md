# Architectural Decision Records (ADRs)

## ADR-001: Selection of Java 21 & Spring Boot 3.3
- **Status**: Approved
- **Context**: Need a robust, strongly typed backend ecosystem with enterprise ORM and concurrency support.
- **Decision**: Use Java 21 (LTS) with Spring Boot 3.3. Virtual Threads (Project Loom) allow non-blocking asynchronous solver polling and high-concurrency WebSocket messaging without thread pool starvation.
- **Consequences**: Enables modern language features (Records, Pattern Matching) and high I/O performance.

## ADR-002: Modular Monolith vs Microservices Architecture
- **Status**: Approved
- **Context**: Routing optimization requires tight real-time coupling between orders, vehicles, spatial algorithms, and solver models.
- **Decision**: Adopt a Modular Monolith. Microservices would introduce network serialization latency, distributed transaction overhead (Sagas), and deployment friction for a single-domain application.
- **Consequences**: Fast in-memory communication between domain modules; straightforward local setup via single Docker container; clear package boundaries preserve future microservice extractability if needed.

## ADR-003: Choice of Timefold Solver over Custom Heuristics
- **Status**: Approved
- **Context**: VRPTW is NP-hard. Writing a custom meta-heuristic solver (Genetic Algorithm / Simulated Annealing from scratch) takes months and lacks constraint incremental score calculation optimization.
- **Decision**: Use **Timefold Solver 1.x** (open-source successor to OptaPlanner).
- **Consequences**: Out-of-the-box incremental score calculation, battle-tested Tabu Search/Local Search, native Spring Boot integration.

## ADR-004: PostgreSQL + PostGIS for Spatial Persistence
- **Status**: Approved
- **Context**: Order locations and depot coordinates require high-precision geospatial indexing and distance queries.
- **Decision**: Use PostgreSQL 16 with PostGIS 3.4 extensions (`GEOMETRY(Point, 4326)`).
- **Consequences**: Standard SQL query compatibility, spatial indexing (`GiST`), native `ST_DistanceSphere` spatial calculations.