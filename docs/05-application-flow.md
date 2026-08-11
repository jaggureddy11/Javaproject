# Application Flow & User Journeys

## 1. Dispatcher End-to-End Operational Journey

```mermaid
flowchart TD
    Start([Dispatcher Logs In]) --> AuthCheck{JWT Valid?}
    AuthCheck -- No --> LoginScreen[Render Login View] --> Start
    AuthCheck -- Yes --> Dashboard[Render Operations Control Center]
    
    Dashboard --> ActionChoice{Select Operational Task}
    
    ActionChoice -- 1. Fleet & Order Setup --> ImportOrders[Import/Create Delivery Orders & Vehicles]
    ImportOrders --> ViewUnassigned[View Orders on Map & Unassigned List]
    ViewUnassigned --> TriggerOpt[Click 'Generate Optimized Plan']
    
    ActionChoice -- 2. Route Optimization --> TriggerOpt
    TriggerOpt --> SolverRunning[Solver Executes: Construction Heuristic + Tabu Search]
    SolverRunning --> PlanReady[Display Optimized Plan & Baseline Matrix]
    PlanReady --> InspectRoutes[Inspect Vehicle Route Sequences & Stop ETAs]
    
    InspectRoutes --> StartSim[Click 'Start Delivery Day Simulation']
    StartSim --> SimRunning[Simulation Running: Live Vehicle Movement on Map]
    
    SimRunning --> IncidentEvent{Mid-Day Incident Occurs?}
    IncidentEvent -- Vehicle Breakdown --> TriggerBreakdown[Trigger Vehicle Breakdown Incident]
    IncidentEvent -- New Urgent Order --> InjectOrder[Inject Urgent Order]
    IncidentEvent -- No Incident --> DeliveriesComplete[All Deliveries Completed]
    
    TriggerBreakdown --> AutoReopt[Backend Locks Active Stops & Executes Re-Optimization]
    InjectOrder --> AutoReopt
    
    AutoReopt --> WSPush[WebSocket Pushes New Route Versions to UI]
    WSPush --> MapUpdate[Map Update: Routes Re-assigned Seamlessly]
    MapUpdate --> SimRunning
    
    DeliveriesComplete --> ViewAnalytics[View Final Analytics & Metrics Report]
    ViewAnalytics --> End([Operational Shift Concluded])
```

---

## 2. Detailed Journey Steps

### Step 1: Authentication & Control Center Initialization
- User enters credentials (`dispatcher@routeresq.io` / `dispatch123`).
- Backend returns JWT token + user details + granted authorities (`ROLE_ADMIN`, `ROLE_DISPATCHER`, `ROLE_DRIVER`).
- React application shell initializes interactive Leaflet map container centered on Greater Chicago region (41.8781° N, 87.6298° W).
- STOMP WebSocket connection established over SockJS to `/ws`.

### Step 2: Operational View Navigation & Route Optimization
- Dispatcher seamlessly navigates between 9 operational views:
  1. **Overview Dashboard**: High-level operational KPIs, system health status, and solver state.
  2. **Interactive Map View**: Centrepiece GIS map with Leaflet polylines, depot markers, and order priority markers.
  3. **Orders Operations**: Multi-attribute filtering (search, status, priority, sorting) and order creation modal (`OrderForm.tsx`).
  4. **Fleet Management**: Sub-tabs for Vehicles, Drivers, and Depots displaying status, shift times, and capacities.
  5. **Routes Inspector**: Detailed sequential stop timelines (DEPOT -> Stop 1 -> Stop 2 -> DEPOT) with ETAs and window compliance.
  6. **VRPTW Solver Center**: Async solver execution (`POST /runs` -> `202 Accepted`), STOMP progress updates, post-solve metrics, and honest infeasibility UX.
  7. **Real-Time Simulation**: Dedicated delivery day simulator displaying live moving vehicle markers, simulation clock, speed controls, and live activity event feed.
  8. **Incidents Operations**: Tracking route disruptions, vehicle breakdowns, and urgent dispatch events.
  9. **Benchmark Suite**: Empirical evaluation of all 6 standard datasets comparing Timefold solver against Nearest-Feasible-Neighbor baseline with SVG charts.

