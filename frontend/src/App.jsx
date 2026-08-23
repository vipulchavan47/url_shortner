import { useState, useEffect } from 'react';
import './App.css';
import QRCodeDisplay from './components/QRCodeDisplay';

export default function App() {
  const [longUrl, setLongUrl] = useState('');
  const [customCode, setCustomCode] = useState('');
  const [result, setResult] = useState(null);
  const [resultData, setResultData] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [clickCount, setClickCount] = useState(0);
  const [analytics, setAnalytics] = useState(null);
  const [analyticsMode, setAnalyticsMode] = useState(false);
  const [analyticsToken, setAnalyticsToken] = useState(null);
  const [showAnalyticsModal, setShowAnalyticsModal] = useState(false);
  const [modalInput, setModalInput] = useState('');
  
  // Expiry state
  const [expiryType, setExpiryType] = useState('none');
  const [expiryValue, setExpiryValue] = useState('');

  const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080';

  useEffect(() => {
    const path = window.location.pathname;
    const m = path.match(/^\/analytics\/(.+)$/);
    if (m) {
      setAnalyticsMode(true);
      setAnalyticsToken(m[1]);
      fetchAnalytics(m[1]);
    }
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    
    if (!longUrl.trim()) {
      setError('Please enter a URL');
      return;
    }

    setLoading(true);
    try {
      const payload = { longUrl: longUrl.trim() };
      if (customCode.trim()) {
        payload.customCode = customCode.trim();
      }

      // Add expiry parameters
      if (expiryType === 'minutes' && expiryValue) {
        payload.expiryTimeMinutes = parseInt(expiryValue);
      } else if (expiryType === 'hours' && expiryValue) {
        payload.expiryTimeHours = parseInt(expiryValue);
      } else if (expiryType === 'days' && expiryValue) {
        payload.expiryTimeDays = parseInt(expiryValue);
      } else if (expiryType === 'timestamp' && expiryValue) {
        payload.expiresAtTimestamp = expiryValue;
      }

      const response = await fetch(`${API_BASE}/api/shorten`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      const data = await response.json();

      if (!response.ok) {
        setError(data.message || 'Failed to shorten URL');
        return;
      }

      setResult(data.shortUrl);
      setResultData(data);
      setClickCount(0);
      setAnalytics(null);
      setLongUrl('');
      setCustomCode('');
      setExpiryType('none');
      setExpiryValue('');
    } catch (err) {
      setError('Network error. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const fetchAnalytics = async (token) => {
    try {
      const response = await fetch(`${API_BASE}/api/analytics/${token}`);
      if (!response.ok) return;
      const data = await response.json();
      setAnalytics(data);
      setClickCount(data.totalClicks || 0);
    } catch (err) {
      console.error('Failed to fetch analytics:', err);
    }
  };

  const fetchClickCount = async () => {
    // prefer analyticsUrl/token if available
    const token = resultData?.analyticsUrl ? resultData.analyticsUrl.split('/').pop() : null;
    if (!token) return;
    fetchAnalytics(token);
  };

  const copyToClipboard = async () => {
    await navigator.clipboard.writeText(result);
    alert('Copied to clipboard!');
  };

  const getExpiryStatus = () => {
    if (!analytics) return null;
    if (analytics.status === 'EXPIRED') {
      return <span className="expired-badge">Expired</span>;
    }
    return <span className="active-badge">Active</span>;
  };

  const getExpiryInfo = () => {
    if (!analytics) return null;
    if (!analytics.expiresAt) {
      return <p className="expiry-info">No expiry • Link active indefinitely</p>;
    }
    return (
      <div className="expiry-details">
        <p className="expiry-info">Expires: <strong>{new Date(analytics.expiresAt).toLocaleString()}</strong></p>
        <p className="expiry-status-text">Status: {getExpiryStatus()}</p>
      </div>
    );
  };

  if (analyticsMode) {
    return (
      <div className="container">
        <div className="card">
          <h1>BitShort Analytics</h1>
          {!analytics ? <p>Loading...</p> : (
            <div className="analytics-dashboard">
              <div>
                <h3 style={{marginBottom:8}}>{analytics.shortCode}</h3>
                <p style={{color:'#9ca3af', marginBottom:12}}><a href={analytics.originalUrl} style={{color:'#3b82f6'}}>{analytics.originalUrl}</a></p>
              </div>

              <div className="summary-cards">
                <div className="card-item">Total Clicks <strong>{analytics.totalClicks}</strong></div>
                <div className="card-item">Today <strong>{analytics.todayClicks}</strong></div>
                <div className="card-item">Last 7 Days <strong>{analytics.last7DaysClicks}</strong></div>
                <div className="card-item">Status {getExpiryStatus()}</div>
              </div>

              <div>
                <h4 style={{marginTop:12}}>Recent Clicks</h4>
                <table className="analytics-table">
                  <thead><tr><th>Time</th><th>Referrer</th><th>Device</th></tr></thead>
                  <tbody>
                    {(analytics.recentClicks || []).map((c, idx) => (
                      <tr key={idx}><td>{new Date(c.occurredAt).toLocaleString()}</td><td>{c.referrer}</td><td>{c.device}</td></tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      </div>
    );
  }

  const goToAnalytics = (token) => {
    if (!token) return;
    window.history.pushState({}, '', `/analytics/${token}`);
    window.location.reload();
  };

  return (
    <div className="container">
      <div className="card">
        <h1>URL Shortener</h1>
        <p className="subtitle">Create short and shareable links instantly</p>


        {!result ? (
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Long URL</label>
              <input
                type="url"
                placeholder="https://example.com"
                value={longUrl}
                onChange={(e) => setLongUrl(e.target.value)}
                disabled={loading}
              />
            </div>

            <div className="form-group">
              <label>Custom Alias (optional)</label>
              <input
                type="text"
                placeholder="e.g. vipul"
                value={customCode}
                onChange={(e) => setCustomCode(e.target.value)}
                disabled={loading}
              />
            </div>

            {/* Expiry Options */}
            <div className="form-group">
              <label>Link Expiry (optional)</label>
              <div className="expiry-options">
                <label className="radio-label">
                  <input
                    type="radio"
                    name="expiry"
                    value="none"
                    checked={expiryType === 'none'}
                    onChange={(e) => {
                      setExpiryType(e.target.value);
                      setExpiryValue('');
                    }}
                    disabled={loading}
                  />
                  No Expiry
                </label>
                <label className="radio-label">
                  <input
                    type="radio"
                    name="expiry"
                    value="minutes"
                    checked={expiryType === 'minutes'}
                    onChange={(e) => setExpiryType(e.target.value)}
                    disabled={loading}
                  />
                  Minutes
                </label>
                <label className="radio-label">
                  <input
                    type="radio"
                    name="expiry"
                    value="hours"
                    checked={expiryType === 'hours'}
                    onChange={(e) => setExpiryType(e.target.value)}
                    disabled={loading}
                  />
                  Hours
                </label>
                <label className="radio-label">
                  <input
                    type="radio"
                    name="expiry"
                    value="days"
                    checked={expiryType === 'days'}
                    onChange={(e) => setExpiryType(e.target.value)}
                    disabled={loading}
                  />
                  Days
                </label>
                <label className="radio-label">
                  <input
                    type="radio"
                    name="expiry"
                    value="timestamp"
                    checked={expiryType === 'timestamp'}
                    onChange={(e) => setExpiryType(e.target.value)}
                    disabled={loading}
                  />
                  Date/Time
                </label>
              </div>

              {expiryType !== 'none' && expiryType !== '' && (
                <div className="expiry-input-group">
                  {expiryType === 'timestamp' ? (
                    <input
                      type="datetime-local"
                      value={expiryValue}
                      onChange={(e) => setExpiryValue(e.target.value)}
                      disabled={loading}
                      className="expiry-input"
                    />
                  ) : (
                    <input
                      type="number"
                      placeholder={`Enter number of ${expiryType}`}
                      value={expiryValue}
                      onChange={(e) => setExpiryValue(e.target.value)}
                      disabled={loading}
                      className="expiry-input"
                      min="1"
                    />
                  )}
                </div>
              )}
            </div>

            {error && <div className="error">{error}</div>}

            <button type="submit" disabled={loading} className="btn">
              {loading ? 'Generating...' : 'Generate Short URL'}
            </button>
          </form>
        ) : (
          <div className="success">
            <p>Your short URL is ready!</p>
            <div className="result-group">
              <input
                type="text"
                value={result}
                readOnly
                className="result-input"
              />
              <button onClick={copyToClipboard} className="btn copy-btn">
                Copy
              </button>
            </div>

            {resultData?.analyticsUrl && (
              <div className="result-expiry-info" style={{marginTop:8}}>
                <p style={{marginBottom:6}}>Analytics URL:</p>
                <div className="result-group" style={{marginTop:6}}>
                  <input
                    type="text"
                    value={resultData.analyticsUrl}
                    readOnly
                    className="result-input"
                  />
                  <button
                    onClick={() => { navigator.clipboard.writeText(resultData.analyticsUrl); alert('Analytics URL copied'); }}
                    className="btn copy-btn"
                  >
                    Copy
                  </button>
                </div>
              </div>
            )}

            {resultData?.expiresAt && (
              <div className="result-expiry-info">
                <p>Expires: <strong>{new Date(resultData.expiresAt).toLocaleString()}</strong></p>
                <p className="expiry-countdown">In: <strong>{resultData.expiryIn}</strong></p>
              </div>
            )}
            
            <QRCodeDisplay shortUrl={result} shortCode={result.split('/').pop()} />

            <div className="analytics">
              <p className="click-count">Clicks: <strong>{clickCount}</strong></p>
              {getExpiryInfo()}
              <div style={{display: 'flex', gap: '8px', marginTop: '8px'}}>
                <button onClick={fetchClickCount} className="btn refresh-btn">
                  Refresh Count
                </button>
                {resultData?.analyticsUrl && (
                  <button
                    onClick={() => {
                      // open modal with analytics URL prefilled
                      setModalInput(resultData.analyticsUrl || '');
                      setShowAnalyticsModal(true);
                    }}
                    className="btn"
                  >
                    View Analytics
                  </button>
                )}
              </div>
            </div>

            <a href={result} target="_blank" rel="noopener noreferrer" className="link-btn">
              Open Link
            </a>
            <button
              onClick={() => {
                setResult(null);
                setResultData(null);
                setError('');
                setClickCount(0);
                setAnalytics(null);
              }}
              className="btn secondary-btn"
            >
              Create Another
            </button>
          </div>
        )}

        {/* separator and view analytics button */}
        <div style={{marginTop:24, textAlign:'center'}}>
          <hr style={{border:'none', height:1, background:'rgba(255,255,255,0.04)', marginBottom:12}} />
          <div style={{color:'#9ca3af', marginBottom:8}}>Already have an analytics link?</div>
          <button className="btn secondary-btn" style={{width:200, margin:'0 auto'}} onClick={() => { setModalInput(''); setShowAnalyticsModal(true); }}>
            View Analytics
          </button>
        </div>

        {/* Modal */}
        {showAnalyticsModal && (
          <div className="modal-overlay" onClick={() => setShowAnalyticsModal(false)}>
            <div className="modal" onClick={(e) => e.stopPropagation()}>
              <div className="modal-header" style={{display:'flex', justifyContent:'space-between', alignItems:'center'}}>
                <h3 style={{margin:0}}>View Analytics</h3>
                <button className="modal-close" onClick={() => setShowAnalyticsModal(false)}>×</button>
              </div>
              <p style={{color:'#9ca3af', marginTop:8}}>Enter your analytics URL</p>
              <input type="text" placeholder="https://.../analytics/" value={modalInput} onChange={(e) => setModalInput(e.target.value)} style={{width:'100%', marginTop:8, padding:10, borderRadius:8, border:'1px solid rgba(255,255,255,0.04)', background:'rgba(255,255,255,0.02)', color:'#e6eef8'}} />
              <div style={{marginTop:12, display:'flex', justifyContent:'flex-end'}}>
                <button className="btn" onClick={() => {
                  const token = (modalInput || '').split('/').pop();
                  if (!token) {
                    alert('Please enter a valid analytics URL or token');
                    return;
                  }
                  window.history.pushState({}, '', `/analytics/${token}`);
                  window.location.reload();
                }}>View Analytics</button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
