// ─── Incidents Operations & Dynamic Recovery View ──────────────
import React, { useState, useEffect, useCallback } from 'react';
import {
  AlertTriangle, RefreshCw, X, ShieldAlert, Cpu, CheckCircle2
} from 'lucide-react';
import { incidentApi, IncidentDto, IncidentType, IncidentStatus, ImpactAnalysisResultDto, RecoveryPlanResponseDto } from '../api/incidentApi';
import { Vehicle, Order } from '../types/domain';
import { useToast } from '../context/ToastContext';

interface IncidentsViewProps {
  vehicles: Vehicle[];
  orders: Order[];
}

export const IncidentsView: React.FC<IncidentsViewProps> = ({
  vehicles,
  orders,
}) => {
  const [incidents, setIncidents] = useState<IncidentDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [typeFilter, setTypeFilter] = useState<string>('ALL');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [showReportModal, setShowReportModal] = useState(false);

  // Inspector & Recovery states
  const [selectedIncident, setSelectedIncident] = useState<IncidentDto | null>(null);
  const [impactResult, setImpactResult] = useState<ImpactAnalysisResultDto | null>(null);
  const [recoveryResult, setRecoveryResult] = useState<RecoveryPlanResponseDto | null>(null);
  const [recovering, setRecovering] = useState(false);
  const [analyzing, setAnalyzing] = useState(false);

  // Form states
  const [incidentType, setIncidentType] = useState<IncidentType>('VEHICLE_BREAKDOWN');
  const [selectedVehicleId, setSelectedVehicleId] = useState('');
  const [selectedOrderId, setSelectedOrderId] = useState('');
  const [description, setDescription] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const { addToast } = useToast();

  const loadIncidents = useCallback(async () => {
    setLoading(true);
    try {
      const type = typeFilter !== 'ALL' ? (typeFilter as IncidentType) : undefined;
      const status = statusFilter !== 'ALL' ? (statusFilter as IncidentStatus) : undefined;
      const list = await incidentApi.list(type, status);
      setIncidents(list);
    } catch {
      addToast('error', 'Failed to load incidents');
    } finally {
      setLoading(false);
    }
  }, [typeFilter, statusFilter, addToast]);

  useEffect(() => {
    loadIncidents();
  }, [loadIncidents]);

  const handleCreateIncident = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const created = await incidentApi.create({
        incidentType,
        vehicleId: selectedVehicleId || undefined,
        orderId: selectedOrderId || undefined,
        description: description.trim() || undefined,
        status: 'OPEN',
      });
      addToast('success', 'Incident reported successfully');
      setShowReportModal(false);
      setDescription('');
      loadIncidents();
      handleSelectIncident(created);
    } catch {
      addToast('error', 'Failed to report incident');
    } finally {
      setSubmitting(false);
    }
  };

  const handleSelectIncident = async (inc: IncidentDto) => {
    setSelectedIncident(inc);
    setImpactResult(null);
    setRecoveryResult(null);

    // Auto-analyze impact
    setAnalyzing(true);
    try {
      const res = await incidentApi.analyze(inc.id);
      setImpactResult(res);
    } catch {
      console.warn('Analysis failed or skipped');
    } finally {
      setAnalyzing(false);
    }
  };

  const handleExecuteRecovery = async () => {
    if (!selectedIncident) return;
    setRecovering(true);
    try {
      const res = await incidentApi.recover(selectedIncident.id);
      setRecoveryResult(res);
      addToast('success', `Recovery complete: ${res.reassignedOrdersCount} orders reassigned`);
      loadIncidents();
    } catch (e: any) {
      addToast('error', e?.response?.data?.message || 'Recovery failed');
    } finally {
      setRecovering(false);
    }
  };

  const incidentTypeBadge = (t: IncidentType) => {
    switch (t) {
      case 'VEHICLE_BREAKDOWN': return { label: 'Breakdown', bg: 'rgba(239,68,68,0.12)', color: 'var(--accent-red)' };
      case 'TRAFFIC_DELAY':     return { label: 'Traffic Delay', bg: 'rgba(245,158,11,0.12)', color: 'var(--accent-amber)' };
      case 'DRIVER_UNAVAILABLE':return { label: 'Driver Unavailable', bg: 'rgba(139,92,246,0.12)', color: 'var(--accent-purple)' };
      case 'URGENT_ORDER':      return { label: 'Urgent Order', bg: 'rgba(59,130,246,0.12)', color: 'var(--accent-blue)' };
      default:                  return { label: t, bg: 'var(--bg-panel)', color: 'var(--text-muted)' };
    }
  };

  return (
    <div style={{ padding: '20px 24px', overflowY: 'auto', height: '100%', display: 'flex', flexDirection: 'column' }}>
      
      {/* Header Bar */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16, flexWrap: 'wrap', gap: 12 }}>
        <div>
          <h2 style={{ fontSize: 20, fontWeight: 800, color: 'var(--text-main)', fontFamily: 'Outfit, sans-serif' }}>
            Disruption & Incident Management
          </h2>
          <p style={{ fontSize: 12, color: 'var(--text-muted)' }}>
            Monitor route disruptions, vehicle breakdowns, and trigger Timefold dynamic recovery
          </p>
        </div>

        <div style={{ display: 'flex', gap: 10 }}>
          <button onClick={loadIncidents} className="btn btn-ghost" style={{ padding: '6px 12px', fontSize: 12, gap: 6 }}>
            {loading ? <div className="spinner" /> : <RefreshCw size={13} />} Refresh
          </button>
          <button onClick={() => setShowReportModal(true)} className="btn btn-danger" style={{ padding: '6px 12px', fontSize: 12, gap: 6 }}>
            <AlertTriangle size={13} /> Report Incident
          </button>
        </div>
      </div>

      {/* Main Split View: Incidents Table (Left) + Recovery Inspector Drawer (Right) */}
      <div style={{ flex: 1, display: 'grid', gridTemplateColumns: selectedIncident ? '1fr 380px' : '1fr', gap: '16px', overflow: 'hidden' }}>
        
        {/* Incidents Table Container */}
        <div className="card" style={{ display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
          
          {/* Filter Strip */}
          <div style={{ padding: '12px 16px', borderBottom: '1px solid var(--border-color)', display: 'flex', gap: 16, alignItems: 'center', background: 'var(--bg-card-header)' }}>
            <span style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: 4 }}>
              Type:
            </span>
            <select
              value={typeFilter}
              onChange={(e) => setTypeFilter(e.target.value)}
              className="form-select"
              style={{ padding: '4px 8px', fontSize: 12, width: 'auto' }}
            >
              <option value="ALL">All Types</option>
              <option value="VEHICLE_BREAKDOWN">Vehicle Breakdown</option>
              <option value="DRIVER_UNAVAILABLE">Driver Unavailable</option>
              <option value="URGENT_ORDER">Urgent Order</option>
              <option value="TRAFFIC_DELAY">Traffic Delay</option>
            </select>

            <span style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: 4 }}>
              Status:
            </span>
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="form-select"
              style={{ padding: '4px 8px', fontSize: 12, width: 'auto' }}
            >
              <option value="ALL">All Statuses</option>
              <option value="OPEN">Open</option>
              <option value="RECOVERY_REQUIRED">Recovery Required</option>
              <option value="RESOLVED">Resolved</option>
              <option value="FAILED">Failed</option>
            </select>
          </div>

          {/* Table */}
          <div style={{ flex: 1, overflowY: 'auto' }}>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Type</th>
                  <th>Vehicle / Order</th>
                  <th>Description</th>
                  <th>Status</th>
                  <th>Occurred</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {incidents.length === 0 ? (
                  <tr>
                    <td colSpan={6} style={{ textAlign: 'center', padding: '32px 16px', color: 'var(--text-muted)' }}>
                      No incidents reported yet.
                    </td>
                  </tr>
                ) : (
                  incidents.map((inc) => {
                    const badge = incidentTypeBadge(inc.incidentType);
                    const isSelected = selectedIncident?.id === inc.id;
                    return (
                      <tr
                        key={inc.id}
                        onClick={() => handleSelectIncident(inc)}
                        style={{ cursor: 'pointer', background: isSelected ? 'rgba(59, 130, 246, 0.08)' : undefined }}
                      >
                        <td>
                          <span style={{ padding: '2px 8px', borderRadius: 4, fontSize: 11, fontWeight: 600, background: badge.bg, color: badge.color }}>
                            {badge.label}
                          </span>
                        </td>
                        <td style={{ fontFamily: 'JetBrains Mono, monospace', fontSize: 11 }}>
                          {inc.vehicleCode ? `🚚 ${inc.vehicleCode}` : inc.orderNumber ? `📦 ${inc.orderNumber}` : '—'}
                        </td>
                        <td style={{ fontSize: 12, color: 'var(--text-main)', maxWidth: 200, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                          {inc.description || 'N/A'}
                        </td>
                        <td>
                          <span
                            style={{
                              padding: '2px 8px',
                              borderRadius: 12,
                              fontSize: 10,
                              fontWeight: 700,
                              background: inc.status === 'RESOLVED' ? 'rgba(34, 197, 94, 0.15)' : inc.status === 'FAILED' ? 'rgba(239, 68, 68, 0.15)' : 'rgba(245, 158, 11, 0.15)',
                              color: inc.status === 'RESOLVED' ? '#22C55E' : inc.status === 'FAILED' ? '#EF4444' : '#F59E0B',
                            }}
                          >
                            ● {inc.status}
                          </span>
                        </td>
                        <td style={{ fontSize: 11, color: 'var(--text-dim)' }}>
                          {new Date(inc.occurredAt).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })}
                        </td>
                        <td>
                          <button
                            onClick={(e) => { e.stopPropagation(); handleSelectIncident(inc); }}
                            className="btn btn-ghost"
                            style={{ padding: '2px 8px', fontSize: 11 }}
                          >
                            Inspect
                          </button>
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>

        </div>

        {/* Recovery Inspector Drawer (Right) */}
        {selectedIncident && (
          <div className="card" style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: '14px', borderLeft: '2px solid #3B82F6', overflowY: 'auto' }}>
            
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ fontWeight: 700, fontSize: '0.95rem', color: 'var(--text-main)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                <ShieldAlert size={16} color="#EF4444" /> Recovery Inspector
              </span>
              <button onClick={() => setSelectedIncident(null)} className="btn btn-ghost" style={{ padding: '2px 6px' }}>
                <X size={14} />
              </button>
            </div>

            <div style={{ background: 'var(--bg-main)', padding: '10px 12px', borderRadius: '6px', fontSize: '0.8rem', display: 'flex', flexDirection: 'column', gap: '4px' }}>
              <div><strong>ID:</strong> {selectedIncident.id.substring(0, 8)}...</div>
              <div><strong>Type:</strong> {selectedIncident.incidentType}</div>
              <div><strong>Target:</strong> {selectedIncident.vehicleCode || selectedIncident.orderNumber || 'System'}</div>
              <div><strong>Status:</strong> {selectedIncident.status}</div>
            </div>

            {/* Impact Analysis Results */}
            {analyzing ? (
              <div style={{ textAlign: 'center', padding: '16px', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                Analyzing operational impact...
              </div>
            ) : impactResult && (
              <div style={{ background: 'rgba(245, 158, 11, 0.08)', border: '1px solid rgba(245, 158, 11, 0.3)', borderRadius: '8px', padding: '12px', display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <div style={{ fontSize: '0.8rem', fontWeight: 700, color: '#FACC15' }}>Impact Analysis</div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-main)' }}>
                  • Preserved Completed Deliveries: <strong>{impactResult.completedStopsCount}</strong>
                </div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-main)' }}>
                  • Affected Undelivered Orders: <strong>{impactResult.affectedOrdersCount}</strong> ({impactResult.affectedOrderNumbers.join(', ') || 'None'})
                </div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-main)' }}>
                  • Candidate Replacement Vehicles: <strong>{impactResult.candidateVehicleCodes.join(', ') || 'None'}</strong>
                </div>
              </div>
            )}

            {/* Trigger Dynamic Recovery Action Button */}
            {selectedIncident.status !== 'RESOLVED' && (
              <button
                onClick={handleExecuteRecovery}
                disabled={recovering}
                className="btn btn-primary"
                style={{ width: '100%', padding: '10px', fontSize: '0.85rem', fontWeight: 700, gap: '8px', display: 'flex', justifyContent: 'center', alignItems: 'center' }}
              >
                {recovering ? (
                  <>Re-optimizing Timefold Sub-Plan...</>
                ) : (
                  <><Cpu size={16} /> Execute Dynamic Timefold Recovery</>
                )}
              </button>
            )}

            {/* Recovery Results Card */}
            {recoveryResult && (
              <div style={{ background: 'rgba(34, 197, 94, 0.08)', border: '1px solid rgba(34, 197, 94, 0.3)', borderRadius: '8px', padding: '12px', display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <div style={{ fontSize: '0.8rem', fontWeight: 700, color: '#4ADE80', display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <CheckCircle2 size={16} /> Recovery Plan Applied
                </div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-main)' }}>
                  • Reassigned Orders: <strong>{recoveryResult.reassignedOrdersCount}</strong>
                </div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-main)' }}>
                  • Replacement Vehicles: <strong>{recoveryResult.replacementVehicleCodes.join(', ')}</strong>
                </div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-main)' }}>
                  • Timefold Solve Duration: <strong>{recoveryResult.solveTimeMs} ms</strong>
                </div>
                <div style={{ fontSize: '0.75rem', color: '#94A3B8', marginTop: '4px', fontStyle: 'italic' }}>
                  {recoveryResult.message}
                </div>
              </div>
            )}

          </div>
        )}

      </div>

      {/* Report Modal */}
      {showReportModal && (
        <div style={{ position: 'fixed', inset: 0, zIndex: 9999, background: 'rgba(0,0,0,0.6)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <div className="card" style={{ width: 440, padding: 24, background: 'var(--bg-panel)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
              <h3 style={{ fontSize: 16, fontWeight: 700, color: 'var(--text-main)' }}>Report Operational Incident</h3>
              <button onClick={() => setShowReportModal(false)} className="btn btn-ghost" style={{ padding: 4 }}><X size={16} /></button>
            </div>

            <form onSubmit={handleCreateIncident} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
              <div>
                <label className="form-label">Incident Type</label>
                <select
                  value={incidentType}
                  onChange={(e) => setIncidentType(e.target.value as IncidentType)}
                  className="form-select"
                >
                  <option value="VEHICLE_BREAKDOWN">Vehicle Breakdown</option>
                  <option value="DRIVER_UNAVAILABLE">Driver Unavailable</option>
                  <option value="URGENT_ORDER">Urgent Order</option>
                  <option value="TRAFFIC_DELAY">Traffic Delay</option>
                </select>
              </div>

              {incidentType === 'VEHICLE_BREAKDOWN' || incidentType === 'DRIVER_UNAVAILABLE' || incidentType === 'TRAFFIC_DELAY' ? (
                <div>
                  <label className="form-label">Affected Vehicle</label>
                  <select
                    value={selectedVehicleId}
                    onChange={(e) => setSelectedVehicleId(e.target.value)}
                    className="form-select"
                  >
                    <option value="">Select vehicle...</option>
                    {vehicles.map((v) => (
                      <option key={v.id} value={v.id}>{v.vehicleCode} ({v.status})</option>
                    ))}
                  </select>
                </div>
              ) : (
                <div>
                  <label className="form-label">Affected Order</label>
                  <select
                    value={selectedOrderId}
                    onChange={(e) => setSelectedOrderId(e.target.value)}
                    className="form-select"
                  >
                    <option value="">Select order...</option>
                    {orders.map((o) => (
                      <option key={o.id} value={o.id}>{o.orderNumber} – {o.customerName}</option>
                    ))}
                  </select>
                </div>
              )}

              <div>
                <label className="form-label">Description / Dispatch Notes</label>
                <textarea
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="e.g. Engine failure on I-90 West; needs urgent re-assignment"
                  className="form-input"
                  rows={3}
                />
              </div>

              <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end', marginTop: 8 }}>
                <button type="button" onClick={() => setShowReportModal(false)} className="btn btn-ghost">Cancel</button>
                <button type="submit" disabled={submitting} className="btn btn-danger">
                  {submitting ? 'Submitting...' : 'Submit Incident'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

    </div>
  );
};
