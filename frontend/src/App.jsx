import { useState } from 'react';
import './App.css';
import QRCodeDisplay from './components/QRCodeDisplay';

export default function App() {
  const [longUrl, setLongUrl] = useState('');
  const [customCode, setCustomCode] = useState('');
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [clickCount, setClickCount] = useState(0);

  const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080';

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
      setClickCount(0);
      setLongUrl('');
      setCustomCode('');
    } catch (err) {
      setError('Network error. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const fetchClickCount = async () => {
    if (!result) return;
    const shortCode = result.split('/').pop();
    try {
      const response = await fetch(`${API_BASE}/api/analytics/${shortCode}`);
      const data = await response.json();
      setClickCount(data.clickCount);
    } catch (err) {
      console.error('Failed to fetch click count:', err);
    }
  };

  const copyToClipboard = async () => {
    await navigator.clipboard.writeText(result);
    alert('Copied to clipboard!');
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
            
            <QRCodeDisplay shortUrl={result} shortCode={result.split('/').pop()} />

            <div className="analytics">
              <p className="click-count">Clicks: <strong>{clickCount}</strong></p>
              <button onClick={fetchClickCount} className="btn refresh-btn">
                Refresh Count
              </button>
            </div>

            <a href={result} target="_blank" rel="noopener noreferrer" className="link-btn">
              Open Link
            </a>
            <button
              onClick={() => {
                setResult(null);
                setError('');
                setClickCount(0);
              }}
              className="btn secondary-btn"
            >
              Create Another
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
