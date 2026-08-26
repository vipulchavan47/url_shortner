import { useState, useEffect } from 'react';

export default function AnalyticsPage({ token }) {
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080';

  useEffect(() => {
    const fetchAnalytics = async () => {
      try {
        const response = await fetch(`${API_BASE}/api/analytics/${token}`);
        if (!response.ok) {
          setError('Analytics not found for this link.');
          setLoading(false);
          return;
        }
        const data = await response.json();
        setAnalytics(data);
      } catch (err) {
        setError('Failed to load analytics. Please try again.');
      } finally {
        setLoading(false);
      }
    };
    fetchAnalytics();
  }, [token, API_BASE]);

  const goHome = () => {
    window.history.pushState({}, '', '/');
    window.location.reload();
  };

  const getExpiryStatus = () => {
    if (!analytics) return null;
    if (analytics.status === 'EXPIRED') {
      return <span className="status-badge status-expired">Expired</span>;
    }
    return <span className="status-badge status-active">Active</span>;
  };

  if (loading) {
    return (
      <div className="analytics-page">
        <div className="analytics-loading">
          <div className="spinner-large" />
          <p>Loading analytics...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="analytics-page">
        <div className="analytics-error-card">
          <h2>Oops!</h2>
          <p>{error}</p>
          <button className="btn-primary" onClick={goHome}>Go Home</button>
        </div>
      </div>
    );
  }

  return (
    <div className="analytics-page">
      <div className="analytics-page-header">
        <button className="btn-back" onClick={goHome}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="19" y1="12" x2="5" y2="12" /><polyline points="12 19 5 12 12 5" /></svg>
          Back to BitShort
        </button>
      </div>

      <div className="analytics-page-content">
        <div className="analytics-page-top">
          <div>
            <h1 className="analytics-page-title">{analytics.shortCode}</h1>
            <a href={analytics.originalUrl} target="_blank" rel="noopener noreferrer" className="analytics-original-url">
              {analytics.originalUrl}
            </a>
          </div>
          <div className="analytics-page-status">
            {getExpiryStatus()}
            {analytics.expiresAt && (
              <span className="analytics-expires-text">
                Expires: {new Date(analytics.expiresAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}
              </span>
            )}
          </div>
        </div>

        <div className="analytics-page-stats">
          <div className="analytics-page-stat">
            <span className="analytics-page-stat-label">TOTAL CLICKS</span>
            <span className="analytics-page-stat-value">{analytics.totalClicks}</span>
          </div>
          <div className="analytics-page-stat">
            <span className="analytics-page-stat-label">TODAY</span>
            <span className="analytics-page-stat-value">{analytics.todayClicks}</span>
          </div>
          <div className="analytics-page-stat">
            <span className="analytics-page-stat-label">LAST 7 DAYS</span>
            <span className="analytics-page-stat-value">{analytics.last7DaysClicks}</span>
          </div>
        </div>

        <div className="analytics-page-table-section">
          <h3 className="analytics-page-section-title">Recent Clicks</h3>
          <div className="analytics-page-table-wrapper">
            <table className="analytics-page-table">
              <thead>
                <tr>
                  <th>Time</th>
                  <th>Referrer</th>
                  <th>Device</th>
                </tr>
              </thead>
              <tbody>
                {(analytics.recentClicks || []).length === 0 ? (
                  <tr>
                    <td colSpan="3" className="analytics-empty">No clicks yet</td>
                  </tr>
                ) : (
                  (analytics.recentClicks || []).map((c, idx) => (
                    <tr key={idx}>
                      <td>{new Date(c.occurredAt).toLocaleString()}</td>
                      <td>{c.referrer}</td>
                      <td>{c.device}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}
