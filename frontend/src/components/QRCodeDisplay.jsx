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
    <div className="qr-section">
      <p className="qr-label">QR Code</p>
      <div className="qr-code-container" ref={qrRef}>
        <QRCode
          value={shortUrl}
          size={200}
          level="H"
          includeMargin={true}
          fgColor="#000000"
          bgColor="#ffffff"
        />
      </div>
      <p className="qr-description">Scan with your phone to open the short URL</p>
      <button onClick={downloadQRCode} className="qr-download-btn">
        Download QR Code
      </button>
    </div>
  );
}

