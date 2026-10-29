import React from 'react';
import { Link } from 'react-router-dom';
import OrbitParticles from './OrbitParticles';
import OrbitRings from './OrbitRings';
import FloatingCard from './FloatingCard';
import { ArrowRight, Clock, CheckCircle2, CreditCard, PieChart, Sparkles } from 'lucide-react';

export default function Hero() {
  return (
    <section id="hero" style={{ position: 'relative', padding: '60px 24px 100px 24px', minHeight: 'calc(100vh - 80px)', display: 'flex', alignItems: 'center' }}>

      {/* Background Soft Glow Effects */}
      <div style={{ position: 'absolute', top: '20%', right: '10%', width: '500px', height: '500px', background: 'radial-gradient(circle, var(--body-radial-1) 0%, var(--body-radial-2) 40%, transparent 70%)', filter: 'blur(50px)', pointerEvents: 'none', zIndex: 1 }} />
      <div style={{ position: 'absolute', top: '40%', left: '5%', width: '400px', height: '400px', background: 'radial-gradient(circle, var(--body-radial-3) 0%, transparent 60%)', filter: 'blur(40px)', pointerEvents: 'none', zIndex: 1 }} />

      <div style={{ maxWidth: '1280px', margin: '0 auto', width: '100%', display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(480px, 1fr))', gap: '48px', alignItems: 'center', position: 'relative', zIndex: 10 }}>

        {/* HERO LEFT COLUMN */}
        <div>
          {/* Eyebrow Badge */}
          <div
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '8px',
              padding: '6px 16px',
              borderRadius: '9999px',
              background: 'rgba(2, 132, 199, 0.1)',
              border: '1px solid var(--border-glow)',
              fontSize: '0.75rem',
              fontWeight: 700,
              color: 'var(--accent-cyan)',
              letterSpacing: '0.1em',
              marginBottom: '24px'
            }}
          >
            <Sparkles size={14} /> SMART SUBSCRIPTIONS • BETTER TOMORROWS
          </div>

          {/* Main Heading */}
          <h1 style={{ fontSize: 'clamp(2.5rem, 5vw, 4rem)', fontWeight: 900, lineHeight: 1.1, letterSpacing: '-0.03em', marginBottom: '20px', color: 'var(--text-heading)' }}>
            Take Control <br />
            of <span className="gradient-text">Your Subscriptions</span>
          </h1>

          {/* Supporting Text */}
          <p style={{ color: 'var(--text-secondary)', fontSize: '1.1rem', lineHeight: 1.7, maxWidth: '540px', marginBottom: '36px' }}>
            SubTrack helps you manage, track, and grow your subscriptions — all in one place. Stay organized, never miss a renewal, and make smarter decisions today for a better tomorrow.
          </p>

          {/* Action Buttons */}
          <div style={{ display: 'flex', gap: '16px', flexWrap: 'wrap' }}>
            <Link to="/register" className="btn btn-electric" style={{ padding: '14px 32px', fontSize: '1.05rem' }}>
              Get Started <ArrowRight size={18} />
            </Link>
            <a href="#features" className="btn btn-outline-blue" style={{ padding: '14px 30px', fontSize: '1.05rem' }}>
              Learn More
            </a>
          </div>
        </div>

        {/* HERO RIGHT COLUMN — USER REFERENCE PICTURE WITH ADJUSTED BOX POSITIONS & SPEED */}
        <div style={{ position: 'relative', display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '540px' }}>

          {/* ORBITAL RINGS & DYNAMIC PARTICLES */}
          <OrbitRings />
          <OrbitParticles count={28} />

          {/* 4 FLOATING NOTIFICATION BOXES */}
          <FloatingCard
            icon={Clock}
            title="Renewal in 12 days"
            subtitle="Premium Plan ($499)"
            position={{ top: '5px', left: '-20px' }}
            animationName="floatSlow1"
            duration="5.5s"
          />
          <FloatingCard
            icon={CheckCircle2}
            title="3 Active Plans"
            subtitle="100% Tracked"
            position={{ top: '25px', right: '-30px' }}
            animationName="floatSlow2"
            duration="6s"
          />
          <FloatingCard
            icon={CreditCard}
            title="Payment Successful"
            subtitle="TXN-1789573912136"
            position={{ bottom: '25px', left: '-30px' }}
            animationName="floatSlow3"
            duration="5.8s"
          />
          <FloatingCard
            icon={PieChart}
            title="₹2,497 Total Spent"
            subtitle="Monthly Analytics"
            position={{ bottom: '5px', right: '-15px' }}
            animationName="floatSlow4"
            duration="6.5s"
          />

          {/* USER REFERENCE MOCKUP IMAGE WITH GLASSMORPHISM BORDER & AMBIENT GLOW */}
          <div
            style={{
              position: 'relative',
              width: '100%',
              maxWidth: '560px',
              borderRadius: '24px',
              overflow: 'hidden',
              border: '1px solid var(--border-color)',
              boxShadow: 'var(--card-shadow-hover)',
              zIndex: 8,
              transition: 'transform 0.5s ease, box-shadow 0.5s ease',
              background: 'var(--bg-card)',
              backdropFilter: 'blur(12px)'
            }}
          >
            <img
              src="/hero-mockup.png"
              alt="SubTrack Subscription Management Workspace Reference Mockup"
              style={{
                width: '100%',
                height: 'auto',
                display: 'block',
                borderRadius: '24px',
                objectFit: 'cover'
              }}
            />
          </div>

        </div>

      </div>

    </section>
  );
}
