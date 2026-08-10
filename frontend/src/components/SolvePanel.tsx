// ─── Solve control panel (map overlay, top-right) ─────────
import React, { useState, useEffect } from 'react';
import { Play, Zap, ChevronDown } from 'lucide-react';
import { depotApi } from '../api/depotApi';
import { optimizationApi } from '../api/optimizationApi';
import { OptimizationRunResponse } from '../types/optimization';
import { useToast } from '../context/ToastContext';

interface SolvePanelProps {
  onResult: (result: OptimizationRunResponse) => void;
}

const SolvePanel: React.FC<SolvePanelProps> = ({ onResult }) => {
  const [depots, setDepots] = useState<{ id: string; name: string }[]>([]);
  const [selectedDepot, setSelectedDepot] = useState('');
  const [maxSeconds, setMaxSeconds] = useState(10);
  const [loading, setLoading] = useState(false);
  const [elapsed, setElapsed] = useState(0);
  const { addToast } = useToast();

  useEffect(() => {
    depotApi.getAll({ size: 20 }).then(p => {
      setDepots(p.content);
      if (p.content.length > 0) setSelectedDepot(p.content[0].id);
    }).catch(() => {});
  }, []);

  useEffect(() => {
    let interval: ReturnType<typeof setInterval> | null = null;
    if (loading) {
      setElapsed(0);
      interval = setInterval(() => setElapsed(e => e + 1), 1000);
    }
    return () => { if (interval) clearInterval(interval); };
  }, [loading]);

  const handleSolve = async () => {
    if (!selectedDepot) { addToast('warning', 'Select a depot first'); return; }
    setLoading(true);
    try {
      const result = await optimizationApi.run({
        depotId: selectedDepot,
        maxSolveSeconds: maxSeconds,
      });
      onResult(result);
      const routes = result.routes?.length ?? 0;
      const dist = result.metrics?.totalDistanceKm?.toFixed(1) ?? '?';
      addToast('success', `Optimization complete — ${routes} routes, ${dist} km total`);
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Optimization failed';
      addToast('error', msg);
    } finally {
      setLoading(false);
    }
  };

  const progress = loading ? Math.min((elapsed / maxSeconds) * 100, 95) : 0;

  return (
    <div className="solve-panel map-overlay-content">
      <h3 style={{ fontSize: 13 }}>
        <Zap size={13} style={{ color: 'var(--accent-blue)' }} />
        VRPTW Optimizer
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
          <p style={{ fontSize: 10, color: 'var(--text-muted)', marginTop: 5, textAlign: 'center', fontFamily: 'JetBrains Mono, monospace' }}>
            Solving… {elapsed}s / {maxSeconds}s
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
