import { useState, useEffect } from 'react';
import './App.css';
import Navbar from './components/layout/Navbar';
import HeroSection from './components/sections/HeroSection';
import AnalyticsSection from './components/sections/AnalyticsSection';
import Footer from './components/sections/Footer';
import AnalyticsPage from './components/pages/AnalyticsPage';

export default function App() {
  const [currentView, setCurrentView] = useState('home');
  const [analyticsToken, setAnalyticsToken] = useState(null);

  useEffect(() => {
    const path = window.location.pathname;
    const m = path.match(/^\/analytics\/(.+)$/);
    if (m) {
      setCurrentView('analytics');
      setAnalyticsToken(m[1]);
    }
  }, []);

  const handleNavigate = (view) => {
    if (view === 'home') {
      window.history.pushState({}, '', '/');
      setCurrentView('home');
      setAnalyticsToken(null);
    }
  };

  if (currentView === 'analytics' && analyticsToken) {
    return (
      <>
        <Navbar onNavigate={handleNavigate} currentView={currentView} />
        <AnalyticsPage token={analyticsToken} />
      </>
    );
  }

  return (
    <div className="app">
      <Navbar onNavigate={handleNavigate} currentView={currentView} />
      <main>
        <HeroSection />
        <AnalyticsSection />
      </main>
      <Footer />
    </div>
  );
}
