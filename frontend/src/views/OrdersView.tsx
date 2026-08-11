// ─── Dedicated Orders Operations View ───────────────────────
import React, { useState, useMemo } from 'react';
import {
  Package, Search, Filter, Plus, MapPin,
  ChevronLeft, ChevronRight, X, Trash2, ArrowUpDown
} from 'lucide-react';
import { Order } from '../types/domain';
import { orderApi } from '../api/orderApi';
import OrderForm from '../components/OrderForm';
import { fmtMinutes, orderStatusColor, priorityColor } from '../utils/display';
import { useToast } from '../context/ToastContext';

interface OrdersViewProps {
  orders: Order[];
  onReloadOrders: () => void;
  onSelectOrderOnMap: (orderId: string) => void;
  userRole?: string;
}

type SortField = 'windowStartMinutes' | 'priority' | 'weightKg' | 'orderNumber';

export const OrdersView: React.FC<OrdersViewProps> = ({
  orders,
  onReloadOrders,
  onSelectOrderOnMap,
  userRole,
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [priorityFilter, setPriorityFilter] = useState<string>('ALL');
  const [sortField, setSortField] = useState<SortField>('windowStartMinutes');
  const [sortAsc, setSortAsc] = useState(true);
  const [page, setPage] = useState(0);
  const pageSize = 15;

  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [updating, setUpdating] = useState(false);
  const { addToast } = useToast();

  // Multi-attribute filtering & sorting
  const filteredOrders = useMemo(() => {
    return orders.filter(o => {
      // Search
      const matchSearch = !searchTerm ||
        o.customerName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        o.orderNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
        o.addressText.toLowerCase().includes(searchTerm.toLowerCase());

      // Status
      const matchStatus = statusFilter === 'ALL' || o.status === statusFilter;

      // Priority
      const matchPriority = priorityFilter === 'ALL' || String(o.priority) === priorityFilter;

      return matchSearch && matchStatus && matchPriority;
    }).sort((a, b) => {
      let valA = a[sortField] ?? 0;
      let valB = b[sortField] ?? 0;
      if (typeof valA === 'string') valA = valA.toLowerCase();
      if (typeof valB === 'string') valB = valB.toLowerCase();

      if (valA < valB) return sortAsc ? -1 : 1;
      if (valA > valB) return sortAsc ? 1 : -1;
      return 0;
    });
  }, [orders, searchTerm, statusFilter, priorityFilter, sortField, sortAsc]);

  const totalPages = Math.ceil(filteredOrders.length / pageSize) || 1;
  const paginatedOrders = useMemo(() => {
    const start = page * pageSize;
    return filteredOrders.slice(start, start + pageSize);
  }, [filteredOrders, page, pageSize]);

  const handleCancelOrder = async (id: string) => {
    if (!window.confirm('Are you sure you want to cancel this order?')) return;
    setUpdating(true);
    try {
      await orderApi.update(id, { status: 'CANCELLED' });
      addToast('success', 'Order cancelled');
      onReloadOrders();
      setSelectedOrder(null);
    } catch {
      addToast('error', 'Failed to cancel order');
    } finally {
      setUpdating(false);
    }
  };

  const handleSort = (field: SortField) => {
    if (sortField === field) {
      setSortAsc(!sortAsc);
    } else {
      setSortField(field);
      setSortAsc(true);
    }
  };

  return (
    <div style={{ padding: '20px 24px', overflowY: 'auto', height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* Header Bar */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16, flexWrap: 'wrap', gap: 12 }}>
        <div>
          <h2 style={{ fontSize: 20, fontWeight: 800, color: 'var(--text-main)', fontFamily: 'Outfit, sans-serif' }}>
            Delivery Orders Operations
          </h2>
          <p style={{ fontSize: 12, color: 'var(--text-muted)' }}>
            Manage demand requests, delivery windows, and order statuses
          </p>
        </div>

        {userRole !== 'DRIVER' && (
          <button
            onClick={() => setShowCreateModal(true)}
            className="btn btn-primary"
            style={{ padding: '8px 16px', fontSize: 13, gap: 6 }}
          >
            <Plus size={14} /> New Order
          </button>
        )}
      </div>

      {/* Filter Controls Toolbar */}
      <div style={{
        display: 'flex', gap: 12, marginBottom: 16, flexWrap: 'wrap', alignItems: 'center',
        padding: '12px 16px', background: 'var(--bg-card)', borderRadius: 10, border: '1px solid var(--border-color)',
      }}>
        {/* Search */}
        <div style={{ position: 'relative', flex: 1, minWidth: 200 }}>
          <Search size={14} style={{ position: 'absolute', left: 10, top: '50%', transform: 'translateY(-50%)', color: 'var(--text-dim)' }} />
          <input
            type="text"
            placeholder="Search by customer, order #, address..."
            value={searchTerm}
            onChange={e => { setSearchTerm(e.target.value); setPage(0); }}
            className="form-input"
            style={{ paddingLeft: 32 }}
          />
        </div>

        {/* Status Filter */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <Filter size={12} style={{ color: 'var(--text-muted)' }} />
          <span style={{ fontSize: 11, color: 'var(--text-muted)', fontWeight: 600 }}>Status:</span>
          <select
            value={statusFilter}
            onChange={e => { setStatusFilter(e.target.value); setPage(0); }}
            className="form-select"
            style={{ width: 140, padding: '5px 8px' }}
          >
            <option value="ALL">All Statuses</option>
            <option value="UNASSIGNED">Unassigned</option>
            <option value="ASSIGNED">Assigned</option>
            <option value="IN_TRANSIT">In Transit</option>
            <option value="DELIVERED">Delivered</option>
            <option value="FAILED">Failed</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </div>

        {/* Priority Filter */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <span style={{ fontSize: 11, color: 'var(--text-muted)', fontWeight: 600 }}>Priority:</span>
          <select
            value={priorityFilter}
            onChange={e => { setPriorityFilter(e.target.value); setPage(0); }}
            className="form-select"
            style={{ width: 110, padding: '5px 8px' }}
          >
            <option value="ALL">All Priorities</option>
            <option value="5">Priority 5 (High)</option>
            <option value="4">Priority 4</option>
            <option value="3">Priority 3</option>
            <option value="2">Priority 2</option>
            <option value="1">Priority 1 (Low)</option>
          </select>
        </div>

        {/* Results Counter */}
        <div style={{ marginLeft: 'auto', fontSize: 11, color: 'var(--text-dim)', fontFamily: 'JetBrains Mono, monospace' }}>
          Showing {filteredOrders.length} of {orders.length} orders
        </div>
      </div>

      {/* Orders Table */}
      <div className="card" style={{ flex: 1, overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
        <div style={{ overflowX: 'auto', flex: 1 }}>
          <table className="bench-table" style={{ width: '100%' }}>
            <thead>
              <tr>
                <th onClick={() => handleSort('orderNumber')} style={{ cursor: 'pointer' }}>
                  Order # <ArrowUpDown size={10} />
                </th>
                <th>Customer</th>
                <th>Address</th>
                <th onClick={() => handleSort('windowStartMinutes')} style={{ cursor: 'pointer' }}>
                  Window <ArrowUpDown size={10} />
                </th>
                <th onClick={() => handleSort('weightKg')} style={{ cursor: 'pointer' }}>
                  Weight <ArrowUpDown size={10} />
                </th>
                <th onClick={() => handleSort('priority')} style={{ cursor: 'pointer' }}>
                  Priority <ArrowUpDown size={10} />
                </th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {paginatedOrders.length === 0 ? (
                <tr>
                  <td colSpan={8} style={{ textAlign: 'center', padding: '40px 16px', color: 'var(--text-dim)' }}>
                    <Package size={32} style={{ margin: '0 auto 8px', opacity: 0.3 }} />
                    <p style={{ fontSize: 13, fontWeight: 600, color: 'var(--text-main)' }}>No orders match your filter criteria</p>
                    <p style={{ fontSize: 11, marginTop: 4 }}>Try clearing search or filters</p>
                  </td>
                </tr>
              ) : (
                paginatedOrders.map(order => {
                  const pColor = priorityColor(order.priority);
                  const sColor = orderStatusColor(order.status);

                  return (
                    <tr key={order.id} style={{ cursor: 'pointer' }} onClick={() => setSelectedOrder(order)}>
                      <td style={{ fontFamily: 'JetBrains Mono, monospace', fontWeight: 600, color: 'var(--text-main)' }}>
                        {order.orderNumber}
                      </td>
                      <td style={{ fontWeight: 600, color: 'var(--text-main)' }}>
                        {order.customerName}
                      </td>
                      <td style={{ maxWidth: 220, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', color: 'var(--text-muted)' }}>
                        {order.addressText}
                      </td>
                      <td style={{ fontFamily: 'JetBrains Mono, monospace' }}>
                        {fmtMinutes(order.windowStartMinutes)} – {fmtMinutes(order.windowEndMinutes)}
                      </td>
                      <td style={{ fontFamily: 'JetBrains Mono, monospace' }}>
                        {order.weightKg} kg
                      </td>
                      <td>
                        <span className="priority-badge" style={{ background: `${pColor}22`, color: pColor }}>
                          P{order.priority}
                        </span>
                      </td>
                      <td>
                        <span className="status-pill" style={{ background: `${sColor}18`, color: sColor }}>
                          <span className="status-dot" style={{ background: sColor }} />
                          {order.status}
                        </span>
                      </td>
                      <td>
                        <button
                          onClick={e => {
                            e.stopPropagation();
                            onSelectOrderOnMap(order.id);
                          }}
                          className="btn btn-ghost"
                          style={{ padding: '3px 8px', fontSize: 11, gap: 4 }}
                          title="Locate on Map"
                        >
                          <MapPin size={11} /> Map
                        </button>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination Bar */}
        <div style={{
          padding: '10px 16px', borderTop: '1px solid var(--border-color)',
          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          fontSize: 12, color: 'var(--text-muted)', background: 'var(--bg-panel)',
        }}>
          <span>
            Page <strong>{page + 1}</strong> of <strong>{totalPages}</strong>
          </span>

          <div style={{ display: 'flex', gap: 6 }}>
            <button
              className="btn btn-ghost"
              disabled={page === 0}
              onClick={() => setPage(p => Math.max(0, p - 1))}
              style={{ padding: '4px 8px' }}
            >
              <ChevronLeft size={14} /> Prev
            </button>
            <button
              className="btn btn-ghost"
              disabled={page >= totalPages - 1}
              onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
              style={{ padding: '4px 8px' }}
            >
              Next <ChevronRight size={14} />
            </button>
          </div>
        </div>
      </div>

      {/* Order Detail Modal / Drawer */}
      {selectedOrder && (
        <>
          <div onClick={() => setSelectedOrder(null)} style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.3)', backdropFilter: 'blur(2px)', zIndex: 1000 }} />
          <div style={{
            position: 'fixed', top: '50%', left: '50%', transform: 'translate(-50%, -50%)',
            zIndex: 1001, width: '90%', maxWidth: 460, background: '#fff', borderRadius: 16,
            border: '1px solid #e5e7eb', boxShadow: '0 24px 60px rgba(0,0,0,0.15)', padding: '24px',
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
              <div>
                <span style={{ fontSize: 10, textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--text-dim)', fontWeight: 600 }}>Order Detail</span>
                <h3 style={{ fontSize: 16, fontWeight: 800, color: 'var(--text-main)', fontFamily: 'Outfit, sans-serif' }}>
                  {selectedOrder.orderNumber}
                </h3>
              </div>
              <button onClick={() => setSelectedOrder(null)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#6b7280' }}>
                <X size={16} />
              </button>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 10, fontSize: 12 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 10px', background: 'var(--bg-card)', borderRadius: 6 }}>
                <span style={{ color: 'var(--text-muted)' }}>Customer</span>
                <span style={{ fontWeight: 600, color: 'var(--text-main)' }}>{selectedOrder.customerName}</span>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 10px', background: 'var(--bg-card)', borderRadius: 6 }}>
                <span style={{ color: 'var(--text-muted)' }}>Address</span>
                <span style={{ fontWeight: 500, color: 'var(--text-main)', maxWidth: 260, textAlign: 'right' }}>{selectedOrder.addressText}</span>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 10px', background: 'var(--bg-card)', borderRadius: 6 }}>
                <span style={{ color: 'var(--text-muted)' }}>Coordinates</span>
                <span style={{ fontFamily: 'JetBrains Mono, monospace', color: 'var(--text-main)' }}>
                  {selectedOrder.location.latitude.toFixed(4)}, {selectedOrder.location.longitude.toFixed(4)}
                </span>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
                <div style={{ padding: '8px 10px', background: 'var(--bg-card)', borderRadius: 6 }}>
                  <div style={{ color: 'var(--text-muted)', fontSize: 10 }}>Delivery Window</div>
                  <div style={{ fontWeight: 600, fontFamily: 'JetBrains Mono, monospace', marginTop: 2 }}>
                    {fmtMinutes(selectedOrder.windowStartMinutes)} – {fmtMinutes(selectedOrder.windowEndMinutes)}
                  </div>
                </div>

                <div style={{ padding: '8px 10px', background: 'var(--bg-card)', borderRadius: 6 }}>
                  <div style={{ color: 'var(--text-muted)', fontSize: 10 }}>Weight & Priority</div>
                  <div style={{ fontWeight: 600, marginTop: 2 }}>
                    {selectedOrder.weightKg} kg • Priority {selectedOrder.priority}/5
                  </div>
                </div>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 10px', background: 'var(--bg-card)', borderRadius: 6, alignItems: 'center' }}>
                <span style={{ color: 'var(--text-muted)' }}>Current Status</span>
                <span className="status-pill" style={{ background: `${orderStatusColor(selectedOrder.status)}18`, color: orderStatusColor(selectedOrder.status) }}>
                  {selectedOrder.status}
                </span>
              </div>
            </div>

            <div style={{ display: 'flex', gap: 10, marginTop: 20 }}>
              <button
                onClick={() => {
                  onSelectOrderOnMap(selectedOrder.id);
                  setSelectedOrder(null);
                }}
                className="btn btn-primary"
                style={{ flex: 1 }}
              >
                <MapPin size={13} /> View on Map
              </button>

              {selectedOrder.status !== 'CANCELLED' && selectedOrder.status !== 'DELIVERED' && userRole !== 'DRIVER' && (
                <button
                  onClick={() => handleCancelOrder(selectedOrder.id)}
                  disabled={updating}
                  className="btn btn-danger"
                >
                  <Trash2 size={13} /> Cancel Order
                </button>
              )}
            </div>
          </div>
        </>
      )}

      {/* Create Modal */}
      {showCreateModal && (
        <OrderForm
          onClose={() => setShowCreateModal(false)}
          onCreated={() => {
            onReloadOrders();
            setShowCreateModal(false);
          }}
        />
      )}
    </div>
  );
};
