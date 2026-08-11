import { apiClient } from './auth';
import { OptimizationRunRequest, OptimizationRunResponse, SolverStatus } from '../types/optimization';

const TERMINAL: SolverStatus[] = ['FEASIBLE', 'COMPLETED', 'FAILED', 'CANCELLED', 'INFEASIBLE'];

export const optimizationApi = {
  /** POST /runs — returns 202 immediately with runId and status=SOLVING */
  async start(request: OptimizationRunRequest): Promise<OptimizationRunResponse> {
    const response = await apiClient.post<OptimizationRunResponse>('/optimization/runs', request);
    return response.data;
  },

  /** Alias kept for backwards-compat */
  async run(request: OptimizationRunRequest): Promise<OptimizationRunResponse> {
    return this.start(request);
  },

  async getRun(id: string): Promise<OptimizationRunResponse> {
    const response = await apiClient.get<OptimizationRunResponse>(`/optimization/runs/${id}`);
    return response.data;
  },

  /**
   * Poll GET /runs/{id} every `intervalMs` until status is terminal.
   * Falls back gracefully if WebSocket is unavailable.
   */
  pollUntilDone(
    id: string,
    onUpdate: (run: OptimizationRunResponse) => void,
    intervalMs = 2000
  ): () => void {
    let active = true;

    const tick = async () => {
      if (!active) return;
      try {
        const run = await optimizationApi.getRun(id);
        onUpdate(run);
        if (!TERMINAL.includes(run.status)) {
          setTimeout(tick, intervalMs);
        }
      } catch {
        if (active) setTimeout(tick, intervalMs * 2);
      }
    };

    setTimeout(tick, intervalMs);
    return () => { active = false; };
  },
};
