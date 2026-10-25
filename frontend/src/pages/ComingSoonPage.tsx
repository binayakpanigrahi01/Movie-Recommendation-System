import { Link } from 'react-router-dom'

export default function ComingSoonPage() {
  return (
    <div className="app-container main-content" style={{ textAlign: 'center' }}>
      <h2>This screen isn't built yet</h2>
      <p style={{ color: 'var(--color-text-muted)' }}>Results and title detail land in the next phase.</p>
      <Link to="/" className="auth-inline-link">
        Back to home
      </Link>
    </div>
  )
}
