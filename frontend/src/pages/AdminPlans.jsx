import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/axios';
import {
  Package,
  Plus,
  Trash2,
  Edit3,
  RefreshCw,
  Shield,
  LayoutDashboard,
  History
} from 'lucide-react';

export default function AdminPlans() {
  const { user } = useAuth();
  const [plans, setPlans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [price, setPrice] = useState('');
  const [durationDays, setDurationDays] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [message, setMessage] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchPlans();
  }, []);

  const fetchPlans = async () => {
    try {
      const res = await api.get('/api/plans/all');
      setPlans(res.data || []);
    } catch (err) {
      setError('Failed to fetch admin plan list.');
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setMessage(null);
    setError(null);
    const payload = {
      name,
      description,
      price: parseFloat(price),
      durationDays: parseInt(durationDays),
      active: true
    };

    try {
      if (editingId) {
        await api.put(`/api/plans/${editingId}`, payload);
        setMessage('Plan updated successfully!');
      } else {
        await api.post('/api/plans', payload);
        setMessage('New plan created successfully!');
      }
      resetForm();
      fetchPlans();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save plan.');
    }
  };

  const handleEdit = (plan) => {
    setEditingId(plan.id);
    setName(plan.name);
    setDescription(plan.description);
    setPrice(plan.price);
    setDurationDays(plan.durationDays);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to permanently delete this plan?')) return;
    try {
      await api.delete(`/api/plans/${id}`);
      setMessage('Plan deleted permanently from catalog.');
      fetchPlans();
    } catch (err) {
      setError('Failed to delete plan.');
    }
  };

  const resetForm = () => {
    setEditingId(null);
    setName('');
    setDescription('');
    setPrice('');
    setDurationDays('');
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '80px 0', color: 'var(--text-secondary)' }}>
        <RefreshCw className="spin" size={36} style={{ marginBottom: '16px', color: 'var(--accent-purple)' }} />
        <p style={{ fontSize: '1rem', fontWeight: 600 }}>Loading Admin Plan Portal...</p>
      </div>
    );
  }

  const activeCount = plans.filter(p => p.active).length;

  return (
    <div style={{ maxWidth: '1120px', margin: '0 auto', padding: '24px 20px' }}>
      
      {/* Top Header & Role Badge */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px', marginBottom: '22px' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <Shield size={26} color="var(--accent-purple)" />
            <h1 style={{ fontSize: '1.85rem', fontWeight: 900, letterSpacing: '-0.02em', color: 'var(--text-heading)' }}>
              Admin Subscription Plan Portal
            </h1>
          </div>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginTop: '4px' }}>
            Create, update, and manage subscription pricing tiers across the SubTrack ecosystem.
          </p>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <span style={{ padding: '5px 14px', borderRadius: '9999px', background: 'rgba(168, 85, 247, 0.15)', border: '1px solid rgba(168, 85, 247, 0.4)', color: 'var(--accent-purple)', fontSize: '0.78rem', fontWeight: 800 }}>
            🛡️ System Administrator Mode
          </span>
        </div>
      </div>

      {/* Admin Sub-Nav Bar */}
      <div className="glass-card" style={{ padding: '14px 20px', marginBottom: '24px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '14px', borderRadius: '14px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }}>
          <span style={{ fontSize: '0.78rem', fontWeight: 800, textTransform: 'uppercase', color: 'var(--text-secondary)', letterSpacing: '0.05em' }}>
            Quick Links:
          </span>
          <Link to="/dashboard" className="btn btn-outline-blue" style={{ padding: '7px 16px', fontSize: '0.8rem' }}>
            <LayoutDashboard size={14} /> Dashboard
          </Link>
          <Link to="/plans" className="btn btn-electric" style={{ padding: '7px 16px', fontSize: '0.8rem' }}>
            <Package size={14} /> View Live Catalog
          </Link>
          <Link to="/subscriptions" className="btn btn-outline-blue" style={{ padding: '7px 16px', fontSize: '0.8rem', color: 'var(--accent-purple)', borderColor: 'rgba(168, 85, 247, 0.4)' }}>
            <History size={14} /> View All Users History →
          </Link>
        </div>

        <div style={{ display: 'flex', gap: '14px', fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
          <span>Total Plans: <strong style={{ color: 'var(--text-heading)' }}>{plans.length}</strong></span>
          <span>•</span>
          <span>Active Tiers: <strong style={{ color: '#10b981' }}>{activeCount}</strong></span>
        </div>
      </div>

      {message && <div className="alert alert-success" style={{ marginBottom: '20px' }}>{message}</div>}
      {error && <div className="alert alert-error" style={{ marginBottom: '20px' }}>{error}</div>}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))', gap: '20px', alignItems: 'flex-start' }}>
        
        {/* Form Card */}
        <div className="glass-card" style={{ padding: '20px 22px', borderRadius: '16px' }}>
          <h3 style={{ fontSize: '1.15rem', fontWeight: 800, color: 'var(--text-heading)', marginBottom: '18px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Plus size={18} color="var(--accent-cyan)" /> {editingId ? 'Edit Subscription Plan' : 'Create New Subscription Plan'}
          </h3>

          <form onSubmit={handleSave}>
            <div style={{ marginBottom: '16px' }}>
              <label className="auth-label">Plan Name</label>
              <input
                type="text"
                className="auth-input"
                placeholder="e.g. Basic Monthly"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
              />
            </div>

            <div style={{ marginBottom: '16px' }}>
              <label className="auth-label">Description</label>
              <input
                type="text"
                className="auth-input"
                placeholder="Brief feature description"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              />
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '20px' }}>
              <div>
                <label className="auth-label">Price (₹)</label>
                <input
                  type="number"
                  step="0.01"
                  className="auth-input"
                  placeholder="199"
                  value={price}
                  onChange={(e) => setPrice(e.target.value)}
                  required
                />
              </div>

              <div>
                <label className="auth-label">Duration (Days)</label>
                <input
                  type="number"
                  className="auth-input"
                  placeholder="30"
                  value={durationDays}
                  onChange={(e) => setDurationDays(e.target.value)}
                  required
                />
              </div>
            </div>

            <div style={{ display: 'flex', gap: '12px' }}>
              <button type="submit" className="btn btn-electric" style={{ flex: 1, padding: '10px' }}>
                {editingId ? 'Update Plan Tiers' : 'Create New Plan'}
              </button>
              {editingId && (
                <button type="button" onClick={resetForm} className="btn btn-outline-blue" style={{ padding: '10px 18px' }}>
                  Cancel
                </button>
              )}
            </div>
          </form>
        </div>

        {/* Existing Plans Offerings Card */}
        <div className="glass-card" style={{ padding: '20px 22px', borderRadius: '16px' }}>
          <h3 style={{ fontSize: '1.15rem', fontWeight: 800, color: 'var(--text-heading)', marginBottom: '18px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Package size={18} color="var(--accent-purple)" /> Existing Plan Offerings ({plans.length})
          </h3>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {plans.map((p) => (
              <div
                key={p.id}
                style={{
                  padding: '14px 16px',
                  borderRadius: '12px',
                  background: 'var(--bg-surface)',
                  border: '1px solid var(--border-color)',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  flexWrap: 'wrap',
                  gap: '12px'
                }}
              >
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <span style={{ fontWeight: 800, color: 'var(--text-heading)', fontSize: '0.95rem' }}>{p.name}</span>
                    <span className={`badge ${p.active ? 'badge-active' : 'badge-cancelled'}`} style={{ fontSize: '0.65rem' }}>
                      {p.active ? 'ACTIVE' : 'INACTIVE'}
                    </span>
                  </div>
                  <p style={{ color: 'var(--text-secondary)', fontSize: '0.8rem', margin: '3px 0 0 0' }}>{p.description}</p>
                  <div style={{ fontSize: '0.78rem', color: 'var(--accent-cyan)', marginTop: '4px', fontWeight: 600 }}>
                    ₹{p.price} • {p.durationDays} Days Duration
                  </div>
                </div>

                <div style={{ display: 'flex', gap: '8px' }}>
                  <button
                    onClick={() => handleEdit(p)}
                    className="btn btn-outline-blue"
                    style={{ padding: '6px 12px', fontSize: '0.75rem' }}
                  >
                    <Edit3 size={13} /> Edit
                  </button>
                  <button
                    onClick={() => handleDelete(p.id)}
                    className="btn"
                    style={{
                      padding: '6px 12px',
                      fontSize: '0.75rem',
                      background: 'rgba(239, 68, 68, 0.1)',
                      border: '1px solid rgba(239, 68, 68, 0.3)',
                      color: '#ef4444'
                    }}
                    title="Permanently Delete Plan"
                  >
                    <Trash2 size={13} /> Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>

      </div>
    </div>
  );
}
