# 3-5 Minute Recruiter Demo Script & Showcase Scenarios

## 1. Demo Narrative Overview
The goal of this demonstration is to showcase RouteResQ's core value within **3 to 5 minutes** to a technical recruiter or hiring manager. The script highlights real-world NP-hard constraint optimization, baseline performance comparison, real-time vehicle movement simulation, automated breakdown recovery, and WebSocket STOMP dynamic route updates.

---

## 2. Minute-by-Minute Step Breakdown

```
[0:00 - 0:45] LOGIN & OPERATIONAL OVERVIEW
1. Dispatcher logs into RouteResQ Control Center.
2. System renders central Leaflet map displaying Chicago Depot, 50 active delivery orders, and 5 fleet trucks.
3. Highlight order attributes: Delivery windows (e.g. 09:00 - 11:00), weights (10-50 kg), and geographic clustering.

[0:45 - 1:45] OPTIMIZATION & BASELINE COMPARISON
1. Dispatcher clicks "Generate Optimized Plan".
2. System executes solver in background (1.4s completion time).
3. Metric panel presents side-by-side comparison:
   - Nearest-Neighbor Baseline: 52.4 km total distance, 2 late window violations.
   - Timefold VRPTW Solver: 41.2 km total distance, 0 hard constraint violations (-21.3% distance reduction).
4. Interactive map overlays 5 color-coded vehicle route polylines with stop sequence numbers (1..N).

[1:45 - 2:45] REAL-TIME SIMULATION & LIVE DISPATCHING
1. Dispatcher clicks "Start Simulation" at 2x speed.
2. Truck icons move along route polylines; stop status badges update from PENDING -> ARRIVED -> DELIVERED.
3. Show live telemetry: Vehicle 1 load capacity drops as orders are fulfilled.

[2:45 - 4:00] INCIDENT RECOVERY (VEHICLE BREAKDOWN)
1. Dispatcher selects Vehicle 2 (currently en-route) and clicks "Simulate Breakdown".
2. Backend immediately locks completed stops on Vehicle 2, marks Vehicle 2 status OUT_OF_SERVICE, and extracts pending orders.
3. System executes dynamic re-optimization with Route Stability soft constraint penalty.
4. Within < 2 seconds, WebSockets push updated routes to UI without refreshing page.
5. Map animates: Pending orders from Vehicle 2 are seamlessly absorbed by Vehicle 1 and Vehicle 3.

[4:00 - 4:30] ANALYTICS & RECAP
1. Dispatcher opens Analytics tab showcasing total distance saved, zero SLA late violations, and 98.4% fleet capacity utilization.
2. Conclude demo emphasizing production-grade Spring Boot 3.3, Timefold VRPTW solver, PostGIS spatial queries, and WebSockets.
```