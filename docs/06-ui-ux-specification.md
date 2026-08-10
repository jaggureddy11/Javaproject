# UI/UX Specification

## 1. Design Aesthetics & Core Principles
RouteResQ follows a high-density, mission-control design language tailored for logistics dispatchers and fleet operations managers.
- **Theme**: Modern SaaS Fleet Operations Control Center (Dark/Light hybrid with slate background `#0f172a`, panel containers `#1e293b`, primary accent blue `#3b82f6`).
- **Typography**: Inter / Outfit for high legibility across dense data tables and map overlays.
- **Map Focus**: The interactive map occupies 65% of screen real estate as the primary visual focus.
- **Semantic Colors**:
  - `Green (#10b981)`: On-time delivery / Healthy vehicle / Zero hard violations.
  - `Yellow (#f59e0b)`: Approaching SLA buffer limit / Warning / In-progress simulation.
  - `Red (#ef4444)`: Vehicle breakdown / Hard constraint violation / Missed time window.
  - `Blue (#3b82f6)`: Normal route line / Active vehicle / Selected entity.

---

## 2. Main Dashboard Layout Grid

```
+-----------------------------------------------------------------------------------------+
| HEADER: RouteResQ Logo | Active Simulation Speed [1x|2x|5x] | Solver Status | User Profile |
+------------------------------------+----------------------------------------------------+
| SIDEBAR (Left: 350px)              | MAIN MAP VIEW (Center & Right: Flex-grow)          |
| [Tabs: Orders | Fleet | Incidents] |                                                    |
|                                    | [ Depot Marker (Square) ]                          |
| - Active Vehicles List             | [ Vehicle Markers (Animated Truck Icons) ]         |
|   Vehicle 1 [Cap: 80%] (EN_ROUTE)  | [ Delivery Stop Markers (Numbered 1..N) ]         |
|   Vehicle 2 [BREAKDOWN!] (RED)     | [ Route Polylines (Color-coded per vehicle) ]      |
|                                    |                                                    |
| - Unassigned Orders List (Count)   | Floating Map Controls: [Layers | Recenter | Zoom]  |
|                                    +----------------------------------------------------+
| [ Button: "Generate Plan" ]        | BOTTOM METRICS & TIMELINE PANEL (Height: 220px)    |
| [ Button: "Start Simulation" ]     | Baseline vs Opt: Dist: 41km vs 52km (-21%)        |
| [ Button: "Simulate Breakdown" ]   | Stop Timeline: Depot -> Stop 1 (09:15) -> Stop 2   |
+------------------------------------+----------------------------------------------------+
```

---

## 3. Detailed Component Specifications

### 3.1 Interactive Map (Leaflet / Mapbox GL)
- **Depot Marker**: Dark indigo square icon with warehouse symbol.
- **Order Markers**: Circular badges with stop sequence numbers (e.g. `1`, `2`, `3`). Hover tooltip presents customer name, address, weight (kg), and delivery window (`09:00 - 10:30`).
- **Vehicle Markers**: Truck icons tinted to vehicle color. During simulation, markers smoothly interpolate along route polyline segments.
- **Route Polylines**: Solid lines for assigned active routes; dashed lines for baseline comparisons.

### 3.2 Metrics & Comparison Matrix Component
Renders side-by-side performance cards:
- **Total Route Distance**: Baseline vs Optimized (e.g. `52.4 km` vs `41.2 km`, **-21.37%**).
- **Estimated Travel Time**: Baseline vs Optimized (e.g. `184 mins` vs `142 mins`, **-22.82%**).
- **Vehicle Fleet Utilization**: Baseline vs Optimized (e.g. `4 Vehicles` vs `3 Vehicles`, **-25%**).
- **Constraint Violations**: Baseline (`2 Time Window Violations`) vs Optimized (`0 Hard Violations`).

### 3.3 Incident Simulation Control Panel
Contains quick-trigger incident actions:
1. `Simulate Vehicle Breakdown`: Select target vehicle -> system marks broken down -> triggers re-optimization modal.
2. `Inject Urgent Order`: Select order template -> places high-priority order on map -> triggers sub-plan solve.
3. `Cancel Delivery Order`: Select active order -> marks cancelled -> updates route sequence.