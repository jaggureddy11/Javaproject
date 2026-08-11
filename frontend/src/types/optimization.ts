import { RouteStatus } from './domain';

export type SolverStatus = 'PENDING' | 'RUNNING' | 'SOLVING' | 'COMPLETED' | 'FEASIBLE' | 'FAILED' | 'CANCELLED' | 'INFEASIBLE';

export interface ScoreDto {
  hard: number;
  soft: number;
}

export interface OptimizationMetricsDto {
  totalDistanceKm: number;
  totalDurationMinutes: number;
  vehiclesUsed: number;
  ordersAssigned: number;
  unassignedOrders: number;
}

export interface StopResultDto {
  stopId: string;
  orderId: string;
  orderNumber: string;
  customerName: string;
  sequenceNumber: number;
  estimatedArrivalMinutes?: number;
  estimatedDepartureMinutes?: number;
}

export interface RouteResultDto {
  routeId: string;
  vehicleId: string;
  vehicleCode: string;
  driverId?: string;
  driverName?: string;
  status: RouteStatus;
  totalDistanceMeters?: number;
  totalDurationMinutes?: number;
  stops: StopResultDto[];
}

export interface OptimizationRunRequest {
  depotId: string;
  orderIds?: string[];
  vehicleIds?: string[];
  maxSolveSeconds?: number;
}

export interface OptimizationRunResponse {
  optimizationRunId: string;
  status: SolverStatus;
  failureReason?: string;
  score?: ScoreDto;
  metrics?: OptimizationMetricsDto;
  routes?: RouteResultDto[];
  startedAt?: string;
  completedAt?: string;
  durationMs?: number;
}
