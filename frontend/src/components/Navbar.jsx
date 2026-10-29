import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { LogOut, LayoutDashboard, Package, History, Shield, LogIn, UserPlus } from 'lucide-react';
import ThemeToggle from './ThemeToggle';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 20);
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path) => location.pathname === path;

  return (
    <nav
      style={{
        position: 'sticky',
        top: 0,
        left: 0,
        right: 0,
        zIndex: 100,
        background: scrolled ? 'var(--nav-bg-scrolled)' : 'var(--nav-bg)',
        backdropFilter: 'blur(16px)',
        WebkitBackdropFilter: 'blur(16px)',
        borderBottom: '1px solid var(--nav-border)',
        transition: 'background 0.3s ease, border-color 0.3s ease, padding 0.3s ease',
        padding: scrolled ? '12px 24px' : '16px 24px'
      }}
    >
      <div
        style={{
          maxWidth: '1320px',
          margin: '0 auto',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          flexWrap: 'wrap',
          gap: '12px'
        }}
      >
        {/* Left: Brand Logo & Tagline */}
        <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: '12px', textDecoration: 'none' }}>
          <div
            style={{
              width: '40px',
              height: '40px',
              borderRadius: '12px',
              background: 'linear-gradient(135deg, #0284c7, #6366f1)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              boxShadow: '0 0 20px rgba(56, 189, 248, 0.4)',
              fontWeight: 900,
              fontSize: '1.4rem',
              color: '#ffffff'
            }}
          >
            S
          </div>
          <div style={{ display: 'flex', flexDirection: 'column' }}>
            <div style={{ fontSize: '1.35rem', fontWeight: 900, color: 'var(--text-heading)', letterSpacing: '-0.02em', lineHeight: 1 }}>
              Sub<span style={{ color: 'var(--accent-cyan)' }}>Track</span>
            </div>
            <div style={{ fontSize: '0.62rem', color: 'var(--accent-cyan)', letterSpacing: '0.1em', textTransform: 'uppercase', marginTop: '3px', fontWeight: 700 }}>
              Subscribe • Track • Grow
            </div>
          </div>
        </Link>

        {/* Navigation Links for Authenticated Users */}
        {user && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
            <Link
              to="/dashboard"
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '6px',
                padding: '8px 16px',
                borderRadius: '10px',
                fontSize: '0.85rem',
                fontWeight: 700,
                color: isActive('/dashboard') ? 'var(--accent-cyan)' : 'var(--nav-link)',
                background: isActive('/dashboard') ? 'rgba(2, 132, 199, 0.12)' : 'transparent',
                border: isActive('/dashboard') ? '1px solid var(--border-glow)' : '1px solid transparent',
                transition: 'all 0.2s ease'
              }}
            >
              <LayoutDashboard size={15} /> Dashboard
            </Link>

            <Link
              to="/plans"
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '6px',
                padding: '8px 16px',
                borderRadius: '10px',
                fontSize: '0.85rem',
                fontWeight: 700,
                color: isActive('/plans') ? 'var(--accent-cyan)' : 'var(--nav-link)',
                background: isActive('/plans') ? 'rgba(2, 132, 199, 0.12)' : 'transparent',
                border: isActive('/plans') ? '1px solid var(--border-glow)' : '1px solid transparent',
                transition: 'all 0.2s ease'
              }}
            >
              <Package size={15} /> Plans Catalog
            </Link>

            <Link
              to="/subscriptions"
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '6px',
                padding: '8px 16px',
                borderRadius: '10px',
                fontSize: '0.85rem',
                fontWeight: 700,
                color: isActive('/subscriptions') ? 'var(--accent-cyan)' : 'var(--nav-link)',
                background: isActive('/subscriptions') ? 'rgba(2, 132, 199, 0.12)' : 'transparent',
                border: isActive('/subscriptions') ? '1px solid var(--border-glow)' : '1px solid transparent',
                transition: 'all 0.2s ease'
              }}
            >
              <History size={15} /> My History
            </Link>

            {user.role === 'ADMIN' && (
              <Link
                to="/admin/plans"
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '6px',
                  padding: '8px 16px',
                  borderRadius: '10px',
                  fontSize: '0.85rem',
                  fontWeight: 800,
                  color: isActive('/admin/plans') ? '#c084fc' : 'var(--accent-purple)',
                  background: isActive('/admin/plans') ? 'rgba(168, 85, 247, 0.2)' : 'rgba(168, 85, 247, 0.08)',
                  border: '1px solid rgba(168, 85, 247, 0.4)',
                  boxShadow: '0 0 12px rgba(168, 85, 247, 0.2)',
                  transition: 'all 0.2s ease'
                }}
              >
                <Shield size={15} /> Admin Portal
              </Link>
            )}
          </div>
        )}

        {/* Right Corner User Actions / Auth Buttons + Sun/Moon Theme Toggle */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          {/* THEME TOGGLE: Placed in the navbar, accessible in all states */}
          <ThemeToggle />

          {user ? (
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', lineHeight: 1.2 }}>
                <span style={{ fontSize: '0.85rem', color: 'var(--text-heading)', fontWeight: 700 }}>
                  {user.name || user.email}
                </span>
                <span
                  style={{
                    fontSize: '0.65rem',
                    fontWeight: 800,
                    textTransform: 'uppercase',
                    color: user.role === 'ADMIN' ? 'var(--accent-purple)' : 'var(--accent-cyan)',
                    letterSpacing: '0.05em'
                  }}
                >
                  {user.role === 'ADMIN' ? '🛡️ Administrator' : '👤 Regular User'}
                </span>
              </div>
              <button
                onClick={handleLogout}
                className="btn btn-outline-blue"
                style={{
                  padding: '8px 16px',
                  fontSize: '0.82rem'
                }}
              >
                <LogOut size={14} /> Logout
              </button>
            </div>
          ) : (
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <Link
                to="/login"
                className="btn btn-outline-blue"
                style={{
                  padding: '9px 20px',
                  fontSize: '0.85rem',
                  fontWeight: 700,
                  letterSpacing: '0.04em',
                  textTransform: 'uppercase'
                }}
              >
                <LogIn size={15} /> LOGIN
              </Link>
              <Link
                to="/register"
                className="btn btn-electric"
                style={{
                  padding: '9px 22px',
                  fontSize: '0.85rem',
                  fontWeight: 800,
                  letterSpacing: '0.04em',
                  textTransform: 'uppercase'
                }}
              >
                <UserPlus size={15} /> REGISTER
              </Link>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
}
