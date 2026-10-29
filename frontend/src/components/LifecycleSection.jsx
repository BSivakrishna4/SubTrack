import React from 'react';
import { Package, CreditCard, CheckCircle2, RefreshCw, AlertOctagon } from 'lucide-react';

export default function LifecycleSection() {
  const steps = [
    { code: 'PLAN', label: 'Plan Catalog', desc: 'Select duration & price tier', icon: Package, color: '#0284c7' },
    { code: 'ACTIVATE', label: 'Mock Payment', desc: 'Instant OpenFeign processing', icon: CreditCard, color: '#6366f1' },
    { code: 'ACTIVE', label: 'Active State', desc: 'Start & expiry date tracked', icon: CheckCircle2, color: '#10b981' },
    { code: 'RENEW', label: 'Auto Renewal', desc: 'Extend plan prior to expiry', icon: RefreshCw, color: '#8b5cf6' },
    { code: 'EXPIRED / CANCELLED', label: 'Lifecycle Termination', desc: '@Scheduled background detection', icon: AlertOctagon, color: '#f59e0b' }
  ];

  return (
    <section id="lifecycle" style={{ padding: '90px 24px', position: 'relative', background: 'var(--bg-secondary)', borderTop: '1px solid var(--border-color)', borderBottom: '1px solid var(--border-color)' }}>
      <div style={{ maxWidth: '1280px', margin: '0 auto' }}>

        {/* Section Header */}
        <div style={{ textAlign: 'center', marginBottom: '60px' }}>
          <span style={{ fontSize: '0.8rem', color: 'var(--accent-indigo)', textTransform: 'uppercase', letterSpacing: '0.12em', fontWeight: 700 }}>
            STATE MACHINE ARCHITECTURE
          </span>
          <h2 style={{ fontSize: 'clamp(2rem, 3.5vw, 2.8rem)', fontWeight: 800, marginTop: '8px', marginBottom: '12px', color: 'var(--text-heading)' }}>
            Your Subscription. <span className="gradient-text">Always in Control.</span>
          </h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '1.05rem', maxWidth: '600px', margin: '0 auto' }}>
            End-to-end status tracking from initial plan selection to automated background expiry.
          </p>
        </div>

        {/* Curved Lifecycle Node Progression */}
        <div style={{ position: 'relative', display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px', alignItems: 'center' }}>

          {steps.map((step, idx) => {
            const IconComp = step.icon;
            return (
              <div
                key={idx}
                className="glass-card"
                style={{
                  textAlign: 'center',
                  padding: '28px 20px',
                  position: 'relative'
                }}
              >
                {/* Node Icon */}
                <div
                  style={{
                    width: '56px',
                    height: '56px',
                    borderRadius: '50%',
                    background: `${step.color}15`,
                    border: `2px solid ${step.color}`,
                    boxShadow: `0 0 20px ${step.color}35`,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    margin: '0 auto 16px auto',
                    color: step.color
                  }}
                >
                  <IconComp size={24} />
                </div>

                <div style={{ fontSize: '0.75rem', fontWeight: 800, color: step.color, letterSpacing: '0.08em', marginBottom: '4px' }}>
                  STEP 0{idx + 1} • {step.code}
                </div>
                <h4 style={{ fontSize: '1.05rem', fontWeight: 700, color: 'var(--text-heading)', marginBottom: '6px' }}>{step.label}</h4>
                <p style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>{step.desc}</p>
              </div>
            );
          })}

        </div>

      </div>
    </section>
  );
}
