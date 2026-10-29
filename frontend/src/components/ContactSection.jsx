import React, { useState } from 'react';
import { MessageSquare, Send, CheckCircle2, HelpCircle, Server, Sparkles, ChevronDown } from 'lucide-react';

const ContactSection = () => {
  const [formData, setFormData] = useState({ name: '', email: '', subject: '', message: '' });
  const [submitted, setSubmitted] = useState(false);
  const [activeFaq, setActiveFaq] = useState(null);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!formData.name || !formData.email || !formData.message) return;
    setSubmitted(true);
    setTimeout(() => {
      setSubmitted(false);
      setFormData({ name: '', email: '', subject: '', message: '' });
    }, 4000);
  };

  const faqs = [
    {
      q: 'How does SubTrack handle subscription lifecycle state transitions?',
      a: 'SubTrack uses an event-driven microservices architecture. When a user activates a plan, subscription-service issues a payment request to payment-service via OpenFeign. Once approved, the status is set to ACTIVE and expiry dates are tracked automatically.'
    },
    {
      q: 'What happens when a recurring payment fails during auto-renewal?',
      a: 'If a payment fails or is declined, the system sets the subscription status to PAYMENT_FAILED or EXPIRED, preventing service leakage while alerting the user on their dashboard.'
    },
    {
      q: 'How are microservices routed through the API Gateway?',
      a: 'All frontend requests flow through Spring Cloud Gateway on port 8080, which dynamically discovers registered instances (`user-service`, `subscription-service`, `payment-service`) via Eureka Discovery Server on port 8761.'
    },
    {
      q: 'Is there an admin portal for managing subscription plans?',
      a: 'Yes! Accounts created with the "System Administrator" role can access `/admin/plans` to create new pricing tiers, edit plan durations, or adjust pricing dynamically.'
    }
  ];

  return (
    <section id="contact" className="relative py-28 px-4" style={{ backgroundColor: 'var(--bg-secondary)', borderTop: '1px solid var(--border-color)', overflow: 'hidden' }}>
      {/* Background radial glow */}
      <div className="absolute top-1/2 right-10 -translate-y-1/2 w-[600px] h-[600px] bg-gradient-to-tr from-cyan-600/10 via-blue-600/10 to-transparent blur-[140px] rounded-full pointer-events-none" />

      <div className="max-w-7xl mx-auto relative z-10">
        
        {/* Section Header */}
        <div className="text-center max-w-3xl mx-auto mb-16">
          <div className="inline-flex items-center space-x-2 px-3.5 py-1.5 rounded-full bg-cyan-950/20 border border-cyan-500/30 text-cyan-600 dark:text-cyan-300 text-xs font-semibold tracking-wider uppercase mb-4 shadow-inner">
            <MessageSquare size={14} className="text-cyan-500" />
            <span>SUPPORT & INQUIRIES</span>
          </div>

          <h2 className="text-3xl sm:text-4xl md:text-5xl font-black tracking-tight leading-tight" style={{ color: 'var(--text-heading)' }}>
            We’re Here to Help You Succeed.
          </h2>

          <p className="text-base md:text-lg mt-4 leading-relaxed" style={{ color: 'var(--text-secondary)' }}>
            Have questions about SubTrack microservices, custom Enterprise plans, or API integration? Send us a message or check our FAQs below.
          </p>
        </div>

        {/* Content Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-start">
          
          {/* Left: Contact Form */}
          <div className="lg:col-span-7 glass-card p-8 sm:p-10 rounded-3xl" style={{ border: '1px solid var(--border-color)' }}>
            <h3 className="text-2xl font-bold mb-2 flex items-center space-x-2" style={{ color: 'var(--text-heading)' }}>
              <span>Send Us a Message</span>
              <Sparkles size={18} style={{ color: 'var(--accent-cyan)' }} />
            </h3>
            <p className="text-sm mb-6" style={{ color: 'var(--text-secondary)' }}>Fill out the form below and our architecture team will get back to you shortly.</p>

            {submitted ? (
              <div className="p-6 rounded-2xl text-center space-y-3 alert-success">
                <CheckCircle2 size={40} className="mx-auto" />
                <h4 className="text-lg font-bold">Message Sent Successfully!</h4>
                <p className="text-xs">Thank you for reaching out to SubTrack support. We will get back to your email within 24 hours.</p>
              </div>
            ) : (
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-semibold mb-1.5 uppercase tracking-wider" style={{ color: 'var(--text-secondary)' }}>Your Name</label>
                    <input
                      type="text"
                      className="auth-input"
                      placeholder="Jane Doe"
                      value={formData.name}
                      onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold mb-1.5 uppercase tracking-wider" style={{ color: 'var(--text-secondary)' }}>Your Email</label>
                    <input
                      type="email"
                      className="auth-input"
                      placeholder="jane@company.com"
                      value={formData.email}
                      onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                      required
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold mb-1.5 uppercase tracking-wider" style={{ color: 'var(--text-secondary)' }}>Subject</label>
                  <input
                    type="text"
                    className="auth-input"
                    placeholder="Microservices Architecture / Account Inquiry"
                    value={formData.subject}
                    onChange={(e) => setFormData({ ...formData, subject: e.target.value })}
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold mb-1.5 uppercase tracking-wider" style={{ color: 'var(--text-secondary)' }}>Message</label>
                  <textarea
                    rows={4}
                    className="auth-input"
                    style={{ resize: 'none' }}
                    placeholder="Tell us how we can help you with your subscription lifecycle..."
                    value={formData.message}
                    onChange={(e) => setFormData({ ...formData, message: e.target.value })}
                    required
                  />
                </div>

                <button
                  type="submit"
                  className="btn btn-electric w-full py-3.5 px-6 rounded-xl font-bold flex items-center justify-center space-x-2 text-sm"
                >
                  <Send size={16} />
                  <span>Send Message to SubTrack Team</span>
                </button>
              </form>
            )}
          </div>

          {/* Right: FAQs & Support Info */}
          <div className="lg:col-span-5 space-y-6">
            
            <div className="glass-card p-6 rounded-3xl" style={{ border: '1px solid var(--border-color)' }}>
              <h3 className="text-xl font-bold mb-4 flex items-center space-x-2" style={{ color: 'var(--text-heading)' }}>
                <HelpCircle size={20} style={{ color: 'var(--accent-cyan)' }} />
                <span>Frequently Asked Questions</span>
              </h3>

              <div className="space-y-3">
                {faqs.map((faq, idx) => {
                  const isOpen = activeFaq === idx;
                  return (
                    <div
                      key={idx}
                      className="rounded-xl overflow-hidden transition-all"
                      style={{ border: '1px solid var(--border-color)', background: 'var(--bg-surface)' }}
                    >
                      <button
                        onClick={() => setActiveFaq(isOpen ? null : idx)}
                        className="w-full px-4 py-3 text-left font-semibold text-sm flex items-center justify-between transition-colors"
                        style={{ color: 'var(--text-heading)', background: 'transparent', border: 'none' }}
                      >
                        <span>{faq.q}</span>
                        <ChevronDown size={16} className={`transform transition-transform ${isOpen ? 'rotate-180' : ''}`} style={{ color: 'var(--accent-cyan)' }} />
                      </button>

                      {isOpen && (
                        <div className="px-4 pb-4 pt-1 text-xs leading-relaxed" style={{ color: 'var(--text-secondary)', borderTop: '1px solid var(--border-subtle)' }}>
                          {faq.a}
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Microservices Endpoint Info */}
            <div className="glass-card p-6 rounded-3xl space-y-3" style={{ border: '1px solid var(--border-color)' }}>
              <div className="flex items-center space-x-2 font-bold text-xs uppercase tracking-wider" style={{ color: 'var(--accent-cyan)' }}>
                <Server size={14} />
                <span>System Gateways & Endpoints</span>
              </div>
              <div className="text-xs space-y-2">
                <div className="flex justify-between items-center p-2.5 rounded-lg" style={{ background: 'var(--bg-surface)', border: '1px solid var(--border-color)' }}>
                  <span style={{ color: 'var(--text-secondary)' }}>API Gateway Proxy</span>
                  <code className="font-mono font-bold" style={{ color: 'var(--accent-cyan)' }}>http://localhost:8080</code>
                </div>
                <div className="flex justify-between items-center p-2.5 rounded-lg" style={{ background: 'var(--bg-surface)', border: '1px solid var(--border-color)' }}>
                  <span style={{ color: 'var(--text-secondary)' }}>Eureka Service Registry</span>
                  <code className="font-mono font-bold" style={{ color: 'var(--accent-cyan)' }}>http://localhost:8761</code>
                </div>
              </div>
            </div>

          </div>

        </div>

      </div>
    </section>
  );
};

export default ContactSection;
