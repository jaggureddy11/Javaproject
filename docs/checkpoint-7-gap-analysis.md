# Checkpoint 7 — Gap Analysis & Audit

## IMPLEMENTED

| Feature | Current Implementation | Files | Status |
|---|---|---|---|
| JWT Auth & Login | `LoginPage.tsx` with email/password form, dev credentials hint, JWT storage in `tokenStorage.ts`, 401 interceptor | `src/components/LoginPage.tsx`, `src/api/auth.ts`, `src/auth/tokenStorage.ts` | COMPLETED |
| Auth Guard & Session | `useAuth` hook in `App.tsx` guarding application shell, logout button in header | `src/App.tsx`, `src/components/Header.tsx` | COMPLETED |
| Async Optimization Backend | `OptimizationService` `@Async` execution on `solverExecutor` pool; `POST /optimization/runs` returns `202 Accepted` + `runId` | `OptimizationService.java`, `OptimizationController.java`, `AsyncConfig.java` | COMPLETED |
| WebSocket & STOMP Push | Backend `WebSocketConfig` at `/ws`; frontend `useWebSocket` hook with SockJS/STOMP singleton client and topic subscriptions | `WebSocketConfig.java`, `src/hooks/useWebSocket.ts`, `src/components/SolvePanel.tsx` | COMPLETED |
| Polling Fallback | `optimizationApi.pollUntilDone()` polls `GET /runs/{id}` every 2s as fallback when WS is unavailable | `src/api/optimizationApi.ts`, `src/components/SolvePanel.tsx` | COMPLETED |
| Interactive Leaflet Map Foundation | Light-theme Leaflet container, custom depot SVG icons, order priority markers, route polylines, map legend overlay | `src/components/MapView.tsx`, `src/components/MapLegend.tsx` | COMPLETED |
| Order Creation | Modal form (`OrderForm.tsx`) supporting depot, customer, address, lat/lng, weight, window, priority with client validation | `src/components/OrderForm.tsx` | COMPLETED |
| 15-Second Fleet & Order Refresh | Background silent refresh in `Sidebar.tsx` updating orders and fleet list every 15s | `src/components/Sidebar.tsx` | COMPLETED |
| Light Theme Styling | Clean design system tokens, typography, custom scrollbars, clean Leaflet tile filters | `src/index.css`, `src/utils/display.ts` | COMPLETED |

---

## MISSING

| Feature | Why Needed | Backend Dependency | Frontend Work | Status |
|---|---|---|---|---|
| Complete Application Shell & Navigation | Enable seamless switching between core operations views (Overview, Orders, Fleet, Routes, Optimization, Incidents, Benchmarks) with active indicators and RBAC visibility | REST APIs (Orders, Fleet, Optimization, Benchmarks, Auth) | Add top/side navigation bar, active view state management, view container routing | MISSING |
| Operations Overview Dashboard | Provide at-a-glance KPI metrics (active orders, pending/unassigned, delivered, active vehicles, on-time rate %, total routes, solver status) derived strictly from real API data | `/api/v1/orders`, `/api/v1/vehicles`, `/api/v1/optimization` | Create `OverviewView.tsx` with KPI metric cards, status charts, quick action bar | MISSING |
| Interactive Map Selection & Geometry Label | Automatically zoom/pan map to focus on selected route, order, or vehicle; display `"Route geometry: estimated"` overlay label | Existing Leaflet MapView | Update `MapView.tsx` with dynamic `useMap` bounds focus hook and estimation badge | MISSING |
| Route Detail & Sequential Stop Timeline | Detailed inspector showing route stats, vehicle, driver, start time, expected return, and sequential timeline (DEPOT -> Stop 1 -> Stop 2 -> DEPOT) | `/api/v1/optimization/runs/{id}` | Create `RouteDetailView.tsx` or route inspector modal/panel with arrival/departure/window details | MISSING |
| Dedicated Orders Operations Page | Full table/card view supporting multi-attribute search, status filter, priority filter, sorting, pagination, order detail drawer, order cancellation | `/api/v1/orders` (filtering, pagination, patch/delete) | Create `OrdersView.tsx` with search input, filters, sort controls, order detail drawer | MISSING |
| Dedicated Fleet Operations Page | Comprehensive fleet view with sub-tabs for Vehicles, Drivers, and Depots displaying status, shift times, capacities, and assigned routes | `/api/v1/vehicles`, `/api/v1/drivers`, `/api/v1/depots` | Create `FleetView.tsx` with sub-tabs, status badges, vehicle/driver assignment details | MISSING |
| Optimization Operations & Failure UX | Dedicated view for VRPTW solver with input selection, STOMP progress, post-solve metrics summary (Hard/Soft score, distance, duration, assigned count), and honest infeasibility/error messages | `/api/v1/optimization/runs` | Create `OptimizationView.tsx` with detailed solve control, metrics breakdown, infeasibility cards | MISSING |
| Benchmark Comparison & Charts View | Complete benchmark dashboard running all 6 datasets (`SMALL`, `MEDIUM`, `LARGE`, `TIGHT_TIME_WINDOWS`, `CAPACITY_PRESSURE`, `SPATIAL_CLUSTERING`), displaying baseline vs Timefold metrics table, honest improvement representation, and SVG distance/duration comparison charts | `/api/v1/optimization/benchmarks` | Create `BenchmarkView.tsx` with benchmark execution, detailed comparison table, SVG charts | MISSING |
| Incidents Operations UI Foundation | Operations panel displaying reported incidents (type, vehicle, driver, order, status, timestamp, description) with type/status filters and incident reporting modal | Incidents data model & REST endpoint | Create `IncidentsView.tsx`, `incidentApi.ts`, incident report modal | MISSING |
| System Health / Actuator Indicator | Expose backend health status (API, Database, Redis, Solver) from Spring Boot Actuator | `/actuator/health` | Create `ActuatorStatus.tsx` status indicator in header/footer | MISSING |
| Role-Based UI (RBAC) Controls | Hide administrative/solver controls for DRIVER role while granting full access to DISPATCHER and ADMIN | `tokenStorage.getUser()` role | Wrap sensitive triggers (Run Optimization, Benchmarks, Order Creation) in RBAC role checks | MISSING |

---

## BROKEN / INCOMPLETE

| Feature | Current Issue | Root Cause | Fix |
|---|---|---|---|
| Map Selection Focus | Selecting a route or order highlights line/marker but does not pan/zoom map view to target location | `BoundsFitter` only ran once on mount | Add `SelectionFitter` component in `MapView.tsx` listening to `selectedRouteId`, `selectedOrderId`, `selectedVehicleId` |
| Infeasible / Failure State Messaging | When solver returns hard score < 0 or fails, toast shows error but solve panel doesn't retain detailed failure breakdown | `SolvePanel.tsx` reset state immediately on error | Display an explicit error/infeasible card in `SolvePanel` & `OptimizationView` with run ID, timestamp, and failure reason |
| Route Stop Details | Stop list in sidebar showed sequence and customer name, but omitted window start/end, weight, priority, and return-to-depot leg | Compact sidebar layout constraints | Expand stop row details in sidebar & build dedicated `RouteDetailView.tsx` timeline |
