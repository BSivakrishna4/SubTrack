import React, { useEffect } from 'react';
import Navbar from '../components/Navbar';
import Hero from '../components/Hero';
import FeatureSection from '../components/FeatureSection';
import LifecycleSection from '../components/LifecycleSection';
import HowItWorks from '../components/HowItWorks';
import DashboardPreview from '../components/DashboardPreview';

const Home = () => {
  useEffect(() => {
    // Scroll to top on load
    window.scrollTo(0, 0);
  }, []);

  return (
    <div
      className="min-h-screen font-sans selection:bg-cyan-500 selection:text-slate-950 overflow-x-hidden"
      style={{
        backgroundColor: 'var(--bg-primary)',
        color: 'var(--text-primary)'
      }}
    >
      {/* Sticky Header */}
      <Navbar />

      {/* Main Landing Page Sections */}
      <main>
        <section id="hero">
          <Hero />
        </section>

        <section id="features">
          <FeatureSection />
        </section>

        <section id="lifecycle">
          <LifecycleSection />
        </section>

        <section id="how-it-works">
          <HowItWorks />
        </section>

        <section id="dashboard-preview">
          <DashboardPreview />
        </section>
      </main>
    </div>
  );
};

export default Home;
