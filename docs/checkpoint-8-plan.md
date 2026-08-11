# Checkpoint 8 — Real-Time Delivery Simulation Engine Plan

## 1. Problem & Objectives
The goal of Checkpoint 8 is to transform RouteResQ from a static route planning tool into an active, real-time logistics operations simulator.
The backend owns the simulation state, clock, vehicle positions, route progression, stop arrivals, service durations, delivery completions, and SLA window tracking. The frontend visualizes the simulation via STOMP WebSockets over SockJS.

---

## 2. Simulation Architecture & Principles
- **Backend Driven**: The backend is the single source of truth for simulation state, vehicle coordinates, and delivery progression.
- **Separate Operations**: Simulation logic is decoupled from `OptimizationService` and resides in a dedicated `com.routeresq.simulation` module.
- **No Uncontrolled Loops**: Uses a `ScheduledExecutorService` (ticking every 200ms–500ms real time) with explicit lifecycle management.
- **In-Memory High-Frequency State & Lightweight Persistence**: High-frequency GPS position updates remain in memory and stream via STOMP; session lifecycle states are persisted.
- **Configurable Simulation Clock**: Independent simulation time starting at dispatch hour (e.g. 08:00 / 480 min) with speed multipliers (`1x`, `2x`, `5x`).

---

## 3. State Machine Design

### Simulation Session Status (`SimulationStatus`)
- `CREATED`: Session instantiated from an optimization run.
- `READY`: Validated and ready for execution.
- `RUNNING`: Active simulation ticking and advancing time.
- `PAUSED`: Temporarily suspended; state preserved.
- `STOPPED`: Manually terminated by dispatcher.
- `COMPLETED`: All vehicles returned to depot; all deliveries complete.
- `FAILED`: Unhandled execution failure.

### Simulation Vehicle State (`SimVehicleStatus`)
- `AT_DEPOT`: Waiting at origin depot.
- `EN_ROUTE`: Traveling along segment toward target stop.
- `ARRIVED`: Reached order stop (or waiting for delivery window).
- `SERVICING`: Executing unloading/service duration (10 min).
- `RETURNING`: Final leg back to origin depot.
- `COMPLETED`: Successfully finished route and parked at depot.

### Stop & Order Status Transitions
- `RouteStop`: `PENDING` → `EN_ROUTE` → `ARRIVED` → `SERVICING` → `COMPLETED`
- `Order`: `ASSIGNED` → `IN_TRANSIT` → `DELIVERED`

---

## 4. Vehicle Movement & Position Interpolation
Using the Haversine urban routing model (30 km/h average travel speed):
1. For a route segment from $A(lat_1, lon_1)$ to $B(lat_2, lon_2)$ with total travel duration $D_{min}$:
2. Calculate segment progress fraction:
   $$\text{progress} = \frac{\text{elapsedSegmentSimulatedMinutes}}{D_{min}}$$
3. Interpolate linear position:
   $$lat(t) = lat_1 + \text{progress} \times (lat_2 - lat_1)$$
   $$lon(t) = lon_1 + \text{progress} \times (lon_2 - lon_1)$$
4. When $\text{progress} \ge 1.0$, trigger arrival event and transition vehicle to `ARRIVED`/`SERVICING`.

---

## 5. Delivery Windows & SLA Tracking
- **Early Arrival**: If vehicle arrives at $T_{arr} < T_{windowStart}$, vehicle remains in `ARRIVED` (waiting at customer site) until $T_{windowStart}$ before starting service.
- **On-Time Arrival**: Service starts immediately; `DELIVERED` event published on service completion.
- **Late Arrival**: If $T_{arr} > T_{windowEnd}$, SLA breach flag is recorded on the stop and broadcast in delivery event.

---

## 6. WebSocket Event Architecture
- **STOMP Endpoint**: `/ws` (SockJS fallback)
- **Topic Channel**: `/topic/simulation/{simulationId}`
- **Event Envelope**:
  ```json
  {
    "eventType": "VEHICLE_POSITION_UPDATED | ORDER_DELIVERED | ROUTE_COMPLETED | SIMULATION_COMPLETED",
    "simulationId": "...",
    "simulatedTimeMinutes": 514.5,
    "simulatedClockFormatted": "08:34",
    "payload": { ... }
  }
  ```

---

## 7. REST API Endpoints (`/api/v1/simulations`)
- `POST /api/v1/simulations`: Create simulation session from `optimizationRunId` and `speedMultiplier`.
- `POST /api/v1/simulations/{id}/start`: Begin simulation loop.
- `POST /api/v1/simulations/{id}/pause`: Pause simulation.
- `POST /api/v1/simulations/{id}/resume`: Resume simulation.
- `POST /api/v1/simulations/{id}/stop`: Terminate simulation.
- `GET /api/v1/simulations/{id}`: Retrieve session metadata and current snapshot.

---

## 8. Frontend Control Center (`SimulationView.tsx`)
- **Top Bar**: Live simulation clock display (`08:34 AM`), status pill, active speed multiplier buttons (`1x`, `2x`, `5x`), Start/Pause/Resume/Stop controls.
- **Live KPI Bar**: Active vehicles count, completed deliveries, remaining deliveries, on-time rate %, total distance traveled.
- **Activity Log Feed**: Real-time ticker logging arrival, service, and delivery completion events.
- **Map Integration**: Dynamic vehicle markers moving along route lines, colored segment polylines (completed vs active vs remaining legs).
- **Summary Overlay**: Post-simulation report presenting delivery success rate, total duration, and SLA performance.

---

## 9. Acceptance Criteria
- [ ] Backend simulation domain entities and thread-safe session manager.
- [ ] Configurable simulation clock (`1x`, `2x`, `5x`).
- [ ] Position linear interpolation & arrival/service time logic.
- [ ] WebSocket event broadcasting over `/topic/simulation/{id}`.
- [ ] REST API endpoints for session lifecycle.
- [ ] Dedicated React `SimulationView` with live control panel, activity feed, and KPI bar.
- [ ] Moving vehicle markers on Leaflet map without full-map rerenders.
- [ ] Unit tests for position interpolation, clock, and delivery lifecycle.
- [ ] `npx tsc --noEmit` returns 0 errors; `npm run build` succeeds; `mvn test` passes cleanly.
