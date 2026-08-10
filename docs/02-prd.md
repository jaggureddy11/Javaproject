# Product Requirements Document (PRD)

## 1. Document Control & Overview
- **Product Name**: RouteResQ
- **Version**: 1.0.0-SNAPSHOT
- **Target Release**: MVP (1-Month Engineering Cycle)
- **Status**: Approved for Architecture & Implementation

---

## 2. Problem Statement & Target Users

### Problem Context
Modern last-mile delivery fleets lose millions in efficiency due to manual dispatching, inefficient stop ordering, and high latency when recovering from mid-shift vehicle failures or urgent order injections.

### User Personas

#### Persona 1: Dispatcher / Operations Manager (Primary)
- **Role**: Manages fleet operations, assigns daily orders, monitors route execution, handles mid-day incidents.
- **Pain Points**: Lacks real-time visibility into vehicle locations; manual re-routing during breakdowns takes 30+ minutes; customer delivery SLAs are missed.
- **Goals**: Create optimal daily route plans in under 30 seconds; automatically recover from vehicle breakdowns without phone calls.

#### Persona 2: Delivery Driver (Secondary)
- **Role**: Operates vehicle, follows assigned delivery stop sequence, updates order status, reports breakdowns.
- **Pain Points**: Disrupted routes change mid-shift without clear notice; overloaded vehicle weight limits.
- **Goals**: Clear stop sequence, accurate arrival ETAs, simple status reporting.

#### Persona 3: System Administrator (Admin)
- **Role**: Manages system users, fleet configurations, inspects system health and optimization benchmarks.
- **Pain Points**: Hard to quantify software ROI; lack of system observability.
- **Goals**: Benchmark baseline vs. optimized fleet metrics, track solver performance.

---

## 3. User Stories & Acceptance Criteria

| ID | Persona | User Story | Acceptance Criteria |
|---|---|---|---|
| **US-01** | Dispatcher | As a Dispatcher, I want to create delivery orders with weight, location, and time windows, so that they can be scheduled. | System validates coordinates, non-negative weight, and valid `[timeWindowStart, timeWindowEnd]`. |
| **US-02** | Dispatcher | As a Dispatcher, I want to run a route optimization job, so that orders are assigned to optimal vehicles and stop sequences. | Timefold solver returns feasible solution respecting capacity and time windows; UI displays routes on map. |
| **US-03** | Dispatcher | As a Dispatcher, I want to compare optimized results against a baseline algorithm, so that I can see distance and time savings. | System displays baseline (Greedy) vs optimized metrics side-by-side with exact percentage improvements. |
| **US-04** | Driver/Sim | As a Driver/Sim, I want to report a vehicle breakdown, so that affected orders are re-assigned. | Triggering breakdown marks vehicle `OUT_OF_SERVICE`, extracts remaining orders, triggers re-optimization, and pushes new routes via WebSockets within 5s. |
| **US-05** | Dispatcher | As a Dispatcher, I want to simulate a delivery day in real-time, so that I can observe fleet movement and live status updates. | Simulation moves vehicles along routes at configurable speed multiplier, broadcasting position & stop completions. |

---

## 4. Functional Requirements

### 4.1 Domain Management
- **FR-DM-01**: System shall support CRUD operations for Depots, Vehicles, Drivers, and Orders.
- **FR-DM-02**: Orders shall include weight (kg), volume ($m^3$), delivery address with PostGIS Point coordinates, and hard delivery time window (`windowStart`, `windowEnd`).
- **FR-DM-03**: Vehicles shall include load capacity (kg), volume capacity ($m^3$), max shift operating hours, and active status (`IDLE`, `EN_ROUTE`, `BREAKDOWN`, `MAINTENANCE`).

### 4.2 Route Optimization Engine
- **FR-OPT-01**: System shall model VRPTW with Timefold Solver.
- **FR-OPT-02**: Hard constraints MUST never be violated in a feasible plan (Capacity, Time Window, Driver Shift, Depot Start/End).
- **FR-OPT-03**: Soft constraints shall minimize total route distance, total travel duration, late delivery SLA buffer penalties, and route disruption penalty during re-optimization.
- **FR-OPT-04**: Baseline generator shall construct routes using a Nearest-Neighbor Greedy strategy for performance comparison.

### 4.3 Incident Recovery & Re-Optimization
- **FR-INC-01**: System shall support 5 incident types: `VEHICLE_BREAKDOWN`, `DRIVER_UNAVAILABLE`, `URGENT_ORDER_INSERTION`, `ORDER_CANCELLATION`, `TIME_WINDOW_CHANGE`.
- **FR-INC-02**: Completed (`DELIVERED`) and in-progress (`IN_TRANSIT`) stops MUST remain locked during re-optimization.
- **FR-INC-03**: System shall re-allocate pending orders from impaired vehicles to available fleet capacity within 5 seconds.

### 4.4 Real-Time Dashboard & Simulation
- **FR-UI-01**: Dashboard shall render interactive Leaflet/Mapbox central map displaying depots, vehicles, order markers, and route polylines.
- **FR-UI-02**: WebSockets (STOMP) shall broadcast live updates (`ROUTE_UPDATED`, `VEHICLE_MOVED`, `INCIDENT_TRIGGERED`) to connected UI clients.
- **FR-UI-03**: Delivery simulation engine shall step through time ticks to move vehicle positions and complete orders.

---

## 5. Non-Functional Requirements (NFRs)
- **NFR-PERF-01**: Optimization solver execution time for 100 orders across 10 vehicles MUST complete within 10 seconds.
- **NFR-PERF-02**: WebSocket status latency from backend incident detection to UI map update MUST be under 500ms.
- **NFR-SEC-01**: All REST endpoints (except Auth) MUST be protected by JWT Bearer tokens and RBAC (`ADMIN`, `DISPATCHER`, `DRIVER`).
- **NFR-RELIABILITY-01**: Database schema MUST enforce spatial integrity via PostGIS geometry types and foreign keys.
- **NFR-OBS-01**: System MUST expose Micrometer metrics for optimization run durations, hard/soft score gauges, and incident counts via `/actuator/prometheus`.

---

## 6. MVP Scope vs Future Scope

### In-Scope for MVP
1. Auth & RBAC (JWT).
2. Fleet & Order CRUD + PostGIS spatial storage.
3. Timefold VRPTW solver implementation.
4. Nearest-Neighbor Baseline generator & visual comparison matrix.
5. Interactive React Map Control Center.
6. Delivery simulation engine.
7. Vehicle Breakdown & Urgent Order Incident Recovery.
8. WebSocket real-time route update pushes.
9. Docker Compose deployment & automated test suite.

### Out-of-Scope (Future Enhancements)
- Multi-depot cross-docking inventory optimization.
- Electric vehicle charging stop constraints (EVRP).
- Native mobile apps for drivers (Android/iOS).
- Real-time traffic congestion API integration (live OSRM traffic tiles).