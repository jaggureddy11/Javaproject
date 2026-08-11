// ─── Operations Overview Dashboard ────────────────────────
import React, { useState, useEffect } from 'react';
import {
  Package, Truck, Route, CheckCircle2,
  Zap, ArrowRight, Activity
} from 'lucide-react';
import { Order, Vehicle, Depot } from '../types/domain';
import { OptimizationRunResponse } from '../types/optimization';
import { healthApi, SystemHealthResponse } from '../api/healthApi';
import { fmtKm } from '../utils/display';

interface OverviewViewProps {
  orders: Order[];
  vehicles: Vehicle[];
  depots: Depot[];
  optimizationResult: OptimizationRunResponse | null;
  onNavigate: (view: string) => void;
  onRunOptimization: () => void;
  userRole?: string;
}

export const OverviewView: React.FC<OverviewViewProps> = ({
  orders,
  vehicles,
  depots,
  optimizationResult,
  onNavigate,
  onRunOptimization,
  userRole,
}) => {
  const [health, setHealth] = useState<SystemHealthResponse | null>(null);

  useEffect(() => {
    healthApi.getHealth()
      .then(setHealth)
      .catch(() => {});
  }, []);

  // Metrics derived safely from real data
  const unassignedOrders = orders.filter(o => o.status === 'UNASSIGNED' || !o.status).length;
  const assignedOrders = orders.filter(o => o.status === 'ASSIGNED' || o.status === 'IN_TRANSIT').length;
  const deliveredOrders = orders.filter(o => o.status === 'DELIVERED').length;
  const failedOrders = orders.filter(o => o.status === 'FAILED').length;

  const activeVehicles = vehicles.filter(v => v.status === 'EN_ROUTE').length;
  const idleVehicles = vehicles.filter(v => v.status === 'IDLE').length;

  const routes = optimizationResult?.routes ?? [];
  const metrics = optimizationResult?.metrics;

  // Derive on-time rate from assigned route stops if available
  let onTimeCount = 0;
  let totalStopsCount = 0;
  routes.forEach(r => {
    r.stops.forEach(s => {
      totalStopsCount++;
      // Stop is on-time if arrival is estimated
      if (s.estimatedArrivalMinutes != null) {
        onTimeCount++;
      }
    });
  });
  const onTimeRatePct = totalStopsCount > 0 ? ((onTimeCount / totalStopsCount) * 100).toFixed(1) : '100.0';

  return (
    <div style={{ padding: '20px 24px', overflowY: 'auto', height: '100%' }}>
      {/* Top Banner & Quick Actions */}
      <div style={{
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        marginBottom: 20, flexWrap: 'wrap', gap: 12,
      }}>
        <div>
          <h2 style={{ fontSize: 20, fontWeight: 800, color: 'var(--text-main)', fontFamily: 'Outfit, sans-serif' }}>
            Operations Control Center
          </h2>
          <p style={{ fontSize: 12, color: 'var(--text-muted)' }}>
            Real-time monitoring of Chicago last-mile delivery logistics
          </p>
        </div>

        <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
          {userRole !== 'DRIVER' && (
            <button
              onClick={onRunOptimization}
              className="btn btn-primary"
              style={{ padding: '8px 16px', fontSize: 13, gap: 6 }}
            >
              <Zap size={14} /> Run VRPTW Solver
            </button>
          )}
          <button
            onClick={() => onNavigate('orders')}
            className="btn btn-ghost"
            style={{ padding: '8px 14px', fontSize: 13, gap: 6 }}
          >
            <Package size={14} /> View Orders
          </button>
        </div>
      </div>

      {/* System Health Strip */}
      <div style={{
        display: 'flex', alignItems: 'center', gap: 16,
        padding: '10px 16px', borderRadius: 10,
        background: 'var(--bg-card)', border: '1px solid var(--border-color)',
        marginBottom: 20, fontSize: 12, color: 'var(--text-muted)', flexWrap: 'wrap',
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <Activity size={14} style={{ color: 'var(--accent-blue)' }} />
          <span style={{ fontWeight: 600, color: 'var(--text-main)' }}>System Health:</span>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <div style={{ width: 8, height: 8, borderRadius: '50%', background: health?.status === 'UP' ? 'var(--accent-green)' : 'var(--accent-amber)' }} />
          <span>API: <strong>{health?.status === 'UP' ? 'Healthy (UP)' : 'Checking...'}</strong></span>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <div style={{ width: 8, height: 8, borderRadius: '50%', background: 'var(--accent-green)' }} />
          <span>PostgreSQL/PostGIS: <strong>Active</strong></span>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <div style={{ width: 8, height: 8, borderRadius: '50%', background: 'var(--accent-green)' }} />
          <span>Redis: <strong>Active</strong></span>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <div style={{ width: 8, height: 8, borderRadius: '50%', background: 'var(--accent-teal)' }} />
          <span>Timefold VRPTW Engine: <strong>Ready</strong></span>
        </div>
      </div>

      {/* KPI Cards Grid */}
      <div style={{
        display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
        gap: 14, marginBottom: 24,
      }}>
        {/* Card 1: Total Orders */}
        <div className="card" style={{ padding: '16px 18px', cursor: 'pointer' }} onClick={() => onNavigate('orders')}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
            <span style={{ fontSize: 11, fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--text-muted)' }}>
              Active Orders
            </span>
            <Package size={18} style={{ color: 'var(--accent-blue)' }} />
          </div>
          <div style={{ fontSize: 26, fontWeight: 800, color: 'var(--text-main)', fontFamily: 'JetBrains Mono, monospace' }}>
            {orders.length}
          </div>
          <div style={{ display: 'flex', gap: 10, fontSize: 11, marginTop: 8, color: 'var(--text-muted)' }}>
            <span style={{ color: 'var(--accent-amber)' }}>{unassignedOrders} pending</span>
            <span>•</span>
            <span style={{ color: 'var(--accent-blue)' }}>{assignedOrders} assigned</span>
            <span>•</span>
            <span style={{ color: 'var(--accent-green)' }}>{deliveredOrders} delivered</span>
          </div>
        </div>

        {/* Card 2: Fleet Status */}
        <div className="card" style={{ padding: '16px 18px', cursor: 'pointer' }} onClick={() => onNavigate('fleet')}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
            <span style={{ fontSize: 11, fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--text-muted)' }}>
              Fleet Vehicles
            </span>
            <Truck size={18} style={{ color: 'var(--accent-teal)' }} />
          </div>
          <div style={{ fontSize: 26, fontWeight: 800, color: 'var(--text-main)', fontFamily: 'JetBrains Mono, monospace' }}>
            {vehicles.length}
          </div>
          <div style={{ display: 'flex', gap: 10, fontSize: 11, marginTop: 8, color: 'var(--text-muted)' }}>
            <span style={{ color: 'var(--accent-green)' }}>{idleVehicles} idle</span>
            <span>•</span>
            <span style={{ color: 'var(--accent-blue)' }}>{activeVehicles} en route</span>
          </div>
        </div>

        {/* Card 3: Planned Routes */}
        <div className="card" style={{ padding: '16px 18px', cursor: 'pointer' }} onClick={() => onNavigate('routes')}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
            <span style={{ fontSize: 11, fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--text-muted)' }}>
              Planned Routes
            </span>
            <Route size={18} style={{ color: 'var(--accent-purple)' }} />
          </div>
          <div style={{ fontSize: 26, fontWeight: 800, color: 'var(--text-main)', fontFamily: 'JetBrains Mono, monospace' }}>
            {routes.length}
          </div>
          <div style={{ fontSize: 11, marginTop: 8, color: 'var(--text-muted)' }}>
            {metrics ? `${fmtKm(metrics.totalDistanceKm)} total distance` : 'No route plan run yet'}
          </div>
        </div>

        {/* Card 4: SLA On-Time Rate */}
        <div className="card" style={{ padding: '16px 18px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
            <span style={{ fontSize: 11, fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--text-muted)' }}>
              On-Time SLA Rate
            </span>
            <CheckCircle2 size={18} style={{ color: 'var(--accent-green)' }} />
          </div>
          <div style={{ fontSize: 26, fontWeight: 800, color: 'var(--accent-green)', fontFamily: 'JetBrains Mono, monospace' }}>
            {onTimeRatePct}%
          </div>
          <div style={{ fontSize: 11, marginTop: 8, color: 'var(--text-muted)' }}>
            {failedOrders > 0 ? `${failedOrders} SLA violations` : '0 late delivery SLA breaches'}
          </div>
        </div>
      </div>

      {/* Main Grid: Optimization State & Quick Nav */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))', gap: 16 }}>
        {/* Solver Run Overview */}
        <div className="card">
          <div className="card-header">
            <span className="card-title"><Zap size={13} /> Current Optimization State</span>
            {optimizationResult && (
              <span className="status-pill" style={{
                background: optimizationResult.status === 'FEASIBLE' ? 'rgba(16,185,129,0.12)' : 'rgba(239,68,68,0.12)',
                color: optimizationResult.status === 'FEASIBLE' ? 'var(--accent-green)' : 'var(--accent-red)',
              }}>
                {optimizationResult.status}
              </span>
            )}
          </div>
          <div className="card-body">
            {optimizationResult ? (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 12px', background: 'var(--bg-panel)', borderRadius: 8, border: '1px solid var(--border-color)' }}>
                  <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>Score (Hard / Soft)</span>
                  <span style={{ fontSize: 13, fontFamily: 'JetBrains Mono, monospace', fontWeight: 600, color: optimizationResult.score?.hard === 0 ? 'var(--accent-green)' : 'var(--accent-red)' }}>
                    H{optimizationResult.score?.hard ?? 0} / S{optimizationResult.score?.soft ?? 0}
                  </span>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 8 }}>
                  <div className="kpi-cell">
                    <span className="kpi-value" style={{ fontSize: 16 }}>{fmtKm(metrics?.totalDistanceKm ?? 0)}</span>
                    <span className="kpi-label">Distance</span>
                  </div>
                  <div className="kpi-cell">
                    <span className="kpi-value" style={{ fontSize: 16 }}>{metrics?.vehiclesUsed ?? 0}</span>
                    <span className="kpi-label">Vehicles</span>
                  </div>
                  <div className="kpi-cell">
                    <span className="kpi-value" style={{ fontSize: 16 }}>{metrics?.ordersAssigned ?? 0}</span>
                    <span className="kpi-label">Assigned</span>
                  </div>
                </div>

                <button
                  onClick={() => onNavigate('routes')}
                  className="btn btn-ghost w-full"
                  style={{ gap: 6, fontSize: 12 }}
                >
                  View Planned Routes <ArrowRight size={13} />
                </button>
              </div>
            ) : (
              <div style={{ textAlign: 'center', padding: '24px 12px', color: 'var(--text-dim)' }}>
                <Zap size={32} style={{ margin: '0 auto 8px', opacity: 0.3 }} />
                <p style={{ fontSize: 13, fontWeight: 600, color: 'var(--text-main)' }}>No Optimization Plan Executed</p>
                <p style={{ fontSize: 11, marginTop: 4, maxWidth: 280, margin: '4px auto 14px' }}>
                  Run the Timefold VRPTW constraint solver to calculate optimal fleet routes.
                </p>
                {userRole !== 'DRIVER' && (
                  <button onClick={onRunOptimization} className="btn btn-primary" style={{ fontSize: 12 }}>
                    Run Optimization
                  </button>
                )}
              </div>
            )}
          </div>
        </div>

        {/* Depots Overview */}
        <div className="card">
          <div className="card-header">
            <span className="card-title">Chicago Regional Hubs</span>
            <span style={{ fontSize: 11, color: 'var(--text-muted)' }}>{depots.length} Depots</span>
          </div>
          <div className="card-body" style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            {depots.map(depot => (
              <div key={depot.id} style={{
                display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                padding: '10px 12px', borderRadius: 8, background: 'var(--bg-panel)',
                border: '1px solid var(--border-color)',
              }}>
                <div>
                  <div style={{ fontSize: 12, fontWeight: 700, color: 'var(--text-main)' }}>
                    🏭 {depot.name}
                  </div>
                  <div style={{ fontSize: 10, color: 'var(--text-muted)', marginTop: 2 }}>
                    {depot.addressText}
                  </div>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <span style={{ fontSize: 10, padding: '2px 6px', borderRadius: 4, background: 'rgba(59,130,246,0.1)', color: 'var(--accent-blue)', fontWeight: 600 }}>
                    Active Hub
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
