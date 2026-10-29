import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { UserPlus, User, Mail, Lock, Shield, ArrowLeft } from 'lucide-react';
import ThemeToggle from '../components/ThemeToggle';

export default function Register() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [role, setRole] = useState('USER');
  const [error, setError] = useState('');
  const { register, loading } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (password !== confirmPassword) {
      setError('Passwords do not match. Please verify your confirm password.');
      return;
    }

    const res = await register(name, email, password, role);
    if (res.success) {
      navigate('/dashboard');
    } else {
      setError(res.error);
    }
  };

  return (
    <div className="auth-container">
      {/* Background glow */}
      <div className="auth-ambient-glow" />

      {/* Top Controls: Back button & Theme toggle */}
      <div style={{ position: 'absolute', top: '20px', left: '20px', right: '20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', zIndex: 20 }}>
        <Link to="/" className="auth-back-btn" style={{ position: 'static' }}>
          <ArrowLeft size={14} />
          <span>Back to Home</span>
        </Link>
        <ThemeToggle />
      </div>

      {/* Form Card */}
      <div className="auth-card">
        {/* Header */}
        <div className="auth-header">
          <div className="auth-logo-badge">S</div>
          <h1 className="auth-title">Create SubTrack Account</h1>
          <p className="auth-subtitle">Start tracking and automating your subscriptions</p>
        </div>

        {error && (
          <div className="alert alert-error" style={{ marginBottom: '14px' }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          {/* Row 1: Full Name & Account Role */}
          <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '14px', marginBottom: '15px' }}>
            <div className="auth-form-group" style={{ marginBottom: 0 }}>
              <label className="auth-label">
                <User size={14} style={{ color: 'var(--accent-cyan)' }} />
                Full Name
              </label>
              <input
                type="text"
                className="auth-input"
                placeholder="John Doe"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
              />
            </div>

            <div className="auth-form-group" style={{ marginBottom: 0 }}>
              <label className="auth-label">
                <Shield size={14} style={{ color: 'var(--accent-cyan)' }} />
                Role
              </label>
              <select
                className="auth-select"
                value={role}
                onChange={(e) => setRole(e.target.value)}
              >
                <option value="USER">User</option>
                <option value="ADMIN">Admin</option>
              </select>
            </div>
          </div>

          {/* Row 2: Email Address */}
          <div className="auth-form-group" style={{ marginBottom: '15px' }}>
            <label className="auth-label">
              <Mail size={14} style={{ color: 'var(--accent-cyan)' }} />
              Email Address
            </label>
            <input
              type="email"
              className="auth-input"
              placeholder="john@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          {/* Row 3: Password & Confirm Password */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px', marginBottom: '18px' }}>
            <div className="auth-form-group" style={{ marginBottom: 0 }}>
              <label className="auth-label">
                <Lock size={13} style={{ color: 'var(--accent-cyan)' }} />
                Password
              </label>
              <input
                type="password"
                className="auth-input"
                placeholder="Min 6 chars"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                minLength={6}
                required
              />
            </div>

            <div className="auth-form-group" style={{ marginBottom: 0 }}>
              <label className="auth-label">
                <Lock size={13} style={{ color: 'var(--accent-cyan)' }} />
                Confirm Password
              </label>
              <input
                type="password"
                className="auth-input"
                placeholder="Re-enter password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                minLength={6}
                required
              />
            </div>
          </div>

          <button type="submit" disabled={loading} className="auth-btn">
            <UserPlus size={16} />
            <span>{loading ? 'Creating Account...' : 'Complete Registration'}</span>
          </button>
        </form>

        <div className="auth-footer-link">
          Already registered? <Link to="/login">Sign In</Link>
        </div>
      </div>

      <div style={{ textAlign: 'center', marginTop: '14px', fontSize: '0.72rem', color: 'var(--text-muted)', zIndex: 10 }}>
        SubTrack Microservices Engine • SOA Project
      </div>
    </div>
  );
}
