// ─── Dedicated Fleet Operations View ────────────────────────
import React, { useState, useEffect } from 'react';
import { Truck, UserCheck, Warehouse, RefreshCw } from 'lucide-react';
import { Vehicle, Driver, Depot } from '../types/domain';
import { driverApi } from '../api/driverApi';
import { fmtMinutes } from '../utils/display';
import { useToast } from '../context/ToastContext';

interface FleetViewProps {
  vehicles: Vehicle[];
  depots: Depot[];
  onReloadFleet: () => void;
}

type FleetSubTab = 'vehicles' | 'drivers' | 'depots';

export const FleetView: React.FC<FleetViewProps> = ({
  vehicles,
  depots,
  onReloadFleet,
}) => {
  const [subTab, setSubTab] = useState<FleetSubTab>('vehicles');
  const [drivers, setDrivers] = useState<Driver[]>([]);
  const [loadingDrivers, setLoadingDrivers] = useState(false);
  const { addToast } = useToast();

  useEffect(() => {
    setLoadingDrivers(true);
    driverApi.getAll({ size: 100 })
      .then(res => setDrivers(res.content))
      .catch(() => addToast('error', 'Failed to load drivers'))
      .finally(() => setLoadingDrivers(false));
  }, [addToast]);

  return (
    <div style={{ padding: '20px 24px', overflowY: 'auto', height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* Header Bar */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16, flexWrap: 'wrap', gap: 12 }}>
        <div>
          <h2 style={{ fontSize: 20, fontWeight: 800, color: 'var(--text-main)', fontFamily: 'Outfit, sans-serif' }}>
            Fleet & Asset Operations
          </h2>
          <p style={{ fontSize: 12, color: 'var(--text-muted)' }}>
            Monitor vehicles, driver shift schedules, and regional hub assets
          </p>
        </div>

        <button
          onClick={onReloadFleet}
          className="btn btn-ghost"
          style={{ padding: '6px 12px', fontSize: 12, gap: 6 }}
        >
          <RefreshCw size={13} /> Refresh Fleet
        </button>
      </div>

      {/* Sub Tabs */}
      <div style={{ display: 'flex', gap: 8, marginBottom: 16, borderBottom: '1px solid var(--border-color)' }}>
        <button
          onClick={() => setSubTab('vehicles')}
          className={`sidebar-tab ${subTab === 'vehicles' ? 'active' : ''}`}
          style={{ flex: 'none', padding: '8px 16px', flexDirection: 'row', gap: 6, fontSize: 12 }}
        >
          <Truck size={14} /> Vehicles ({vehicles.length})
        </button>

        <button
          onClick={() => setSubTab('drivers')}
          className={`sidebar-tab ${subTab === 'drivers' ? 'active' : ''}`}
          style={{ flex: 'none', padding: '8px 16px', flexDirection: 'row', gap: 6, fontSize: 12 }}
        >
          <UserCheck size={14} /> Drivers ({drivers.length})
        </button>

        <button
          onClick={() => setSubTab('depots')}
          className={`sidebar-tab ${subTab === 'depots' ? 'active' : ''}`}
          style={{ flex: 'none', padding: '8px 16px', flexDirection: 'row', gap: 6, fontSize: 12 }}
        >
          <Warehouse size={14} /> Depots ({depots.length})
        </button>
      </div>

      {/* Sub Tab 1: Vehicles */}
      {subTab === 'vehicles' && (
        <div className="card" style={{ flex: 1, overflow: 'hidden' }}>
          <div style={{ overflowX: 'auto', height: '100%' }}>
            <table className="bench-table" style={{ width: '100%' }}>
              <thead>
                <tr>
                  <th>Vehicle Code</th>
                  <th>Status</th>
                  <th>Assigned Driver</th>
                  <th>Home Depot</th>
                  <th>Max Weight</th>
                  <th>Max Volume</th>
                  <th>Location</th>
                </tr>
              </thead>
              <tbody>
                {vehicles.length === 0 ? (
                  <tr>
                    <td colSpan={7} style={{ textAlign: 'center', padding: '32px 16px', color: 'var(--text-dim)' }}>
                      No vehicles found
                    </td>
                  </tr>
                ) : (
                  vehicles.map(v => {
                    const statusColor = v.status === 'EN_ROUTE' ? 'var(--accent-blue)'
                      : v.status === 'IDLE' ? 'var(--accent-green)'
                      : 'var(--text-dim)';

                    return (
                      <tr key={v.id}>
                        <td style={{ fontFamily: 'JetBrains Mono, monospace', fontWeight: 600, color: 'var(--text-main)' }}>
                          {v.vehicleCode}
                        </td>
                        <td>
                          <span className="status-pill" style={{ background: `${statusColor}18`, color: statusColor }}>
                            <span className="status-dot" style={{ background: statusColor }} />
                            {v.status}
                          </span>
                        </td>
                        <td style={{ color: 'var(--text-main)', fontWeight: 500 }}>
                          {v.driverName ?? 'Unassigned'}
                        </td>
                        <td style={{ color: 'var(--text-muted)' }}>
                          {v.depotName ?? 'Central Depot'}
                        </td>
                        <td style={{ fontFamily: 'JetBrains Mono, monospace' }}>
                          {v.maxWeightKg} kg
                        </td>
                        <td style={{ fontFamily: 'JetBrains Mono, monospace' }}>
                          {v.maxVolumeM3} m³
                        </td>
                        <td style={{ fontFamily: 'JetBrains Mono, monospace', color: 'var(--text-dim)', fontSize: 10 }}>
                          {v.currentLocation
                            ? `${v.currentLocation.latitude.toFixed(4)}, ${v.currentLocation.longitude.toFixed(4)}`
                            : 'Depot'}
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Sub Tab 2: Drivers */}
      {subTab === 'drivers' && (
        <div className="card" style={{ flex: 1, overflow: 'hidden' }}>
          <div style={{ overflowX: 'auto', height: '100%' }}>
            <table className="bench-table" style={{ width: '100%' }}>
              <thead>
                <tr>
                  <th>Driver Name</th>
                  <th>License Number</th>
                  <th>Phone</th>
                  <th>Shift Schedule</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {loadingDrivers ? (
                  <tr>
                    <td colSpan={5} style={{ textAlign: 'center', padding: '32px 16px', color: 'var(--text-dim)' }}>
                      Loading drivers...
                    </td>
                  </tr>
                ) : drivers.length === 0 ? (
                  <tr>
                    <td colSpan={5} style={{ textAlign: 'center', padding: '32px 16px', color: 'var(--text-dim)' }}>
                      No drivers registered
                    </td>
                  </tr>
                ) : (
                  drivers.map(d => (
                    <tr key={d.id}>
                      <td style={{ fontWeight: 600, color: 'var(--text-main)' }}>
                        {d.name}
                      </td>
                      <td style={{ fontFamily: 'JetBrains Mono, monospace', color: 'var(--text-muted)' }}>
                        {d.licenseNumber}
                      </td>
                      <td style={{ fontFamily: 'JetBrains Mono, monospace', color: 'var(--text-muted)' }}>
                        {d.phone ?? 'N/A'}
                      </td>
                      <td style={{ fontFamily: 'JetBrains Mono, monospace' }}>
                        {fmtMinutes(d.shiftStartMinutes)} – {fmtMinutes(d.shiftEndMinutes)}
                      </td>
                      <td>
                        <span className="status-pill" style={{
                          background: d.status === 'ACTIVE' ? 'rgba(16,185,129,0.12)' : 'rgba(239,68,68,0.12)',
                          color: d.status === 'ACTIVE' ? 'var(--accent-green)' : 'var(--accent-red)',
                        }}>
                          {d.status}
                        </span>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Sub Tab 3: Depots */}
      {subTab === 'depots' && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: 14 }}>
          {depots.map(depot => (
            <div key={depot.id} className="card" style={{ padding: '16px 18px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
                <Warehouse size={18} style={{ color: 'var(--accent-blue)' }} />
                <h3 style={{ fontSize: 15, fontWeight: 700, color: 'var(--text-main)', fontFamily: 'Outfit, sans-serif' }}>
                  {depot.name}
                </h3>
              </div>

              <div style={{ fontSize: 12, color: 'var(--text-muted)', marginBottom: 12 }}>
                {depot.addressText}
              </div>

              <div style={{
                display: 'flex', justifyContent: 'space-between', padding: '8px 10px',
                background: 'var(--bg-panel)', borderRadius: 6, border: '1px solid var(--border-color)',
                fontSize: 11, fontFamily: 'JetBrains Mono, monospace', color: 'var(--text-dim)',
              }}>
                <span>Coordinates</span>
                <span>{depot.location.latitude.toFixed(4)}, {depot.location.longitude.toFixed(4)}</span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
