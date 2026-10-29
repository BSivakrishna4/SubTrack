import React, { useMemo } from 'react';

export default function OrbitParticles({ count = 26 }) {
  // Generate randomized particle parameters once
  const particles = useMemo(() => {
    return Array.from({ length: count }).map((_, i) => {
      const radiusX = 260 + (i % 5) * 45; // Varying orbital radiuses
      const radiusY = 160 + (i % 4) * 35;
      const duration = 14 + (i % 7) * 4; // Varying orbit speeds (14s to 38s)
      const delay = -(i * 1.5); // Staggered initial positions
      const size = 3 + (i % 4) * 2; // Particle diameter (3px to 9px)
      const opacity = 0.35 + (i % 5) * 0.15;
      const isTwinkle = i % 3 === 0;
      const color = i % 2 === 0 ? '#38bdf8' : (i % 3 === 0 ? '#c084fc' : '#6366f1');

      return {
        id: i,
        radiusX,
        radiusY,
        duration,
        delay,
        size,
        opacity,
        isTwinkle,
        color
      };
    });
  }, [count]);

  return (
    <div style={{ position: 'absolute', inset: 0, pointerEvents: 'none', zIndex: 5, overflow: 'hidden' }}>
      {particles.map((p) => (
        <div
          key={p.id}
          style={{
            position: 'absolute',
            top: '50%',
            left: '50%',
            width: `${p.size}px`,
            height: `${p.size}px`,
            borderRadius: '50%',
            backgroundColor: p.color,
            boxShadow: `0 0 ${p.size * 3}px ${p.color}`,
            opacity: p.opacity,
            animation: `orbitMotion-${p.id} ${p.duration}s linear infinite`,
            animationDelay: `${p.delay}s`
          }}
        />
      ))}

      {/* Dynamic Keyframe Injection for Elliptical Path */}
      <style>{`
        ${particles.map(p => `
          @keyframes orbitMotion-${p.id} {
            0% {
              transform: translate(-50%, -50%) rotate(0deg) translate(${p.radiusX}px, 0px) rotate(0deg);
            }
            100% {
              transform: translate(-50%, -50%) rotate(360deg) translate(${p.radiusX}px, 0px) rotate(-360deg);
            }
          }
        `).join('\n')}
      `}</style>
    </div>
  );
}
