import { apiClient } from './auth';
import { Depot, PageResponse } from '../types/domain';

export const depotApi = {
  async list(page = 0, size = 20): Promise<PageResponse<Depot>> {
    const response = await apiClient.get<PageResponse<Depot>>('/depots', { params: { page, size } });
    return response.data;
  },

  async get(id: string): Promise<Depot> {
    const response = await apiClient.get<Depot>(`/depots/${id}`);
    return response.data;
  },

  async create(data: Partial<Depot>): Promise<Depot> {
    const response = await apiClient.post<Depot>('/depots', data);
    return response.data;
  },

  async update(id: string, data: Partial<Depot>): Promise<Depot> {
    const response = await apiClient.patch<Depot>(`/depots/${id}`, data);
    return response.data;
  },

  async delete(id: string): Promise<void> {
    await apiClient.delete(`/depots/${id}`);
  },

  async getAll(params: { size?: number } = {}): Promise<import('../types/domain').PageResponse<import('../types/domain').Depot>> {
    return depotApi.list(0, params.size ?? 20);
  },
};
