export default function Footer() {
  return (
    <footer className="footer">
      <div className="footer-inner">
        <span className="footer-logo">
          <span className="logo-bit">Bit</span><span className="logo-short">Short</span>
        </span>
        <span className="footer-copy">&copy; {new Date().getFullYear()}</span>
      </div>
    </footer>
  );
}
