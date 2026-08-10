-- RouteResQ Migration V2__domain_enhancements.sql
-- Schema enhancements for optimistic locking, additional indexes, and audit columns

-- 1. Add version column for Optimistic Locking (@Version)
ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS version INT NOT NULL DEFAULT 0;
ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE drivers ADD COLUMN IF NOT EXISTS version INT NOT NULL DEFAULT 0;
ALTER TABLE drivers ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE routes ADD COLUMN IF NOT EXISTS version INT NOT NULL DEFAULT 0;
ALTER TABLE routes ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE route_stops ADD COLUMN IF NOT EXISTS version INT NOT NULL DEFAULT 0;
ALTER TABLE route_stops ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE depots ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE optimization_runs ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE incidents ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'OPEN';
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

-- 2. Add spatial index on vehicles current_location
CREATE INDEX IF NOT EXISTS idx_vehicles_location ON vehicles USING GIST (current_location);

-- 3. Add performance indexes for status fields and foreign keys
CREATE INDEX IF NOT EXISTS idx_vehicles_status ON vehicles (status);
CREATE INDEX IF NOT EXISTS idx_vehicles_depot ON vehicles (depot_id);
CREATE INDEX IF NOT EXISTS idx_drivers_status ON drivers (status);
CREATE INDEX IF NOT EXISTS idx_orders_depot ON orders (depot_id);
CREATE INDEX IF NOT EXISTS idx_routes_status ON routes (status);
CREATE INDEX IF NOT EXISTS idx_routes_vehicle ON routes (vehicle_id);
CREATE INDEX IF NOT EXISTS idx_route_stops_route ON route_stops (route_id);
CREATE INDEX IF NOT EXISTS idx_incidents_type ON incidents (incident_type);
