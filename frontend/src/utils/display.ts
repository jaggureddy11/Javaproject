// ─── Route colours (8 colours cycling) ───────────────────
export const ROUTE_COLORS = [
  '#3b82f6', // blue
  '#14b8a6', // teal
  '#a855f7', // purple
  '#f59e0b', // amber
  '#ec4899', // pink
  '#0891b2', // deeper cyan (matches --route-5)
  '#65a30d', // olive-green (matches --route-6)
  '#f97316', // orange
];

export const routeColor = (index: number) =>
  ROUTE_COLORS[index % ROUTE_COLORS.length];

// ─── Format minutes since midnight ───────────────────────
export const fmtMinutes = (mins: number): string => {
  const h = Math.floor(mins / 60) % 24;
  const m = mins % 60;
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
};

// ─── Format distance ──────────────────────────────────────
export const fmtKm = (km: number): string => {
  if (km < 1) return `${Math.round(km * 1000)} m`;
  return `${km.toFixed(1)} km`;
};

// ─── Format duration ─────────────────────────────────────
export const fmtDuration = (mins: number): string => {
  if (mins < 60) return `${Math.round(mins)}m`;
  const h = Math.floor(mins / 60);
  const m = Math.round(mins % 60);
  return m > 0 ? `${h}h ${m}m` : `${h}h`;
};

// ─── Improvement colour class ─────────────────────────────
export const improvementClass = (pct: number): string => {
  if (pct > 1) return 'improvement-positive';
  if (pct < -1) return 'improvement-negative';
  return 'improvement-neutral';
};

// ─── Status colours ───────────────────────────────────────
export const orderStatusColor = (status: string): string => {
  switch (status) {
    case 'DELIVERED': return 'var(--accent-green)';
    case 'IN_TRANSIT': return 'var(--accent-blue)';
    case 'ASSIGNED':  return 'var(--accent-indigo)';
    case 'FAILED':    return 'var(--accent-red)';
    case 'CANCELLED': return 'var(--text-dim)';
    default:          return 'var(--accent-amber)'; // UNASSIGNED
  }
};

export const priorityColor = (p: number): string => {
  if (p >= 5) return '#ef4444';
  if (p >= 4) return '#f97316';
  if (p >= 3) return '#f59e0b';
  if (p >= 2) return '#3b82f6';
  return '#6b84a3';
};
