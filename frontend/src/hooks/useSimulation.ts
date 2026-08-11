import { useState, useCallback, useEffect } from 'react';
import { simulationApi, SimulationSessionResponse, SimulationVehicleStateDto } from '../api/simulationApi';
import { useRealtime } from '../context/RealtimeContext';

export interface ActivityLogItem {
  id: string;
  timestampFormatted: string;
  eventType: string;
  message: string;
}

export function useSimulation() {
  const [session, setSession] = useState<SimulationSessionResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [activityLogs, setActivityLogs] = useState<ActivityLogItem[]>([]);
  const [speedMultiplier, setSpeedMultiplier] = useState<number>(5);

  const activeSimulationId = session?.simulationId;

  // Handler for incoming WebSocket events over STOMP topic /topic/simulation/{simulationId}
  const handleWebSocketMessage = useCallback((event: any) => {
    if (!event || !event.eventType) return;

    const timeStr = event.simulatedClockFormatted || '08:00';

    if (event.eventType === 'VEHICLE_POSITION_UPDATED' && Array.isArray(event.payload)) {
      const updatedVehicles: SimulationVehicleStateDto[] = event.payload;
      setSession((prev) => {
        if (!prev) return prev;
        const completedCount = updatedVehicles.reduce((acc, v) => acc + (v.status === 'COMPLETED' ? v.totalStops : v.currentStopIndex - 1), 0);
        return {
          ...prev,
          simulatedCurrentTimeMinutes: event.simulatedCurrentTimeMinutes,
          simulatedClockFormatted: timeStr,
          vehicleStates: updatedVehicles,
          completedDeliveriesCount: Math.min(completedCount, prev.totalDeliveriesCount),
          totalDistanceTravelledKm: updatedVehicles.reduce((acc, v) => acc + (v.distanceTravelledKm || 0), 0),
        };
      });
    } else if (event.eventType === 'ORDER_DELIVERED' && event.payload) {
      const p = event.payload;
      const logText = `Order ${p.orderNumber} delivered to ${p.customerName} by ${p.vehicleCode}`;
      setActivityLogs((prev) => [
        { id: Math.random().toString(), timestampFormatted: timeStr, eventType: 'ORDER_DELIVERED', message: logText },
        ...prev.slice(0, 49),
      ]);
    } else if (typeof event.payload === 'string') {
      setActivityLogs((prev) => [
        { id: Math.random().toString(), timestampFormatted: timeStr, eventType: event.eventType, message: event.payload },
        ...prev.slice(0, 49),
      ]);
      if (event.eventType === 'SIMULATION_COMPLETED') {
        setSession((prev) => prev ? { ...prev, status: 'COMPLETED' } : prev);
      } else if (event.eventType === 'SIMULATION_PAUSED') {
        setSession((prev) => prev ? { ...prev, status: 'PAUSED' } : prev);
      } else if (event.eventType === 'SIMULATION_RESUMED' || event.eventType === 'SIMULATION_STARTED') {
        setSession((prev) => prev ? { ...prev, status: 'RUNNING' } : prev);
      }
    }
  }, []);

  const stompTopic = activeSimulationId ? `/topic/simulation/${activeSimulationId}` : null;
  const { subscribe, connectionState } = useRealtime();
  const isConnected = connectionState === 'CONNECTED';

  useEffect(() => {
    if (!stompTopic) return;
    const unsub = subscribe(stompTopic, (data) => {
      handleWebSocketMessage(data);
    });
    return () => unsub();
  }, [stompTopic, subscribe, handleWebSocketMessage]);

  const createSession = useCallback(async (runId: string, speed: number = 5) => {
    setLoading(true);
    setError(null);
    try {
      const data = await simulationApi.create({ optimizationRunId: runId, speedMultiplier: speed });
      setSession(data);
      setActivityLogs([{
        id: Math.random().toString(),
        timestampFormatted: data.simulatedClockFormatted,
        eventType: 'CREATED',
        message: `Simulation session initialized for optimization run ${runId.substring(0, 8)}...`,
      }]);
      return data;
    } catch (err: any) {
      const msg = err?.response?.data?.message || 'Failed to create simulation session';
      setError(msg);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const start = useCallback(async () => {
    if (!activeSimulationId) return;
    try {
      const data = await simulationApi.start(activeSimulationId);
      setSession(data);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to start simulation');
    }
  }, [activeSimulationId]);

  const pause = useCallback(async () => {
    if (!activeSimulationId) return;
    try {
      const data = await simulationApi.pause(activeSimulationId);
      setSession(data);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to pause simulation');
    }
  }, [activeSimulationId]);

  const resume = useCallback(async () => {
    if (!activeSimulationId) return;
    try {
      const data = await simulationApi.resume(activeSimulationId);
      setSession(data);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to resume simulation');
    }
  }, [activeSimulationId]);

  const stop = useCallback(async () => {
    if (!activeSimulationId) return;
    try {
      const data = await simulationApi.stop(activeSimulationId);
      setSession(data);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to stop simulation');
    }
  }, [activeSimulationId]);

  return {
    session,
    loading,
    error,
    isConnected,
    activityLogs,
    speedMultiplier,
    setSpeedMultiplier,
    createSession,
    start,
    pause,
    resume,
    stop,
  };
}
