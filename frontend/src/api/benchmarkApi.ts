import { apiClient } from './auth';
import { BenchmarkRequest, BenchmarkResult } from '../types/benchmark';

export const benchmarkApi = {
  async runBenchmark(request: BenchmarkRequest): Promise<BenchmarkResult> {
    const response = await apiClient.post<BenchmarkResult>('/optimization/benchmarks', request);
    return response.data;
  },

  async run(request: BenchmarkRequest): Promise<BenchmarkResult> {
    return benchmarkApi.runBenchmark(request);
  },
};
