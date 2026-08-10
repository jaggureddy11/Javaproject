// ─── Map legend overlay ────────────────────────────────────
import React from 'react';
import { RouteResultDto } from '../types/optimization';
import { routeColor, fmtKm } from '../utils/display';

interface MapLegendProps {
  routes: RouteResultDto[];
  selectedRouteId: string | null;
  onSelectRoute: (id: string | null) => void;
}

const MapLegend: React.FC<MapLegendProps> = ({ routes, selectedRouteId, onSelectRoute }) => {
  if (routes.length === 0) return null;

  return (
    <div className="legend map-overlay-content">
      <div style={{ fontSize: 10, fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.07em', color: 'var(--text-dim)', marginBottom: 4 }}>
        Routes
      </div>
      {routes.map((route, i) => {
        const color = routeColor(i);
        const isSelected = selectedRouteId === route.routeId;
        const distKm = (route.totalDistanceMeters ?? 0) / 1000;
        return (
          <div
            key={route.routeId}
            className="legend-item"
            style={{
              cursor: 'pointer',
              opacity: selectedRouteId && !isSelected ? 0.45 : 1,
              background: isSelected ? 'rgba(59,130,246,0.08)' : 'transparent',
              borderRadius: 4,
              padding: '2px 4px',
              margin: '0 -4px',
              transition: 'all 0.15s',
            }}
            onClick={() => onSelectRoute(isSelected ? null : route.routeId)}
          >
            <div className="legend-swatch" style={{ background: color }} />
            <span style={{ flex: 1, fontSize: 11, fontWeight: isSelected ? 600 : 400, color: isSelected ? 'var(--text-main)' : 'var(--text-muted)' }}>
              {route.vehicleCode}
            </span>
            <span style={{ fontSize: 10, fontFamily: 'JetBrains Mono, monospace', color: 'var(--text-dim)' }}>
              {fmtKm(distKm)}
            </span>
            <span style={{ fontSize: 10, color: 'var(--text-dim)' }}>
              {route.stops.length}↓
            </span>
          </div>
        );
      })}
      {selectedRouteId && (
        <button
          onClick={() => onSelectRoute(null)}
          style={{
            marginTop: 4, width: '100%', fontSize: 10, color: 'var(--accent-blue)',
            background: 'transparent', border: 'none', cursor: 'pointer', textAlign: 'center',
            padding: '3px 0',
          }}
        >
          Show all routes
        </button>
      )}
    </div>
  );
};

export default MapLegend;
