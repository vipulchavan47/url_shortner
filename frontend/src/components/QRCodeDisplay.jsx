import { useRef } from 'react';
import QRCode from 'qrcode.react';

export default function QRCodeDisplay({ shortUrl, shortCode }) {
  const qrRef = useRef();

  const downloadQRCode = () => {
    if (qrRef.current) {
      const canvas = qrRef.current.querySelector('canvas');
      const link = document.createElement('a');
      link.href = canvas.toDataURL('image/png');
      link.download = `qr_code_${shortCode}.png`;
      link.click();
    }
  };

  return (
    <div className="qr-display">
      <div className="qr-code-box" ref={qrRef}>
        <QRCode
          value={shortUrl}
          size={160}
          level="H"
          includeMargin={true}
          fgColor="#1a1a1a"
          bgColor="#ffffff"
        />
      </div>
      <button onClick={downloadQRCode} className="btn-download-qr">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
          <polyline points="7 10 12 15 17 10" />
          <line x1="12" y1="15" x2="12" y2="3" />
        </svg>
        Download
      </button>
    </div>
  );
}
