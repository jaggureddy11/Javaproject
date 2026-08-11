import { apiClient } from './auth';

export type IncidentType = 'VEHICLE_BREAKDOWN' | 'TRAFFIC_DELAY' | 'DRIVER_UNAVAILABLE' | 'URGENT_ORDER' | 'ORDER_CANCELLED' | 'DEADLINE_CHANGED';
export type IncidentStatus = 'OPEN' | 'ANALYZING' | 'RECOVERY_REQUIRED' | 'RECOVERING' | 'RESOLVING' | 'RESOLVED' | 'CLOSED' | 'FAILED';

export interface IncidentDto {
  id: string;
  incidentType: IncidentType;
  status: IncidentStatus;
  vehicleId?: string;
  vehicleCode?: string;
  orderId?: string;
  orderNumber?: string;
  description?: string;
  occurredAt: string;
  createdAt?: string;
}

export interface CreateIncidentRequestDto {
  incidentType: IncidentType;
  status?: IncidentStatus;
  vehicleId?: string;
  orderId?: string;
  description?: string;
}

export interface ImpactAnalysisResultDto {
  incidentId: string;
  brokenVehicleId?: string;
  brokenVehicleCode?: string;
  affectedRouteId?: string;
  completedStopsCount: number;
  affectedOrdersCount: number;
  affectedOrderIds: string[];
  affectedOrderNumbers: string[];
  candidateVehicleIds: string[];
  candidateVehicleCodes: string[];
  recoveryFeasible: boolean;
  message: string;
}

export interface RecoveryPlanResponseDto {
  incidentId: string;
  status: IncidentStatus;
  originalRouteId?: string;
  replacementRouteIds: string[];
  replacementVehicleCodes: string[];
  affectedOrdersCount: number;
  reassignedOrdersCount: number;
  solveTimeMs: number;
  totalDistanceChangeKm: number;
  feasible: boolean;
  message: string;
}

export const incidentApi = {
  async list(type?: IncidentType, status?: IncidentStatus): Promise<IncidentDto[]> {
    const response = await apiClient.get<IncidentDto[]>('/incidents', {
      params: { type, status },
    });
    return response.data;
  },

  async create(data: CreateIncidentRequestDto): Promise<IncidentDto> {
    const response = await apiClient.post<IncidentDto>('/incidents', data);
    return response.data;
  },

  async get(id: string): Promise<IncidentDto> {
    const response = await apiClient.get<IncidentDto>(`/incidents/${id}`);
    return response.data;
  },

  async analyze(id: string): Promise<ImpactAnalysisResultDto> {
    const response = await apiClient.post<ImpactAnalysisResultDto>(`/incidents/${id}/analyze`);
    return response.data;
  },

  async recover(id: string): Promise<RecoveryPlanResponseDto> {
    const response = await apiClient.post<RecoveryPlanResponseDto>(`/incidents/${id}/recover`);
    return response.data;
  },
};
