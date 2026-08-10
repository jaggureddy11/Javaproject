# System Architecture Specification

## 1. Modular Monolith Component Architecture

```mermaid
graph TB
    subgraph Client Tier
        UI[React 18 Dashboard SPA]
    end

    subgraph API Gateways & Security
        SecFilter[Spring Security Filter Chain - JWT]
        WSEndpoint[STOMP WebSocket Endpoint /ws-net]
    end

    subgraph Business Logic Modules (Modular Monolith)
        AuthMod[auth module]
        FleetMod[fleet module: Vehicle, Driver, Depot]
        OrderMod[order module: Order, Address]
        OptMod[optimization module: Timefold Solver, Constraints]
        BaseMod[baseline module: Nearest-Neighbor]
        IncMod[incident module: Recovery Manager]
        SimMod[simulation module: Fleet Simulator]
        NotifMod[notification module: WS Publisher]
    end

    subgraph Data & Storage Tier
        Flyway[Flyway Migrations]
        Postgres[(PostgreSQL 16 + PostGIS 3.4)]
        Redis[(Redis Cache - Spatial Matrix)]
    end

    UI -->|HTTPS REST| SecFilter
    UI <-->|WSS STOMP| WSEndpoint
    
    SecFilter --> AuthMod
    SecFilter --> FleetMod
    SecFilter --> OrderMod
    SecFilter --> OptMod
    SecFilter --> IncMod
    SecFilter --> SimMod

    OptMod --> FleetMod
    OptMod --> OrderMod
    OptMod --> BaseMod
    IncMod --> OptMod
    IncMod --> NotifMod
    SimMod --> NotifMod
    
    NotifMod --> WSEndpoint
    
    FleetMod --> Postgres
    OrderMod --> Postgres
    OptMod --> Redis
    Flyway --> Postgres
```

---

## 2. Sequence Diagrams for Key Workflows

### 2.1 Batch Route Optimization Sequence

```mermaid
sequenceDiagram
    autonumber
    actor Dispatcher
    participant UI as React Control Center
    participant OptCtrl as OptimizationController
    participant OptSvc as RouteOptimizationService
    participant RouteProv as RoutingProvider (Haversine/OSRM)
    participant Timefold as Timefold SolverManager
    participant DB as PostgreSQL/PostGIS

    Dispatcher->>UI: Click "Generate Optimized Plan"
    UI->>OptCtrl: POST /api/v1/optimization/runs { depotId, orderIds, vehicleIds }
    OptCtrl->>OptSvc: createAndRunOptimization(request)
    OptSvc->>DB: Fetch Depot, Orders, Vehicles
    DB-->>OptSvc: Entities loaded
    OptSvc->>RouteProv: Calculate Distance & Time Matrix (N x N)
    RouteProv-->>OptSvc: Matrix populated
    OptSvc->>Timefold: solve(problemId, routePlanSolution)
    Note over Timefold: Executes Construction Heuristic<br/>& Local Search (Tabu Search)
    Timefold-->>OptSvc: Optimal RoutePlanSolution returned
    OptSvc->>DB: Save Routes, RouteStops & OptimizationRun Metrics
    OptSvc-->>OptCtrl: OptimizationRunDTO (Scores, Routes, Distance)
    OptCtrl-->>UI: 200 OK + JSON Payload
    UI->>Dispatcher: Render Optimized Polylines & Stop Sequence on Map
```

---

### 2.2 Vehicle Breakdown Incident & Re-Optimization Sequence

```mermaid
sequenceDiagram
    autonumber
    actor Dispatcher
    participant UI as React Control Center
    participant IncCtrl as IncidentController
    participant IncSvc as IncidentRecoveryService
    participant OptSvc as RouteOptimizationService
    participant Timefold as Timefold SolverManager
    participant WS as WebSocket Publisher
    participant DB as PostgreSQL

    Dispatcher->>UI: Select Vehicle A -> Click "Report Vehicle Breakdown"
    UI->>IncCtrl: POST /api/v1/incidents { type: VEHICLE_BREAKDOWN, vehicleId: A }
    IncCtrl->>IncSvc: handleVehicleBreakdown(vehicleId)
    IncSvc->>DB: Update Vehicle A status = OUT_OF_SERVICE
    IncSvc->>DB: Fetch assigned uncompleted orders for Vehicle A & available active vehicles
    Note over IncSvc: Lock completed/in-transit stops on all vehicles.<br/>Mark uncompleted orders as UNASSIGNED.
    IncSvc->>OptSvc: reoptimizeSubPlan(unassignedOrders, availableVehicles, disruptionPenalty)
    OptSvc->>Timefold: solve(problemId, subSolution)
    Note over Timefold: Solves VRPTW with<br/>Disruption Cost Soft Constraint
    Timefold-->>OptSvc: New Feasible RoutePlan returned
    OptSvc->>DB: Persist new RouteVersions & RouteStops
    OptSvc->>WS: publishEvent("/topic/routes", ROUTE_REOPTIMIZED_EVENT)
    WS-->>UI: Push STOMP Message
    UI->>Dispatcher: Animate Route Transition on Map (Vehicle A routes shifted to B & C)
```

---

## 3. Concurrency & Threading Model
1. **Spring Virtual Threads (Java 21)**: Web request handling executes on virtual threads, ensuring high throughput for REST calls and database queries.
2. **Timefold Solver Async Execution**: `SolverManager.solveBuilder()` offloads NP-hard solving to background worker threads, allowing non-blocking API polling or WebSocket progress broadcasts.
3. **Simulation Loop**: Scheduled single-thread executor (`TaskScheduler`) ticks every 1 second to advance vehicle positions along route polylines and publish WebSocket location updates.