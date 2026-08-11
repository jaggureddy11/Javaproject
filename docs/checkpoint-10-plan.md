# Checkpoint 10 — Unified Real-Time Event Platform Plan

## 1. Problem & Architecture Overview
RouteResQ currently emits various ad-hoc STOMP messages over multiple topic formats (`/topic/simulation/{id}`, `/topic/incidents/{id}`, `/topic/optimization/{id}`).
Checkpoint 10 unifies all real-time events across Optimization, Simulation, Incidents, Fleet, Orders, and Routes under a single, strongly-typed event envelope (`RealtimeEvent`), clean topic hierarchy, single STOMP connection, exponential backoff reconnection, event deduplication, sequence ordering checks, and automated REST resynchronization upon client reconnect.

---

## 2. Unified Event Envelope (`RealtimeEvent`)

```json
{
  "eventId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "eventType": "VEHICLE_POSITION_UPDATED",
  "entityType": "VEHICLE",
  "entityId": "V-101",
  "occurredAt": "2026-08-12T01:25:00Z",
  "sequence": 1042,
  "simulationId": "7b92a101-3529-4070-b81e-63c80eb59ad3",
  "incidentId": null,
  "optimizationRunId": "9c12b304-1234-5678-9abc-def012345678",
  "payload": { ... }
}
```

### Strongly Typed Event Categories (`RealtimeEventType`)
- **Optimization**: `OPTIMIZATION_STARTED`, `OPTIMIZATION_PROGRESS`, `OPTIMIZATION_COMPLETED`, `OPTIMIZATION_FAILED`
- **Simulation**: `SIMULATION_STARTED`, `SIMULATION_PAUSED`, `SIMULATION_RESUMED`, `SIMULATION_STOPPED`, `SIMULATION_COMPLETED`
- **Vehicle**: `VEHICLE_POSITION_UPDATED`, `VEHICLE_STATUS_CHANGED`
- **Route**: `ROUTE_UPDATED`, `ROUTE_REPLANNED`
- **Order**: `ORDER_STATUS_CHANGED`, `ORDER_DELIVERED`, `ORDER_REASSIGNED`
- **Incident & Recovery**: `INCIDENT_CREATED`, `INCIDENT_ANALYZED`, `RECOVERY_STARTED`, `RECOVERY_COMPLETED`, `RECOVERY_FAILED`

---

## 3. Topic Architecture
- `/topic/operations`: Global operations stream for high-level business events (`ORDER_DELIVERED`, `VEHICLE_STATUS_CHANGED`, `ROUTE_REPLANNED`, `INCIDENT_CREATED`, `RECOVERY_COMPLETED`). (Excludes high-frequency position ticks).
- `/topic/simulation/{simulationId}`: High-frequency vehicle positions, simulation clock, live activity log.
- `/topic/optimization/{optimizationRunId}`: Live solver progress.
- `/topic/incidents/{incidentId}`: Dynamic recovery status.

---

## 4. Frontend Realtime Event Bus (`RealtimeContext` & `useRealtime`)
- **Single STOMP Connection**: One shared STOMP connection managed at the application root (`App.tsx`).
- **Connection States**: `'CONNECTING' | 'CONNECTED' | 'DISCONNECTED' | 'RECONNECTING' | 'ERROR'`.
- **Status Indicator**: Compact status badge on top navigation bar (`● LIVE`, `● RECONNECTING`, `● OFFLINE`).
- **Exponential Backoff Reconnection**: Reconnect delays at 1s, 2s, 4s, 8s, 16s, max 30s.
- **Event Deduplication & Sequence Guard**: Bounded LRU cache of `eventId`s (last 500 events) to drop duplicate frames; sequence check to prevent processing out-of-order stale events.
- **Automated REST Resynchronization**: On reconnect, automatically triggers a REST snapshot fetch to resync client state before applying live events.

---

## 5. Backend Publisher Helper (`RealtimeEventPublisher`)
- Centralized Spring service injecting `SimpMessagingTemplate`.
- Automatically wraps payloads in `RealtimeEvent` envelope with sequence counter, timestamp, and entity metadata.
- Broadcasts to specific topic channels and mirrors high-level events to `/topic/operations`.

---

## 6. Verification Plan & Acceptance Criteria
- [ ] `RealtimeEventPublisher` wraps and emits typed events.
- [ ] `/topic/operations` streams system-wide business events.
- [ ] Single STOMP connection shared across entire frontend.
- [ ] Exponential backoff reconnection & connection badge indicator.
- [ ] Event deduplication and sequence ordering enforced in `RealtimeContext`.
- [ ] Automatic REST resynchronization on reconnect.
- [ ] All views (Dashboard, Map, Orders, Fleet, Routes, Simulation, Incidents) reactively update without page refresh.
- [ ] `RealtimeEventTest` unit/integration tests pass cleanly.
- [ ] `npx tsc --noEmit` returns 0 errors; `npm run build` succeeds; `mvn test` passes cleanly.
