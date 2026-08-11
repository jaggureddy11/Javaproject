// ─── RouteResQ Mission-Control Dashboard & App Shell ───────
import React, { useState, useCallback, useEffect } from 'react';
import { ToastProvider } from './context/ToastContext';
import Header from './components/Header';
import Sidebar from './components/Sidebar';
import MapView from './components/MapView';
import SolvePanel from './components/SolvePanel';
import MapLegend from './components/MapLegend';
import LoginPage from './components/LoginPage';
import { OverviewView } from './views/OverviewView';
import { OrdersView } from './views/OrdersView';
import { FleetView } from './views/FleetView';
import { RoutesView } from './views/RoutesView';
import { OptimizationView } from './views/OptimizationView';
import { SimulationView } from './views/SimulationView';
import { IncidentsView } from './views/IncidentsView';
import { BenchmarkView } from './views/BenchmarkView';
import { Order, Depot, Vehicle } from './types/domain';
import { OptimizationRunResponse } from './types/optimization';
import { depotApi } from './api/depotApi';
import { vehicleApi } from './api/vehicleApi';
import { orderApi } from './api/orderApi';
import { tokenStorage } from './auth/tokenStorage';
import { User } from './types/auth';
import { useRealtime } from './context/RealtimeContext';

// Load depots once
let cachedDepots: Depot[] = [];

// ─── Auth guard hook ───────────────────────────────────────
function useAuth() {
  const [authenticated, setAuthenticated] = useState<boolean>(
    () => Boolean(tokenStorage.getToken())
  );

  useEffect(() => {
    const onLogout = () => setAuthenticated(false);
    window.addEventListener('routeresq:logout', onLogout);
    return () => window.removeEventListener('routeresq:logout', onLogout);
  }, []);

  return {
    authenticated,
    login: () => setAuthenticated(true),
    logout: () => { tokenStorage.clear(); setAuthenticated(false); },
  };
}

