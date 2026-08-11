export type RealtimeEventType =
  | 'OPTIMIZATION_STARTED'
  | 'OPTIMIZATION_PROGRESS'
  | 'OPTIMIZATION_COMPLETED'
  | 'OPTIMIZATION_FAILED'
  | 'SIMULATION_STARTED'
  | 'SIMULATION_PAUSED'
  | 'SIMULATION_RESUMED'
  | 'SIMULATION_STOPPED'
  | 'SIMULATION_COMPLETED'
  | 'VEHICLE_POSITION_UPDATED'
  | 'VEHICLE_STATUS_CHANGED'
  | 'ROUTE_UPDATED'
  | 'ROUTE_REPLANNED'
  | 'ORDER_STATUS_CHANGED'
  | 'ORDER_DELIVERED'
  | 'ORDER_REASSIGNED'
  | 'INCIDENT_CREATED'
  | 'INCIDENT_ANALYZED'
  | 'RECOVERY_STARTED'
  | 'RECOVERY_COMPLETED'
  | 'RECOVERY_FAILED';

export type RealtimeConnectionState = 'CONNECTING' | 'CONNECTED' | 'DISCONNECTED' | 'RECONNECTING' | 'ERROR';

export interface RealtimeEvent<T = any> {
  eventId: string;
  eventType: RealtimeEventType;
  entityType?: string;
  entityId?: string;
  occurredAt?: string;
  sequence?: number;
  simulationId?: string;
  incidentId?: string;
  optimizationRunId?: string;
  payload?: T;
}
