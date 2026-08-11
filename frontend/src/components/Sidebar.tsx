// ─── Left sidebar: Fleet, Orders, Benchmark tabs ──────────
import React, { useState, useEffect, useCallback, useRef } from 'react';
import {
  Route, Package, BarChart2, RefreshCw, ChevronDown, ChevronRight,
  Clock, Weight, AlertCircle, CheckCircle, Plus,
} from 'lucide-react';
import { Order, Vehicle } from '../types/domain';
import { OptimizationRunResponse, RouteResultDto } from '../types/optimization';
import { BenchmarkResult } from '../types/benchmark';
import { orderApi } from '../api/orderApi';
import { vehicleApi } from '../api/vehicleApi';
import { benchmarkApi } from '../api/benchmarkApi';
import OrderForm from './OrderForm';
import {
  routeColor, fmtMinutes, fmtKm, fmtDuration,
  improvementClass, orderStatusColor, priorityColor,
} from '../utils/display';
import { useToast } from '../context/ToastContext';

type SidebarTab = 'fleet' | 'orders' | 'benchmark';

interface SidebarProps {
  optimizationResult: OptimizationRunResponse | null;
  selectedRouteId: string | null;
  onSelectRoute: (id: string | null) => void;
  selectedOrderId: string | null;
  onSelectOrder: (id: string | null) => void;
  onOrdersLoaded: (orders: Order[]) => void;
}

// ── Live clock for ETA comparison ──────────────────────────
function useNowMinutes() {
  const [now, setNow] = useState(() => {
    const d = new Date();
    return d.getHours() * 60 + d.getMinutes();
  });
  useEffect(() => {
    const t = setInterval(() => {
      const d = new Date();
      setNow(d.getHours() * 60 + d.getMinutes());
    }, 30_000);
    return () => clearInterval(t);
  }, []);
  return now;
}

// ── ETA colour based on lateness ──────────────────────────
function etaColor(etaMinutes: number | undefined, windowEnd: number | undefined, currentMinutes: number): string {
  if (etaMinutes == null || windowEnd == null) return 'var(--text-dim)';
  // Already past window end
  if (currentMinutes > windowEnd) return 'var(--accent-red)';
  // ETA itself is after the window end
  if (etaMinutes > windowEnd) return 'var(--accent-red)';
  // ETA is within 15 min of window close
  if (windowEnd - etaMinutes < 15) return 'var(--accent-amber)';
  return 'var(--accent-green)';
}

