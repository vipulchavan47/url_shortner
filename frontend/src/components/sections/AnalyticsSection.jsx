import { useState } from 'react';

export default function AnalyticsSection() {
  const [analyticsInput, setAnalyticsInput] = useState('');
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080';

  const fetchAnalytics = async (e) => {
    e.preventDefault();
    setError('');
    setAnalytics(null);

    const input = analyticsInput.trim();
    if (!input) {
      setError('Please enter an analytics URL or token.');
      return;
    }

    const token = input.split('/').pop();
    if (!token) {
      setError('Could not extract a token from the input.');
      return;
    }

    setLoading(true);
    try {
      const response = await fetch(`${API_BASE}/api/analytics/${token}`);
      if (!response.ok) {
        setError('Analytics not found for this link.');
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

  const getExpiryStatus = () => {
    if (!analytics) return null;
    if (analytics.status === 'EXPIRED') {
      return <span className="status-badge status-expired">Expired</span>;
    }
    return <span className="status-badge status-active">Active</span>;
  };

  return (
    <section className="analytics-section" id="analytics-section">
      <p className="section-eyebrow">ANALYTICS</p>
      <h2 className="section-title">See how your links perform.</h2>

      <form className="analytics-form" onSubmit={fetchAnalytics}>
        <div className="analytics-input-row">
          <input
            type="text"
            placeholder="Enter your analytics URL or token..."
            value={analyticsInput}
            onChange={(e) => setAnalyticsInput(e.target.value)}
            disabled={loading}
            className="analytics-input"
          />
          <button type="submit" disabled={loading} className="btn-primary btn-analytics-submit">
            {loading ? (
              <span className="btn-loading">
                <span className="spinner" />
              </span>
            ) : (
              'View Analytics'
            )}
          </button>
        </div>
        {error && <div className="form-error">{error}</div>}
      </form>

      {analytics && (
        <div className="analytics-result">
          <div className="analytics-result-header">
            <h3 className="analytics-result-title">{analytics.shortCode}</h3>
            <a href={analytics.originalUrl} target="_blank" rel="noopener noreferrer" className="analytics-result-original">
              {analytics.originalUrl}
            </a>
          </div>

          <div className="analytics-result-grid">
            <div className="analytics-result-item">
              <span className="analytics-result-label">TOTAL CLICKS</span>
              <span className="analytics-result-value">{analytics.totalClicks}</span>
            </div>
            <div className="analytics-result-item">
              <span className="analytics-result-label">TODAY</span>
              <span className="analytics-result-value">{analytics.todayClicks}</span>
            </div>
            <div className="analytics-result-item">
              <span className="analytics-result-label">LAST 7 DAYS</span>
              <span className="analytics-result-value">{analytics.last7DaysClicks}</span>
            </div>
            <div className="analytics-result-item">
              <span className="analytics-result-label">STATUS</span>
              {getExpiryStatus()}
            </div>
          </div>

          {analytics.expiresAt && (
            <div className="analytics-result-expires">
              Expires: {new Date(analytics.expiresAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric', hour: '2-digit', minute: '2-digit' })}
            </div>
          )}

          <div className="analytics-clicks-section">
            <h4 className="analytics-clicks-title">Recent Clicks</h4>
            {(analytics.recentClicks || []).length === 0 ? (
              <p className="analytics-clicks-empty">No clicks yet</p>
            ) : (
              <div className="analytics-clicks-list">
                {(analytics.recentClicks || []).map((c, idx) => (
                  <div className="analytics-click-row" key={idx}>
                    <span className="click-col-time">{new Date(c.occurredAt).toLocaleString()}</span>
                    <span className="click-col-ref">{c.referrer}</span>
                    <span className="click-col-device">{c.device}</span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </section>
  );
}
