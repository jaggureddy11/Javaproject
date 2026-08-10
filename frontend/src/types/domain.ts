export interface LocationDto {
  latitude: number;
  longitude: number;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface Depot {
  id: string;
  name: string;
  addressText: string;
  location: LocationDto;
  createdAt: string;
  updatedAt: string;
}

export type DriverStatus = 'ACTIVE' | 'ON_BREAK' | 'OFF_DUTY' | 'UNAVAILABLE';

export interface Driver {
  id: string;
  name: string;
  licenseNumber: string;
  phone?: string;
  status: DriverStatus;
  shiftStartMinutes: number;
  shiftEndMinutes: number;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export type VehicleStatus = 'IDLE' | 'EN_ROUTE' | 'MAINTENANCE' | 'OFFLINE';

export interface Vehicle {
  id: string;
  vehicleCode: string;
  depotId: string;
  depotName?: string;
  driverId?: string;
  driverName?: string;
  maxWeightKg: number;
  maxVolumeM3: number;
  status: VehicleStatus;
  currentLocation?: LocationDto;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export type OrderStatus = 'UNASSIGNED' | 'ASSIGNED' | 'IN_TRANSIT' | 'DELIVERED' | 'FAILED' | 'CANCELLED';

export type RouteStatus = 'PLANNED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface Order {
  id: string;
  orderNumber: string;
  depotId: string;
  depotName?: string;
  customerName: string;
  location: LocationDto;
  addressText: string;
  weightKg: number;
  volumeM3: number;
  windowStartMinutes: number;
  windowEndMinutes: number;
  serviceDurationMinutes: number;
  priority: number;
  status: OrderStatus;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateOrderRequest {
  orderNumber: string;
  depotId: string;
  customerName: string;
  location: LocationDto;
  addressText: string;
  weightKg: number;
  volumeM3?: number;
  windowStartMinutes?: number;
  windowEndMinutes?: number;
  serviceDurationMinutes?: number;
  priority?: number;
  status?: OrderStatus;
}