const Sidebar: React.FC<SidebarProps> = ({
  optimizationResult,
  selectedRouteId,
  onSelectRoute,
  selectedOrderId,
  onSelectOrder,
  onOrdersLoaded,
}) => {
  const [tab, setTab] = useState<SidebarTab>('fleet');
  const [orders, setOrders] = useState<Order[]>([]);
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [expandedRoute, setExpandedRoute] = useState<string | null>(null);
  const [benchmarkResults, setBenchmarkResults] = useState<BenchmarkResult[]>([]);
  const [benchmarkLoading, setBenchmarkLoading] = useState(false);
  const [ordersLoading, setOrdersLoading] = useState(false);
  const [lastRefresh, setLastRefresh] = useState<Date | null>(null);
  const [showOrderForm, setShowOrderForm] = useState(false);
  const { addToast } = useToast();
  const nowMinutes = useNowMinutes();
  const autoRefreshRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const loadOrders = useCallback(async (silent = false) => {
    if (!silent) setOrdersLoading(true);
    try {
      const page = await orderApi.getAll({ size: 200 });
      setOrders(page.content);
      onOrdersLoaded(page.content);
      setLastRefresh(new Date());
    } catch {
      if (!silent) addToast('error', 'Failed to load orders');
    } finally {
      if (!silent) setOrdersLoading(false);
    }
  }, [addToast, onOrdersLoaded]);

  const loadVehicles = useCallback(async (silent = false) => {
    try {
      const page = await vehicleApi.getAll({ size: 100 });
      setVehicles(page.content);
    } catch {
      if (!silent) addToast('error', 'Failed to load vehicles');
    }
  }, [addToast]);

  // Initial load
  useEffect(() => {
    loadOrders();
    loadVehicles();
  }, [loadOrders, loadVehicles]);

  // ── 15-second auto-refresh ─────────────────────────────
  useEffect(() => {
    autoRefreshRef.current = setInterval(() => {
      loadOrders(true);
      loadVehicles(true);
    }, 15_000);
    return () => {
      if (autoRefreshRef.current) clearInterval(autoRefreshRef.current);
    };
  }, [loadOrders, loadVehicles]);

  const runBenchmarks = async () => {
    setBenchmarkLoading(true);
    const datasets = ['SMALL', 'MEDIUM', 'SPATIAL_CLUSTERING', 'TIGHT_TIME_WINDOWS'] as const;
    const results: BenchmarkResult[] = [];
    for (const dataset of datasets) {
      try {
        const r = await benchmarkApi.run({ dataset, maxSolveSeconds: 5 });
        results.push(r);
      } catch {
        addToast('warning', `Benchmark ${dataset} failed`);
      }
    }
    setBenchmarkResults(results);
    setBenchmarkLoading(false);
    if (results.length > 0) addToast('success', `${results.length} benchmarks completed`);
  };

  const handleOrderCreated = (order: Order) => {
    const updated = [order, ...orders];
    setOrders(updated);
    onOrdersLoaded(updated);
  };

  const routes = optimizationResult?.routes ?? [];
  const metrics = optimizationResult?.metrics;

  // Build order lookup for ETA checking
  const orderById = new Map(orders.map(o => [o.id, o]));

  // ── Fleet tab ─────────────────────────────────────────────
  const renderFleet = () => (
    <>
      {/* KPI bar */}
      {metrics && (
        <div className="card">
          <div className="card-header">
            <span className="card-title"><BarChart2 size={12} /> Optimization Metrics</span>
            <span className="status-pill" style={{
              background: (metrics.unassignedOrders === 0) ? 'rgba(16,185,129,0.12)' : 'rgba(239,68,68,0.12)',
              color: (metrics.unassignedOrders === 0) ? 'var(--accent-green)' : 'var(--accent-red)',
            }}>
              {(metrics.unassignedOrders === 0) ? 'Feasible' : `${metrics.unassignedOrders} unassigned`}
            </span>
          </div>
          <div className="card-body">
            <div className="kpi-grid">
              <div className="kpi-cell">
                <span className="kpi-value">{fmtKm(metrics.totalDistanceKm)}</span>
                <span className="kpi-label">Total Distance</span>
              </div>
              <div className="kpi-cell">
                <span className="kpi-value">{fmtDuration(metrics.totalDurationMinutes)}</span>
                <span className="kpi-label">Duration</span>
              </div>
              <div className="kpi-cell">
                <span className="kpi-value">{metrics.vehiclesUsed}</span>
                <span className="kpi-label">Vehicles Used</span>
              </div>
              <div className="kpi-cell">
                <span className="kpi-value">{metrics.ordersAssigned}</span>
                <span className="kpi-label">Orders Assigned</span>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Routes */}
      {routes.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '32px 16px', color: 'var(--text-dim)' }}>
          <Route size={28} style={{ margin: '0 auto 10px', opacity: 0.3 }} />
          <p style={{ fontSize: 12, fontWeight: 600 }}>No routes planned</p>
          <p style={{ fontSize: 11, marginTop: 4 }}>Run optimization to see routes</p>
        </div>
      ) : (
        routes.map((route: RouteResultDto, i: number) => {
          const color = routeColor(i);
          const isExpanded = expandedRoute === route.routeId;
          const isSelected = selectedRouteId === route.routeId;
          const distKm = (route.totalDistanceMeters ?? 0) / 1000;

          return (
            <div
              key={route.routeId}
              className={`route-item ${isSelected ? 'active' : ''} ${isExpanded ? 'expanded' : ''}`}
              onClick={() => {
                onSelectRoute(isSelected ? null : route.routeId);
                setExpandedRoute(isExpanded ? null : route.routeId);
              }}
            >
              <div className="route-item-header">
                <div className="route-color-dot" style={{ background: color }} />
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-main)' }} className="truncate">
                    {route.vehicleCode}
                  </div>
                  {route.driverName && (
                    <div style={{ fontSize: 10, color: 'var(--text-muted)' }} className="truncate">
                      {route.driverName}
                    </div>
                  )}
                </div>
                <div style={{ textAlign: 'right', flexShrink: 0 }}>
                  <div style={{ fontSize: 11, fontFamily: 'JetBrains Mono, monospace', color: 'var(--text-muted)' }}>
                    {fmtKm(distKm)}
                  </div>
                  <div style={{ fontSize: 10, color: 'var(--text-dim)' }}>{route.stops.length} stops</div>
                </div>
                {isExpanded
                  ? <ChevronDown size={13} style={{ color: 'var(--text-dim)', flexShrink: 0 }} />
                  : <ChevronRight size={13} style={{ color: 'var(--text-dim)', flexShrink: 0 }} />
                }
              </div>
              <div className="route-item-body">
                <div className="stop-list">
                  {route.stops.map(stop => {
                    const order = orderById.get(stop.orderId);
                    const etaCol = etaColor(stop.estimatedArrivalMinutes, order?.windowEndMinutes, nowMinutes);
                    return (
                      <div
                        key={stop.stopId}
                        className="stop-row"
                        onClick={e => { e.stopPropagation(); onSelectOrder(stop.orderId); }}
                      >
                        <span className="stop-seq">{stop.sequenceNumber}.</span>
                        <div style={{ flex: 1, minWidth: 0 }}>
                          <div className="truncate" style={{ fontSize: 11, color: 'var(--text-main)' }}>
                            {stop.customerName}
                          </div>
                          {stop.estimatedArrivalMinutes != null && (
                            <div style={{ fontSize: 10, color: etaCol, fontFamily: 'JetBrains Mono, monospace' }}>
                              ETA {fmtMinutes(stop.estimatedArrivalMinutes)}
                              {order?.windowEndMinutes != null && ` / ${fmtMinutes(order.windowEndMinutes)}`}
                            </div>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            </div>
          );
        })
      )}

      {/* Fleet status */}
      {vehicles.length > 0 && (
        <div className="card">
          <div className="card-header">
            <span className="card-title">Fleet Status</span>
            <span style={{ fontSize: 10, color: 'var(--text-dim)' }}>
              {lastRefresh ? `Updated ${lastRefresh.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: false })}` : ''}
            </span>
          </div>
          <div className="card-body" style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
            {vehicles.map(v => (
              <div key={v.id} style={{
                display: 'flex', alignItems: 'center', gap: 8,
                padding: '5px 8px', borderRadius: 6,
                background: 'var(--bg-panel)', border: '1px solid var(--border-color)',
              }}>
                <div style={{
                  width: 7, height: 7, borderRadius: '50%',
                  background: v.status === 'EN_ROUTE' ? 'var(--accent-blue)'
                    : v.status === 'IDLE' ? 'var(--accent-green)'
                    : 'var(--text-dim)',
                  ...(v.status === 'EN_ROUTE' ? { animation: 'glow-pulse 2s infinite' } : {}),
                }} />
                <span style={{ flex: 1, fontSize: 12, fontFamily: 'JetBrains Mono, monospace' }} className="truncate">
                  {v.vehicleCode}
                </span>
                <span style={{ fontSize: 10, color: 'var(--text-dim)' }}>{v.status}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </>
  );

  // ── Orders tab ────────────────────────────────────────────
  const renderOrders = () => (
    <>
      <div className="flex-between" style={{ padding: '4px 2px' }}>
        <span style={{ fontSize: 11, color: 'var(--text-muted)' }}>
          {orders.length} orders
        </span>
        <div style={{ display: 'flex', gap: 6 }}>
          <button
            className="btn btn-primary"
            onClick={() => setShowOrderForm(true)}
            title="New Order"
            style={{ padding: '5px 10px', fontSize: 11, gap: 4 }}
          >
            <Plus size={12} /> New
          </button>
          <button className="btn-icon" onClick={() => loadOrders()} title="Refresh">
            {ordersLoading ? <div className="spinner" /> : <RefreshCw size={13} />}
          </button>
        </div>
      </div>

      {orders.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '32px 16px', color: 'var(--text-dim)' }}>
          <Package size={28} style={{ margin: '0 auto 10px', opacity: 0.3 }} />
          <p style={{ fontSize: 12, fontWeight: 600 }}>No orders found</p>
          <p style={{ fontSize: 11, marginTop: 4 }}>Seed data or create an order above</p>
        </div>
      ) : (
        orders.map(order => {
          const pColor = priorityColor(order.priority);
          const sColor = orderStatusColor(order.status);
          return (
            <div
              key={order.id}
              className={`order-row ${selectedOrderId === order.id ? 'selected' : ''}`}
              onClick={() => onSelectOrder(selectedOrderId === order.id ? null : order.id)}
            >
              <div className="priority-badge" style={{ background: `${pColor}22`, color: pColor }}>
                {order.priority}
              </div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div className="truncate" style={{ fontSize: 12, color: 'var(--text-main)', fontWeight: 500 }}>
                  {order.customerName}
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 2 }}>
                  <span style={{ fontSize: 10, color: 'var(--text-dim)', fontFamily: 'JetBrains Mono, monospace' }}>
                    {order.orderNumber}
                  </span>
                  <span style={{ fontSize: 10, display: 'flex', alignItems: 'center', gap: 3, color: 'var(--text-dim)' }}>
                    <Clock size={9} />
                    {fmtMinutes(order.windowStartMinutes)}–{fmtMinutes(order.windowEndMinutes)}
                  </span>
                  <span style={{ fontSize: 10, display: 'flex', alignItems: 'center', gap: 3, color: 'var(--text-dim)' }}>
                    <Weight size={9} />
                    {order.weightKg}kg
                  </span>
                </div>
              </div>
              <div style={{ width: 7, height: 7, borderRadius: '50%', background: sColor, flexShrink: 0 }} />
            </div>
          );
        })
      )}
    </>
  );

  // ── Benchmark tab ─────────────────────────────────────────
  const renderBenchmark = () => (
    <>
      <div className="card">
        <div className="card-header">
          <span className="card-title"><BarChart2 size={12} /> Solver vs Baseline</span>
        </div>
        <div className="card-body">
          <p style={{ fontSize: 11, color: 'var(--text-muted)', marginBottom: 10, lineHeight: 1.5 }}>
            Runs standard benchmark datasets and measures Timefold VRPTW improvement over greedy nearest-neighbor baseline.
          </p>
          <button
            className="btn btn-primary w-full"
            onClick={runBenchmarks}
            disabled={benchmarkLoading}
          >
            {benchmarkLoading ? <><div className="spinner" /> Running…</> : <><BarChart2 size={13} /> Run Benchmarks</>}
          </button>
        </div>
      </div>

      {benchmarkResults.length > 0 && (
        <div className="card">
          <div className="card-header">
            <span className="card-title">Results</span>
          </div>
          <div className="card-body" style={{ padding: 0, overflowX: 'auto' }}>
            <table className="bench-table">
              <thead>
                <tr>
                  <th>Dataset</th>
                  <th>Baseline</th>
                  <th>Optimized</th>
                  <th>Δ%</th>
                </tr>
              </thead>
              <tbody>
                {benchmarkResults.map(r => {
                  const delta = r.improvement.distanceImprovementPercent;
                  return (
                    <tr key={r.dataset}>
                      <td style={{ color: 'var(--text-main)', fontWeight: 600, fontSize: 10 }}>
                        {r.dataset.replace(/_/g, ' ')}
                      </td>
                      <td>{fmtKm(r.baseline.distanceKm)}</td>
                      <td>{fmtKm(r.optimized.distanceKm)}</td>
                      <td className={improvementClass(delta)}>
                        {delta > 0 ? '+' : ''}{delta.toFixed(1)}%
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          <div style={{ padding: '10px 14px', borderTop: '1px solid var(--border-color)', display: 'flex', flexDirection: 'column', gap: 6 }}>
            {benchmarkResults.map(r => (
              <div key={r.dataset} style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 11 }}>
                {r.optimized.lateDeliveries === 0
                  ? <CheckCircle size={12} style={{ color: 'var(--accent-green)', flexShrink: 0 }} />
                  : <AlertCircle size={12} style={{ color: 'var(--accent-amber)', flexShrink: 0 }} />
                }
                <span style={{ color: 'var(--text-muted)' }}>{r.dataset.replace(/_/g, ' ')}</span>
                <span style={{ marginLeft: 'auto', color: 'var(--text-dim)' }}>
                  {r.optimized.lateDeliveries === 0 ? 'SLA ✓' : `${r.optimized.lateDeliveries} late`}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}

      {benchmarkResults.length > 0 && (() => {
        const best = benchmarkResults.reduce((a, b) =>
          a.improvement.distanceImprovementPercent > b.improvement.distanceImprovementPercent ? a : b
        );
        const avgImprovement = benchmarkResults.reduce((s, r) =>
          s + r.improvement.distanceImprovementPercent, 0) / benchmarkResults.length;
        return (
          <div className="kpi-grid">
            <div className="kpi-cell">
              <span className="kpi-value" style={{ color: 'var(--accent-green)', fontSize: 18 }}>
                +{best.improvement.distanceImprovementPercent.toFixed(1)}%
              </span>
              <span className="kpi-label">Best Improvement</span>
              <span className="kpi-sub">{best.dataset.replace(/_/g, ' ')}</span>
            </div>
            <div className="kpi-cell">
              <span className="kpi-value" style={{
                color: avgImprovement > 0 ? 'var(--accent-green)' : 'var(--accent-red)', fontSize: 18,
              }}>
                {avgImprovement > 0 ? '+' : ''}{avgImprovement.toFixed(1)}%
              </span>
              <span className="kpi-label">Avg Improvement</span>
              <span className="kpi-sub">{benchmarkResults.length} datasets</span>
            </div>
          </div>
        );
      })()}
    </>
  );

  return (
    <>
      <aside className="sidebar">
        {/* Tabs */}
        <div className="sidebar-tabs">
          <button className={`sidebar-tab ${tab === 'fleet' ? 'active' : ''}`}
            onClick={() => setTab('fleet')} id="tab-fleet">
            <Route size={14} />Fleet
          </button>
          <button className={`sidebar-tab ${tab === 'orders' ? 'active' : ''}`}
            onClick={() => setTab('orders')} id="tab-orders">
            <Package size={14} />Orders
          </button>
          <button className={`sidebar-tab ${tab === 'benchmark' ? 'active' : ''}`}
            onClick={() => setTab('benchmark')} id="tab-benchmark">
            <BarChart2 size={14} />Bench
          </button>
        </div>

        {/* Content */}
        <div className="sidebar-content">
          {tab === 'fleet' && renderFleet()}
          {tab === 'orders' && renderOrders()}
          {tab === 'benchmark' && renderBenchmark()}
        </div>
      </aside>

      {/* Order creation modal */}
      {showOrderForm && (
        <OrderForm
          onClose={() => setShowOrderForm(false)}
          onCreated={handleOrderCreated}
        />
      )}
    </>
  );
};

export default Sidebar;
