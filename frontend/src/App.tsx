// ─── RouteResQ Mission-Control Dashboard ─────────────────
import React, { useState, useCallback } from 'react';
import { ToastProvider } from './context/ToastContext';
import Header from './components/Header';
import Sidebar from './components/Sidebar';
import MapView from './components/MapView';
import SolvePanel from './components/SolvePanel';
import MapLegend from './components/MapLegend';
import { Order, Depot } from './types/domain';
import { OptimizationRunResponse } from './types/optimization';
import { depotApi } from './api/depotApi';

// Load depots once
let cachedDepots: Depot[] = [];

const AppInner: React.FC = () => {
  const [orders, setOrders] = useState<Order[]>([]);
  const [depots, setDepots] = useState<Depot[]>(cachedDepots);
  const [optimizationResult, setOptimizationResult] = useState<OptimizationRunResponse | null>(null);
  const [selectedRouteId, setSelectedRouteId] = useState<string | null>(null);
  const [selectedOrderId, setSelectedOrderId] = useState<string | null>(null);

  // Load depots on first render
  React.useEffect(() => {
    if (cachedDepots.length === 0) {
      depotApi.getAll({ size: 20 }).then(p => {
        cachedDepots = p.content;
        setDepots(p.content);
      }).catch(() => {});
    }
  }, []);

  const handleOrdersLoaded = useCallback((loaded: Order[]) => {
    setOrders(loaded);
  }, []);

  const handleOptimizationResult = useCallback((result: OptimizationRunResponse) => {
    setOptimizationResult(result);
    setSelectedRouteId(null);
    setSelectedOrderId(null);
  }, []);

  const routes = optimizationResult?.routes ?? [];
  const metrics = optimizationResult?.metrics;

  return (
    <div className="app-shell">
      <Header
        solverReady={true}
        totalOrders={orders.length}
        activeRoutes={routes.length}
      />

      <div className="app-body">
        {/* Left sidebar */}
        <Sidebar
          optimizationResult={optimizationResult}
          selectedRouteId={selectedRouteId}
          onSelectRoute={setSelectedRouteId}
          selectedOrderId={selectedOrderId}
          onSelectOrder={setSelectedOrderId}
          onOrdersLoaded={handleOrdersLoaded}
        />

        {/* Map area */}
        <main className="map-canvas">
          <MapView
            depots={depots}
            orders={orders}
            optimizationResult={optimizationResult}
            selectedRouteId={selectedRouteId}
            selectedOrderId={selectedOrderId}
            onSelectOrder={setSelectedOrderId}
          />

          {/* Solve panel overlay — top right */}
          <div className="map-overlay map-top-right">
            <SolvePanel onResult={handleOptimizationResult} />
          </div>

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
                  ? 'rgba(16,185,129,0.12)' : 'rgba(239,68,68,0.12)',
                border: `1px solid ${optimizationResult.score.hard === 0
                  ? 'rgba(16,185,129,0.3)' : 'rgba(239,68,68,0.3)'}`,
                borderRadius: 8,
                backdropFilter: 'blur(6px)',
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
                background: 'rgba(12,21,36,0.85)', borderRadius: 16,
                border: '1px solid var(--border-color)',
                backdropFilter: 'blur(8px)',
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

          {/* Orders loaded but no routes yet */}
          {routes.length === 0 && orders.length > 0 && (
            <div style={{
              position: 'absolute', bottom: 24, left: '50%', transform: 'translateX(-50%)',
              pointerEvents: 'none',
            }}>
              <div style={{
                display: 'flex', alignItems: 'center', gap: 8,
                padding: '8px 16px',
                background: 'rgba(12,21,36,0.85)',
                border: '1px solid var(--border-color)',
                borderRadius: 20,
                backdropFilter: 'blur(6px)',
                fontSize: 12, color: 'var(--text-muted)',
              }}>
                <div className="live-dot" />
                <span>{orders.length} orders loaded — run optimizer to see routes</span>
              </div>
            </div>
          )}
        </main>
      </div>
    </div>
  );
};

export default function App() {
  return (
    <ToastProvider>
      <AppInner />
    </ToastProvider>
  );
}
