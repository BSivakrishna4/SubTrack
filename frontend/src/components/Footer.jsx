import React from 'react';
import { Link } from 'react-router-dom';

const Footer = () => {
  return (
    <footer
      className="relative pt-16 pb-12 px-4 sm:px-6 lg:px-8 overflow-hidden"
      style={{
        backgroundColor: 'var(--footer-bg)',
        borderTop: '1px solid var(--footer-border)',
        color: 'var(--footer-text)'
      }}
    >
      {/* Background glow dot */}
      <div className="absolute bottom-0 left-1/2 -translate-x-1/2 w-full h-[150px] bg-gradient-to-t from-cyan-500/5 to-transparent pointer-events-none" />

      <div className="max-w-7xl mx-auto relative z-10">
        <div className="grid grid-cols-1 md:grid-cols-5 gap-10 pb-12" style={{ borderBottom: '1px solid var(--border-color)' }}>
          
          {/* Brand Info */}
          <div className="md:col-span-2 space-y-4">
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-cyan-500 via-blue-600 to-indigo-600 flex items-center justify-center shadow-[0_0_20px_rgba(0,240,255,0.4)]">
                <span className="text-xl font-black text-white tracking-tighter">S</span>
              </div>
              <div className="flex flex-col">
                <span className="text-xl font-bold tracking-tight" style={{ color: 'var(--text-heading)' }}>SubTrack</span>
                <span className="text-[10px] tracking-wider uppercase font-semibold" style={{ color: 'var(--accent-cyan)' }}>Subscribe • Track • Grow</span>
              </div>
            </div>
            <p className="text-sm max-w-sm leading-relaxed" style={{ color: 'var(--footer-text)' }}>
              SubTrack is an enterprise-grade Subscription Lifecycle & Recurring Revenue Management System designed with modern microservices architecture.
            </p>
            <div className="flex items-center space-x-4 pt-2 text-xs" style={{ color: 'var(--text-muted)' }}>
              <span className="inline-flex items-center space-x-1.5">
                <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
                <span>Microservices Online</span>
              </span>
              <span>•</span>
              <span>v1.0.0</span>
            </div>
          </div>

          {/* Quick Links */}
          <div>
            <h4 className="text-xs font-bold uppercase tracking-wider mb-4" style={{ color: 'var(--text-heading)' }}>Platform</h4>
            <ul className="space-y-2 text-sm">
              <li><a href="#hero" className="hover:text-cyan-500 transition-colors">Overview</a></li>
              <li><a href="#features" className="hover:text-cyan-500 transition-colors">Core Features</a></li>
              <li><a href="#lifecycle" className="hover:text-cyan-500 transition-colors">Subscription Engine</a></li>
              <li><a href="#how-it-works" className="hover:text-cyan-500 transition-colors">How it Works</a></li>
              <li><a href="#dashboard-preview" className="hover:text-cyan-500 transition-colors">Live Demo</a></li>
            </ul>
          </div>

          {/* Services */}
          <div>
            <h4 className="text-xs font-bold uppercase tracking-wider mb-4" style={{ color: 'var(--text-heading)' }}>Microservices</h4>
            <ul className="space-y-2 text-sm">
              <li><span className="hover:text-cyan-500 transition-colors cursor-default">User Service (8081)</span></li>
              <li><span className="hover:text-cyan-500 transition-colors cursor-default">Subscription Service (8082)</span></li>
              <li><span className="hover:text-cyan-500 transition-colors cursor-default">Payment Service (8083)</span></li>
              <li><span className="hover:text-cyan-500 transition-colors cursor-default">API Gateway (8080)</span></li>
              <li><span className="hover:text-cyan-500 transition-colors cursor-default">Eureka Discovery (8761)</span></li>
            </ul>
          </div>

          {/* Account */}
          <div>
            <h4 className="text-xs font-bold uppercase tracking-wider mb-4" style={{ color: 'var(--text-heading)' }}>Get Started</h4>
            <ul className="space-y-2 text-sm">
              <li><Link to="/register" className="hover:underline font-semibold" style={{ color: 'var(--accent-cyan)' }}>Create Account</Link></li>
              <li><Link to="/login" className="hover:text-cyan-500 transition-colors">Sign In</Link></li>
              <li><Link to="/login" className="hover:text-cyan-500 transition-colors">Admin Portal</Link></li>
              <li><a href="http://localhost:8761" target="_blank" rel="noreferrer" className="hover:text-cyan-500 transition-colors">Eureka Dashboard</a></li>
            </ul>
          </div>

        </div>

        {/* Bottom Bar */}
        <div className="pt-8 flex flex-col sm:flex-row items-center justify-between text-xs gap-4" style={{ color: 'var(--text-muted)' }}>
          <p>© {new Date().getFullYear()} SubTrack – Service-Oriented Architecture (SOA) Project. All rights reserved.</p>
          <div className="flex items-center space-x-6">
            <span className="hover:underline transition-colors cursor-pointer">Privacy Policy</span>
            <span className="hover:underline transition-colors cursor-pointer">Terms of Service</span>
            <span className="hover:underline transition-colors cursor-pointer">Security</span>
          </div>
        </div>

      </div>
    </footer>
  );
};

export default Footer;
