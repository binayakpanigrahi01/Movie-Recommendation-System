import { useState, type FormEvent } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import Button from '../components/Button'
import TextInput from '../components/TextInput'
import { ApiError } from '../api/client'
import { login as loginRequest, signup as signupRequest } from '../api/auth'
import { getProfile } from '../api/profile'
import { useAuth } from '../context/AuthContext'
import heroImage from '../assets/hero.png'

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export default function AuthPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const { login: setAuthed } = useAuth()
  const mode = location.pathname === '/signup' ? 'signup' : 'login'

  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [formError, setFormError] = useState<{ message: string; showLoginLink?: boolean } | null>(null)
  const [loading, setLoading] = useState(false)

  const afterAuth = async () => {
    try {
      await getProfile()
      navigate('/')
    } catch (err) {
      if (err instanceof ApiError && err.code === 'PROFILE_NOT_FOUND') {
        navigate('/profile/setup')
        return
      }
      navigate('/')
    }
  }

  const handleSignup = async (e: FormEvent) => {
    e.preventDefault()
    setFormError(null)
    const errors: Record<string, string> = {}
    if (!name.trim()) errors.name = 'Name is required'
    if (!EMAIL_RE.test(email)) errors.email = 'Enter a valid email address'
    if (password.length < 8) errors.password = 'At least 8 characters'
    if (password !== confirm) errors.confirm = 'Passwords do not match'
    setFieldErrors(errors)
    if (Object.keys(errors).length > 0) return

    setLoading(true)
    try {
      const res = await signupRequest(name.trim(), email.trim(), password)
      setAuthed(res.token, res.user)
      navigate('/profile/setup')
    } catch (err) {
      if (err instanceof ApiError && err.code === 'EMAIL_ALREADY_REGISTERED') {
        setFormError({ message: 'Email already registered.', showLoginLink: true })
      } else if (err instanceof ApiError) {
        setFormError({ message: err.message })
      } else {
        setFormError({ message: 'Something went wrong. Try again.' })
      }
    } finally {
      setLoading(false)
    }
  }

  const handleLogin = async (e: FormEvent) => {
    e.preventDefault()
    setFormError(null)
    const errors: Record<string, string> = {}
    if (!EMAIL_RE.test(email)) errors.email = 'Enter a valid email address'
    if (!password) errors.password = 'Password is required'
    setFieldErrors(errors)
    if (Object.keys(errors).length > 0) return

    setLoading(true)
    try {
      const res = await loginRequest(email.trim(), password)
      setAuthed(res.token, res.user)
      await afterAuth()
    } catch (err) {
      if (err instanceof ApiError && err.code === 'INVALID_CREDENTIALS') {
        setFormError({ message: 'Incorrect email or password.' })
      } else if (err instanceof ApiError) {
        setFormError({ message: err.message })
      } else {
        setFormError({ message: 'Something went wrong. Try again.' })
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-layout">
      <div className="auth-hero" style={{ backgroundImage: `url(${heroImage})` }}>
        <h1 className="auth-hero-title">
          MOVIE
          <br />
          RECOMMENDER
        </h1>
      </div>
      <div className="auth-panel">
        <div className="auth-card glass-surface">
          <div className="auth-tabs" role="tablist">
            <Link
              to="/signup"
              role="tab"
              aria-selected={mode === 'signup'}
              className={`auth-tab ${mode === 'signup' ? 'active' : ''}`}
            >
              Sign up
            </Link>
            <Link
              to="/login"
              role="tab"
              aria-selected={mode === 'login'}
              className={`auth-tab ${mode === 'login' ? 'active' : ''}`}
            >
              Log in
            </Link>
          </div>

          {formError && (
            <p className="auth-form-error">
              {formError.message}{' '}
              {formError.showLoginLink && (
                <Link to="/login" className="auth-inline-link">
                  Log in instead
                </Link>
              )}
            </p>
          )}

          {mode === 'signup' ? (
            <form className="auth-form" onSubmit={handleSignup} noValidate>
              <TextInput label="Name" value={name} onChange={(e) => setName(e.target.value)} error={fieldErrors.name} autoComplete="name" />
              <TextInput label="Email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} error={fieldErrors.email} autoComplete="email" />
              <TextInput label="Password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} error={fieldErrors.password} autoComplete="new-password" />
              <TextInput label="Confirm password" type="password" value={confirm} onChange={(e) => setConfirm(e.target.value)} error={fieldErrors.confirm} autoComplete="new-password" />
              <Button type="submit" loading={loading} className="auth-submit">
                Sign up
              </Button>
            </form>
          ) : (
            <form className="auth-form" onSubmit={handleLogin} noValidate>
              <TextInput label="Email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} error={fieldErrors.email} autoComplete="email" />
              <TextInput label="Password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} error={fieldErrors.password} autoComplete="current-password" />
              <Link to="/forgot-password" className="auth-inline-link auth-forgot-link">
                Forgot password?
              </Link>
              <Button type="submit" loading={loading} className="auth-submit">
                Log in
              </Button>
            </form>
          )}
        </div>
      </div>
    </div>
  )
}
