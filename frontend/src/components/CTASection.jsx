import React from 'react';
import { Link } from 'react-router-dom';

const CTASection = () => {
  return (
    <section className="relative py-28 px-4 overflow-hidden" style={{ borderTop: '1px solid var(--border-color)' }}>
      {/* Radial ambient background glow */}
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[350px] bg-gradient-to-tr from-cyan-500/10 via-blue-600/15 to-purple-600/10 blur-[120px] rounded-full pointer-events-none" />

      <div className="max-w-5xl mx-auto relative z-10">
        <div className="glass-card p-10 md:p-16 text-center rounded-3xl" style={{ border: '1px solid var(--border-glow)' }}>
          
          <div className="inline-flex items-center space-x-2 px-3.5 py-1.5 rounded-full bg-cyan-950/20 border border-cyan-500/30 text-cyan-600 dark:text-cyan-300 text-xs font-semibold tracking-wider uppercase mb-6 shadow-inner">
            <span className="w-2 h-2 rounded-full bg-cyan-400 animate-ping" />
            <span>Ready to Transform Your Workflow?</span>
          </div>

          <h2 className="text-3xl sm:text-4xl md:text-5xl font-black tracking-tight mb-6 leading-tight" style={{ color: 'var(--text-heading)' }}>
            Take Control of Every Subscription.
          </h2>

          <p className="text-base md:text-lg max-w-2xl mx-auto mb-10 leading-relaxed" style={{ color: 'var(--text-secondary)' }}>
            Join forward-thinking individuals and teams who automate renewals, eliminate hidden charges, and gain 100% visibility over recurring spend.
          </p>

          <div className="flex flex-col sm:flex-row items-center justify-center gap-4 mb-12">
            <Link
              to="/register"
              className="btn btn-electric w-full sm:w-auto px-8 py-4 text-center"
            >
              Get Started Free →
            </Link>
            <Link
              to="/login"
              className="btn btn-outline-blue w-full sm:w-auto px-8 py-4 text-center"
            >
              Sign In to Dashboard
            </Link>
          </div>

          {/* Quick Stats Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 pt-8 mt-2" style={{ borderTop: '1px solid var(--border-color)' }}>
            <div className="text-center">
              <div className="text-2xl font-extrabold" style={{ color: 'var(--accent-cyan)' }}>99.9% Uptime</div>
              <div className="text-xs font-medium uppercase tracking-wider mt-1" style={{ color: 'var(--text-muted)' }}>Enterprise Microservices</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-extrabold" style={{ color: 'var(--accent-blue)' }}>&lt; 2 Mins</div>
              <div className="text-xs font-medium uppercase tracking-wider mt-1" style={{ color: 'var(--text-muted)' }}>Instant Onboarding</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-extrabold" style={{ color: 'var(--accent-purple)' }}>Zero Cost</div>
              <div className="text-xs font-medium uppercase tracking-wider mt-1" style={{ color: 'var(--text-muted)' }}>Free Tier Available</div>
            </div>
          </div>

        </div>
      </div>
    </section>
  );
};

export default CTASection;
