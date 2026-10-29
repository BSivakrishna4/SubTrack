import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/axios';
import { Package, Check, CreditCard, RefreshCw } from 'lucide-react';

export default function Plans() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [plans, setPlans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activatingPlanId, setActivatingPlanId] = useState(null);
  const [simulateFailure, setSimulateFailure] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchPlans();
  }, []);

  const fetchPlans = async () => {
    try {
      const response = await api.get('/api/plans');
      setPlans(response.data || []);
    } catch (err) {
      setError('Failed to fetch plan catalog from Subscription Service.');
    } finally {
      setLoading(false);
    }
  };

  const handleActivate = async (planId) => {
    if (!user) {
      navigate('/login');
      return;
    }

    setActivatingPlanId(planId);
    setError(null);

    try {
      const response = await api.post('/api/subscriptions/activate', {
        userId: user.userId,
        planId: planId,
        simulatePaymentFailure: simulateFailure
      });

      alert(`Subscription Activated Successfully!\nStatus: ${response.data.status}\nTxn Ref: ${response.data.lastPayment?.transactionReference}`);
      navigate('/dashboard');
    } catch (err) {
      const msg = err.response?.data?.message || 'Activation failed. Mock payment was rejected.';
      setError(msg);
    } finally {
      setActivatingPlanId(null);
    }
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '60px 0', color: 'var(--text-secondary)' }}>
        <RefreshCw className="spin" size={32} style={{ marginBottom: '16px', color: 'var(--accent-cyan)' }} />
        <p>Loading Subscription Plans...</p>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '1060px', margin: '0 auto', padding: '24px 20px' }}>
      <div style={{ textAlign: 'center', marginBottom: '24px' }}>
        <span style={{ fontSize: '0.8rem', color: 'var(--accent-cyan)', textTransform: 'uppercase', letterSpacing: '0.1em', fontWeight: 700 }}>
          SUBSCRIPTION CATALOG
        </span>
        <h1 style={{ fontSize: '1.85rem', fontWeight: 800, marginTop: '2px', color: 'var(--text-heading)' }}>
          Choose Your SubTrack Plan
        </h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.88rem', maxWidth: '540px', margin: '4px auto 0 auto' }}>
          Select a flexible plan tailored for your subscription requirements. Process instant mock payments and track recurring billing.
        </p>

        {/* Mock Payment Failure Simulator Toggle */}
        <div style={{ display: 'inline-flex', alignItems: 'center', gap: '8px', marginTop: '14px', background: 'var(--bg-surface)', padding: '6px 14px', borderRadius: '999px', border: '1px solid var(--border-color)' }}>
          <input
            type="checkbox"
            id="simulateFail"
            checked={simulateFailure}
            onChange={(e) => setSimulateFailure(e.target.checked)}
            style={{ cursor: 'pointer', accentColor: '#ef4444' }}
          />
          <label htmlFor="simulateFail" style={{ fontSize: '0.8rem', color: simulateFailure ? '#ef4444' : 'var(--text-secondary)', cursor: 'pointer', fontWeight: 500 }}>
            Simulate Mock Payment Failure (For QA / Testing)
          </label>
        </div>
      </div>

      {error && <div className="alert alert-error" style={{ maxWidth: '600px', margin: '0 auto 20px auto' }}>{error}</div>}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '20px', alignItems: 'stretch' }}>
        {plans.map((plan) => (
          <div
            key={plan.id}
            className="glass-card"
            style={{
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'space-between',
              padding: '24px',
              borderRadius: '16px',
              position: 'relative'
            }}
          >
            <div>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '14px' }}>
                <div style={{
                  width: '38px',
                  height: '38px',
                  borderRadius: '10px',
                  background: 'rgba(2, 132, 199, 0.12)',
                  border: '1px solid var(--border-glow)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center'
                }}>
                  <Package size={20} color="var(--accent-cyan)" />
                </div>
                <span className="badge badge-active" style={{ fontSize: '0.72rem', padding: '4px 10px' }}>
                  {plan.durationDays} Days Duration
                </span>
              </div>

              <h3 style={{ fontSize: '1.25rem', fontWeight: 800, marginBottom: '6px', color: 'var(--text-heading)' }}>
                {plan.name}
              </h3>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '14px', minHeight: '38px', lineHeight: '1.45' }}>
                {plan.description}
              </p>

              <div style={{ fontSize: '1.75rem', fontWeight: 800, marginBottom: '16px', color: 'var(--text-heading)', display: 'flex', alignItems: 'baseline', gap: '4px' }}>
                ₹{plan.price} <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', fontWeight: 400 }}>/ {plan.durationDays} days</span>
              </div>

              <ul style={{ listStyle: 'none', padding: 0, margin: '0 0 20px 0', display: 'flex', flexDirection: 'column', gap: '10px' }}>
                <li style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.82rem', color: 'var(--text-primary)' }}>
                  <Check size={14} color="#10b981" style={{ flexShrink: 0 }} /> SubTrack Lifecycle APIs
                </li>
                <li style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.82rem', color: 'var(--text-primary)' }}>
                  <Check size={14} color="#10b981" style={{ flexShrink: 0 }} /> OpenFeign Mock Payment
                </li>
                <li style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.82rem', color: 'var(--text-primary)' }}>
                  <Check size={14} color="#10b981" style={{ flexShrink: 0 }} /> Automated Expiry Scheduler
                </li>
              </ul>
            </div>

            <button
              onClick={() => handleActivate(plan.id)}
              className="btn btn-electric"
              style={{ width: '100%', padding: '11px 16px', fontSize: '0.88rem', borderRadius: '12px', marginTop: 'auto' }}
              disabled={activatingPlanId === plan.id}
            >
              {activatingPlanId === plan.id ? (
                'Processing Mock Payment...'
              ) : (
                <><CreditCard size={16} /> Activate {plan.name}</>
              )}
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
