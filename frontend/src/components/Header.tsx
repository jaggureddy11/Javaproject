// ─── Header component ─────────────────────────────────────
import React from 'react';
import { Truck, Activity, Shield } from 'lucide-react';

interface HeaderProps {
  solverReady: boolean;
  totalOrders: number;
  activeRoutes: number;
}

const Header: React.FC<HeaderProps> = ({ solverReady, totalOrders, activeRoutes }) => {
  return (
    <header className="app-header">
      {/* Logo */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginRight: 'auto' }}>
        <div style={{
          width: 34, height: 34, borderRadius: 8,
          background: 'linear-gradient(135deg, #2563eb, #7c3aed)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          boxShadow: '0 0 16px rgba(59,130,246,0.3)',
        }}>
          <Truck size={16} color="#fff" strokeWidth={2.5} />
        </div>
        <div>
          <h1 style={{ fontSize: 15, fontWeight: 800, color: '#e2eaf5', fontFamily: 'Outfit, sans-serif', letterSpacing: '-0.02em' }}>
            RouteResQ
          </h1>
          <p style={{ fontSize: 10, color: 'var(--text-dim)', letterSpacing: '0.05em', textTransform: 'uppercase' }}>
            Last-Mile Delivery Control Center
          </p>
        </div>
      </div>

      {/* Status pills */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
        <div style={{
          display: 'flex', alignItems: 'center', gap: 6,
          padding: '5px 12px', borderRadius: 20,
          background: 'var(--bg-card)', border: '1px solid var(--border-color)',
          fontSize: 11, color: 'var(--text-muted)',
        }}>
          <Activity size={12} style={{ color: 'var(--accent-teal)' }} />
          <span style={{ fontFamily: 'JetBrains Mono, monospace', fontSize: 11 }}>
            {totalOrders} orders
          </span>
        </div>

        <div style={{
          display: 'flex', alignItems: 'center', gap: 6,
          padding: '5px 12px', borderRadius: 20,
          background: 'var(--bg-card)', border: '1px solid var(--border-color)',
          fontSize: 11, color: 'var(--text-muted)',
        }}>
          <Truck size={12} style={{ color: 'var(--accent-blue)' }} />
          <span style={{ fontFamily: 'JetBrains Mono, monospace', fontSize: 11 }}>
            {activeRoutes} routes
          </span>
        </div>

        <div style={{
          display: 'flex', alignItems: 'center', gap: 6,
          padding: '5px 12px', borderRadius: 20,
          background: solverReady ? 'rgba(16,185,129,0.08)' : 'rgba(239,68,68,0.08)',
          border: `1px solid ${solverReady ? 'rgba(16,185,129,0.25)' : 'rgba(239,68,68,0.25)'}`,
          fontSize: 11, color: solverReady ? 'var(--accent-green)' : 'var(--accent-red)',
        }}>
          <div className={solverReady ? 'live-dot' : ''} style={{
            width: 6, height: 6, borderRadius: '50%',
            background: solverReady ? 'var(--accent-green)' : 'var(--accent-red)',
          }} />
          <span style={{ fontWeight: 600 }}>{solverReady ? 'Solver Ready' : 'Solver Offline'}</span>
        </div>

        <div style={{
          display: 'flex', alignItems: 'center', gap: 5,
          padding: '5px 10px', borderRadius: 8,
          background: 'var(--bg-card)', border: '1px solid var(--border-color)',
          fontSize: 11, color: 'var(--text-dim)', cursor: 'default',
        }}>
          <Shield size={12} />
          <span style={{ fontSize: 10, fontFamily: 'JetBrains Mono, monospace' }}>RBAC</span>
        </div>
      </div>
    </header>
  );
};

export default Header;
