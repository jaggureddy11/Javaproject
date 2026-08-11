// ─── Header component with Application Shell Navigation ─────
import React, { useState, useEffect } from 'react';
import {
  Truck, Shield, LogOut, Clock,
  LayoutDashboard, Map as MapIcon, Package, Route as RouteIcon,
  Zap, AlertTriangle, BarChart2, PlayCircle
} from 'lucide-react';
import { authApi } from '../api/auth';
import { tokenStorage } from '../auth/tokenStorage';
import { User } from '../types/auth';

interface HeaderProps {
  solverReady: boolean;
  totalOrders: number;
  activeRoutes: number;
  activeView: string;
  onNavigate: (view: string) => void;
  onLogout: () => void;
}

import { useRealtime } from '../context/RealtimeContext';

function useClock() {
  const [time, setTime] = useState(() => new Date());
  useEffect(() => {
    const t = setInterval(() => setTime(new Date()), 30_000);
    return () => clearInterval(t);
  }, []);
  return time.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: false });
}

function RealtimeStatusBadge() {
  const { connectionState } = useRealtime();
  const isLive = connectionState === 'CONNECTED';
  const isReconnecting = connectionState === 'RECONNECTING' || connectionState === 'CONNECTING';

  const color = isLive ? '#22C55E' : isReconnecting ? '#F59E0B' : '#EF4444';
  const bg = isLive ? 'rgba(34, 197, 94, 0.12)' : isReconnecting ? 'rgba(245, 158, 11, 0.12)' : 'rgba(239, 68, 68, 0.12)';
  const label = isLive ? 'LIVE' : isReconnecting ? 'RECONNECTING' : 'OFFLINE';

  return (
    <div style={{
      display: 'flex', alignItems: 'center', gap: 5,
      padding: '4px 10px', borderRadius: 20,
      background: bg, border: `1px solid ${color}`,
      fontSize: 10, color: color, fontWeight: 700,
    }}>
      <div style={{ width: 6, height: 6, borderRadius: '50%', background: color }} />
      <span>● {label}</span>
    </div>
  );
}

