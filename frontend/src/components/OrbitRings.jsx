import React from 'react';

export default function OrbitRings() {
  return (
    <div style={{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)', pointerEvents: 'none', zIndex: 2 }}>
      {/* Outer Ring */}
      <div
        style={{
          width: '640px',
          height: '420px',
          borderRadius: '50%',
          border: '1px stroke rgba(56, 189, 248, 0.18)',
          outline: '1px dashed rgba(56, 189, 248, 0.15)',
          boxShadow: '0 0 40px rgba(56, 189, 248, 0.08)',
          animation: 'spinSlow 45s linear infinite',
          transformOrigin: 'center center'
        }}
      />

      {/* Middle Ring */}
      <div
        style={{
          position: 'absolute',
          top: '50%',
          left: '50%',
          width: '520px',
          height: '340px',
          transform: 'translate(-50%, -50%)',
          borderRadius: '50%',
          border: '1px solid rgba(99, 102, 241, 0.22)',
          boxShadow: 'inset 0 0 30px rgba(99, 102, 241, 0.1)',
          animation: 'spinReverse 35s linear infinite'
        }}
      />

      {/* Inner Glowing Accent Ring */}
      <div
        style={{
          position: 'absolute',
          top: '50%',
          left: '50%',
          width: '380px',
          height: '240px',
          transform: 'translate(-50%, -50%)',
          borderRadius: '50%',
          border: '1px dashed rgba(168, 85, 247, 0.25)',
          animation: 'spinSlow 25s linear infinite'
        }}
      />
    </div>
  );
}
