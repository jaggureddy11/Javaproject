import { apiClient } from './auth';
import { CreateOrderRequest, Order, OrderStatus, PageResponse } from '../types/domain';

export const orderApi = {
  async list(status?: OrderStatus, depotId?: string, page = 0, size = 20): Promise<PageResponse<Order>> {
    const response = await apiClient.get<PageResponse<Order>>('/orders', { params: { status, depotId, page, size } });
    return response.data;
  },

  async get(id: string): Promise<Order> {
    const response = await apiClient.get<Order>(`/orders/${id}`);
    return response.data;
  },

  async getNearby(latitude: number, longitude: number, radiusMeters = 5000): Promise<Order[]> {
    const response = await apiClient.get<Order[]>('/orders/nearby', { params: { latitude, longitude, radiusMeters } });
    return response.data;
  },

  async create(data: CreateOrderRequest): Promise<Order> {
    const response = await apiClient.post<Order>('/orders', data);
    return response.data;
  },

  async update(id: string, data: Partial<Order>): Promise<Order> {
    const response = await apiClient.patch<Order>(`/orders/${id}`, data);
    return response.data;
  },

  async delete(id: string): Promise<void> {
    await apiClient.delete(`/orders/${id}`);
  },

  async getAll(params: { size?: number; status?: OrderStatus } = {}): Promise<PageResponse<Order>> {
    return orderApi.list(params.status, undefined, 0, params.size ?? 50);
  },
};
