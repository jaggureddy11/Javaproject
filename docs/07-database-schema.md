# Database Schema & Spatial Architecture

## 1. Relational Entity Relationship Diagram (ERD)

```mermaid
erDiagram
    USERS ||--o{ ROLES : "assigned"
    DEPOTS ||--o{ VEHICLES : "houses"
    DEPOTS ||--o{ ORDERS : "originates"
    DRIVERS ||--o| VEHICLES : "assigned to"
    VEHICLES ||--o{ ROUTES : "operates"
    ROUTES ||--o{ ROUTE_STOPS : "contains"
    ORDERS ||--o| ROUTE_STOPS : "fulfilled by"
    OPTIMIZATION_RUNS ||--o{ ROUTES : "generates"
    OPTIMIZATION_RUNS ||--o{ OPTIMIZATION_METRICS : "records"
    VEHICLES ||--o{ INCIDENTS : "subject of"
    ROUTES ||--o{ ROUTE_VERSIONS : "tracks history"

    USERS {
        uuid id PK
        string email UK
        string password_hash
        string first_name
        string last_name
        string role
        boolean active
        timestamp created_at
    }

    DEPOTS {
        uuid id PK
        string name
        geometry location "Point(EPSG:4326)"
        string address_text
        timestamp created_at
    }

    DRIVERS {
        uuid id PK
        string license_number UK
        string name
        string phone
        string status "ACTIVE, OFF_SHIFT, INJURED"
        integer shift_start_minutes
        integer shift_end_minutes
    }

    VEHICLES {
        uuid id PK
        uuid depot_id FK
        uuid driver_id FK
        string vehicle_code UK
        decimal max_weight_kg
        decimal max_volume_m3
        string status "IDLE, EN_ROUTE, BREAKDOWN, MAINTENANCE"
        timestamp created_at
    }

    ORDERS {
        uuid id PK
        uuid depot_id FK
        string order_number UK
        string customer_name
        geometry location "Point(EPSG:4326)"
        string address_text
        decimal weight_kg
        decimal volume_m3
        integer window_start_minutes
        integer window_end_minutes
        integer service_duration_minutes
        string status "UNASSIGNED, ASSIGNED, IN_TRANSIT, DELIVERED, CANCELLED"
        timestamp created_at
    }

    OPTIMIZATION_RUNS {
        uuid id PK
        string run_type "INITIAL, REOPTIMIZATION, BASELINE"
        string solver_status "SOLVING, FEASIBLE, OPTIMAL, TIMED_OUT"
        integer hard_score
        integer soft_score
        integer execution_duration_ms
        timestamp created_at
    }

    ROUTES {
        uuid id PK
        uuid optimization_run_id FK
        uuid vehicle_id FK
        integer version_number
        decimal total_distance_km
        integer total_duration_minutes
        string status "PLANNED, ACTIVE, COMPLETED, REOPTIMIZED"
        timestamp created_at
    }

    ROUTE_STOPS {
        uuid id PK
        uuid route_id FK
        uuid order_id FK
        integer sequence_number
        integer estimated_arrival_minutes
        integer estimated_departure_minutes
        string stop_status "PENDING, ARRIVED, COMPLETED, SKIPPED"
        boolean locked
    }

    INCIDENTS {
        uuid id PK
        string incident_type "VEHICLE_BREAKDOWN, DRIVER_UNAVAILABLE, URGENT_ORDER"
        uuid vehicle_id FK
        uuid order_id FK
        string description
        timestamp occurred_at
    }
```

---

## 2. PostGIS Spatial Architecture & Indexing
- **Coordinate Reference System (CRS)**: WGS 84 (`EPSG:4326`) for latitude/longitude storage.
- **Spatial Column Type**: `geometry(Point, 4326)` on `depots` and `orders`.
- **Spatial Indexing**:
  ```sql
  CREATE INDEX idx_depots_location ON depots USING GIST (location);
  CREATE INDEX idx_orders_location ON orders USING GIST (location);
  ```
- **Spatial Distance Queries**:
  Uses `ST_DistanceSphere` for fast geodesic distance calculations in meters:
  ```sql
  SELECT id, customer_name, 
         ST_DistanceSphere(location, ST_MakePoint(-87.6298, 41.8781)) AS distance_meters
  FROM orders
  WHERE ST_DWithin(location, ST_MakePoint(-87.6298, 41.8781)::geography, 50000);
  ```

---

## 3. Core Table DDL Specifications

```sql
CREATE TABLE depots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    location GEOMETRY(Point, 4326) NOT NULL,
    address_text TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    depot_id UUID NOT NULL REFERENCES depots(id),
    order_number VARCHAR(50) UNIQUE NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    location GEOMETRY(Point, 4326) NOT NULL,
    address_text TEXT NOT NULL,
    weight_kg NUMERIC(10, 2) NOT NULL CHECK (weight_kg > 0),
    volume_m3 NUMERIC(10, 2) DEFAULT 0.1,
    window_start_minutes INT NOT NULL, -- Minutes from midnight (e.g. 540 = 09:00)
    window_end_minutes INT NOT NULL,   -- Minutes from midnight (e.g. 660 = 11:00)
    service_duration_minutes INT DEFAULT 10,
    status VARCHAR(30) NOT NULL DEFAULT 'UNASSIGNED',
    version INT NOT NULL DEFAULT 0, -- Optimistic locking
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE route_stops (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    route_id UUID NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
    order_id UUID REFERENCES orders(id),
    sequence_number INT NOT NULL,
    estimated_arrival_minutes INT NOT NULL,
    estimated_departure_minutes INT NOT NULL,
    stop_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_route_sequence UNIQUE (route_id, sequence_number)
);
```