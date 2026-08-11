import { apiClient } from './auth';

export interface CreateSimulationRequest {
  optimizationRunId: string;
  speedMultiplier?: number;
}

export interface SimulationVehicleStateDto {
  vehicleId: string;
  vehicleCode: string;
  driverName: string;
  routeId: string;
  status: 'AT_DEPOT' | 'EN_ROUTE' | 'ARRIVED' | 'SERVICING' | 'RETURNING' | 'COMPLETED';
  latitude: number;
  longitude: number;
  currentStopIndex: number;
  totalStops: number;
  currentOrderId?: string;
  currentOrderNumber?: string;
  currentCustomerName?: string;
  distanceTravelledKm: number;
  distanceRemainingKm: number;
  estimatedArrivalMinutes?: number;
}

export interface SimulationSessionResponse {
  simulationId: string;
  optimizationRunId: string;
  status: 'CREATED' | 'READY' | 'RUNNING' | 'PAUSED' | 'STOPPED' | 'COMPLETED' | 'FAILED';
  speedMultiplier: number;
  simulatedCurrentTimeMinutes: number;
  simulatedClockFormatted: string;
  activeVehiclesCount: number;
  totalDeliveriesCount: number;
  completedDeliveriesCount: number;
  lateDeliveriesCount: number;
  totalDistanceTravelledKm: number;
  vehicleStates: SimulationVehicleStateDto[];
  createdAt?: string;
  startedAt?: string;
  completedAt?: string;
}

export const simulationApi = {
  create: async (request: CreateSimulationRequest): Promise<SimulationSessionResponse> => {
    const res = await apiClient.post<SimulationSessionResponse>('/simulations', request);
    return res.data;
  },
  start: async (id: string): Promise<SimulationSessionResponse> => {
    const res = await apiClient.post<SimulationSessionResponse>(`/simulations/${id}/start`);
    return res.data;
  },
  pause: async (id: string): Promise<SimulationSessionResponse> => {
    const res = await apiClient.post<SimulationSessionResponse>(`/simulations/${id}/pause`);
    return res.data;
  },
  resume: async (id: string): Promise<SimulationSessionResponse> => {
    const res = await apiClient.post<SimulationSessionResponse>(`/simulations/${id}/resume`);
    return res.data;
  },
  stop: async (id: string): Promise<SimulationSessionResponse> => {
    const res = await apiClient.post<SimulationSessionResponse>(`/simulations/${id}/stop`);
    return res.data;
  },
  get: async (id: string): Promise<SimulationSessionResponse> => {
    const res = await apiClient.get<SimulationSessionResponse>(`/simulations/${id}`);
    return res.data;
  },
};
