import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../api/axios';
import StatusBadge from '../components/StatusBadge';
import { History, CreditCard, RefreshCw, User, Users, Trash2 } from 'lucide-react';

export default function MySubscriptions() {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';
  const [viewMode, setViewMode] = useState(isAdmin ? 'all' : 'my'); // 'my' or 'all'
  const [subscriptions, setSubscriptions] = useState([]);
  const [payments, setPayments] = useState([]);
  const [userMap, setUserMap] = useState({});
  const [loading, setLoading] = useState(true);

  const handleDeleteSubscription = async (subId) => {
    if (!window.confirm(`Are you sure you want to permanently delete subscription #${subId}?`)) return;
    try {
      await api.delete(`/api/subscriptions/${subId}`);
      fetchData();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete subscription record.');
    }
  };

  useEffect(() => {
    if (!user) return;
    fetchData();
  }, [user, viewMode]);

  const fetchData = async () => {
    setLoading(true);
    try {
      if (viewMode === 'all' && isAdmin) {
        const [subRes, payRes, usersRes] = await Promise.all([
          api.get('/api/subscriptions/all'),
          api.get('/api/payments/all'),
          api.get('/api/users')
        ]);
        setSubscriptions(subRes.data || []);
        setPayments(payRes.data || []);

        const map = {};
        (usersRes.data || []).forEach(u => {
          map[u.id] = u.name;
        });
        setUserMap(map);
      } else {
        const [subRes, payRes] = await Promise.all([
          api.get(`/api/subscriptions/user/${user.userId}`),
          api.get(`/api/payments/user/${user.userId}`)
        ]);
        setSubscriptions(subRes.data || []);
        setPayments(payRes.data || []);
      }
    } catch (err) {
      console.error('Failed to fetch history:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '60px 0', color: 'var(--text-secondary)' }}>
        <RefreshCw className="spin" size={32} style={{ marginBottom: '16px', color: 'var(--accent-cyan)' }} />
        <p>Loading History Records...</p>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '1120px', margin: '0 auto', padding: '24px 20px' }}>
      
      {/* Header & View Mode Switcher */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '16px', marginBottom: '24px' }}>
        <div>
          <h1 style={{ fontSize: '1.85rem', fontWeight: 800, color: 'var(--text-heading)', letterSpacing: '-0.02em' }}>
            {viewMode === 'all' ? 'All Users Subscriptions & Transactions' : 'My Subscription & Payment History'}
          </h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginTop: '4px' }}>
            {viewMode === 'all'
              ? 'Administrator master view showing all subscription plans and payments taken by users.'
              : 'Personal historical record of all your plan activations, renewals, and transactions.'}
          </p>
        </div>

        {isAdmin && (
          <div style={{ display: 'inline-flex', background: 'var(--bg-surface)', padding: '4px', borderRadius: '12px', border: '1px solid var(--border-color)' }}>
            <button
              onClick={() => setViewMode('all')}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
                padding: '7px 16px',
                borderRadius: '8px',
                fontSize: '0.82rem',
                fontWeight: 700,
                border: 'none',
                cursor: 'pointer',
                background: viewMode === 'all' ? 'linear-gradient(135deg, #a855f7, #6366f1)' : 'transparent',
                color: viewMode === 'all' ? '#ffffff' : 'var(--text-secondary)',
                transition: 'all 0.2s ease'
              }}
            >
              <Users size={15} /> All Users Master View
            </button>
            <button
              onClick={() => setViewMode('my')}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
                padding: '7px 16px',
                borderRadius: '8px',
                fontSize: '0.82rem',
                fontWeight: 700,
                border: 'none',
                cursor: 'pointer',
                background: viewMode === 'my' ? 'rgba(2, 132, 199, 0.15)' : 'transparent',
                color: viewMode === 'my' ? 'var(--accent-cyan)' : 'var(--text-secondary)',
                transition: 'all 0.2s ease'
              }}
            >
              <User size={15} /> My Personal History
            </button>
          </div>
        )}
      </div>

      {/* Box 1: Subscription Lifecycle Events */}
      <div className="glass-card" style={{ padding: '20px 24px', marginBottom: '24px', borderRadius: '16px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
          <h2 style={{ fontSize: '1.15rem', fontWeight: 800, color: 'var(--text-heading)', display: 'flex', alignItems: 'center', gap: '8px', margin: 0 }}>
            <History size={19} color="var(--accent-cyan)" />
            {viewMode === 'all' ? 'All User Subscription Plans' : 'My Subscription Lifecycle Events'}
          </h2>
          <span className="badge badge-secondary" style={{ fontSize: '0.75rem' }}>
            {subscriptions.length} {subscriptions.length === 1 ? 'Record' : 'Records'}
          </span>
        </div>

        {subscriptions.length > 0 ? (
          <div style={{ overflowX: 'auto', maxHeight: '420px', overflowY: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.86rem' }}>
              <thead style={{ position: 'sticky', top: 0, background: 'var(--bg-card)', backdropFilter: 'blur(8px)', zIndex: 1 }}>
                <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-secondary)' }}>
                  <th style={{ padding: '10px 14px', fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>ID</th>
                  {viewMode === 'all' && (
                    <th style={{ padding: '10px 14px', fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--accent-purple)' }}>Username</th>
                  )}
                  <th style={{ padding: '10px 14px', fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Plan Name</th>
                  <th style={{ padding: '10px 14px', fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Price</th>
                  <th style={{ padding: '10px 14px', fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Start Date</th>
                  <th style={{ padding: '10px 14px', fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Expiry Date</th>
                  <th style={{ padding: '10px 14px', fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Status</th>
                  <th style={{ padding: '10px 14px', fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.05em', textAlign: 'center' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {subscriptions.map(s => (
                  <tr key={s.id} style={{ borderBottom: '1px solid var(--border-subtle)', transition: 'background 0.15s ease' }}>
                    <td style={{ padding: '10px 14px', fontWeight: 700, color: 'var(--accent-cyan)' }}>#{s.id}</td>
                    {viewMode === 'all' && (
                      <td style={{ padding: '10px 14px', fontWeight: 700, color: 'var(--text-heading)' }}>
                        {userMap[s.userId] || `User #${s.userId}`}
                      </td>
                    )}
                    <td style={{ padding: '10px 14px', fontWeight: 600, color: 'var(--text-heading)' }}>{s.plan?.name}</td>
                    <td style={{ padding: '10px 14px', fontWeight: 700, color: 'var(--text-heading)' }}>₹{s.plan?.price}</td>
                    <td style={{ padding: '10px 14px', color: 'var(--text-secondary)' }}>{new Date(s.startDate).toLocaleDateString()}</td>
                    <td style={{ padding: '10px 14px', color: 'var(--text-secondary)' }}>{new Date(s.expiryDate).toLocaleDateString()}</td>
                    <td style={{ padding: '10px 14px' }}>
                      <StatusBadge status={s.status} />
                    </td>
                    <td style={{ padding: '10px 14px', textAlign: 'center' }}>
                      {s.status === 'CANCELLED' ? (
                        <button
                          onClick={() => handleDeleteSubscription(s.id)}
                          style={{
                            background: 'rgba(239, 68, 68, 0.12)',
                            border: '1px solid rgba(239, 68, 68, 0.3)',
                            color: '#ef4444',
                            padding: '4px 10px',
                            borderRadius: '8px',
                            cursor: 'pointer',
                            fontSize: '0.75rem',
                            fontWeight: 600,
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: '4px',
                            transition: 'all 0.2s ease'
                          }}
                          title="Permanently remove cancelled subscription record"
                        >
                          <Trash2 size={13} /> Remove
                        </button>
                      ) : (
                        <span style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>—</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div style={{ textAlign: 'center', padding: '32px 16px', color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
            <History size={32} style={{ margin: '0 auto 10px auto', color: 'var(--text-muted)' }} />
            <p>No subscription lifecycle records found.</p>
          </div>
        )}
      </div>

      {/* Box 2: Payment Records */}
      <div className="glass-card" style={{ padding: '20px 24px', borderRadius: '16px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
          <h2 style={{ fontSize: '1.15rem', fontWeight: 800, color: 'var(--text-heading)', display: 'flex', alignItems: 'center', gap: '8px', margin: 0 }}>
            <CreditCard size={19} color="var(--accent-purple)" />
            {viewMode === 'all' ? 'All User Payment Records' : 'My Payment Records'}
          </h2>
          <span className="badge badge-secondary" style={{ fontSize: '0.75rem' }}>
            {payments.length} {payments.length === 1 ? 'Transaction' : 'Transactions'}
          </span>
        </div>

        {payments.length > 0 ? (
          <div style={{ overflowX: 'auto', maxHeight: '420px', overflowY: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.86rem' }}>
              <thead style={{ position: 'sticky', top: 0, background: 'var(--bg-card)', backdropFilter: 'blur(8px)', zIndex: 1 }}>
                <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-secondary)' }}>
                  <th style={{ padding: '10px 14px', fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Txn Reference</th>
                  {viewMode === 'all' && (
                    <th style={{ padding: '10px 14px', fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--accent-purple)' }}>Username</th>
                  )}
                  <th style={{ padding: '10px 14px', fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Amount</th>
                  <th style={{ padding: '10px 14px', fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Status</th>
                  <th style={{ padding: '10px 14px', fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Timestamp</th>
                </tr>
              </thead>
              <tbody>
                {payments.map(p => (
                  <tr key={p.paymentId} style={{ borderBottom: '1px solid var(--border-subtle)', transition: 'background 0.15s ease' }}>
                    <td style={{ padding: '10px 14px', fontFamily: 'monospace', color: 'var(--accent-purple)', fontWeight: 700 }}>
                      {p.transactionReference}
                    </td>
                    {viewMode === 'all' && (
                      <td style={{ padding: '10px 14px', fontWeight: 700, color: 'var(--text-heading)' }}>
                        {userMap[p.userId] || `User #${p.userId}`}
                      </td>
                    )}
                    <td style={{ padding: '10px 14px', fontWeight: 700, color: 'var(--text-heading)' }}>
                      ₹{p.amount}
                    </td>
                    <td style={{ padding: '10px 14px' }}>
                      <span className={`badge ${p.status === 'SUCCESS' ? 'badge-active' : 'badge-cancelled'}`}>
                        {p.status}
                      </span>
                    </td>
                    <td style={{ padding: '10px 14px', color: 'var(--text-secondary)' }}>
                      {new Date(p.paymentDate).toLocaleString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div style={{ textAlign: 'center', padding: '32px 16px', color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
            <CreditCard size={32} style={{ margin: '0 auto 10px auto', color: 'var(--text-muted)' }} />
            <p>No payment transaction logs found.</p>
          </div>
        )}
      </div>
    </div>
  );
}
