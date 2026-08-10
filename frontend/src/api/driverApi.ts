import { apiClient } from './auth';
import { Driver, DriverStatus, PageResponse } from '../types/domain';

export const driverApi = {
  async list(status?: DriverStatus, page = 0, size = 20): Promise<PageResponse<Driver>> {
    const response = await apiClient.get<PageResponse<Driver>>('/drivers', { params: { status, page, size } });
    return response.data;
  },

  async get(id: string): Promise<Driver> {
    const response = await apiClient.get<Driver>(`/drivers/${id}`);
    return response.data;
  },

  async create(data: Partial<Driver>): Promise<Driver> {
    const response = await apiClient.post<Driver>('/drivers', data);
    return response.data;
  },

  async update(id: string, data: Partial<Driver>): Promise<Driver> {
    const response = await apiClient.patch<Driver>(`/drivers/${id}`, data);
    return response.data;
  },

  async delete(id: string): Promise<void> {
    await apiClient.delete(`/drivers/${id}`);
  },
};
