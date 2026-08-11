import axios from 'axios';

export interface HealthComponent {
  status: 'UP' | 'DOWN' | 'UNKNOWN';
  details?: Record<string, unknown>;
}

export interface SystemHealthResponse {
  status: 'UP' | 'DOWN' | 'UNKNOWN';
  components?: {
    db?: HealthComponent;
    redis?: HealthComponent;
    ping?: HealthComponent;
    diskSpace?: HealthComponent;
  };
}

export const healthApi = {
  async getHealth(): Promise<SystemHealthResponse> {
    try {
      const response = await axios.get<SystemHealthResponse>('/actuator/health');
      return response.data;
    } catch {
      return { status: 'DOWN' };
    }
  },
};