### Step 3: Real-Time Delivery Simulation & WebSocket Streaming
- Dispatcher navigates to **Simulation** view, selects an optimization run ID, and selects speed multiplier (`1x`, `2x`, `5x`).
- Click **Initialize Simulation** (`POST /api/v1/simulations`) → `SimulationService` creates in-memory session.
- Click **Start** (`POST /api/v1/simulations/{id}/start`) → Backend launches `ScheduledExecutorService` (ticking every 250ms real time).
- Simulated clock advances starting at `08:00 AM`. Vehicle position coordinates linearly interpolate along route segments.
- STOMP client receives `VEHICLE_POSITION_UPDATED` events on topic `/topic/simulation/{simulationId}` and animates truck markers smoothly across map.
- Stop arrival triggers `ARRIVED` → `SERVICING` (10 min service duration) → `ORDER_DELIVERED` event → Order status updates to `DELIVERED` → Live ticker logs delivery event.
- Upon completion of all vehicle routes, `SIMULATION_COMPLETED` modal presents final delivery success rate, on-time percentage, and total distance.

### Step 4: Incident Recovery & Dynamic Timefold Re-Optimization
- Dispatcher or active simulation triggers vehicle breakdown (e.g. `V-101` engine failure at `08:42 AM` simulated time).
- Backend creates incident entity (`IncidentStatus.OPEN`) and executes `IncidentImpactAnalyzer.analyzeImpact()`:
  - `Completed Stops` ($S_{completed}$): Locked and preserved on original route $R_{broken}$.
  - `Affected Orders` ($O_{affected}$): Undelivered orders pending on $V_{broken}$.
  - `Candidate Replacement Vehicles` ($V_{available}$): Identified based on active status, remaining capacity, remaining driver shift hours, and current simulated coordinates.
- Dispatcher clicks **Execute Dynamic Timefold Recovery** (`POST /api/v1/incidents/{id}/recover`):
  - `IncidentRecoveryService` passes $O_{affected}$ and $V_{available}$ into Timefold VRPTW solver engine.
  - Sub-plan re-optimization solves in under 200ms.
  - Replacement route created with incremented `versionNumber` (e.g. Version 2) assigned to replacement vehicle ($V_{replacement}$).
  - Active `SimulationService` session halts $V_{broken}$, attaches replacement route to $V_{replacement}$, and broadcasts STOMP events (`ROUTE_REPLANNED`, `RECOVERY_COMPLETED`).
  - Interactive Leaflet map updates instantly without browser refresh, and delivery simulation continues smoothly to 100% completion.

### Step 5: Unified Real-Time Event Platform & Cross-View Synchronization
- A single STOMP WebSocket connection is established and shared across the entire application shell via `RealtimeProvider`.
- All backend events are wrapped in a strongly-typed `RealtimeEvent` envelope (`eventId`, `eventType`, `entityType`, `entityId`, `occurredAt`, `sequence`, `payload`).
- Business events (`ORDER_DELIVERED`, `ROUTE_REPLANNED`, `VEHICLE_STATUS_CHANGED`, `INCIDENT_CREATED`, `RECOVERY_COMPLETED`) stream over `/topic/operations`.
- High-frequency position ticks stream over `/topic/simulation/{simulationId}`.
- If network disconnects, `RealtimeContext` executes exponential backoff reconnection (`1s` → `2s` → `4s` → `8s` → `16s` → `30s max`), displaying a connection badge (`● LIVE`, `● RECONNECTING`, `● OFFLINE`) on the header.
- Upon reconnect, `registerResyncHandler()` automatically triggers REST resynchronization to replace stale client state before continuing live event processing.