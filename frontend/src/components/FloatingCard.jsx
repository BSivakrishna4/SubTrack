import React from 'react';

export default function FloatingCard({
  icon: Icon,
  title,
  subtitle,
  position,
  animationName = 'floatSlow1',
  duration = '7s',
  animationDelay = '0s'
}) {
  return (
    <div
      style={{
        position: 'absolute',
        ...position,
        zIndex: 10,
        background: 'var(--bg-card)',
        backdropFilter: 'blur(16px)',
        WebkitBackdropFilter: 'blur(16px)',
        border: '1px solid var(--border-color)',
        boxShadow: 'var(--card-shadow)',
        borderRadius: '16px',
        padding: '12px 18px',
        display: 'flex',
        alignItems: 'center',
        gap: '12px',
        animation: `${animationName} ${duration} ease-in-out infinite`,
        animationDelay: animationDelay,
        whiteSpace: 'nowrap',
        transition: 'transform 0.3s ease, border-color 0.3s ease, background 0.3s ease'
      }}
    >
      {Icon && (
        <div style={{ background: 'rgba(2, 132, 199, 0.12)', padding: '8px', borderRadius: '10px', display: 'flex', color: 'var(--accent-cyan)' }}>
          <Icon size={18} />
        </div>
      )}
      <div>
        <div style={{ fontSize: '0.85rem', fontWeight: 700, color: 'var(--text-heading)' }}>{title}</div>
        {subtitle && <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>{subtitle}</div>}
      </div>
    </div>
  );
}
