import { apiClient } from './auth';
import { OptimizationRunRequest, OptimizationRunResponse } from '../types/optimization';

export const optimizationApi = {
  async run(request: OptimizationRunRequest): Promise<OptimizationRunResponse> {
    const response = await apiClient.post<OptimizationRunResponse>('/optimization/runs', request);
    return response.data;
  },

  async getRun(id: string): Promise<OptimizationRunResponse> {
    const response = await apiClient.get<OptimizationRunResponse>(`/optimization/runs/${id}`);
    return response.data;
  },
};
