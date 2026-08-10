import { apiClient } from './auth';
import { PageResponse, Vehicle, VehicleStatus } from '../types/domain';

export const vehicleApi = {
  async list(status?: VehicleStatus, depotId?: string, page = 0, size = 20): Promise<PageResponse<Vehicle>> {
    const response = await apiClient.get<PageResponse<Vehicle>>('/vehicles', { params: { status, depotId, page, size } });
    return response.data;
  },

  async get(id: string): Promise<Vehicle> {
    const response = await apiClient.get<Vehicle>(`/vehicles/${id}`);
    return response.data;
  },

  async getNearby(latitude: number, longitude: number, radiusMeters = 5000): Promise<Vehicle[]> {
    const response = await apiClient.get<Vehicle[]>('/vehicles/nearby', { params: { latitude, longitude, radiusMeters } });
    return response.data;
  },

  async create(data: Partial<Vehicle>): Promise<Vehicle> {
    const response = await apiClient.post<Vehicle>('/vehicles', data);
    return response.data;
  },

  async update(id: string, data: Partial<Vehicle>): Promise<Vehicle> {
    const response = await apiClient.patch<Vehicle>(`/vehicles/${id}`, data);
    return response.data;
  },

  async delete(id: string): Promise<void> {
    await apiClient.delete(`/vehicles/${id}`);
  },

  async getAll(params: { size?: number } = {}): Promise<import('../types/domain').PageResponse<import('../types/domain').Vehicle>> {
    return vehicleApi.list(undefined, undefined, 0, params.size ?? 20);
  },
};
