// ─── Leaflet map with routes, depots, order markers ──────
import React, { useEffect, useRef } from 'react';
import {
  MapContainer, TileLayer, Polyline, Marker, Popup, CircleMarker,
  useMap,
} from 'react-leaflet';
import L from 'leaflet';
import { Depot } from '../types/domain';
import { Order } from '../types/domain';
import { OptimizationRunResponse } from '../types/optimization';
import {
  routeColor, fmtMinutes, priorityColor,
} from '../utils/display';

// ── Fix Leaflet default icon paths (Vite quirk) ────────────
delete (L.Icon.Default.prototype as unknown as { _getIconUrl?: unknown })._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

// ── Custom depot icon ──────────────────────────────────────
const makeDepotIcon = () =>
  L.divIcon({
    className: '',
    html: `<div style="
      width:28px;height:28px;
      border-radius:6px;
      background:linear-gradient(135deg,#2563eb,#7c3aed);
      border:2px solid rgba(255,255,255,0.25);
      display:flex;align-items:center;justify-content:center;
      box-shadow:0 0 12px rgba(59,130,246,0.5);
    ">
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
        <rect x="1" y="3" width="15" height="13"/><polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/>
      </svg>
    </div>`,
    iconSize: [28, 28],
    iconAnchor: [14, 14],
    popupAnchor: [0, -16],
  });

// ── Custom order icon ──────────────────────────────────────
const makeOrderIcon = (priority: number, color: string, selected: boolean) =>
  L.divIcon({
    className: '',
    html: `<div style="
      width:${selected ? 22 : 18}px;height:${selected ? 22 : 18}px;
      border-radius:50%;
      background:${color}33;
      border:2px solid ${color};
      display:flex;align-items:center;justify-content:center;
      font-size:9px;font-weight:700;color:${color};
      font-family:'JetBrains Mono',monospace;
      box-shadow:${selected ? `0 0 12px ${color}80` : 'none'};
      transition:all 0.15s;
    ">${priority}</div>`,
    iconSize: [selected ? 22 : 18, selected ? 22 : 18],
    iconAnchor: [selected ? 11 : 9, selected ? 11 : 9],
    popupAnchor: [0, -12],
  });

// ── Auto-fit bounds ────────────────────────────────────────
const BoundsFitter: React.FC<{ orders: Order[]; depots: Depot[] }> = ({ orders, depots }) => {
  const map = useMap();
  const fitted = useRef(false);

  useEffect(() => {
    if (fitted.current) return;
    const pts: [number, number][] = [
      ...orders.map(o => [o.location.latitude, o.location.longitude] as [number, number]),
      ...depots.map(d => [d.location.latitude, d.location.longitude] as [number, number]),
    ];
    if (pts.length === 0) return;
    map.fitBounds(L.latLngBounds(pts), { padding: [40, 40] });
    fitted.current = true;
  }, [map, orders, depots]);

  return null;
};

// ── Popup HTML builder ─────────────────────────────────────
const orderPopupHtml = (order: Order) => `
  <div style="min-width:180px;font-family:'Inter',sans-serif">
    <div style="font-weight:700;font-size:13px;color:#e2eaf5;margin-bottom:6px">
      ${order.customerName}
    </div>
    <div style="display:flex;flex-direction:column;gap:4px">
      <div style="display:flex;justify-content:space-between;font-size:11px">
        <span style="color:#6b84a3">Order</span>
        <span style="color:#e2eaf5;font-family:'JetBrains Mono',monospace">${order.orderNumber}</span>
      </div>
      <div style="display:flex;justify-content:space-between;font-size:11px">
        <span style="color:#6b84a3">Window</span>
        <span style="color:#e2eaf5">${fmtMinutes(order.windowStartMinutes)} – ${fmtMinutes(order.windowEndMinutes)}</span>
      </div>
      <div style="display:flex;justify-content:space-between;font-size:11px">
        <span style="color:#6b84a3">Weight</span>
        <span style="color:#e2eaf5">${order.weightKg} kg</span>
      </div>
      <div style="display:flex;justify-content:space-between;font-size:11px">
        <span style="color:#6b84a3">Priority</span>
        <span style="color:#e2eaf5">${order.priority}/5</span>
      </div>
      <div style="display:flex;justify-content:space-between;font-size:11px">
        <span style="color:#6b84a3">Status</span>
        <span style="color:#e2eaf5">${order.status}</span>
      </div>
    </div>
  </div>
`;

// ── Main map component ─────────────────────────────────────
interface MapViewProps {
  depots: Depot[];
  orders: Order[];
  optimizationResult: OptimizationRunResponse | null;
  selectedRouteId: string | null;
  selectedOrderId: string | null;
  onSelectOrder: (id: string | null) => void;
}

// Chicago city center
const CHICAGO_CENTER: [number, number] = [41.8781, -87.6298];

