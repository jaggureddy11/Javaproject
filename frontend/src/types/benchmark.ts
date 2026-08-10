export type BenchmarkDataset =
  | 'SMALL'
  | 'MEDIUM'
  | 'LARGE'
  | 'TIGHT_TIME_WINDOWS'
  | 'CAPACITY_PRESSURE'
  | 'SPATIAL_CLUSTERING';

export interface BenchmarkMetrics {
  distanceKm: number;
  durationMinutes: number;
  travelDurationMinutes: number;
  serviceDurationMinutes: number;
  vehiclesUsed: number;
  routesCount: number;
  ordersAssigned: number;
  ordersUnassigned: number;
  lateDeliveries: number;
  capacityViolations: number;
  shiftViolations: number;
  feasible: boolean;
  solveTimeMs: number;
}

export interface ImprovementMetrics {
  distanceImprovementPercent: number;
  durationImprovementPercent: number;
  vehicleReductionPercent: number;
}

export interface BenchmarkRequest {
  dataset: BenchmarkDataset;
  maxSolveSeconds?: number;
}

export interface BenchmarkResult {
  dataset: BenchmarkDataset;
  timestamp: string;
  ordersCount: number;
  vehiclesCount: number;
  baseline: BenchmarkMetrics;
  optimized: BenchmarkMetrics;
  improvement: ImprovementMetrics;
}
