// ─── Solve control panel ────────────────────────────────────
import React, { useState, useEffect, useRef } from 'react';
import { Play, Zap, ChevronDown, Wifi, WifiOff } from 'lucide-react';
import { depotApi } from '../api/depotApi';
import { optimizationApi } from '../api/optimizationApi';
import { OptimizationRunResponse, SolverStatus } from '../types/optimization';
import { useToast } from '../context/ToastContext';
import { useWebSocket } from '../hooks/useWebSocket';

interface SolvePanelProps {
  onResult: (result: OptimizationRunResponse) => void;
}

const TERMINAL: SolverStatus[] = ['FEASIBLE', 'COMPLETED', 'FAILED', 'CANCELLED', 'INFEASIBLE'];

const SolvePanel: React.FC<SolvePanelProps> = ({ onResult }) => {
  const [depots, setDepots] = useState<{ id: string; name: string }[]>([]);
  const [selectedDepot, setSelectedDepot] = useState('');
  const [maxSeconds, setMaxSeconds] = useState(10);
  const [loading, setLoading] = useState(false);
  const [elapsed, setElapsed] = useState(0);
  const [wsConnected, setWsConnected] = useState(false);
  const { addToast } = useToast();

  const stopPollRef = useRef<(() => void) | null>(null);
  const unsubscribeRef = useRef<(() => void) | null>(null);

  const { subscribe } = useWebSocket({
    onConnect: () => setWsConnected(true),
    onError: () => setWsConnected(false),
  });

  useEffect(() => {
    depotApi.getAll({ size: 20 }).then(p => {
      setDepots(p.content);
      if (p.content.length > 0) setSelectedDepot(p.content[0].id);
    }).catch(() => {});
  }, []);

  // Elapsed-time ticker while solving
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

    if (TERMINAL.includes(result.status) && result.routes && result.routes.length > 0) {
      onResult(result);
      const routes = result.routes?.length ?? 0;
      const dist = result.metrics?.totalDistanceKm?.toFixed(1) ?? '?';
      addToast('success', `Optimization complete — ${routes} routes, ${dist} km total`);
    } else if (result.status === 'FAILED') {
      addToast('error', result.failureReason ?? 'Optimization failed');
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

      // If result is already terminal (immediate infeasibility), finish right away
      if (TERMINAL.includes(initial.status)) {
        handleFinished(initial);
        return;
      }

      const runId = initial.optimizationRunId;

      // Try WebSocket first
      unsubscribeRef.current = subscribe(
        `/topic/optimization/${runId}`,
        (payload) => handleFinished(payload as OptimizationRunResponse)
      );

      // Always start polling as fallback (stops itself when WS fires)
      stopPollRef.current = optimizationApi.pollUntilDone(runId, (run) => {
        if (TERMINAL.includes(run.status) && run.routes && run.routes.length > 0) {
          handleFinished(run);
        }
      });

    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })
        ?.response?.data?.message ?? 'Failed to start optimization';
      addToast('error', msg);
      setLoading(false);
    }
  };

  const progress = loading ? Math.min((elapsed / maxSeconds) * 100, 92) : 0;

  return (
    <div className="solve-panel map-overlay-content">
      <h3 style={{ fontSize: 13 }}>
        <Zap size={13} style={{ color: 'var(--accent-blue)' }} />
        VRPTW Optimizer
        {/* WebSocket status indicator */}
        <span style={{ marginLeft: 'auto' }} title={wsConnected ? 'Real-time connected' : 'Polling mode'}>
          {wsConnected
            ? <Wifi size={11} style={{ color: 'var(--accent-green)', opacity: 0.8 }} />
            : <WifiOff size={11} style={{ color: 'var(--text-dim)', opacity: 0.6 }} />}
        </span>
      </h3>

      <div className="form-group">
        <label className="form-label">Depot</label>
        <div style={{ position: 'relative' }}>
          <select
            className="form-select"
            value={selectedDepot}
            onChange={e => setSelectedDepot(e.target.value)}
            disabled={loading}
            id="select-depot"
          >
            {depots.length === 0
              ? <option value="">Loading depots…</option>
              : depots.map(d => <option key={d.id} value={d.id}>{d.name}</option>)
            }
          </select>
          <ChevronDown size={12} style={{
            position: 'absolute', right: 8, top: '50%', transform: 'translateY(-50%)',
            color: 'var(--text-muted)', pointerEvents: 'none',
          }} />
        </div>
      </div>

      <div className="form-group">
        <label className="form-label">Solve Time: {maxSeconds}s</label>
        <input
          type="range"
          min={5} max={60} step={5}
          value={maxSeconds}
          onChange={e => setMaxSeconds(Number(e.target.value))}
          disabled={loading}
          style={{ width: '100%', accentColor: 'var(--accent-blue)', cursor: 'pointer' }}
        />
        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 9, color: 'var(--text-dim)', marginTop: 2 }}>
          <span>5s</span><span>30s</span><span>60s</span>
        </div>
      </div>

      {loading && (
        <div style={{ marginBottom: 10 }}>
          <div className="progress-bar">
            <div className="progress-fill" style={{ width: `${progress}%` }} />
          </div>
          <p style={{
            fontSize: 10, color: 'var(--text-muted)', marginTop: 5,
            textAlign: 'center', fontFamily: 'JetBrains Mono, monospace',
          }}>
            {wsConnected ? '⚡ Live · ' : ''}Solving… {elapsed}s / {maxSeconds}s
          </p>
        </div>
      )}

      <button
        className="btn btn-primary w-full"
        onClick={handleSolve}
        disabled={loading || !selectedDepot}
        id="btn-run-optimization"
      >
        {loading
          ? <><div className="spinner" /> Solving…</>
          : <><Play size={13} /> Run Optimization</>
        }
      </button>
    </div>
  );
};

export default SolvePanel;
