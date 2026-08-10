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

### Step 1: Authentication & Dashboard Initialization
- User enters credentials (`dispatcher@routeresq.io` / `password`).
- Backend returns JWT token + user details.
- React app initializes Mapbox/Leaflet container centered on Depot coordinates (e.g., Chicago Logistics Center: 41.8781° N, 87.6298° W).
- STOMP WebSocket connection established to `/ws-net`.

### Step 2: Order & Fleet Preparation
- Dispatcher clicks "Load Demo Dataset" or creates custom orders.
- Orders display delivery addresses, weight limits, and time windows (e.g., 09:00 - 11:30).
- Vehicles display max capacity (e.g., 500 kg) and driver assignments.

### Step 3: Optimization Execution & Baseline Audit
- Dispatcher clicks "Generate Optimized Plan".
- UI displays active solving spinner with timer.
- System executes dual run:
  1. **Nearest-Neighbor Baseline**: Sequential greedy assignment.
  2. **Timefold Solver**: Constraint-based VRPTW solution.
- Results matrix presents side-by-side comparison: Total Distance (km), Total Duration (mins), Capacity Utilization (%), Hard Violations (0 vs N).

### Step 4: Real-Time Simulation & Incident Recovery
- Dispatcher clicks "Start Simulation" (1 sec real time = 1 min simulated time).
- Vehicle markers move along route polylines.
- Dispatcher selects Vehicle 2 and clicks "Simulate Vehicle Breakdown".
- System automatically locks completed stops on Vehicle 2, sets Vehicle 2 status to `OUT_OF_SERVICE`, extracts pending stops, and re-allocates them to Vehicle 1 and Vehicle 3.
- Map visually updates route polylines in real time without refreshing.