// ─── Dedicated Optimization Operations Center ────────────────
import React, { useState, useEffect, useRef } from 'react';
import {
  Zap, Play, Wifi, WifiOff, CheckCircle2, AlertOctagon,
  BarChart2, ShieldAlert
} from 'lucide-react';
import { Depot, Order, Vehicle } from '../types/domain';
import { OptimizationRunResponse, SolverStatus } from '../types/optimization';
import { optimizationApi } from '../api/optimizationApi';
import { useWebSocket } from '../hooks/useWebSocket';
import { useToast } from '../context/ToastContext';
import { fmtKm, fmtDuration } from '../utils/display';

interface OptimizationViewProps {
  depots: Depot[];
  orders: Order[];
  vehicles: Vehicle[];
  optimizationResult: OptimizationRunResponse | null;
  onResult: (result: OptimizationRunResponse) => void;
  onNavigate: (view: string) => void;
  userRole?: string;
}

const TERMINAL: SolverStatus[] = ['FEASIBLE', 'COMPLETED', 'FAILED', 'CANCELLED', 'INFEASIBLE'];

export const OptimizationView: React.FC<OptimizationViewProps> = ({
  depots,
  orders,
  vehicles,
  optimizationResult,
  onResult,
  onNavigate,
  userRole,
}) => {
  const [selectedDepot, setSelectedDepot] = useState('');
  const [maxSeconds, setMaxSeconds] = useState(10);
  const [loading, setLoading] = useState(false);
  const [elapsed, setElapsed] = useState(0);
  const [wsConnected, setWsConnected] = useState(false);
  const [latestResult, setLatestResult] = useState<OptimizationRunResponse | null>(optimizationResult);
  const { addToast } = useToast();

  const stopPollRef = useRef<(() => void) | null>(null);
  const unsubscribeRef = useRef<(() => void) | null>(null);

  const { subscribe } = useWebSocket({
    onConnect: () => setWsConnected(true),
    onError: () => setWsConnected(false),
  });

  useEffect(() => {
    if (depots.length > 0 && !selectedDepot) {
      setSelectedDepot(depots[0].id);
    }
  }, [depots, selectedDepot]);

  useEffect(() => {
    setLatestResult(optimizationResult);
  }, [optimizationResult]);

  useEffect(() => {
    let interval: ReturnType<typeof setInterval> | null = null;
    if (loading) {
      setElapsed(0);
      interval = setInterval(() => setElapsed(e => e + 1), 1000);
    }
    return () => { if (interval) clearInterval(interval); };
  }, [loading]);

  const handleFinished = (result: OptimizationRunResponse) => {
    setLoading(false);
    stopPollRef.current?.();
    unsubscribeRef.current?.();
    stopPollRef.current = null;
    unsubscribeRef.current = null;

    setLatestResult(result);
    onResult(result);

    if (TERMINAL.includes(result.status) && result.routes && result.routes.length > 0) {
      addToast('success', `Optimization completed: ${result.routes.length} routes planned (${result.metrics?.totalDistanceKm.toFixed(1)} km)`);
    } else if (result.status === 'FAILED' || (result.score && result.score.hard < 0)) {
      addToast('error', result.failureReason || 'Solver returned infeasible solution');
    }
  };

  const handleSolve = async () => {
    if (!selectedDepot) { addToast('warning', 'Select a depot first'); return; }
    setLoading(true);

    try {
      const initial = await optimizationApi.start({
        depotId: selectedDepot,
        maxSolveSeconds: maxSeconds,
      });

      if (TERMINAL.includes(initial.status)) {
        handleFinished(initial);
        return;
      }

      const runId = initial.optimizationRunId;

      unsubscribeRef.current = subscribe(
        `/topic/optimization/${runId}`,
        (payload) => handleFinished(payload as OptimizationRunResponse)
      );

      stopPollRef.current = optimizationApi.pollUntilDone(runId, (run) => {
        if (TERMINAL.includes(run.status) && run.routes && run.routes.length > 0) {
          handleFinished(run);
        }
      });

    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Failed to start solver';
      addToast('error', msg);
      setLoading(false);
    }
  };

  const progress = loading ? Math.min((elapsed / maxSeconds) * 100, 92) : 0;
  const isFailed = latestResult?.status === 'FAILED' || (latestResult?.score && latestResult.score.hard < 0);

  return (
    <div style={{ padding: '20px 24px', overflowY: 'auto', height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* Header Bar */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16, flexWrap: 'wrap', gap: 12 }}>
        <div>
          <h2 style={{ fontSize: 20, fontWeight: 800, color: 'var(--text-main)', fontFamily: 'Outfit, sans-serif' }}>
            Timefold VRPTW Solver Center
          </h2>
          <p style={{ fontSize: 12, color: 'var(--text-muted)' }}>
            Configure constraint parameters and run exact metaheuristic route optimization
          </p>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 11, color: 'var(--text-muted)', padding: '4px 10px', background: 'var(--bg-card)', borderRadius: 20, border: '1px solid var(--border-color)' }}>
          {wsConnected ? <Wifi size={13} style={{ color: 'var(--accent-green)' }} /> : <WifiOff size={13} style={{ color: 'var(--text-dim)' }} />}
          <span>STOMP Stream: <strong>{wsConnected ? 'Connected' : 'Polling mode'}</strong></span>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: 16, flex: 1, minHeight: 0 }}>
        {/* Left Card: Solver Parameters & Launch Controls */}
        <div className="card" style={{ display: 'flex', flexDirection: 'column' }}>
          <div className="card-header">
            <span className="card-title"><Zap size={13} /> Solver Configuration</span>
          </div>

          <div className="card-body" style={{ display: 'flex', flexDirection: 'column', gap: 16, flex: 1 }}>
            <div className="form-group">
              <label className="form-label">Target Hub / Depot</label>
              <select
                className="form-select"
                value={selectedDepot}
                onChange={e => setSelectedDepot(e.target.value)}
                disabled={loading}
              >
                {depots.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Max Solve Termination Time: {maxSeconds}s</label>
              <input
                type="range"
                min={5} max={60} step={5}
                value={maxSeconds}
                onChange={e => setMaxSeconds(Number(e.target.value))}
                disabled={loading}
                style={{ width: '100%', accentColor: 'var(--accent-blue)' }}
              />
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 10, color: 'var(--text-dim)', marginTop: 2 }}>
                <span>5s (Quick)</span><span>30s (Balanced)</span><span>60s (Deep Solve)</span>
              </div>
            </div>

            {/* Input Stats */}
            <div style={{ padding: '12px', background: 'var(--bg-panel)', borderRadius: 8, border: '1px solid var(--border-color)', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
              <div>
                <div style={{ fontSize: 10, color: 'var(--text-muted)', textTransform: 'uppercase' }}>Unassigned Demand</div>
                <div style={{ fontSize: 18, fontWeight: 700, color: 'var(--text-main)', fontFamily: 'JetBrains Mono, monospace' }}>
                  {orders.filter(o => o.status === 'UNASSIGNED' || !o.status).length} orders
                </div>
              </div>

              <div>
                <div style={{ fontSize: 10, color: 'var(--text-muted)', textTransform: 'uppercase' }}>Available Fleet</div>
                <div style={{ fontSize: 18, fontWeight: 700, color: 'var(--text-main)', fontFamily: 'JetBrains Mono, monospace' }}>
                  {vehicles.length} trucks
                </div>
              </div>
            </div>

            {loading && (
              <div>
                <div className="progress-bar">
                  <div className="progress-fill" style={{ width: `${progress}%` }} />
                </div>
                <p style={{ fontSize: 11, color: 'var(--text-muted)', marginTop: 6, textAlign: 'center', fontFamily: 'JetBrains Mono, monospace' }}>
                  Solving Timefold VRPTW… {elapsed}s / {maxSeconds}s
                </p>
              </div>
            )}

            {userRole === 'DRIVER' ? (
              <div style={{ padding: '10px 12px', background: 'rgba(239,68,68,0.06)', borderRadius: 8, border: '1px solid rgba(239,68,68,0.2)', fontSize: 11, color: 'var(--accent-red)', display: 'flex', alignItems: 'center', gap: 6 }}>
                <ShieldAlert size={14} /> Optimization triggers are restricted to Dispatcher & Admin roles.
              </div>
            ) : (
              <button
                onClick={handleSolve}
                disabled={loading || !selectedDepot}
                className="btn btn-primary w-full"
                style={{ padding: '10px', fontSize: 13, marginTop: 'auto' }}
              >
                {loading ? <><div className="spinner" /> Solving Constraints...</> : <><Play size={14} /> Launch Timefold Solver</>}
              </button>
            )}
          </div>
        </div>

        {/* Right Card: Solver Run Results & Metrics Summary */}
        <div className="card" style={{ display: 'flex', flexDirection: 'column' }}>
          <div className="card-header">
            <span className="card-title"><BarChart2 size={13} /> Solver Output Metrics</span>
            {latestResult && (
              <span className="status-pill" style={{
                background: !isFailed ? 'rgba(16,185,129,0.12)' : 'rgba(239,68,68,0.12)',
                color: !isFailed ? 'var(--accent-green)' : 'var(--accent-red)',
              }}>
                {!isFailed ? 'FEASIBLE' : 'FAILED / INFEASIBLE'}
              </span>
            )}
          </div>

          <div className="card-body" style={{ flex: 1, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: 14 }}>
            {latestResult ? (
              <>
                {/* Failure / Infeasible Alert Card */}
                {isFailed ? (
                  <div style={{
                    padding: '14px 16px', borderRadius: 10,
                    background: 'rgba(239,68,68,0.08)', border: '1px solid rgba(239,68,68,0.3)',
                    color: 'var(--accent-red)', display: 'flex', flexDirection: 'column', gap: 6,
                  }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, fontWeight: 700, fontSize: 14 }}>
                      <AlertOctagon size={18} /> Infeasible Optimization Result
                    </div>
                    <p style={{ fontSize: 12, lineHeight: 1.5, color: '#dc2626' }}>
                      {latestResult.failureReason || `Hard constraint violations present (Hard Score: ${latestResult.score?.hard}). Fleet capacity or time windows cannot satisfy current demand.`}
                    </p>
                    <div style={{ fontSize: 10, fontFamily: 'JetBrains Mono, monospace', marginTop: 4, color: 'var(--text-dim)' }}>
                      Run ID: {latestResult.optimizationRunId}
                    </div>
                  </div>
                ) : (
                  <div style={{
                    padding: '14px 16px', borderRadius: 10,
                    background: 'rgba(16,185,129,0.08)', border: '1px solid rgba(16,185,129,0.3)',
                    color: 'var(--accent-green)', display: 'flex', alignItems: 'center', gap: 10,
                  }}>
                    <CheckCircle2 size={20} />
                    <div>
                      <div style={{ fontWeight: 700, fontSize: 14 }}>0 Hard Constraint Violations</div>
                      <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>
                        All delivery time windows, capacity limits, and shift constraints satisfied.
                      </div>
                    </div>
                  </div>
                )}

                {/* Score Grid */}
                <div className="kpi-grid">
                  <div className="kpi-cell">
                    <span className="kpi-value" style={{ color: isFailed ? 'var(--accent-red)' : 'var(--accent-green)' }}>
                      {latestResult.score?.hard ?? 0}
                    </span>
                    <span className="kpi-label">Hard Score (Feasibility)</span>
                  </div>

                  <div className="kpi-cell">
                    <span className="kpi-value" style={{ color: 'var(--accent-blue)' }}>
                      {latestResult.score?.soft ?? 0}
                    </span>
                    <span className="kpi-label">Soft Score (Distance/Duration)</span>
                  </div>

                  <div className="kpi-cell">
                    <span className="kpi-value">{fmtKm(latestResult.metrics?.totalDistanceKm ?? 0)}</span>
                    <span className="kpi-label">Total Distance</span>
                  </div>

                  <div className="kpi-cell">
                    <span className="kpi-value">{fmtDuration(latestResult.metrics?.totalDurationMinutes ?? 0)}</span>
                    <span className="kpi-label">Total Duration</span>
                  </div>

                  <div className="kpi-cell">
                    <span className="kpi-value">{latestResult.metrics?.vehiclesUsed ?? 0}</span>
                    <span className="kpi-label">Vehicles Dispatched</span>
                  </div>

                  <div className="kpi-cell">
                    <span className="kpi-value">{latestResult.metrics?.ordersAssigned ?? 0}</span>
                    <span className="kpi-label">Orders Serviced</span>
                  </div>
                </div>

                {/* Action button */}
                {!isFailed && (
                  <button onClick={() => onNavigate('routes')} className="btn btn-primary w-full" style={{ padding: '9px', fontSize: 12 }}>
                    Inspect {latestResult.routes?.length ?? 0} Generated Routes
                  </button>
                )}
              </>
            ) : (
              <div style={{ textAlign: 'center', padding: '40px 16px', color: 'var(--text-dim)' }}>
                <Zap size={36} style={{ margin: '0 auto 10px', opacity: 0.3 }} />
                <p style={{ fontSize: 13, fontWeight: 600, color: 'var(--text-main)' }}>No Solver Output Available</p>
                <p style={{ fontSize: 11, marginTop: 4 }}>Configure solver parameters and launch run</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
