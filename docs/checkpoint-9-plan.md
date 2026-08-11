# Checkpoint 9 — Incident Recovery & Dynamic Re-Optimization Plan

## 1. Problem & Architecture Overview
Logistics operations face unexpected disruptions (vehicle breakdowns, driver unavailability, traffic congestion, urgent order insertions).
Checkpoint 9 equips RouteResQ with dynamic incident detection, impact analysis, partial route preservation, Timefold-driven sub-plan re-optimization, and live STOMP WebSocket route updates without interrupting active simulations or requiring browser refreshes.

---

## 2. Incident Types & Lifecycle State Machine

### Incident Types (`IncidentType`)
- `VEHICLE_BREAKDOWN`: Engine/mechanical failure on active vehicle (Primary Demo Scenario).
- `DRIVER_UNAVAILABLE`: Driver shift expiration or sudden unavailability.
- `URGENT_ORDER`: High-priority emergency order insertion during active simulation.
- `TRAFFIC_DELAY`: Severe delay on active route segment.
- `ORDER_CANCELLED`: Customer cancels order mid-route.
- `DEADLINE_CHANGED`: Time window constraint updated dynamically.

### Incident Lifecycle (`IncidentStatus`)
- `OPEN` / `REPORTED`: Incident reported manually or automatically by simulation.
- `ANALYZING`: `IncidentImpactAnalyzer` evaluating affected route, completed stops, and candidate replacement vehicles.
- `RECOVERY_REQUIRED`: Impact confirmed; recovery plan requested.
- `RECOVERING`: Timefold solver executing sub-plan re-optimization.
- `RESOLVED`: Recovery plan applied; replacement routes active.
- `FAILED`: Recovery infeasible (e.g. no vehicle with remaining capacity/shift).

---

## 3. Impact Analysis & Partial Route Preservation (`IncidentImpactAnalyzer`)
- **Crucial Rule**: NEVER re-optimize or cancel completed deliveries!
- **Impact Identification**:
  1. Locate affected vehicle $V_{broken}$ and its active route $R_{broken}$.
  2. Split route stops into:
     - `Completed Stops` ($S_{completed}$): Already delivered orders — locked and preserved on $R_{broken}$.
     - `Affected Orders` ($O_{affected}$): Undelivered orders pending on $V_{broken}$.
  3. Locate candidate replacement vehicles ($V_{available}$) checking:
     - `Vehicle Status`: `IDLE` or `EN_ROUTE` (not `BREAKDOWN`/`MAINTENANCE`).
     - `Remaining Capacity`: $\text{MaxWeight} - \text{CurrentLoad}$.
     - `Remaining Driver Shift`: Hours remaining on driver shift schedule.
     - `Current Location`: Current simulated lat/lon coordinates.

---

## 4. Timefold Dynamic Re-Optimization (`IncidentRecoveryService`)
- Reuses Timefold solver engine (`OptimizationEngine` & `VRPTWConstraintProvider`).
- **Recovery Sub-Plan Problem Formulation**:
  - `Entities`: Only $O_{affected}$ (and new urgent orders if applicable).
  - `Vehicles`: $V_{available}$ at their current locations at simulated time $T_{sim}$.
  - `Constraints`: Hard capacity, hard delivery time windows relative to $T_{sim}$, driver shift limits.
- **Output**: `RecoveryResultDto` containing replacement routes, vehicle assignments, score, and ETA/distance deltas.

---

## 5. Route Versioning & Simulation Integration
- **Original Route Update**: Status updated to `PARTIALLY_COMPLETED` / `REOPTIMIZED`. Broken vehicle status set to `BREAKDOWN`.
- **Replacement Route Creation**: New route entity created with incremented `versionNumber` (e.g. Version 2) assigned to replacement vehicle.
- **Simulation Synchronization**: `SimulationService` updates in-memory vehicle state, halts $V_{broken}$, attaches replacement route, and continues delivery ticking.
- **STOMP Events Broadcast**:
  - `/topic/incidents/{incidentId}`: `INCIDENT_ANALYZED`, `RECOVERY_STARTED`, `RECOVERY_COMPLETED`
  - `/topic/simulation/{simulationId}`: `ROUTE_REPLANNED`, `VEHICLE_POSITION_UPDATED`

---

## 6. REST API Endpoints (`/api/v1/incidents`)
- `POST /api/v1/incidents`: Create/report an incident.
- `GET /api/v1/incidents`: List incidents (filtered by type/status).
- `GET /api/v1/incidents/{id}`: Get incident details.
- `POST /api/v1/incidents/{id}/analyze`: Execute impact analysis.
- `POST /api/v1/incidents/{id}/recover`: Execute Timefold re-optimization and apply recovery plan.

---

## 7. Frontend Operations UX (`IncidentsView.tsx` & `SimulationView.tsx`)
- **Incidents Table & Filter Bar**: Filter by type, status, and vehicle.
- **Report Incident Modal**: Trigger manual vehicle breakdown or urgent order insertion.
- **Incident Detail Drawer**: Before/After recovery comparison (affected orders count, replacement vehicle code, distance delta, solve time ms).
- **Simulation Integration**: Live red incident alert banner on simulation screen; broken vehicle marker turns red; replacement vehicle route renders immediately upon STOMP push.

---

## 8. Acceptance Criteria
- [ ] `IncidentImpactAnalyzer` identifies affected orders and preserves completed stops.
- [ ] `IncidentRecoveryService` executes Timefold re-optimization for affected orders.
- [ ] Vehicle capacity, time windows, and remaining driver shift hours enforced.
- [ ] Broken vehicle marked `BREAKDOWN`; replacement route created with version increment.
- [ ] Active simulation in `SimulationService` receives updated routes and continues ticking without page refresh.
- [ ] STOMP WebSocket events broadcast to clients.
- [ ] REST API endpoints for incident reporting, analysis, and recovery.
- [ ] Frontend `IncidentsView` and `SimulationView` show live recovery workflow and before/after comparison.
- [ ] End-to-end unit and integration test (`IncidentRecoveryServiceTest.java`) passes cleanly.
- [ ] `npx tsc --noEmit` returns 0 errors; `npm run build` succeeds; `mvn test` passes cleanly.