const AppInner: React.FC = () => {
  const [activeView, setActiveView] = useState<string>('overview');
  const [orders, setOrders] = useState<Order[]>([]);
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [depots, setDepots] = useState<Depot[]>(cachedDepots);
  const [optimizationResult, setOptimizationResult] = useState<OptimizationRunResponse | null>(null);
  const [selectedRouteId, setSelectedRouteId] = useState<string | null>(null);
  const [selectedOrderId, setSelectedOrderId] = useState<string | null>(null);
  const { authenticated, login, logout } = useAuth();

  const user = tokenStorage.getUser<User>();

  const loadAllData = useCallback(() => {
    if (!authenticated) return;
    orderApi.getAll({ size: 200 })
      .then(p => setOrders(p.content))
      .catch(() => {});

    vehicleApi.getAll({ size: 100 })
      .then(p => setVehicles(p.content))
      .catch(() => {});
  }, [authenticated]);

  // Initial load & real-time global operations subscription
  const { subscribe, registerResyncHandler } = useRealtime();

  useEffect(() => {
    if (!authenticated) return;
    if (cachedDepots.length === 0) {
      depotApi.getAll({ size: 20 }).then(p => {
        cachedDepots = p.content;
        setDepots(p.content);
      }).catch(() => {});
    }
    loadAllData();

    // Register resync on reconnect
    const unsubResync = registerResyncHandler(loadAllData);

    // Subscribe to global operations event stream /topic/operations
    const unsubTopic = subscribe('/topic/operations', (event: any) => {
      if (
        event.eventType === 'ORDER_DELIVERED' ||
        event.eventType === 'ORDER_REASSIGNED' ||
        event.eventType === 'VEHICLE_STATUS_CHANGED' ||
        event.eventType === 'ROUTE_REPLANNED' ||
        event.eventType === 'INCIDENT_CREATED' ||
        event.eventType === 'RECOVERY_COMPLETED'
      ) {
        loadAllData();
      }
    });

    return () => {
      unsubResync();
      unsubTopic();
    };
  }, [authenticated, loadAllData, subscribe, registerResyncHandler]);

  const handleOrdersLoaded = useCallback((loaded: Order[]) => {
    setOrders(loaded);
  }, []);

  const handleOptimizationResult = useCallback((result: OptimizationRunResponse) => {
    setOptimizationResult(result);
    setSelectedRouteId(null);
    setSelectedOrderId(null);
  }, []);

  const handleSelectOrderOnMap = (orderId: string) => {
    setSelectedOrderId(orderId);
    setSelectedRouteId(null);
    setActiveView('map');
  };

  if (!authenticated) {
    return <LoginPage onLogin={login} />;
  }

  const routes = optimizationResult?.routes ?? [];
  const metrics = optimizationResult?.metrics;

  return (
    <div className="app-shell">
      <Header
        solverReady={true}
        totalOrders={orders.length}
        activeRoutes={routes.length}
        activeView={activeView}
        onNavigate={setActiveView}
        onLogout={logout}
      />

      <div className="app-body" style={{ position: 'relative' }}>
        {/* Render View Based on Active Tab */}

        {/* 1. Overview */}
        {activeView === 'overview' && (
          <div style={{ width: '100%', height: '100%', overflow: 'hidden' }}>
            <OverviewView
              orders={orders}
              vehicles={vehicles}
              depots={depots}
              optimizationResult={optimizationResult}
              onNavigate={setActiveView}
              onRunOptimization={() => setActiveView('optimization')}
              userRole={user?.role}
            />
          </div>
        )}

        {/* 2. Map-First Operations Mode */}
        {activeView === 'map' && (
          <>
            <Sidebar
              optimizationResult={optimizationResult}
              selectedRouteId={selectedRouteId}
              onSelectRoute={setSelectedRouteId}
              selectedOrderId={selectedOrderId}
              onSelectOrder={setSelectedOrderId}
              onOrdersLoaded={handleOrdersLoaded}
            />

            <main className="map-canvas">
              <MapView
                depots={depots}
                orders={orders}
                optimizationResult={optimizationResult}
                selectedRouteId={selectedRouteId}
                selectedOrderId={selectedOrderId}
                onSelectOrder={setSelectedOrderId}
              />

              {/* Solve panel overlay — top right (Hidden for DRIVER) */}
              {user?.role !== 'DRIVER' && (
                <div className="map-overlay map-top-right">
                  <SolvePanel onResult={handleOptimizationResult} />
                </div>
              )}

              {/* Legend overlay — bottom left */}
              {routes.length > 0 && (
                <div className="map-overlay map-bottom-left">
                  <MapLegend
                    routes={routes}
                    selectedRouteId={selectedRouteId}
                    onSelectRoute={setSelectedRouteId}
                  />
                </div>
              )}

              {/* Score badge — top left of map */}
              {optimizationResult?.score && (
                <div className="map-overlay" style={{ top: 14, left: 14 }}>
                  <div className="map-overlay-content score-badge" style={{
                    background: optimizationResult.score.hard === 0
                      ? 'rgba(16,185,129,0.10)' : 'rgba(239,68,68,0.10)',
                  }}>
                    <span style={{ fontSize: 11, color: 'var(--text-muted)' }}>Score</span>
                    <span style={{
                      color: optimizationResult.score.hard === 0 ? 'var(--accent-green)' : 'var(--accent-red)',
                      fontFamily: 'JetBrains Mono, monospace',
                      fontSize: 12,
                    }}>
                      H{optimizationResult.score.hard} / S{optimizationResult.score.soft}
                    </span>
                    {metrics && (
                      <>
                        <span style={{ color: 'var(--text-dim)', fontSize: 10 }}>|</span>
                        <span style={{ fontFamily: 'JetBrains Mono, monospace', fontSize: 11, color: 'var(--text-muted)' }}>
                          {metrics.totalDistanceKm.toFixed(1)} km
                        </span>
                      </>
                    )}
                  </div>
                </div>
              )}

              {/* Empty state */}
              {routes.length === 0 && orders.length === 0 && (
                <div style={{
                  position: 'absolute', inset: 0,
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  pointerEvents: 'none',
                }}>
                  <div style={{
                    textAlign: 'center', padding: '28px 40px',
                    background: 'rgba(255,255,255,0.95)', borderRadius: 16,
                    border: '1px solid var(--border-color)',
                    backdropFilter: 'blur(8px)',
                    boxShadow: '0 8px 32px rgba(0,0,0,0.08)',
                  }}>
                    <div style={{ fontSize: 36, marginBottom: 10 }}>🗺️</div>
                    <h2 style={{ fontSize: 16, fontWeight: 700, color: 'var(--text-main)', marginBottom: 6 }}>
                      Chicago Delivery Network
                    </h2>
                    <p style={{ fontSize: 12, color: 'var(--text-muted)', maxWidth: 320, lineHeight: 1.6 }}>
                      Start the backend (<code style={{ fontFamily: 'JetBrains Mono, monospace', fontSize: 11 }}>docker compose up</code>)
                      and seed data. Then click <strong>Run Optimization</strong> to solve the VRPTW.
                    </p>
                  </div>
                </div>
              )}

              {/* Orders loaded status */}
              {routes.length === 0 && orders.length > 0 && (
                <div style={{
                  position: 'absolute', bottom: 24, left: '50%', transform: 'translateX(-50%)',
                  pointerEvents: 'none',
                }}>
                  <div style={{
                    display: 'flex', alignItems: 'center', gap: 8,
                    padding: '8px 16px',
                    background: 'rgba(255,255,255,0.92)',
                    border: '1px solid var(--border-color)',
                    borderRadius: 20,
                    backdropFilter: 'blur(6px)',
                    boxShadow: '0 2px 12px rgba(0,0,0,0.08)',
                    fontSize: 12, color: 'var(--text-muted)',
                  }}>
                    <div className="live-dot" />
                    <span>{orders.length} orders loaded — run optimizer to see routes</span>
                  </div>
                </div>
              )}
            </main>
          </>
        )}

        {/* 3. Orders Operations View */}
        {activeView === 'orders' && (
          <div style={{ width: '100%', height: '100%', overflow: 'hidden' }}>
            <OrdersView
              orders={orders}
              onReloadOrders={loadAllData}
              onSelectOrderOnMap={handleSelectOrderOnMap}
              userRole={user?.role}
            />
          </div>
        )}

        {/* 4. Fleet Operations View */}
        {activeView === 'fleet' && (
          <div style={{ width: '100%', height: '100%', overflow: 'hidden' }}>
            <FleetView
              vehicles={vehicles}
              depots={depots}
              onReloadFleet={loadAllData}
            />
          </div>
        )}

        {/* 5. Routes Inspector & Timelines View */}
        {activeView === 'routes' && (
          <div style={{ width: '100%', height: '100%', overflow: 'hidden' }}>
            <RoutesView
              optimizationResult={optimizationResult}
              orders={orders}
              selectedRouteId={selectedRouteId}
              onSelectRoute={setSelectedRouteId}
              onSelectOrderOnMap={handleSelectOrderOnMap}
              onRunOptimization={() => setActiveView('optimization')}
            />
          </div>
        )}

        {/* 6. Optimization Solver Center */}
        {activeView === 'optimization' && (
          <div style={{ width: '100%', height: '100%', overflow: 'hidden' }}>
            <OptimizationView
              depots={depots}
              orders={orders}
              vehicles={vehicles}
              optimizationResult={optimizationResult}
              onResult={handleOptimizationResult}
              onNavigate={setActiveView}
              userRole={user?.role}
            />
          </div>
        )}

        {/* 7. Real-Time Delivery Simulator View */}
        {activeView === 'simulation' && (
          <div style={{ width: '100%', height: '100%', overflow: 'hidden' }}>
            <SimulationView onSelectOrderOnMap={handleSelectOrderOnMap} />
          </div>
        )}

        {/* 8. Incidents & Disruptions View */}
        {activeView === 'incidents' && (
          <div style={{ width: '100%', height: '100%', overflow: 'hidden' }}>
            <IncidentsView
              vehicles={vehicles}
              orders={orders}
            />
          </div>
        )}

        {/* 8. Benchmark Comparison View */}
        {activeView === 'benchmarks' && (
          <div style={{ width: '100%', height: '100%', overflow: 'hidden' }}>
            <BenchmarkView userRole={user?.role} />
          </div>
        )}
      </div>
    </div>
  );
};

import { RealtimeProvider } from './context/RealtimeContext';

export default function App() {
  return (
    <ToastProvider>
      <RealtimeProvider>
        <AppInner />
      </RealtimeProvider>
    </ToastProvider>
  );
}
