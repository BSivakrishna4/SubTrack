import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';
import { Package, Check, Sparkles, ArrowRight, ShieldCheck, Zap } from 'lucide-react';

const PlansSection = () => {
  const [plans, setPlans] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchPlans();
  }, []);

  const fetchPlans = async () => {
    try {
      const response = await api.get('/api/plans');
      if (response.data && response.data.length > 0) {
        setPlans(response.data);
      } else {
        // Fallback default catalog if API is loading
        setPlans([
          { id: 1, name: 'Starter Tier', price: 0, durationDays: 30, description: 'Essential subscription tracking for individuals and small setups.' },
          { id: 2, name: 'Pro Subscription', price: 499, durationDays: 30, description: 'Automated renewal alerts, payment tracking, and analytics.' },
          { id: 3, name: 'Enterprise Business', price: 1499, durationDays: 365, description: 'Full microservices access, unlimited plans, and SLA uptime.' }
        ]);
      }
    } catch (err) {
      // Fallback UI data
      setPlans([
        { id: 1, name: 'Starter Tier', price: 0, durationDays: 30, description: 'Essential subscription tracking for individuals and small setups.' },
        { id: 2, name: 'Pro Subscription', price: 499, durationDays: 30, description: 'Automated renewal alerts, payment tracking, and analytics.' },
        { id: 3, name: 'Enterprise Business', price: 1499, durationDays: 365, description: 'Full microservices access, unlimited plans, and SLA uptime.' }
      ]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <section id="plans" className="relative py-28 px-4" style={{ backgroundColor: 'var(--bg-primary)', borderTop: '1px solid var(--border-color)', overflow: 'hidden' }}>
      {/* Background ambient lighting */}
      <div className="absolute top-1/3 left-1/2 -translate-x-1/2 w-[700px] h-[400px] bg-gradient-to-r from-cyan-500/10 via-blue-600/10 to-indigo-600/10 blur-[140px] rounded-full pointer-events-none" />

      <div className="max-w-7xl mx-auto relative z-10">
        
        {/* Header */}
        <div className="text-center max-w-3xl mx-auto mb-16">
          <div className="inline-flex items-center space-x-2 px-3.5 py-1.5 rounded-full bg-cyan-950/20 border border-cyan-500/30 text-cyan-600 dark:text-cyan-300 text-xs font-semibold tracking-wider uppercase mb-4 shadow-inner">
            <Zap size={14} className="text-cyan-500" />
            <span>FLEXIBLE PRICING CATALOG</span>
          </div>

          <h2 className="text-3xl sm:text-4xl md:text-5xl font-black tracking-tight leading-tight" style={{ color: 'var(--text-heading)' }}>
            Transparent Plans for Every Growth Stage.
          </h2>

          <p className="text-base md:text-lg mt-4 leading-relaxed" style={{ color: 'var(--text-secondary)' }}>
            Choose a plan tailored to your recurring revenue management needs. Automate billing cycles with zero hidden fees.
          </p>
        </div>

        {/* Plans Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 items-stretch">
          {plans.map((plan, idx) => {
            const isPopular = idx === 1 || plan.name.toLowerCase().includes('pro');

            return (
              <div
                key={plan.id || idx}
                className="glass-card relative rounded-3xl p-8 flex flex-col justify-between transition-all duration-300 transform hover:-translate-y-2"
                style={{
                  border: isPopular ? '2px solid var(--accent-cyan)' : '1px solid var(--border-color)',
                  boxShadow: isPopular ? '0 0 35px rgba(2, 132, 199, 0.25)' : 'var(--card-shadow)'
                }}
              >
                {isPopular && (
                  <div className="absolute -top-4 left-1/2 -translate-x-1/2 px-4 py-1 rounded-full bg-gradient-to-r from-cyan-500 to-blue-600 text-white font-bold text-xs uppercase tracking-wider shadow-lg flex items-center space-x-1">
                    <Sparkles size={12} />
                    <span>Most Popular</span>
                  </div>
                )}

                <div>
                  <div className="flex items-center justify-between mb-4">
                    <div className="p-3 rounded-2xl" style={{ background: 'rgba(2, 132, 199, 0.12)', color: 'var(--accent-cyan)' }}>
                      <Package size={24} />
                    </div>
                    <span className="badge badge-secondary" style={{ fontSize: '0.72rem' }}>
                      {plan.durationDays || 30} Days Duration
                    </span>
                  </div>

                  <h3 className="text-2xl font-bold mb-2" style={{ color: 'var(--text-heading)' }}>{plan.name}</h3>
                  <p className="text-sm mb-6 min-h-[40px] leading-relaxed" style={{ color: 'var(--text-secondary)' }}>
                    {plan.description}
                  </p>

                  <div className="mb-6 pb-6" style={{ borderBottom: '1px solid var(--border-color)' }}>
                    <div className="flex items-baseline space-x-1">
                      <span className="text-4xl font-extrabold" style={{ color: 'var(--text-heading)' }}>₹{plan.price}</span>
                      <span className="text-sm" style={{ color: 'var(--text-secondary)' }}>/ {plan.durationDays || 30} days</span>
                    </div>
                    <span className="text-xs font-semibold mt-1 block" style={{ color: 'var(--accent-cyan)' }}>Includes OpenFeign Payment Gateway</span>
                  </div>

                  {/* Feature Checklist */}
                  <ul className="space-y-3.5 mb-8 text-sm" style={{ color: 'var(--text-primary)' }}>
                    <li className="flex items-center space-x-3">
                      <div className="w-5 h-5 rounded-full flex items-center justify-center shrink-0" style={{ background: 'rgba(2, 132, 199, 0.12)', border: '1px solid var(--accent-cyan)' }}>
                        <Check size={12} className="text-cyan-600 dark:text-cyan-400" />
                      </div>
                      <span>Full Access to SubTrack Lifecycle APIs</span>
                    </li>
                    <li className="flex items-center space-x-3">
                      <div className="w-5 h-5 rounded-full flex items-center justify-center shrink-0" style={{ background: 'rgba(2, 132, 199, 0.12)', border: '1px solid var(--accent-cyan)' }}>
                        <Check size={12} className="text-cyan-600 dark:text-cyan-400" />
                      </div>
                      <span>OpenFeign Payment Gateway Integration</span>
                    </li>
                    <li className="flex items-center space-x-3">
                      <div className="w-5 h-5 rounded-full flex items-center justify-center shrink-0" style={{ background: 'rgba(2, 132, 199, 0.12)', border: '1px solid var(--accent-cyan)' }}>
                        <Check size={12} className="text-cyan-600 dark:text-cyan-400" />
                      </div>
                      <span>Automated Expiry & Renewal Alerts</span>
                    </li>
                    <li className="flex items-center space-x-3">
                      <div className="w-5 h-5 rounded-full flex items-center justify-center shrink-0" style={{ background: 'rgba(2, 132, 199, 0.12)', border: '1px solid var(--accent-cyan)' }}>
                        <Check size={12} className="text-cyan-600 dark:text-cyan-400" />
                      </div>
                      <span>Eureka Service Discovery Support</span>
                    </li>
                  </ul>
                </div>

                <Link
                  to="/plans"
                  className={`w-full py-3.5 px-6 rounded-xl font-bold text-sm text-center transition-all flex items-center justify-center space-x-2 ${
                    isPopular ? 'btn-electric' : 'btn-outline-blue'
                  }`}
                >
                  <span>Select {plan.name}</span>
                  <ArrowRight size={16} />
                </Link>
              </div>
            );
          })}
        </div>

        {/* Footer note */}
        <div className="mt-12 text-center text-xs flex items-center justify-center space-x-2" style={{ color: 'var(--text-muted)' }}>
          <ShieldCheck size={14} style={{ color: 'var(--accent-cyan)' }} />
          <span>All plans backed by SubTrack Enterprise Microservices SLA</span>
        </div>

      </div>
    </section>
  );
};

export default PlansSection;
