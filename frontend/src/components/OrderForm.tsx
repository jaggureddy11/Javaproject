// ─── New Order creation modal ──────────────────────────────
import React, { useState, useEffect } from 'react';
import { X, MapPin, Package, Clock, Weight, AlertCircle } from 'lucide-react';
import { orderApi } from '../api/orderApi';
import { depotApi } from '../api/depotApi';
import { Order, CreateOrderRequest } from '../types/domain';
import { useToast } from '../context/ToastContext';

interface OrderFormProps {
  onClose: () => void;
  onCreated: (order: Order) => void;
}

const OrderForm: React.FC<OrderFormProps> = ({ onClose, onCreated }) => {
  const [depots, setDepots] = useState<{ id: string; name: string }[]>([]);
  const [depotId, setDepotId] = useState('');
  const [customerName, setCustomerName] = useState('');
  const [addressText, setAddressText] = useState('');
  const [latitude, setLatitude] = useState('');
  const [longitude, setLongitude] = useState('');
  const [weightKg, setWeightKg] = useState('');
  const [windowStart, setWindowStart] = useState('09:00');
  const [windowEnd, setWindowEnd] = useState('17:00');
  const [priority, setPriority] = useState(3);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const { addToast } = useToast();

  useEffect(() => {
    depotApi.getAll({ size: 20 }).then(p => {
      setDepots(p.content);
      if (p.content.length > 0) setDepotId(p.content[0].id);
    }).catch(() => {});
  }, []);

  const timeToMinutes = (t: string) => {
    const [h, m] = t.split(':').map(Number);
    return h * 60 + m;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    const lat = parseFloat(latitude);
    const lon = parseFloat(longitude);
    const weight = parseFloat(weightKg);

    if (!customerName.trim()) { setError('Customer name is required.'); return; }
    if (isNaN(lat) || isNaN(lon)) { setError('Valid latitude and longitude required.'); return; }
    if (isNaN(weight) || weight <= 0) { setError('Weight must be a positive number.'); return; }
    if (!depotId) { setError('Select a depot.'); return; }

    const orderNumber = `ORD-${Date.now().toString().slice(-6)}`;

    const payload: CreateOrderRequest = {
      orderNumber,
      depotId,
      customerName: customerName.trim(),
      location: { latitude: lat, longitude: lon },
      addressText: addressText.trim() || `${lat.toFixed(4)}N, ${Math.abs(lon).toFixed(4)}W`,
      weightKg: weight,
      volumeM3: 0.25,
      windowStartMinutes: timeToMinutes(windowStart),
      windowEndMinutes: timeToMinutes(windowEnd),
      serviceDurationMinutes: 10,
      priority,
    };

    setLoading(true);
    try {
      const order = await orderApi.create(payload);
      addToast('success', `Order ${order.orderNumber} created`);
      onCreated(order);
      onClose();
    } catch {
      setError('Failed to create order. Check all fields and try again.');
    } finally {
      setLoading(false);
    }
  };

  const inputStyle: React.CSSProperties = {
    width: '100%', padding: '8px 10px',
    border: '1px solid #e5e7eb', borderRadius: 7,
    fontSize: 12, color: '#111827', outline: 'none',
    background: '#f9fafb', boxSizing: 'border-box',
    fontFamily: 'Inter, sans-serif',
  };

  const labelStyle: React.CSSProperties = {
    fontSize: 11, fontWeight: 600, color: '#374151',
    textTransform: 'uppercase', letterSpacing: '0.04em', marginBottom: 4, display: 'block',
  };

  return (
    <>
      {/* Backdrop */}
      <div
        onClick={onClose}
        style={{
          position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.25)',
          backdropFilter: 'blur(2px)', zIndex: 1000,
        }}
      />
      {/* Modal */}
      <div style={{
        position: 'fixed', top: '50%', left: '50%',
        transform: 'translate(-50%, -50%)',
        zIndex: 1001, width: '92%', maxWidth: 420,
        background: '#fff', borderRadius: 16,
        border: '1px solid #e5e7eb',
        boxShadow: '0 24px 60px rgba(0,0,0,0.14)',
        padding: '24px 24px 20px',
        maxHeight: '90vh', overflowY: 'auto',
      }}>
        {/* Header */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 20 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <Package size={16} style={{ color: 'var(--accent-blue)' }} />
            <span style={{ fontWeight: 700, fontSize: 14, color: '#111827', fontFamily: 'Outfit, sans-serif' }}>
              New Delivery Order
            </span>
          </div>
          <button onClick={onClose} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#6b7280', padding: 4 }}>
            <X size={16} />
          </button>
        </div>

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          {/* Depot */}
          <div>
            <label style={labelStyle}>Depot</label>
            <select value={depotId} onChange={e => setDepotId(e.target.value)} style={inputStyle}>
              {depots.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
            </select>
          </div>

          {/* Customer */}
          <div>
            <label style={labelStyle}>Customer Name</label>
            <input value={customerName} onChange={e => setCustomerName(e.target.value)}
              placeholder="Acme Corp" style={inputStyle} />
          </div>

          {/* Address */}
          <div>
            <label style={{ ...labelStyle, display: 'flex', alignItems: 'center', gap: 5 }}>
              <MapPin size={11} /> Address
            </label>
            <input value={addressText} onChange={e => setAddressText(e.target.value)}
              placeholder="123 Main St, Chicago, IL" style={inputStyle} />
          </div>

          {/* Lat / Lon */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 }}>
            <div>
              <label style={labelStyle}>Latitude</label>
              <input type="number" step="any" value={latitude} onChange={e => setLatitude(e.target.value)}
                placeholder="41.8781" style={inputStyle} />
            </div>
            <div>
              <label style={labelStyle}>Longitude</label>
              <input type="number" step="any" value={longitude} onChange={e => setLongitude(e.target.value)}
                placeholder="-87.6298" style={inputStyle} />
            </div>
          </div>

          {/* Weight */}
          <div>
            <label style={{ ...labelStyle, display: 'flex', alignItems: 'center', gap: 5 }}>
              <Weight size={11} /> Weight (kg)
            </label>
            <input type="number" step="0.1" value={weightKg} onChange={e => setWeightKg(e.target.value)}
              placeholder="15.5" style={inputStyle} />
          </div>

          {/* Time window */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 }}>
            <div>
              <label style={{ ...labelStyle, display: 'flex', alignItems: 'center', gap: 5 }}>
                <Clock size={11} /> Window Start
              </label>
              <input type="time" value={windowStart} onChange={e => setWindowStart(e.target.value)} style={inputStyle} />
            </div>
            <div>
              <label style={labelStyle}>Window End</label>
              <input type="time" value={windowEnd} onChange={e => setWindowEnd(e.target.value)} style={inputStyle} />
            </div>
          </div>

          {/* Priority */}
          <div>
            <label style={labelStyle}>Priority: {priority}/5</label>
            <input type="range" min={1} max={5} step={1} value={priority}
              onChange={e => setPriority(Number(e.target.value))}
              style={{ width: '100%', accentColor: 'var(--accent-blue)' }} />
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 9, color: '#9ca3af' }}>
              <span>Low</span><span>Med</span><span>High</span>
            </div>
          </div>

          {/* Error */}
          {error && (
            <div style={{
              display: 'flex', alignItems: 'center', gap: 7, padding: '8px 10px',
              background: 'rgba(239,68,68,0.06)', border: '1px solid rgba(239,68,68,0.2)', borderRadius: 7,
            }}>
              <AlertCircle size={13} style={{ color: '#ef4444', flexShrink: 0 }} />
              <span style={{ fontSize: 12, color: '#dc2626' }}>{error}</span>
            </div>
          )}

          {/* Actions */}
          <div style={{ display: 'flex', gap: 8, marginTop: 4 }}>
            <button type="button" onClick={onClose} className="btn btn-ghost" style={{ flex: 1 }}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading} style={{ flex: 2 }}>
              {loading ? <><div className="spinner" /> Creating…</> : <><Package size={13} /> Create Order</>}
            </button>
          </div>
        </form>
      </div>
    </>
  );
};

export default OrderForm;
