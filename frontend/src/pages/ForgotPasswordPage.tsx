import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import Button from '../components/Button'
import TextInput from '../components/TextInput'
import { forgotPassword } from '../api/auth'

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [sent, setSent] = useState(false)

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    if (!EMAIL_RE.test(email)) {
      setError('Enter a valid email address')
      return
    }
    setError('')
    setLoading(true)
    try {
      await forgotPassword(email.trim())
    } finally {
      setLoading(false)
      setSent(true)
    }
  }

  return (
    <div className="auth-layout auth-layout-centered">
      <div className="auth-panel">
        <div className="auth-card glass-surface">
          <Link to="/login" className="auth-inline-link auth-back-link">
            &larr; Back to log in
          </Link>
          {sent ? (
            <div className="form-container">
              <h2>Check your inbox</h2>
              <p style={{ color: 'var(--color-text-muted)' }}>
                If that email is registered, we've sent a reset link. It expires in 30 minutes.
              </p>
            </div>
          ) : (
            <form className="auth-form" onSubmit={handleSubmit} noValidate>
              <h2>Reset your password</h2>
              <TextInput label="Email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} error={error} autoComplete="email" />
              <Button type="submit" loading={loading} className="auth-submit">
                Send reset link
              </Button>
            </form>
          )}
        </div>
      </div>
    </div>
  )
}
