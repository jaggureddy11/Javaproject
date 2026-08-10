import axios from 'axios';
import { LoginRequest, LoginResponse } from '../types/auth';
import { tokenStorage } from '../auth/tokenStorage';

export const apiClient = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use(
  (config) => {
    const token = tokenStorage.getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export const authApi = {
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response = await apiClient.post<LoginResponse>('/auth/login', credentials);
    if (response.data.accessToken) {
      tokenStorage.setToken(response.data.accessToken);
      tokenStorage.setUser(response.data.user);
    }
    return response.data;
  },

  logout(): void {
    tokenStorage.clear();
  },
};
