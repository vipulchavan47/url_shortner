import { useState } from 'react';
import QRCodeDisplay from './QRCodeDisplay';

export default function ResultCard({ result, resultData, onCreateAnother }) {
  const [copied, setCopied] = useState(false);
  const [analyticsCopied, setAnalyticsCopied] = useState(false);
  const [clickCount, setClickCount] = useState(0);
  const [showQR, setShowQR] = useState(false);

  const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080';

  const copyToClipboard = async (text, setCopiedState) => {
    try {
      await navigator.clipboard.writeText(text);
      setCopiedState(true);
      setTimeout(() => setCopiedState(false), 2000);
    } catch {
      const el = document.createElement('textarea');
      el.value = text;
      document.body.appendChild(el);
      el.select();
      document.execCommand('copy');
      document.body.removeChild(el);
      setCopiedState(true);
      setTimeout(() => setCopiedState(false), 2000);
    }
  };

  const fetchClickCount = async () => {
    const token = resultData?.analyticsUrl ? resultData.analyticsUrl.split('/').pop() : null;
    if (!token) return;
    try {
      const response = await fetch(`${API_BASE}/api/analytics/${token}`);
      if (!response.ok) return;
      const data = await response.json();
      setClickCount(data.totalClicks || 0);
    } catch (err) {
      console.error('Failed to fetch click count:', err);
    }
  };

  const goToAnalytics = () => {
    const token = resultData?.analyticsUrl ? resultData.analyticsUrl.split('/').pop() : null;
    if (!token) return;
    window.history.pushState({}, '', `/analytics/${token}`);
    window.location.reload();
  };

  const shortCode = result.split('/').pop();

  return (
    <div className="result-card">
      <div className="result-header">
        <div className="result-badge">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <polyline points="20 6 9 17 4 12" />
          </svg>
          Your short link is ready
        </div>
      </div>

      <div className="result-url-display">
        <div className="result-url-text">
          <span className="result-domain">{result.replace(/^https?:\/\//, '').split('/')[0]}</span>
          <span className="result-slash">/</span>
          <span className="result-code">{shortCode}</span>
        </div>
        <div className="result-actions">
          <button onClick={() => copyToClipboard(result, setCopied)} className="btn-result-action">
            {copied ? (
              <>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12" /></svg>
                Copied
              </>
            ) : (
              <>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="9" y="9" width="13" height="13" rx="2" ry="2" /><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" /></svg>
                Copy
              </>
            )}
          </button>
          <a href={result} target="_blank" rel="noopener noreferrer" className="btn-result-action btn-result-open">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" /><polyline points="15 3 21 3 21 9" /><line x1="10" y1="14" x2="21" y2="3" /></svg>
            Open
          </a>
        </div>
      </div>

      {resultData?.expiresAt && (
        <div className="result-expiry">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10" /><polyline points="12 6 12 12 16 14" /></svg>
          <span>Expires: {new Date(resultData.expiresAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric', hour: '2-digit', minute: '2-digit' })}</span>
          <span className="expiry-countdown-badge">{resultData.expiryIn}</span>
        </div>
      )}

      <div className="result-bottom-row">
        <div className="result-qr-col">
          <button className="qr-toggle-btn" onClick={() => setShowQR(!showQR)}>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
              <rect x="3" y="3" width="7" height="7" /><rect x="14" y="3" width="7" height="7" /><rect x="3" y="14" width="7" height="7" /><rect x="14" y="14" width="3" height="3" /><line x1="21" y1="14" x2="21" y2="14.01" /><line x1="14" y1="21" x2="14" y2="21.01" /><line x1="21" y1="21" x2="21" y2="21.01" />
            </svg>
            {showQR ? 'Hide QR Code' : 'Show QR Code'}
          </button>
          {showQR && (
            <div className="result-qr-expand">
              <QRCodeDisplay shortUrl={result} shortCode={shortCode} />
            </div>
          )}
        </div>

        <div className="result-analytics-col">
          <div className="result-click-count">
            <span className="click-number">{clickCount}</span>
            <span className="click-label">clicks</span>
          </div>
          <div className="result-analytics-actions">
            <button onClick={fetchClickCount} className="btn-result-action btn-small">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><polyline points="23 4 23 10 17 10" /><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" /></svg>
              Refresh
            </button>
            {resultData?.analyticsUrl && (
              <button onClick={goToAnalytics} className="btn-result-action btn-small btn-analytics-link">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="18" y1="20" x2="18" y2="10" /><line x1="12" y1="20" x2="12" y2="4" /><line x1="6" y1="20" x2="6" y2="14" /></svg>
                Analytics
              </button>
            )}
          </div>
        </div>
      </div>

      {resultData?.analyticsUrl && (
        <div className="result-analytics-url">
          <span className="analytics-url-label">Analytics URL</span>
          <div className="analytics-url-row">
            <code className="analytics-url-text">{resultData.analyticsUrl}</code>
            <button
              onClick={() => copyToClipboard(resultData.analyticsUrl, setAnalyticsCopied)}
              className="btn-copy-inline"
            >
              {analyticsCopied ? 'Copied' : 'Copy'}
            </button>
          </div>
        </div>
      )}

      <button onClick={onCreateAnother} className="btn-create-another">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="12" y1="5" x2="12" y2="19" /><line x1="5" y1="12" x2="19" y2="12" /></svg>
        Create Another
      </button>
    </div>
  );
}
