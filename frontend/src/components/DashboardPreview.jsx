import React from 'react';
import { LayoutDashboard, CheckCircle2, ShieldCheck } from 'lucide-react';

export default function DashboardPreview() {
  return (
    <section id="dashboard-preview" style={{ padding: '90px 24px', position: 'relative', background: 'var(--bg-secondary)', borderTop: '1px solid var(--border-color)' }}>
      <div style={{ maxWidth: '1280px', margin: '0 auto' }}>

        <div style={{ textAlign: 'center', marginBottom: '50px' }}>
          <span style={{ fontSize: '0.8rem', color: 'var(--accent-cyan)', textTransform: 'uppercase', letterSpacing: '0.12em', fontWeight: 700 }}>
            LIVE SYSTEM DEMONSTRATION
          </span>
          <h2 style={{ fontSize: 'clamp(2rem, 3.5vw, 2.8rem)', fontWeight: 800, marginTop: '8px', marginBottom: '12px', color: 'var(--text-heading)' }}>
            Built for <span className="gradient-text">Complete Transparency</span>
          </h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '1.05rem', maxWidth: '600px', margin: '0 auto' }}>
            Here is what your active SubTrack dashboard looks like after signing in.
          </p>
        </div>

        {/* Dashboard Preview Container */}
        <div
          className="glass-card"
          style={{
            padding: '32px',
            maxWidth: '1000px',
            margin: '0 auto',
            border: '1px solid var(--border-color)',
            boxShadow: 'var(--card-shadow-hover)'
          }}
        >
          {/* Header */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px', borderBottom: '1px solid var(--border-color)', paddingBottom: '16px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <LayoutDashboard size={22} color="var(--accent-cyan)" />
              <h3 style={{ fontSize: '1.2rem', fontWeight: 800, color: 'var(--text-heading)' }}>SubTrack Dashboard Overview</h3>
            </div>
            <span className="badge badge-active" style={{ fontSize: '0.75rem' }}>
              <ShieldCheck size={14} /> REST API Gateway Connected
            </span>
          </div>

          {/* Metric Cards */}
          <div className="grid-3" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px', marginBottom: '24px' }}>
            <div style={{ background: 'var(--bg-surface)', padding: '16px', borderRadius: '12px', border: '1px solid var(--border-color)' }}>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Active Subscriptions</div>
              <div style={{ fontSize: '1.8rem', fontWeight: 800, color: 'var(--accent-cyan)', marginTop: '4px' }}>3 Active</div>
            </div>
            <div style={{ background: 'var(--bg-surface)', padding: '16px', borderRadius: '12px', border: '1px solid var(--border-color)' }}>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Monthly Spending</div>
              <div style={{ fontSize: '1.8rem', fontWeight: 800, color: 'var(--accent-purple)', marginTop: '4px' }}>₹2,497</div>
            </div>
            <div style={{ background: 'var(--bg-surface)', padding: '16px', borderRadius: '12px', border: '1px solid var(--border-color)' }}>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Upcoming Renewals</div>
              <div style={{ fontSize: '1.8rem', fontWeight: 800, color: '#f59e0b', marginTop: '4px' }}>1 Pending</div>
            </div>
          </div>

          {/* Sample Active Plan Details Card */}
          <div style={{ background: 'var(--bg-surface)', padding: '20px', borderRadius: '14px', border: '1px solid var(--border-color)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--accent-cyan)', fontWeight: 700, letterSpacing: '0.05em' }}>CURRENT ACTIVE PLAN</div>
                <h4 style={{ fontSize: '1.3rem', fontWeight: 800, color: 'var(--text-heading)', marginTop: '2px' }}>Pro Quarterly Plan</h4>
              </div>
              <span className="badge badge-active"><CheckCircle2 size={14} /> ACTIVE</span>
            </div>
            <p style={{ fontSize: '0.88rem', color: 'var(--text-secondary)', marginBottom: '16px' }}>
              Advanced features & multi-device synchronization. Next renewal due on <strong>15 Mar 2026</strong>.
            </p>
            <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
              <button className="btn btn-electric" style={{ padding: '8px 18px', fontSize: '0.85rem' }}>Renew Subscription</button>
              <button className="btn btn-secondary" style={{ padding: '8px 18px', fontSize: '0.85rem' }}>Cancel Subscription</button>
            </div>
          </div>

        </div>

      </div>
    </section>
  );
}
