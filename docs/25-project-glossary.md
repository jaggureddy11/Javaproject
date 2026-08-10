# Project Glossary & Domain Terminology

| Term | Definition |
|---|---|
| **VRP** | **Vehicle Routing Problem**: Classic combinatorial optimization problem determining optimal vehicle routes to serve a given set of customers. |
| **VRPTW** | **Vehicle Routing Problem with Time Windows**: Extension of VRP where deliveries must occur within specific customer time windows ($[w_{start}, w_{end}]$). |
| **Timefold Solver** | Open-source constraint satisfaction solver in Java (successor to OptaPlanner) for solving NP-hard optimization problems. |
| **Planning Entity** | Domain object modified by Timefold solver during optimization (e.g. `RouteStop`). |
| **Planning Variable** | Property on a Planning Entity that solver changes to find optimal score (e.g. `previousStopOrVehicle`). |
| **Shadow Variable** | Variable whose value is calculated automatically based on changes to primary planning variables (e.g. `arrivalTime`). |
| **Hard Constraint** | Mandatory rule that MUST NOT be violated in a valid operational route plan (e.g. vehicle capacity limit). |
| **Soft Constraint** | Optional preference goal prioritized by solver score (e.g. minimize total distance, minimize route disruption). |
| **Route Stability** | Concept of minimizing un-forced changes to existing planned routes during dynamic re-optimization. |
| **Disruption Penalty**| Soft score cost applied when a re-optimization job changes an unaffected stop sequence. |
| **PostGIS** | Spatial database extender for PostgreSQL providing geographic object support and spatial indexing (`GiST`). |
| **STOMP** | Simple Text Oriented Messaging Protocol used over WebSockets for pub/sub client messaging. |
| **Testcontainers** | Java library supporting lightweight, throwaway Docker containers for database integration testing. |
| **Haversine Formula**| Mathematical equation determining great-circle distance between two points on a sphere given latitude and longitude. |