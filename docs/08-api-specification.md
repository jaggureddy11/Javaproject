# REST API Specification

## 1. Base URL & Authentication
- **Base URL**: `/api/v1`
- **Authentication**: `Authorization: Bearer <JWT_TOKEN>`
- **Response Format**: `application/json`

---

## 2. API Endpoints Overview

### 2.1 Authentication (`/api/v1/auth`)
- `POST /api/v1/auth/login`: Authenticates dispatcher/admin and returns JWT token.
- `POST /api/v1/auth/refresh`: Refreshes expired JWT token.

### 2.2 Order Management (`/api/v1/orders`)
- `GET /api/v1/orders`: Lists delivery orders with status filter.
- `POST /api/v1/orders`: Creates a new delivery order.
- `GET /api/v1/orders/{id}`: Retrieves order details.
- `PATCH /api/v1/orders/{id}`: Updates order status or delivery window.

### 2.3 Fleet Management (`/api/v1/vehicles`, `/api/v1/drivers`)
- `GET /api/v1/vehicles`: Lists fleet vehicles, status, and capacity.
- `POST /api/v1/vehicles`: Registers a new vehicle.
- `PATCH /api/v1/vehicles/{id}`: Updates vehicle status (e.g. `BREAKDOWN`).

### 2.4 Optimization Engine (`/api/v1/optimization`)
- `POST /api/v1/optimization/runs`: Initiates a new route optimization job.
- `GET /api/v1/optimization/runs/{id}`: Polling endpoint for optimization status & metrics.
- `POST /api/v1/optimization/baseline`: Generates Nearest-Neighbor baseline comparison run.

### 2.5 Incident & Recovery (`/api/v1/incidents`)
- `POST /api/v1/incidents`: Triggers operational incident (`VEHICLE_BREAKDOWN`, `URGENT_ORDER`) and executes automatic sub-plan re-optimization.

---

## 3. Sample API Payloads

### 3.1 `POST /api/v1/optimization/runs` Request & Response

**Request**:
```json
{
  "depotId": "a1b2c3d4-0000-0000-0000-111111111111",
  "orderIds": [
    "o1010000-0000-0000-0000-000000000101",
    "o1020000-0000-0000-0000-000000000102"
  ],
  "vehicleIds": [
    "v1000000-0000-0000-0000-000000000001"
  ],
  "maxSolverDurationSeconds": 10
}
```

**Response (200 OK)**:
```json
{
  "runId": "run99999-0000-0000-0000-999999999999",
  "solverStatus": "OPTIMAL",
  "hardScore": 0,
  "softScore": -41250,
  "executionDurationMs": 1450,
  "totalDistanceKm": 41.25,
  "totalDurationMinutes": 142,
  "routes": [
    {
      "routeId": "r0000001-0000-0000-0000-000000000001",
      "vehicleId": "v1000000-0000-0000-0000-000000000001",
      "vehicleCode": "TRUCK-01",
      "stops": [
        {
          "sequenceNumber": 1,
          "orderNumber": "ORD-101",
          "customerName": "Acme Corp",
          "estimatedArrival": "09:25",
          "weightKg": 45.0,
          "status": "PENDING"
        }
      ]
    }
  ]
}
```

---

### 3.2 `POST /api/v1/incidents` Breakdown Request & Response

**Request**:
```json
{
  "incidentType": "VEHICLE_BREAKDOWN",
  "vehicleId": "v1000000-0000-0000-0000-000000000001",
  "description": "Engine failure on I-90 West"
}
```

**Response (200 OK)**:
```json
{
  "incidentId": "inc-5555-0000-0000",
  "affectedVehicleId": "v1000000-0000-0000-0000-000000000001",
  "affectedOrdersCount": 4,
  "reoptimizedRunId": "run-reopt-8888-0000",
  "message": "Vehicle marked BREAKDOWN. 4 orders successfully re-allocated to TRUCK-02 and TRUCK-03.",
  "newRoutes": [ /* Updated route structures */ ]
}
```