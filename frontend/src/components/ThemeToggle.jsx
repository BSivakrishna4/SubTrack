import React from 'react';
import { Sun, Moon } from 'lucide-react';
import { useTheme } from '../context/ThemeContext';

export default function ThemeToggle({ className = '', style = {} }) {
  const { theme, toggleTheme, isDark } = useTheme();

  return (
    <button
      type="button"
      onClick={toggleTheme}
      id="theme-toggle-btn"
      aria-label={isDark ? 'Switch to Light Mode' : 'Switch to Dark Mode'}
      title={isDark ? 'Switch to Light Mode' : 'Switch to Dark Mode'}
      className={`theme-toggle-btn ${className}`}
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        position: 'relative',
        width: '42px',
        height: '42px',
        borderRadius: '12px',
        cursor: 'pointer',
        border: '1px solid var(--border-color)',
        background: isDark ? 'rgba(15, 23, 42, 0.75)' : '#ffffff',
        color: isDark ? '#38bdf8' : '#d97706',
        boxShadow: isDark
          ? '0 0 16px rgba(56, 189, 248, 0.2)'
          : '0 2px 10px rgba(0, 0, 0, 0.08), 0 0 14px rgba(245, 158, 11, 0.15)',
        transition: 'all 0.25s cubic-bezier(0.16, 1, 0.3, 1)',
        padding: 0,
        ...style
      }}
    >
      <span
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          transform: isDark ? 'rotate(0deg) scale(1)' : 'rotate(360deg) scale(1)',
          transition: 'transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1)'
        }}
      >
        {isDark ? (
          <Moon size={18} strokeWidth={2.2} />
        ) : (
          <Sun size={18} strokeWidth={2.2} />
        )}
      </span>
    </button>
  );
}
