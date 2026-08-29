import './App.css';
import Navbar from './components/layout/Navbar';
import HeroSection from './components/sections/HeroSection';

export default function App() {
  const handleNavigate = (view) => {
    if (view === 'home') {
      window.history.pushState({}, '', '/');
    }
  };

  return (
    <div className="app">
      <Navbar onNavigate={handleNavigate} currentView="home" />
      <main>
        <HeroSection />
      </main>
    </div>
  );
}