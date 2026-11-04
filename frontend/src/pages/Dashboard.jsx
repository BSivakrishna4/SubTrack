import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/axios';
import StatusBadge from '../components/StatusBadge';
import ExpiryAlert from '../components/ExpiryAlert';
import { CreditCard, RefreshCw, XCircle, Package, ArrowRight, ShieldCheck } from 'lucide-react';

export default function Dashboard() {
  const { user } = useAuth();
  const [subscriptions, setSubscriptions] = useState([]);
  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [message, setMessage] = useState(null);
  const [error, setError] = useState(null);

  const fetchDashboardData = async () => {
    if (!user) return;
    setLoading(true);
    try {
      const [subsRes, payRes] = await Promise.all([
        api.get(`/api/subscriptions/user/${user.userId}`),
        api.get(`/api/payments/user/${user.userId}`)
      ]);
      setSubscriptions(subsRes.data || []);
      setPayments(payRes.data || []);
    } catch (err) {
      console.error('Failed to load dashboard data:', err);
      setError('Could not connect to SubTrack services via API Gateway.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, [user]);

  // Pick the latest active subscription if multiple exist
  const activeSubs = subscriptions.filter(s => s.status === 'ACTIVE');
  const activeSub = activeSubs.length > 0 ? activeSubs[activeSubs.length - 1] : null;

  const handleRenew = async (subId) => {
    if (!window.confirm('Are you sure you want to process mock payment to renew this subscription?')) return;
    setActionLoading(true);
    setMessage(null);
    setError(null);
    try {
      const response = await api.put(`/api/subscriptions/${subId}/renew`);
      setMessage(`Subscription renewed successfully! New Expiry Date: ${new Date(response.data.expiryDate).toLocaleDateString()}`);
      fetchDashboardData();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to renew subscription.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleCancel = async (subId) => {
    if (!window.confirm('Are you sure you want to cancel this subscription?')) return;
    setActionLoading(true);
    setMessage(null);
    setError(null);
    try {
      await api.put(`/api/subscriptions/${subId}/cancel`);
      setMessage('Subscription cancelled successfully.');
      fetchDashboardData();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to cancel subscription.');
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '80px 0', color: 'var(--text-secondary)' }}>
        <RefreshCw className="spin" size={36} style={{ marginBottom: '16px', color: 'var(--accent-cyan)' }} />
        <p style={{ fontSize: '1rem', fontWeight: 600 }}>Loading SubTrack Dashboard...</p>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '1120px', margin: '0 auto', padding: '24px 20px' }}>

      {/* Top Header & Role Badge */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px', marginBottom: '22px' }}>
        <div>
          <h1 style={{ fontSize: '1.85rem', fontWeight: 900, letterSpacing: '-0.02em', color: 'var(--text-heading)' }}>
            Welcome Back, <span style={{ color: 'var(--accent-cyan)' }}>{user?.name}</span> 👋
          </h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem', marginTop: '4px' }}>
            Manage your subscription lifecycle, recurring billing, and payment records.
          </p>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <span style={{ padding: '6px 16px', borderRadius: '9999px', background: 'rgba(2, 132, 199, 0.1)', border: '1px solid var(--border-glow)', color: 'var(--accent-cyan)', fontSize: '0.8rem', fontWeight: 700 }}>
            Account Role: {user?.role === 'ADMIN' ? '🛡️ System Administrator' : '👤 Regular User'}
          </span>
        </div>
      </div>

      {message && <div className="alert alert-success" style={{ marginBottom: '24px' }}>{message}</div>}
      {error && <div className="alert alert-error" style={{ marginBottom: '24px' }}>{error}</div>}

      {/* Active Subscription Overview Card */}
      {activeSub ? (
        <div style={{ marginBottom: '32px' }}>
          <ExpiryAlert expiryDate={activeSub.expiryDate} />

          <div className="glass-card" style={{ borderLeft: '4px solid var(--accent-cyan)', padding: '28px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '16px', marginBottom: '20px' }}>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '8px' }}>
                  <Package size={24} style={{ color: 'var(--accent-cyan)' }} />
                  <h2 style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--text-heading)' }}>
                    {activeSub.plan?.name || 'Active Subscription Plan'}
                  </h2>
                  <StatusBadge status={activeSub.status} />
                </div>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
                  {activeSub.plan?.description || 'Active subscription tier'}
                </p>
              </div>

              <div style={{ textAlign: 'right' }}>
                <div style={{ fontSize: '2rem', fontWeight: 900, color: 'var(--text-heading)' }}>
                  {activeSub.plan?.price != null ? `₹${activeSub.plan.price}` : ''}{' '}
                  <span style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', fontWeight: 400 }}>
                    {activeSub.plan?.durationDays ? `/ ${activeSub.plan.durationDays} days` : ''}
                  </span>
                </div>
                <div style={{ fontSize: '0.8rem', color: 'var(--accent-cyan)', fontWeight: 600, marginTop: '2px' }}>
                  Microservices Sub ID: #{activeSub.id}
                </div>
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px', padding: '16px', background: 'var(--bg-surface)', borderRadius: '12px', border: '1px solid var(--border-color)', marginBottom: '24px' }}>
              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 700 }}>Activation Date</div>
                <div style={{ fontSize: '0.95rem', fontWeight: 700, marginTop: '2px', color: 'var(--text-heading)' }}>
                  {new Date(activeSub.startDate).toLocaleDateString()}
                </div>
              </div>

              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 700 }}>Expiration Date</div>
                <div style={{ fontSize: '0.95rem', fontWeight: 700, marginTop: '2px', color: 'var(--accent-cyan)' }}>
                  {new Date(activeSub.expiryDate).toLocaleDateString()}
                </div>
              </div>

              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 700 }}>Payment Status</div>
                <div style={{ fontSize: '0.95rem', fontWeight: 700, marginTop: '2px', color: '#10b981', display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <ShieldCheck size={16} /> Verified via Gateway
                </div>
              </div>
            </div>

            {/* Action Buttons */}
            <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
              {activeSub.status === 'ACTIVE' && (
                <>
                  <button
                    onClick={() => handleRenew(activeSub.id)}
                    className="btn btn-electric"
                    disabled={actionLoading}
                    style={{ padding: '10px 22px' }}
                  >
                    <RefreshCw size={16} className={actionLoading ? 'spin' : ''} />
                    <span>Renew Subscription</span>
                  </button>

                  <button
                    onClick={() => handleCancel(activeSub.id)}
                    className="btn btn-outline-blue"
                    disabled={actionLoading}
                    style={{ padding: '10px 22px', borderColor: 'rgba(239, 68, 68, 0.6)', color: '#ef4444' }}
                  >
                    <XCircle size={16} />
                    <span>Cancel Subscription</span>
                  </button>
                </>
              )}

              <Link to="/plans" className="btn btn-outline-blue" style={{ padding: '10px 22px' }}>
                <Package size={16} /> Change / Upgrade Plan
              </Link>
            </div>
          </div>
        </div>
      ) : (
        <div
          className="glass-card"
          style={{
            textAlign: 'center',
            padding: '32px 20px',
            marginBottom: '24px',
            borderRadius: '16px',
            border: '1px solid var(--border-color)',
            background: 'var(--bg-card)'
          }}
        >
          <div
            style={{
              width: '48px',
              height: '48px',
              borderRadius: '12px',
              background: 'rgba(2, 132, 199, 0.12)',
              border: '1px solid var(--border-glow)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              margin: '0 auto 12px auto'
            }}
          >
            <Package size={24} style={{ color: 'var(--accent-cyan)' }} />
          </div>

          <h3 style={{ fontSize: '1.25rem', fontWeight: 800, color: 'var(--text-heading)', letterSpacing: '-0.01em' }}>
            No Active Subscription Found
          </h3>

          <p style={{ color: 'var(--text-secondary)', fontSize: '0.88rem', maxWidth: '480px', margin: '6px auto 18px auto', lineHeight: '1.5' }}>
            {user?.role === 'ADMIN'
              ? 'Your administrator account currently has no active subscription. You can activate a plan to test lifecycle tracking or manage plan offerings in the Admin Portal.'
              : 'You do not have an active subscription plan right now. Choose a flexible plan from our catalog to start tracking your lifecycle.'}
          </p>

          <div style={{ display: 'inline-flex', gap: '10px', alignItems: 'center', flexWrap: 'wrap', justifyContent: 'center' }}>
            <Link to="/plans" className="btn btn-electric" style={{ padding: '9px 22px', fontSize: '0.85rem' }}>
              Browse Available Plans <ArrowRight size={14} />
            </Link>
            {user?.role === 'ADMIN' && (
              <Link
                to="/admin/plans"
                className="btn btn-secondary"
                style={{
                  padding: '9px 22px',
                  fontSize: '0.85rem'
                }}
              >
                Open Admin Plan Manager
              </Link>
            )}
          </div>
        </div>
      )}

      {/* Payment History Log */}
      <div className="glass-card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <h3 style={{ fontSize: '1.2rem', fontWeight: 800, color: 'var(--text-heading)', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <CreditCard size={20} style={{ color: 'var(--accent-cyan)' }} /> Recent Payment Transactions
          </h3>
          <Link to="/subscriptions" style={{ fontSize: '0.82rem', color: 'var(--accent-cyan)', fontWeight: 700, textDecoration: 'underline' }}>
            View Full History →
          </Link>
        </div>

        {payments.length > 0 ? (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.88rem' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-secondary)' }}>
                  <th style={{ padding: '12px' }}>Txn Reference</th>
                  <th style={{ padding: '12px' }}>Amount</th>
                  <th style={{ padding: '12px' }}>Status</th>
                  <th style={{ padding: '12px' }}>Timestamp</th>
                </tr>
              </thead>
              <tbody>
                {payments.slice(0, 5).map(p => (
                  <tr key={p.paymentId} style={{ borderBottom: '1px solid var(--border-subtle)' }}>
                    <td style={{ padding: '12px', fontFamily: 'monospace', color: 'var(--accent-cyan)', fontWeight: 700 }}>
                      {p.transactionReference}
                    </td>
                    <td style={{ padding: '12px', fontWeight: 700, color: 'var(--text-heading)' }}>
                      ₹{p.amount}
                    </td>
                    <td style={{ padding: '12px' }}>
                      <span className={`badge ${p.status === 'SUCCESS' ? 'badge-active' : 'badge-cancelled'}`}>
                        {p.status}
                      </span>
                    </td>
                    <td style={{ padding: '12px', color: 'var(--text-secondary)' }}>
                      {new Date(p.paymentDate).toLocaleString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', textAlign: 'center', padding: '20px 0' }}>
            No payment transaction logs available yet.
          </p>
        )}
      </div>

    </div>
  );
}