const MapView: React.FC<MapViewProps> = ({
  depots,
  orders,
  optimizationResult,
  selectedRouteId,
  selectedOrderId,
  onSelectOrder,
}) => {
  const routes = optimizationResult?.routes ?? [];

  // Build order lookup for route stop coordinates
  const orderById = new Map(orders.map(o => [o.id, o]));

  // Build route polylines from stop order IDs
  const routeLines: { routeId: string; color: string; coords: [number, number][]; vehicleCode: string }[] = [];
  routes.forEach((route, i) => {
    const color = routeColor(i);
    if (route.stops.length === 0) return;

    // Find depot for this route (use first depot as fallback)
    const depot = depots[0];
    const depotCoord: [number, number] = depot
      ? [depot.location.latitude, depot.location.longitude]
      : CHICAGO_CENTER;

    const stopCoords: [number, number][] = route.stops
      .sort((a, b) => a.sequenceNumber - b.sequenceNumber)
      .map(stop => {
        const order = orderById.get(stop.orderId);
        return order ? [order.location.latitude, order.location.longitude] : null;
      })
      .filter((c): c is [number, number] => c !== null);

    if (stopCoords.length > 0) {
      routeLines.push({
        routeId: route.routeId,
        color,
        vehicleCode: route.vehicleCode,
        coords: [depotCoord, ...stopCoords, depotCoord],
      });
    }
  });

  // Which orders are assigned to which route
  const orderRouteMap = new Map<string, { routeIdx: number; sequenceNumber: number }>();
  routes.forEach((route, i) => {
    route.stops.forEach(stop => {
      orderRouteMap.set(stop.orderId, { routeIdx: i, sequenceNumber: stop.sequenceNumber });
    });
  });

  return (
    <MapContainer
      center={CHICAGO_CENTER}
      zoom={11}
      style={{ width: '100%', height: '100%' }}
      zoomControl={true}
    >
      <TileLayer
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        maxZoom={19}
      />

      <BoundsFitter orders={orders} depots={depots} />

      {/* ── Route polylines ──────────────────────────────── */}
      {routeLines.map(line => {
        const isSelected = selectedRouteId === line.routeId;
        const isOther = selectedRouteId !== null && selectedRouteId !== line.routeId;
        return (
          <Polyline
            key={line.routeId}
            positions={line.coords}
            pathOptions={{
              color: line.color,
              weight: isSelected ? 4 : isOther ? 1.5 : 2.5,
              opacity: isSelected ? 1 : isOther ? 0.25 : 0.75,
              dashArray: undefined,
            }}
          />
        );
      })}

      {/* ── Depot markers ────────────────────────────────── */}
      {depots.map(depot => (
        <Marker
          key={depot.id}
          position={[depot.location.latitude, depot.location.longitude]}
          icon={makeDepotIcon()}
        >
          <Popup>
            <div style={{ fontFamily: 'Inter, sans-serif', minWidth: 160 }}>
              <div style={{ fontWeight: 700, fontSize: 13, color: '#e2eaf5', marginBottom: 4 }}>
                🏭 {depot.name}
              </div>
              <div style={{ fontSize: 11, color: '#6b84a3' }}>{depot.addressText}</div>
              <div style={{ fontSize: 10, color: '#3a5070', marginTop: 4, fontFamily: 'JetBrains Mono, monospace' }}>
                {depot.location.latitude.toFixed(4)}, {depot.location.longitude.toFixed(4)}
              </div>
            </div>
          </Popup>
        </Marker>
      ))}

      {/* ── Order markers ────────────────────────────────── */}
      {orders.map(order => {
        const assignment = orderRouteMap.get(order.id);
        const color = assignment
          ? routeColor(assignment.routeIdx)
          : priorityColor(order.priority);
        const isSelected = selectedOrderId === order.id;

        return (
          <Marker
            key={order.id}
            position={[order.location.latitude, order.location.longitude]}
            icon={makeOrderIcon(order.priority, color, isSelected)}
            eventHandlers={{
              click: () => onSelectOrder(isSelected ? null : order.id),
            }}
          >
            <Popup>
              <div dangerouslySetInnerHTML={{ __html: orderPopupHtml(order) }} />
            </Popup>
          </Marker>
        );
      })}

      {/* ── Route sequence number markers ────────────────── */}
      {routes.map((route, i) => {
        const color = routeColor(i);
        const isHidden = selectedRouteId !== null && selectedRouteId !== route.routeId;
        if (isHidden) return null;
        return route.stops.map(stop => {
          const order = orderById.get(stop.orderId);
          if (!order) return null;
          return (
            <CircleMarker
              key={`seq-${stop.stopId}`}
              center={[order.location.latitude, order.location.longitude]}
              radius={7}
              pathOptions={{ color, fillColor: color, fillOpacity: 0.9, weight: 1.5 }}
            >
              <Popup>
                <div dangerouslySetInnerHTML={{ __html: orderPopupHtml(order) }} />
              </Popup>
            </CircleMarker>
          );
        });
      })}
    </MapContainer>
  );
};

export default MapView;
