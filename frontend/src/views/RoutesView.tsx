// ─── Dedicated Routes Inspector & Timeline View ──────────────
import React, { useMemo } from 'react';
import {
  Route as RouteIcon, MapPin, CheckCircle,
  AlertTriangle, Zap, User
} from 'lucide-react';
import { Order } from '../types/domain';
import { OptimizationRunResponse } from '../types/optimization';
import { fmtMinutes, fmtKm, fmtDuration, routeColor } from '../utils/display';

interface RoutesViewProps {
  optimizationResult: OptimizationRunResponse | null;
  orders: Order[];
  selectedRouteId: string | null;
  onSelectRoute: (routeId: string | null) => void;
  onSelectOrderOnMap: (orderId: string) => void;
  onRunOptimization: () => void;
}

export const RoutesView: React.FC<RoutesViewProps> = ({
  optimizationResult,
  orders,
  selectedRouteId,
  onSelectRoute,
  onSelectOrderOnMap,
  onRunOptimization,
}) => {
  const routes = optimizationResult?.routes ?? [];
  const orderMap = useMemo(() => new Map(orders.map(o => [o.id, o])), [orders]);

  // Default select first route if none selected
  const activeRoute = useMemo(() => {
    if (!selectedRouteId && routes.length > 0) return routes[0];
    return routes.find(r => r.routeId === selectedRouteId) ?? routes[0] ?? null;
  }, [routes, selectedRouteId]);

  return (
    <div style={{ padding: '20px 24px', overflowY: 'auto', height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* Header Bar */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16, flexWrap: 'wrap', gap: 12 }}>
        <div>
          <h2 style={{ fontSize: 20, fontWeight: 800, color: 'var(--text-main)', fontFamily: 'Outfit, sans-serif' }}>
            Route Dispatch & Sequential Timelines
          </h2>
          <p style={{ fontSize: 12, color: 'var(--text-muted)' }}>
            Inspect planned delivery sequences, arrival ETAs, service durations, and SLA compliance
          </p>
        </div>

        {routes.length === 0 && (
          <button onClick={onRunOptimization} className="btn btn-primary" style={{ padding: '8px 16px', fontSize: 13, gap: 6 }}>
            <Zap size={14} /> Solve Routes
          </button>
        )}
      </div>

      {routes.length === 0 ? (
        <div className="card" style={{ padding: '48px 24px', textAlign: 'center', color: 'var(--text-dim)' }}>
          <RouteIcon size={40} style={{ margin: '0 auto 12px', opacity: 0.3 }} />
          <h3 style={{ fontSize: 16, fontWeight: 700, color: 'var(--text-main)', marginBottom: 6 }}>No Active Route Plan</h3>
          <p style={{ fontSize: 12, maxWidth: 360, margin: '0 auto 16px', color: 'var(--text-muted)' }}>
            Run the VRPTW optimizer to generate multi-vehicle delivery routes with sequenced stop timelines.
          </p>
          <button onClick={onRunOptimization} className="btn btn-primary">
            Run Optimization
          </button>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: '320px 1fr', gap: 16, flex: 1, minHeight: 0 }}>
          {/* Left Column: Routes Selector List */}
          <div className="card" style={{ display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
            <div className="card-header">
              <span className="card-title"><RouteIcon size={12} /> Planned Routes ({routes.length})</span>
            </div>
            <div className="card-body" style={{ flex: 1, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: 8, padding: 10 }}>
              {routes.map((route, i) => {
                const color = routeColor(i);
                const isSelected = activeRoute?.routeId === route.routeId;
                const distKm = (route.totalDistanceMeters ?? 0) / 1000;

                return (
                  <div
                    key={route.routeId}
                    onClick={() => onSelectRoute(route.routeId)}
                    style={{
                      padding: '10px 12px', borderRadius: 8,
                      background: isSelected ? 'var(--bg-active)' : 'var(--bg-panel)',
                      border: `1px solid ${isSelected ? 'var(--accent-blue)' : 'var(--border-color)'}`,
                      cursor: 'pointer', transition: 'all 0.15s',
                    }}
                  >
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                      <div style={{ width: 10, height: 10, borderRadius: '50%', background: color }} />
                      <span style={{ fontSize: 13, fontWeight: 700, color: 'var(--text-main)', fontFamily: 'JetBrains Mono, monospace' }}>
                        {route.vehicleCode}
                      </span>
                      <span className="status-pill" style={{ marginLeft: 'auto', fontSize: 9, padding: '1px 6px', background: 'rgba(59,130,246,0.1)', color: 'var(--accent-blue)' }}>
                        {route.status}
                      </span>
                    </div>

                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 11, color: 'var(--text-muted)' }}>
                      <span><User size={10} style={{ display: 'inline', marginRight: 3 }} />{route.driverName ?? 'Driver'}</span>
                      <span style={{ fontFamily: 'JetBrains Mono, monospace' }}>{fmtKm(distKm)} • {route.stops.length} stops</span>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Right Column: Route Inspector & Sequential Timeline */}
          {activeRoute && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 16, overflowY: 'auto' }}>
              {/* Route Summary Overview Header */}
              <div className="card" style={{ padding: '16px 20px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 12, marginBottom: 14 }}>
                  <div>
                    <div style={{ fontSize: 11, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em', fontWeight: 700 }}>
                      Selected Route Overview
                    </div>
                    <h3 style={{ fontSize: 18, fontWeight: 800, color: 'var(--text-main)', fontFamily: 'Outfit, sans-serif', marginTop: 2 }}>
                      Vehicle {activeRoute.vehicleCode} ({activeRoute.driverName ?? 'Assigned Driver'})
                    </h3>
                  </div>

                  <div style={{ display: 'flex', gap: 8, fontSize: 11, fontFamily: 'JetBrains Mono, monospace' }}>
                    <div style={{ padding: '6px 12px', background: 'var(--bg-panel)', borderRadius: 6, border: '1px solid var(--border-color)' }}>
                      <span style={{ color: 'var(--text-muted)' }}>Distance: </span>
                      <strong style={{ color: 'var(--text-main)' }}>{fmtKm((activeRoute.totalDistanceMeters ?? 0) / 1000)}</strong>
                    </div>

                    <div style={{ padding: '6px 12px', background: 'var(--bg-panel)', borderRadius: 6, border: '1px solid var(--border-color)' }}>
                      <span style={{ color: 'var(--text-muted)' }}>Duration: </span>
                      <strong style={{ color: 'var(--text-main)' }}>{fmtDuration(activeRoute.totalDurationMinutes ?? 0)}</strong>
                    </div>

                    <div style={{ padding: '6px 12px', background: 'var(--bg-panel)', borderRadius: 6, border: '1px solid var(--border-color)' }}>
                      <span style={{ color: 'var(--text-muted)' }}>Stops: </span>
                      <strong style={{ color: 'var(--text-main)' }}>{activeRoute.stops.length}</strong>
                    </div>
                  </div>
                </div>

                <div style={{ fontSize: 11, color: 'var(--text-dim)', fontFamily: 'JetBrains Mono, monospace' }}>
                  Optimization Run ID: {optimizationResult?.optimizationRunId ?? 'N/A'}
                </div>
              </div>

              {/* Sequential Timeline Component */}
              <div className="card" style={{ padding: '16px 20px' }}>
                <h4 style={{ fontSize: 13, fontWeight: 700, color: 'var(--text-main)', marginBottom: 16, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                  Sequential Stop Timeline
                </h4>

                <div style={{ display: 'flex', flexDirection: 'column', gap: 0, position: 'relative' }}>
                  {/* Start Depot */}
                  <div style={{ display: 'flex', gap: 14, alignItems: 'flex-start', paddingBottom: 16, position: 'relative' }}>
                    <div style={{ width: 28, height: 28, borderRadius: 6, background: 'var(--accent-blue)', color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 12, fontWeight: 700, flexShrink: 0, zIndex: 2 }}>
                      🏠
                    </div>
                    <div style={{ flex: 1, padding: '4px 10px', background: 'var(--bg-panel)', borderRadius: 6, border: '1px solid var(--border-color)' }}>
                      <div style={{ fontWeight: 700, fontSize: 12, color: 'var(--text-main)' }}>DEPOT DEPARTURE</div>
                      <div style={{ fontSize: 11, color: 'var(--text-muted)', fontFamily: 'JetBrains Mono, monospace' }}>
                        Departure Time: 08:00 (480 min)
                      </div>
                    </div>
                  </div>

                  {/* Stops */}
                  {activeRoute.stops
                    .sort((a, b) => a.sequenceNumber - b.sequenceNumber)
                    .map(stop => {
                      const order = orderMap.get(stop.orderId);
                      const isLate = order?.windowEndMinutes != null && stop.estimatedArrivalMinutes != null && stop.estimatedArrivalMinutes > order.windowEndMinutes;

                      return (
                        <div key={stop.stopId} style={{ display: 'flex', gap: 14, alignItems: 'flex-start', paddingBottom: 16, position: 'relative' }}>
                          <div style={{ width: 28, height: 28, borderRadius: '50%', background: isLate ? 'rgba(239,68,68,0.15)' : 'rgba(59,130,246,0.15)', border: `2px solid ${isLate ? 'var(--accent-red)' : 'var(--accent-blue)'}`, color: isLate ? 'var(--accent-red)' : 'var(--accent-blue)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 11, fontWeight: 700, flexShrink: 0, zIndex: 2, fontFamily: 'JetBrains Mono, monospace' }}>
                            {stop.sequenceNumber}
                          </div>

                          <div style={{ flex: 1, padding: '10px 12px', background: 'var(--bg-panel)', borderRadius: 8, border: `1px solid ${isLate ? 'rgba(239,68,68,0.3)' : 'var(--border-color)'}` }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 4 }}>
                              <span style={{ fontSize: 13, fontWeight: 700, color: 'var(--text-main)' }}>
                                {stop.customerName}
                              </span>
                              <span style={{ fontSize: 11, fontFamily: 'JetBrains Mono, monospace', color: 'var(--text-muted)' }}>
                                Order {stop.orderNumber}
                              </span>
                            </div>

                            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 16, fontSize: 11, color: 'var(--text-muted)', marginTop: 6 }}>
                              <div>
                                <span>ETA: </span>
                                <strong style={{ color: isLate ? 'var(--accent-red)' : 'var(--text-main)', fontFamily: 'JetBrains Mono, monospace' }}>
                                  {fmtMinutes(stop.estimatedArrivalMinutes ?? 0)}
                                </strong>
                              </div>

                              <div>
                                <span>Departure: </span>
                                <strong style={{ color: 'var(--text-main)', fontFamily: 'JetBrains Mono, monospace' }}>
                                  {fmtMinutes(stop.estimatedDepartureMinutes ?? 0)}
                                </strong>
                              </div>

                              {order && (
                                <div>
                                  <span>Delivery Window: </span>
                                  <strong style={{ color: 'var(--text-main)', fontFamily: 'JetBrains Mono, monospace' }}>
                                    {fmtMinutes(order.windowStartMinutes)} – {fmtMinutes(order.windowEndMinutes)}
                                  </strong>
                                </div>
                              )}

                              {isLate ? (
                                <span className="status-pill" style={{ background: 'rgba(239,68,68,0.12)', color: 'var(--accent-red)', marginLeft: 'auto' }}>
                                  <AlertTriangle size={10} /> SLA Breach
                                </span>
                              ) : (
                                <span className="status-pill" style={{ background: 'rgba(16,185,129,0.12)', color: 'var(--accent-green)', marginLeft: 'auto' }}>
                                  <CheckCircle size={10} /> On-Time
                                </span>
                              )}
                            </div>

                            <button
                              onClick={() => onSelectOrderOnMap(stop.orderId)}
                              className="btn btn-ghost"
                              style={{ marginTop: 8, padding: '3px 8px', fontSize: 10, gap: 4 }}
                            >
                              <MapPin size={10} /> Locate Stop on Map
                            </button>
                          </div>
                        </div>
                      );
                    })}

                  {/* Return to Depot */}
                  <div style={{ display: 'flex', gap: 14, alignItems: 'flex-start' }}>
                    <div style={{ width: 28, height: 28, borderRadius: 6, background: 'var(--accent-teal)', color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 12, fontWeight: 700, flexShrink: 0, zIndex: 2 }}>
                      🏁
                    </div>
                    <div style={{ flex: 1, padding: '4px 10px', background: 'var(--bg-panel)', borderRadius: 6, border: '1px solid var(--border-color)' }}>
                      <div style={{ fontWeight: 700, fontSize: 12, color: 'var(--text-main)' }}>RETURN TO DEPOT</div>
                      <div style={{ fontSize: 11, color: 'var(--text-muted)', fontFamily: 'JetBrains Mono, monospace' }}>
                        Expected Return: {fmtMinutes(480 + (activeRoute.totalDurationMinutes ?? 0))} ({activeRoute.totalDurationMinutes ?? 0} min total)
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