const Header: React.FC<HeaderProps> = ({
  solverReady,
  totalOrders,
  activeRoutes,
  activeView,
  onNavigate,
  onLogout,
}) => {
  const clock = useClock();
  const user = tokenStorage.getUser<User>();

  const handleLogout = () => {
    authApi.logout();
    onLogout();
  };

  const navItems = [
    { id: 'overview', label: 'Overview', icon: LayoutDashboard },
    { id: 'map', label: 'Map View', icon: MapIcon },
    { id: 'orders', label: 'Orders', icon: Package },
    { id: 'fleet', label: 'Fleet', icon: Truck },
    { id: 'routes', label: 'Routes', icon: RouteIcon },
    { id: 'optimization', label: 'Solver', icon: Zap },
    { id: 'simulation', label: 'Simulation', icon: PlayCircle },
    { id: 'incidents', label: 'Incidents', icon: AlertTriangle },
    { id: 'benchmarks', label: 'Benchmarks', icon: BarChart2 },
  ];

  return (
    <header className="app-header">
      {/* Logo */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginRight: 16 }}>
        <div style={{
          width: 32, height: 32, borderRadius: 8,
          background: 'linear-gradient(135deg, #2563eb, #7c3aed)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          boxShadow: '0 0 14px rgba(59,130,246,0.25)', flexShrink: 0,
        }}>
          <Truck size={15} color="#fff" strokeWidth={2.5} />
        </div>
        <div>
          <h1 style={{ fontSize: 14, fontWeight: 800, color: 'var(--text-main)', fontFamily: 'Outfit, sans-serif', letterSpacing: '-0.02em', lineHeight: 1 }}>
            RouteResQ
          </h1>
          <p style={{ fontSize: 9, color: 'var(--text-dim)', letterSpacing: '0.04em', textTransform: 'uppercase', marginTop: 2 }}>
            Control Center
          </p>
        </div>
      </div>

      {/* Nav Links */}
      <nav style={{ display: 'flex', gap: 4, marginRight: 'auto', overflowX: 'auto', padding: '2px 0' }}>
        {navItems.map(item => {
          const Icon = item.icon;
          const isActive = activeView === item.id;

          // Driver role hides solver/benchmark navigationTriggers if driver role
          if (user?.role === 'DRIVER' && (item.id === 'optimization' || item.id === 'benchmarks')) {
            return null;
          }

          return (
            <button
              key={item.id}
              onClick={() => onNavigate(item.id)}
              style={{
                display: 'flex', alignItems: 'center', gap: 6,
                padding: '6px 10px', borderRadius: 6,
                background: isActive ? 'var(--bg-card)' : 'transparent',
                color: isActive ? 'var(--accent-blue)' : 'var(--text-muted)',
                border: `1px solid ${isActive ? 'var(--border-color)' : 'transparent'}`,
                fontWeight: isActive ? 700 : 500,
                fontSize: 12, cursor: 'pointer', transition: 'all 0.15s',
                whiteSpace: 'nowrap',
              }}
            >
              <Icon size={13} style={{ color: isActive ? 'var(--accent-blue)' : 'var(--text-dim)' }} />
              {item.label}
            </button>
          );
        })}
      </nav>

      {/* Status pills & User Actions */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexShrink: 0 }}>
        {/* Live clock */}
        <div style={{
          display: 'flex', alignItems: 'center', gap: 5,
          padding: '4px 10px', borderRadius: 20,
          background: 'var(--bg-card)', border: '1px solid var(--border-color)',
          fontSize: 11, color: 'var(--text-muted)',
        }}>
          <Clock size={11} style={{ color: 'var(--accent-blue)' }} />
          <span style={{ fontFamily: 'JetBrains Mono, monospace', fontSize: 11 }}>{clock}</span>
        </div>

        {/* Orders Badge */}
        <div style={{
          display: 'flex', alignItems: 'center', gap: 5,
          padding: '4px 10px', borderRadius: 20,
          background: 'var(--bg-card)', border: '1px solid var(--border-color)',
          fontSize: 11, color: 'var(--text-muted)',
        }}>
          <span style={{ fontFamily: 'JetBrains Mono, monospace', fontSize: 11 }}>
            {totalOrders} orders
          </span>
        </div>

        {/* Routes Badge */}
        <div style={{
          display: 'flex', alignItems: 'center', gap: 5,
          padding: '4px 10px', borderRadius: 20,
          background: 'var(--bg-card)', border: '1px solid var(--border-color)',
          fontSize: 11, color: 'var(--text-muted)',
        }}>
          <span style={{ fontFamily: 'JetBrains Mono, monospace', fontSize: 11 }}>
            {activeRoutes} routes
          </span>
        </div>

        <div style={{
          display: 'flex', alignItems: 'center', gap: 5,
          padding: '4px 10px', borderRadius: 20,
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

        {/* Real-time Connection Status Indicator Badge */}
        <RealtimeStatusBadge />

        {/* User Role Badge */}
        <div style={{
          display: 'flex', alignItems: 'center', gap: 5,
          padding: '4px 8px', borderRadius: 6,
          background: 'var(--bg-card)', border: '1px solid var(--border-color)',
          fontSize: 10, color: 'var(--text-dim)', cursor: 'default',
        }}>
          <Shield size={11} />
          <span style={{ fontFamily: 'JetBrains Mono, monospace', fontWeight: 600 }}>
            {user?.role ?? 'DISPATCHER'}
          </span>
        </div>

        {/* User & Logout */}
        {user && (
          <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
            <span style={{ fontSize: 11, color: 'var(--text-muted)', fontWeight: 600 }}>
              {user.firstName}
            </span>
            <button
              onClick={handleLogout}
              title="Logout"
              style={{
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                width: 28, height: 28, borderRadius: 6,
                background: 'transparent', border: '1px solid var(--border-color)',
                color: 'var(--text-muted)', cursor: 'pointer',
                transition: 'all 0.15s',
              }}
            >
              <LogOut size={12} />
            </button>
          </div>
        )}
      </div>
    </header>
  );
};

export default Header;
