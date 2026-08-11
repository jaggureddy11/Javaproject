import React, { useState } from 'react';
import { useSimulation } from '../hooks/useSimulation';
import { OptimizationRunResponse } from '../types/optimization';
import { incidentApi } from '../api/incidentApi';
import MapView from '../components/MapView';
import { Play, Pause, Square, Clock, Truck, CheckCircle2, Activity, Gauge, Zap, AlertTriangle } from 'lucide-react';

interface SimulationViewProps {
  onSelectOrderOnMap?: (orderId: string) => void;
  optimizationResult?: OptimizationRunResponse | null;
}

export const SimulationView: React.FC<SimulationViewProps> = ({ optimizationResult }) => {
  const [customRunId, setCustomRunId] = useState<string>(optimizationResult?.optimizationRunId || '');
  const [breakdownSimulating, setBreakdownSimulating] = useState(false);
  const [recoveryAlert, setRecoveryAlert] = useState<string | null>(null);

  const {
    session,
    loading: simLoading,
    isConnected,
    activityLogs,
    speedMultiplier,
    setSpeedMultiplier,
    createSession,
    start,
    pause,
    resume,
    stop,
  } = useSimulation();

  const runIdToUse = customRunId || optimizationResult?.optimizationRunId || '';

  const handleCreateSession = async () => {
    if (!runIdToUse) return;
    try {
      await createSession(runIdToUse, speedMultiplier);
    } catch (e) {
      console.error('Failed to create simulation session:', e);
    }
  };

  const handleSimulateBreakdown = async () => {
    if (!session || !session.vehicleStates || session.vehicleStates.length === 0) return;
    setBreakdownSimulating(true);
    try {
      const targetVeh = session.vehicleStates[0];
      const created = await incidentApi.create({
        incidentType: 'VEHICLE_BREAKDOWN',
        vehicleId: targetVeh.vehicleId,
        description: `Simulated engine breakdown for vehicle ${targetVeh.vehicleCode} at ${session.simulatedClockFormatted}`,
      });

      const recovery = await incidentApi.recover(created.id);
      setRecoveryAlert(`🚨 Breakdown on ${targetVeh.vehicleCode}: ${recovery.reassignedOrdersCount} orders reassigned to [${recovery.replacementVehicleCodes.join(', ')}] in ${recovery.solveTimeMs} ms.`);
    } catch (e: any) {
      console.error('Simulated breakdown failed:', e);
    } finally {
      setBreakdownSimulating(false);
    }
  };

  const isRunning = session?.status === 'RUNNING';
  const isPaused = session?.status === 'PAUSED';
  const isCreated = session?.status === 'CREATED';
  const isCompleted = session?.status === 'COMPLETED';

  // Calculate live completion metrics
  const completedDeliveries = session?.completedDeliveriesCount || 0;
  const totalDeliveries = session?.totalDeliveriesCount || 0;
  const lateDeliveries = session?.lateDeliveriesCount || 0;
  const onTimeRate = totalDeliveries > 0 ? Math.round(((totalDeliveries - lateDeliveries) / totalDeliveries) * 100) : 100;
  const distKm = session?.totalDistanceTravelledKm ? session.totalDistanceTravelledKm.toFixed(1) : '0.0';

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: 'calc(100vh - 64px)', background: '#0F172A', color: '#F8FAFC' }}>
      
      {/* Top Simulation Control Panel & KPI Bar */}
      <div style={{ padding: '16px 24px', background: '#1E293B', borderBottom: '1px solid #334155', display: 'flex', flexDirection: 'column', gap: '12px' }}>
        
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px' }}>
          
          {/* Left: Setup & Run Selection */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <div style={{ padding: '8px 12px', background: 'rgba(59, 130, 246, 0.1)', border: '1px solid #3B82F6', borderRadius: '8px', color: '#60A5FA', display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.875rem', fontWeight: 600 }}>
              <Zap size={16} /> Real-Time Delivery Simulator
            </div>

            {!session ? (
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <input
                  type="text"
                  placeholder="Enter Optimization Run UUID..."
                  value={customRunId}
                  onChange={(e) => setCustomRunId(e.target.value)}
                  style={{ background: '#0F172A', color: '#F8FAFC', border: '1px solid #475569', borderRadius: '6px', padding: '6px 12px', fontSize: '0.85rem', width: '280px' }}
                />

                <button
                  onClick={handleCreateSession}
                  disabled={!runIdToUse || simLoading}
                  style={{
                    background: '#3B82F6',
                    color: '#FFF',
                    border: 'none',
                    borderRadius: '6px',
                    padding: '6px 16px',
                    fontWeight: 600,
                    cursor: runIdToUse ? 'pointer' : 'not-allowed',
                    opacity: runIdToUse ? 1 : 0.6,
                    fontSize: '0.85rem',
                  }}
                >
                  {simLoading ? 'Initializing...' : 'Initialize Simulation'}
                </button>
              </div>
            ) : (
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <span style={{ fontSize: '0.85rem', color: '#94A3B8' }}>Session:</span>
                <span style={{ fontFamily: 'monospace', background: '#0F172A', padding: '4px 8px', borderRadius: '4px', border: '1px solid #334155', fontSize: '0.8rem', color: '#38BDF8' }}>
                  {session.simulationId.substring(0, 8)}...
                </span>
                <span
                  style={{
                    padding: '4px 10px',
                    borderRadius: '9999px',
                    fontSize: '0.75rem',
                    fontWeight: 700,
                    background: isRunning ? 'rgba(34, 197, 94, 0.2)' : isPaused ? 'rgba(234, 179, 8, 0.2)' : 'rgba(148, 163, 184, 0.2)',
                    color: isRunning ? '#4ADE80' : isPaused ? '#FACC15' : '#94A3B8',
                    border: `1px solid ${isRunning ? '#22C55E' : isPaused ? '#EAB308' : '#64748B'}`,
                  }}
                >
                  ● {session.status}
                </span>
              </div>
            )}
          </div>

          {/* Right: Simulation Controls & Speed Selector */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            
            {/* Speed Selector */}
            <div style={{ display: 'flex', alignItems: 'center', background: '#0F172A', border: '1px solid #334155', borderRadius: '6px', padding: '2px' }}>
              <span style={{ padding: '4px 8px', fontSize: '0.75rem', color: '#64748B', fontWeight: 600 }}>SPEED</span>
              {[1, 2, 5].map((speed) => (
                <button
                  key={speed}
                  onClick={() => setSpeedMultiplier(speed)}
                  style={{
                    background: speedMultiplier === speed ? '#3B82F6' : 'transparent',
                    color: speedMultiplier === speed ? '#FFF' : '#94A3B8',
                    border: 'none',
                    borderRadius: '4px',
                    padding: '4px 10px',
                    fontSize: '0.75rem',
                    fontWeight: 700,
                    cursor: 'pointer',
                  }}
                >
                  {speed}x
                </button>
              ))}
            </div>

            {/* Execution Buttons */}
            {session && (
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                {(isCreated || isPaused) && (
                  <button
                    onClick={isCreated ? start : resume}
                    style={{
                      background: '#10B981',
                      color: '#FFF',
                      border: 'none',
                      borderRadius: '6px',
                      padding: '6px 14px',
                      fontWeight: 600,
                      cursor: 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '6px',
                      fontSize: '0.85rem',
                    }}
                  >
                    <Play size={14} fill="#FFF" /> {isCreated ? 'Start' : 'Resume'}
                  </button>
                )}

                {isRunning && (
                  <button
                    onClick={pause}
                    style={{
                      background: '#F59E0B',
                      color: '#FFF',
                      border: 'none',
                      borderRadius: '6px',
                      padding: '6px 14px',
                      fontWeight: 600,
                      cursor: 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '6px',
                      fontSize: '0.85rem',
                    }}
                  >
                    <Pause size={14} fill="#FFF" /> Pause
                  </button>
                )}

                {(isRunning || isPaused) && (
                  <>
                    <button
                      onClick={stop}
                      style={{
                        background: '#EF4444',
                        color: '#FFF',
                        border: 'none',
                        borderRadius: '6px',
                        padding: '6px 14px',
                        fontWeight: 600,
                        cursor: 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '6px',
                        fontSize: '0.85rem',
                      }}
                    >
                      <Square size={14} fill="#FFF" /> Stop
                    </button>

                    <button
                      onClick={handleSimulateBreakdown}
                      disabled={breakdownSimulating}
                      style={{
                        background: 'rgba(239, 68, 68, 0.15)',
                        color: '#FCA5A5',
                        border: '1px solid #EF4444',
                        borderRadius: '6px',
                        padding: '6px 14px',
                        fontWeight: 700,
                        cursor: 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '6px',
                        fontSize: '0.85rem',
                      }}
                    >
                      <AlertTriangle size={14} color="#EF4444" /> {breakdownSimulating ? 'Recovering...' : 'Simulate Breakdown'}
                    </button>
                  </>
                )}
              </div>
            )}
          </div>
        </div>

        {/* Recovery Alert Banner */}
        {recoveryAlert && (
          <div
            style={{
              background: 'rgba(239, 68, 68, 0.15)',
              border: '1px solid #EF4444',
              borderRadius: '6px',
              padding: '8px 12px',
              fontSize: '0.8rem',
              color: '#FCA5A5',
              fontWeight: 600,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
            }}
          >
            <span>{recoveryAlert}</span>
            <button
              onClick={() => setRecoveryAlert(null)}
              style={{ background: 'transparent', border: 'none', color: '#FCA5A5', cursor: 'pointer', fontWeight: 700 }}
            >
              ✕
            </button>
          </div>
        )}

        {/* KPI Strip */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))', gap: '12px' }}>
          
          <div style={{ background: '#0F172A', padding: '10px 14px', borderRadius: '8px', border: '1px solid #334155' }}>
            <div style={{ fontSize: '0.75rem', color: '#94A3B8', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Clock size={14} color="#38BDF8" /> SIMULATION TIME
            </div>
            <div style={{ fontSize: '1.25rem', fontWeight: 800, color: '#38BDF8', marginTop: '2px', fontFamily: 'monospace' }}>
              {session?.simulatedClockFormatted || '08:00 AM'}
            </div>
          </div>

          <div style={{ background: '#0F172A', padding: '10px 14px', borderRadius: '8px', border: '1px solid #334155' }}>
            <div style={{ fontSize: '0.75rem', color: '#94A3B8', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Truck size={14} color="#60A5FA" /> VEHICLES
            </div>
            <div style={{ fontSize: '1.25rem', fontWeight: 800, color: '#F8FAFC', marginTop: '2px' }}>
              {session?.activeVehiclesCount || 0} Dispatched
            </div>
          </div>

          <div style={{ background: '#0F172A', padding: '10px 14px', borderRadius: '8px', border: '1px solid #334155' }}>
            <div style={{ fontSize: '0.75rem', color: '#94A3B8', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <CheckCircle2 size={14} color="#4ADE80" /> DELIVERIES
            </div>
            <div style={{ fontSize: '1.25rem', fontWeight: 800, color: '#4ADE80', marginTop: '2px' }}>
              {completedDeliveries} / {totalDeliveries}
            </div>
          </div>

          <div style={{ background: '#0F172A', padding: '10px 14px', borderRadius: '8px', border: '1px solid #334155' }}>
            <div style={{ fontSize: '0.75rem', color: '#94A3B8', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Gauge size={14} color="#FACC15" /> ON-TIME RATE
            </div>
            <div style={{ fontSize: '1.25rem', fontWeight: 800, color: '#FACC15', marginTop: '2px' }}>
              {onTimeRate}%
            </div>
          </div>

          <div style={{ background: '#0F172A', padding: '10px 14px', borderRadius: '8px', border: '1px solid #334155' }}>
            <div style={{ fontSize: '0.75rem', color: '#94A3B8', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Activity size={14} color="#A78BFA" /> DISTANCE
            </div>
            <div style={{ fontSize: '1.25rem', fontWeight: 800, color: '#A78BFA', marginTop: '2px' }}>
              {distKm} km
            </div>
          </div>

        </div>

      </div>

      {/* Main Content Area: GIS Map (Left/Center) + Activity Feed Ticker (Right) */}
      <div style={{ flex: 1, display: 'flex', overflow: 'hidden' }}>
        
        {/* Interactive Simulation Map */}
        <div style={{ flex: 1, position: 'relative' }}>
          <MapView
            vehiclePositions={session?.vehicleStates?.map((v) => ({
              vehicleId: v.vehicleId,
              vehicleCode: v.vehicleCode,
              latitude: v.latitude,
              longitude: v.longitude,
              status: v.status,
              driverName: v.driverName,
              currentStopIndex: v.currentStopIndex,
              totalStops: v.totalStops,
              currentOrderNumber: v.currentOrderNumber,
              currentCustomerName: v.currentCustomerName,
            }))}
          />

          {/* Simulation Geometry Overlay Badge */}
          <div
            style={{
              position: 'absolute',
              bottom: '16px',
              right: '16px',
              zIndex: 1000,
              background: 'rgba(15, 23, 42, 0.9)',
              border: '1px solid #334155',
              padding: '6px 12px',
              borderRadius: '6px',
              fontSize: '0.75rem',
              color: '#94A3B8',
              backdropFilter: 'blur(4px)',
            }}
          >
            ● Simulation: Estimated Haversine linear routing geometry
          </div>
        </div>

        {/* Activity Feed Ticker Panel */}
        <div style={{ width: '340px', background: '#1E293B', borderLeft: '1px solid #334155', display: 'flex', flexDirection: 'column' }}>
          
          <div style={{ padding: '14px 16px', borderBottom: '1px solid #334155', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <span style={{ fontWeight: 700, fontSize: '0.9rem', color: '#F8FAFC', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Activity size={16} color="#38BDF8" /> Live Event Feed
            </span>
            <span style={{ fontSize: '0.75rem', color: isConnected ? '#4ADE80' : '#F87171' }}>
              {isConnected ? '● STOMP Live' : '○ Polling'}
            </span>
          </div>

          <div style={{ flex: 1, overflowY: 'auto', padding: '12px', display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {activityLogs.length === 0 ? (
              <div style={{ padding: '24px 12px', textAlign: 'center', color: '#64748B', fontSize: '0.85rem' }}>
                No events logged yet. Initialize and start simulation to view live events.
              </div>
            ) : (
              activityLogs.map((item) => (
                <div
                  key={item.id}
                  style={{
                    background: '#0F172A',
                    padding: '10px 12px',
                    borderRadius: '6px',
                    borderLeft: `3px solid ${
                      item.eventType === 'ORDER_DELIVERED' ? '#10B981' : item.eventType === 'VEHICLE_ARRIVED' ? '#3B82F6' : item.eventType === 'SIMULATION_COMPLETED' ? '#8B5CF6' : '#64748B'
                    }`,
                    fontSize: '0.8rem',
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '4px' }}>
                    <span style={{ fontFamily: 'monospace', fontWeight: 700, color: '#38BDF8', fontSize: '0.75rem' }}>
                      {item.timestampFormatted}
                    </span>
                    <span style={{ fontSize: '0.7rem', color: '#94A3B8', textTransform: 'uppercase', fontWeight: 600 }}>
                      {item.eventType}
                    </span>
                  </div>
                  <div style={{ color: '#E2E8F0', lineHeight: 1.3 }}>{item.message}</div>
                </div>
              ))
            )}
          </div>

        </div>

      </div>

      {/* Completion Summary Modal Overlay */}
      {isCompleted && (
        <div style={{ position: 'fixed', inset: 0, zIndex: 9999, background: 'rgba(0, 0, 0, 0.75)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <div style={{ background: '#1E293B', border: '1px solid #475569', borderRadius: '12px', padding: '28px', maxWidth: '440px', width: '100%', textAlign: 'center' }}>
            <CheckCircle2 size={48} color="#10B981" style={{ margin: '0 auto 16px' }} />
            <h2 style={{ fontSize: '1.4rem', fontWeight: 800, color: '#F8FAFC', marginBottom: '8px' }}>Simulation Completed!</h2>
            <p style={{ fontSize: '0.875rem', color: '#94A3B8', marginBottom: '20px' }}>
              All dispatched vehicles have successfully delivered customer orders and returned to depot.
            </p>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', marginBottom: '24px', textAlign: 'left' }}>
              <div style={{ background: '#0F172A', padding: '12px', borderRadius: '8px', border: '1px solid #334155' }}>
                <div style={{ fontSize: '0.75rem', color: '#94A3B8' }}>Deliveries Completed</div>
                <div style={{ fontSize: '1.1rem', fontWeight: 700, color: '#10B981' }}>{completedDeliveries} / {totalDeliveries}</div>
              </div>
              <div style={{ background: '#0F172A', padding: '12px', borderRadius: '8px', border: '1px solid #334155' }}>
                <div style={{ fontSize: '0.75rem', color: '#94A3B8' }}>On-Time Rate</div>
                <div style={{ fontSize: '1.1rem', fontWeight: 700, color: '#FACC15' }}>{onTimeRate}%</div>
              </div>
            </div>

            <button
              onClick={() => window.location.reload()}
              style={{ background: '#3B82F6', color: '#FFF', border: 'none', borderRadius: '8px', padding: '10px 24px', fontWeight: 600, cursor: 'pointer', width: '100%' }}
            >
              Close & Return to Dashboard
            </button>
          </div>
        </div>
      )}

    </div>
  );
};
