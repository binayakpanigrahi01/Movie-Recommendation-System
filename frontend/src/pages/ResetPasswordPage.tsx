import { useState, type FormEvent } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import Button from '../components/Button'
import TextInput from '../components/TextInput'
import { ApiError } from '../api/client'
import { resetPassword } from '../api/auth'

export default function ResetPasswordPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token') ?? ''

  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [expired, setExpired] = useState(false)
  const [done, setDone] = useState(false)
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    const errors: Record<string, string> = {}
    if (password.length < 8) errors.password = 'At least 8 characters'
    if (password !== confirm) errors.confirm = 'Passwords do not match'
    setFieldErrors(errors)
    if (Object.keys(errors).length > 0) return

    setLoading(true)
    try {
      await resetPassword(token, password)
      setDone(true)
    } catch (err) {
      if (err instanceof ApiError && err.status === 410) {
        setExpired(true)
      } else if (err instanceof ApiError) {
        setFieldErrors({ password: err.message })
      } else {
        setFieldErrors({ password: 'Something went wrong. Try again.' })
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-layout auth-layout-centered">
      <div className="auth-panel">
        <div className="auth-card glass-surface">
          {expired ? (
            <div className="form-container">
              <h2>This link has expired</h2>
              <p style={{ color: 'var(--color-text-muted)' }}>Reset links only work for 30 minutes.</p>
              <Link to="/forgot-password">
                <Button className="auth-submit">Send a new link</Button>
              </Link>
            </div>
          ) : done ? (
            <div className="form-container">
              <h2>Password updated</h2>
              <Link to="/login" className="auth-inline-link">
                Back to log in
              </Link>
            </div>
          ) : (
            <form className="auth-form" onSubmit={handleSubmit} noValidate>
              <h2>Set a new password</h2>
              <TextInput label="New password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} error={fieldErrors.password} autoComplete="new-password" />
              <TextInput label="Confirm password" type="password" value={confirm} onChange={(e) => setConfirm(e.target.value)} error={fieldErrors.confirm} autoComplete="new-password" />
              <Button type="submit" loading={loading} className="auth-submit">
                Update password
              </Button>
            </form>
          )}
        </div>
      </div>
    </div>
  )
}
