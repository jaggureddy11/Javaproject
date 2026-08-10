-- RouteResQ Database Schema - V1__init_schema.sql

-- Enable PostGIS Extension
CREATE EXTENSION IF NOT EXISTS postgis;

-- 1. Users & Security Roles
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'DISPATCHER',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Depots
CREATE TABLE depots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    location GEOMETRY(Point, 4326) NOT NULL,
    address_text TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_depots_location ON depots USING GIST (location);

-- 3. Drivers
CREATE TABLE drivers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    license_number VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(30),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    shift_start_minutes INT NOT NULL DEFAULT 480, -- 08:00
    shift_end_minutes INT NOT NULL DEFAULT 1020,   -- 17:00
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. Vehicles
CREATE TABLE vehicles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    depot_id UUID NOT NULL REFERENCES depots(id),
    driver_id UUID REFERENCES drivers(id),
    vehicle_code VARCHAR(50) UNIQUE NOT NULL,
    max_weight_kg NUMERIC(10, 2) NOT NULL CHECK (max_weight_kg > 0),
    max_volume_m3 NUMERIC(10, 2) DEFAULT 10.0,
    status VARCHAR(30) NOT NULL DEFAULT 'IDLE',
    current_location GEOMETRY(Point, 4326),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. Delivery Orders
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    depot_id UUID NOT NULL REFERENCES depots(id),
    order_number VARCHAR(50) UNIQUE NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    location GEOMETRY(Point, 4326) NOT NULL,
    address_text TEXT NOT NULL,
    weight_kg NUMERIC(10, 2) NOT NULL CHECK (weight_kg > 0),
    volume_m3 NUMERIC(10, 2) DEFAULT 0.1,
    window_start_minutes INT NOT NULL, -- Minutes from midnight
    window_end_minutes INT NOT NULL,   -- Minutes from midnight
    service_duration_minutes INT DEFAULT 10,
    status VARCHAR(30) NOT NULL DEFAULT 'UNASSIGNED',
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_orders_location ON orders USING GIST (location);
CREATE INDEX idx_orders_status ON orders (status);

-- 6. Optimization Runs
CREATE TABLE optimization_runs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    run_type VARCHAR(30) NOT NULL DEFAULT 'INITIAL', -- INITIAL, REOPTIMIZATION, BASELINE
    solver_status VARCHAR(30) NOT NULL DEFAULT 'SOLVING',
    hard_score INT DEFAULT 0,
    soft_score INT DEFAULT 0,
    execution_duration_ms INT DEFAULT 0,
    total_distance_km NUMERIC(10, 2) DEFAULT 0,
    total_duration_minutes INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 7. Routes
CREATE TABLE routes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    optimization_run_id UUID REFERENCES optimization_runs(id) ON DELETE CASCADE,
    vehicle_id UUID NOT NULL REFERENCES vehicles(id),
    version_number INT NOT NULL DEFAULT 1,
    total_distance_km NUMERIC(10, 2) DEFAULT 0,
    total_duration_minutes INT DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'PLANNED',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 8. Route Stops
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

-- 9. Operational Incidents
CREATE TABLE incidents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    incident_type VARCHAR(50) NOT NULL, -- VEHICLE_BREAKDOWN, DRIVER_UNAVAILABLE, URGENT_ORDER
    vehicle_id UUID REFERENCES vehicles(id),
    order_id UUID REFERENCES orders(id),
    description TEXT,
    occurred_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 10. Audit Logs
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
