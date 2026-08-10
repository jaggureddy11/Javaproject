# Real-Time Architecture & WebSocket Specification

## 1. WebSocket & STOMP Protocol Architecture
RouteResQ uses Spring WebSocket with **STOMP (Simple Text Oriented Messaging Protocol)** fallback to SockJS to stream live operational changes to the React frontend.

```mermaid
graph TD
    subgraph Spring Boot Backend
        EvtBus[Spring Event Bus / Domain Events]
        WSSvc[NotificationService]
        Broker[Simple In-Memory STOMP Broker]
    end

    subgraph STOMP Topics
        T_Routes[/topic/routes]
        T_Incidents[/topic/incidents]
        T_Sim[/topic/simulation]
    end

    subgraph React Clients
        UI1[Dispatcher Control Center]
        UI2[Fleet Overview Monitor]
    end

    EvtBus -->|RouteUpdatedEvent| WSSvc
    WSSvc -->|convertAndSend| Broker
    Broker --> T_Routes
    Broker --> T_Incidents
    Broker --> T_Sim
    
    T_Routes -->|Push JSON| UI1
    T_Incidents -->|Push JSON| UI1
    T_Sim -->|Push JSON| UI2
```

---

## 2. Topic Taxonomies & Payloads

### 2.1 Topic: `/topic/routes`
Broadcasts whenever a new route plan is solved or re-optimized.

```json
{
  "eventType": "ROUTE_REOPTIMIZED",
  "timestamp": "2026-08-11T10:15:30Z",
  "optimizationRunId": "run-reopt-8888-0000",
  "affectedVehicles": ["TRUCK-01", "TRUCK-02"],
  "disruptionCost": 500,
  "routes": [
    {
      "vehicleCode": "TRUCK-02",
      "stops": [
        { "sequence": 1, "orderNumber": "ORD-104", "status": "IN_TRANSIT" },
        { "sequence": 2, "orderNumber": "ORD-108", "status": "PENDING" }
      ]
    }
  ]
}
```

### 2.2 Topic: `/topic/simulation`
Broadcasts live vehicle coordinate interpolation during simulation ticks (1 Hz).

```json
{
  "eventType": "VEHICLE_POSITION_UPDATE",
  "timestamp": "2026-08-11T10:15:31Z",
  "vehicleId": "v1000000-0000-0000-0000-000000000001",
  "latitude": 41.8825,
  "longitude": -87.6321,
  "currentSpeedKmh": 42.5,
  "currentStopSequence": 2,
  "etaNextStopMinutes": 4
}
```

---

## 3. Connection Lifecycle & Reconnection
- **Endpoint**: `/ws-net`
- **Heartbeat**: 10,000 ms client-to-server / server-to-client heartbeat.
- **Auto-Reconnect**: React STOMP client attempts automatic exponential backoff reconnect (1s, 2s, 4s, 8s, max 30s) if connection drops.