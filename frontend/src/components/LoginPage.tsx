// ─── Login Page ────────────────────────────────────────────
import React, { useState } from 'react';
import { Truck, Lock, Mail, Eye, EyeOff, AlertCircle } from 'lucide-react';
import { authApi } from '../api/auth';
import { useToast } from '../context/ToastContext';

interface LoginPageProps {
  onLogin: () => void;
}

const LoginPage: React.FC<LoginPageProps> = ({ onLogin }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const { addToast } = useToast();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !password) { setError('Please enter your email and password.'); return; }
    setError('');
    setLoading(true);
    try {
      await authApi.login({ email, password });
      addToast('success', 'Welcome back!');
      onLogin();
    } catch {
      setError('Invalid credentials. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      minHeight: '100vh', width: '100vw',
      background: 'linear-gradient(135deg, #f0f4ff 0%, #fafbff 60%, #f0f7ff 100%)',
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      fontFamily: 'Inter, system-ui, sans-serif',
      position: 'relative', overflow: 'hidden',
    }}>
      {/* Background decoration */}
      <div style={{
        position: 'absolute', top: -120, right: -120,
        width: 500, height: 500, borderRadius: '50%',
        background: 'radial-gradient(circle, rgba(59,130,246,0.08) 0%, transparent 70%)',
        pointerEvents: 'none',
      }} />
      <div style={{
        position: 'absolute', bottom: -80, left: -80,
        width: 400, height: 400, borderRadius: '50%',
        background: 'radial-gradient(circle, rgba(124,58,237,0.06) 0%, transparent 70%)',
        pointerEvents: 'none',
      }} />

      {/* Card */}
      <div style={{
        width: '100%', maxWidth: 420,
        background: '#ffffff',
        borderRadius: 20,
        border: '1px solid #e5e7eb',
        boxShadow: '0 20px 60px rgba(0,0,0,0.08), 0 4px 20px rgba(59,130,246,0.06)',
        padding: '40px 36px',
        position: 'relative', zIndex: 1,
      }}>
        {/* Logo */}
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <div style={{
            width: 56, height: 56, borderRadius: 14,
            background: 'linear-gradient(135deg, #2563eb, #7c3aed)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            margin: '0 auto 16px',
            boxShadow: '0 8px 24px rgba(59,130,246,0.3)',
          }}>
            <Truck size={24} color="#fff" strokeWidth={2.5} />
          </div>
          <h1 style={{
            fontSize: 24, fontWeight: 800, color: '#111827',
            fontFamily: 'Outfit, sans-serif', letterSpacing: '-0.02em',
            marginBottom: 4,
          }}>RouteResQ</h1>
          <p style={{ fontSize: 13, color: '#6b7280' }}>
            Last-Mile Delivery Control Center
          </p>
        </div>

        {/* Demo credentials hint */}
        <div style={{
          background: 'rgba(59,130,246,0.06)', border: '1px solid rgba(59,130,246,0.15)',
          borderRadius: 10, padding: '10px 14px', marginBottom: 24,
          display: 'flex', flexDirection: 'column', gap: 3,
        }}>
          <p style={{ fontSize: 11, fontWeight: 600, color: '#3b82f6', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            Dev Credentials
          </p>
          <p style={{ fontSize: 12, color: '#374151', fontFamily: 'JetBrains Mono, monospace' }}>
            admin@routeresq.io / admin123
          </p>
          <p style={{ fontSize: 12, color: '#374151', fontFamily: 'JetBrains Mono, monospace' }}>
            dispatcher@routeresq.io / dispatch123
          </p>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          {/* Email */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
            <label style={{ fontSize: 12, fontWeight: 600, color: '#374151', letterSpacing: '0.01em' }}>
              Email Address
            </label>
            <div style={{ position: 'relative' }}>
              <Mail size={14} style={{
                position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)',
                color: '#9ca3af', pointerEvents: 'none',
              }} />
              <input
                id="login-email"
                type="email"
                value={email}
                onChange={e => setEmail(e.target.value)}
                placeholder="you@routeresq.io"
                disabled={loading}
                style={{
                  width: '100%', padding: '10px 12px 10px 36px',
                  border: '1px solid #e5e7eb', borderRadius: 8,
                  fontSize: 13, color: '#111827', outline: 'none',
                  background: '#f9fafb', transition: 'border-color 0.15s',
                  boxSizing: 'border-box',
                }}
                onFocus={e => { e.currentTarget.style.borderColor = '#3b82f6'; e.currentTarget.style.background = '#fff'; }}
                onBlur={e => { e.currentTarget.style.borderColor = '#e5e7eb'; e.currentTarget.style.background = '#f9fafb'; }}
              />
            </div>
          </div>

          {/* Password */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
            <label style={{ fontSize: 12, fontWeight: 600, color: '#374151', letterSpacing: '0.01em' }}>
              Password
            </label>
            <div style={{ position: 'relative' }}>
              <Lock size={14} style={{
                position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)',
                color: '#9ca3af', pointerEvents: 'none',
              }} />
              <input
                id="login-password"
                type={showPassword ? 'text' : 'password'}
                value={password}
                onChange={e => setPassword(e.target.value)}
                placeholder="••••••••"
                disabled={loading}
                style={{
                  width: '100%', padding: '10px 36px 10px 36px',
                  border: '1px solid #e5e7eb', borderRadius: 8,
                  fontSize: 13, color: '#111827', outline: 'none',
                  background: '#f9fafb', transition: 'border-color 0.15s',
                  boxSizing: 'border-box',
                }}
                onFocus={e => { e.currentTarget.style.borderColor = '#3b82f6'; e.currentTarget.style.background = '#fff'; }}
                onBlur={e => { e.currentTarget.style.borderColor = '#e5e7eb'; e.currentTarget.style.background = '#f9fafb'; }}
              />
              <button
                type="button"
                onClick={() => setShowPassword(v => !v)}
                style={{
                  position: 'absolute', right: 10, top: '50%', transform: 'translateY(-50%)',
                  background: 'none', border: 'none', cursor: 'pointer', color: '#9ca3af', padding: 2,
                }}
              >
                {showPassword ? <EyeOff size={14} /> : <Eye size={14} />}
              </button>
            </div>
          </div>

          {/* Error message */}
          {error && (
            <div style={{
              display: 'flex', alignItems: 'center', gap: 8,
              padding: '10px 12px', borderRadius: 8,
              background: 'rgba(239,68,68,0.06)', border: '1px solid rgba(239,68,68,0.2)',
            }}>
              <AlertCircle size={14} style={{ color: '#ef4444', flexShrink: 0 }} />
              <span style={{ fontSize: 12, color: '#dc2626' }}>{error}</span>
            </div>
          )}

          {/* Submit */}
          <button
            id="btn-login"
            type="submit"
            disabled={loading}
            style={{
              width: '100%', padding: '11px 0',
              background: loading ? '#93c5fd' : 'linear-gradient(135deg, #2563eb, #3b82f6)',
              color: '#fff', border: 'none', borderRadius: 9,
              fontSize: 14, fontWeight: 700, cursor: loading ? 'not-allowed' : 'pointer',
              fontFamily: 'Inter, sans-serif',
              boxShadow: loading ? 'none' : '0 4px 14px rgba(59,130,246,0.35)',
              transition: 'all 0.15s',
              display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
            }}
          >
            {loading ? (
              <>
                <div style={{
                  width: 14, height: 14, border: '2px solid rgba(255,255,255,0.4)',
                  borderTopColor: '#fff', borderRadius: '50%',
                  animation: 'spin 0.6s linear infinite',
                }} />
                Signing in…
              </>
            ) : 'Sign In'}
          </button>
        </form>

        {/* Footer */}
        <p style={{ textAlign: 'center', marginTop: 24, fontSize: 11, color: '#9ca3af' }}>
          RouteResQ v1.0 · Powered by Timefold VRPTW
        </p>
      </div>
    </div>
  );
};

export default LoginPage;
