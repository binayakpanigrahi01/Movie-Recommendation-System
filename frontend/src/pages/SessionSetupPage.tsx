import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Button from '../components/Button'
import Chip from '../components/Chip'
import Stepper from '../components/Stepper'
import { ApiError } from '../api/client'
import { startSession, type Occasion, type SessionMediaType, type TimeLimit } from '../api/sessions'
import { useSessionFlow } from '../context/SessionFlowContext'

const MEDIA_TYPES: { value: SessionMediaType; label: string }[] = [
  { value: 'movie', label: 'Movie' },
  { value: 'series', label: 'Series' },
  { value: 'either', label: 'Either' },
]

const MOVIE_TIME_LIMITS: { value: TimeLimit; label: string }[] = [
  { value: 'under_90', label: 'Under 90 min' },
  { value: '90_120', label: '90–120 min' },
  { value: '2_2.5', label: '2–2.5 hrs' },
  { value: 'no_limit', label: 'No limit' },
]

const SERIES_TIME_LIMITS: { value: TimeLimit; label: string }[] = [
  { value: 'episode_or_two', label: 'An episode or two' },
  { value: 'binge', label: 'A binge session' },
  { value: 'new_show', label: 'A new show to follow for weeks' },
]

const OCCASIONS: { value: Occasion; label: string }[] = [
  { value: 'friends_hangout', label: 'Friends hangout' },
  { value: 'date_night', label: 'Date night' },
  { value: 'family_time', label: 'Family time' },
  { value: 'solo_chill', label: 'Solo chill' },
]

export default function SessionSetupPage() {
  const navigate = useNavigate()
  const { begin } = useSessionFlow()
  const [mediaType, setMediaType] = useState<SessionMediaType | null>(null)
  const [groupSize, setGroupSize] = useState(1)
  const [timeLimit, setTimeLimit] = useState<TimeLimit | null>(null)
  const [occasion, setOccasion] = useState<Occasion | null>(null)
  const [error, setError] = useState('')
  const [starting, setStarting] = useState(false)

  const timeLimitOptions = useMemo(() => {
    if (mediaType === 'movie') return MOVIE_TIME_LIMITS
    if (mediaType === 'series') return SERIES_TIME_LIMITS
    if (mediaType === 'either') return [...MOVIE_TIME_LIMITS, ...SERIES_TIME_LIMITS]
    return []
  }, [mediaType])

  const canStart = mediaType !== null && timeLimit !== null

  const handleMediaTypeChange = (value: SessionMediaType) => {
    setMediaType(value)
    setTimeLimit(null)
  }

  const handleStart = async () => {
    if (!canStart) return
    setError('')
    setStarting(true)
    try {
      const res = await startSession({
        mediaType: mediaType!,
        groupSize,
        timeLimit: timeLimit!,
        occasion,
      })
      begin({ sessionId: res.sessionId, mediaType: mediaType!, groupSize, timeLimit: timeLimit! })
      navigate(groupSize > 1 ? `/session/${res.sessionId}/handoff/0` : `/session/${res.sessionId}/person/0`)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Something went wrong. Try again.')
      setStarting(false)
    }
  }

  return (
    <div className="app-container main-content">
      <div className="form-container">
        <div className="session-setup-header">
          <Button variant="secondary" onClick={() => navigate('/')}>
            &larr; Cancel
          </Button>
          <h2>Let's set up tonight</h2>
        </div>

        <section className="wizard-step">
          <p className="wizard-label">What are we watching?</p>
          <div className="session-media-cards">
            {MEDIA_TYPES.map((m) => (
              <button
                key={m.value}
                type="button"
                className={`session-media-card ${mediaType === m.value ? 'active' : ''}`}
                aria-pressed={mediaType === m.value}
                onClick={() => handleMediaTypeChange(m.value)}
              >
                {m.label}
              </button>
            ))}
          </div>
        </section>

        <section className="wizard-step">
          <p className="wizard-label">How many people are watching?</p>
          <Stepper value={groupSize} onChange={setGroupSize} min={1} max={8} label="Group size" />
        </section>

        <section className="wizard-step">
          <p className="wizard-label">How much time?</p>
          {timeLimitOptions.length === 0 ? (
            <p style={{ color: 'var(--color-text-muted)' }}>Pick what you're watching first.</p>
          ) : (
            <div className="chips-row">
              {timeLimitOptions.map((t) => (
                <Chip key={t.value} label={t.label} selected={timeLimit === t.value} onToggle={() => setTimeLimit(t.value)} />
              ))}
            </div>
          )}
        </section>

        <section className="wizard-step">
          <p className="wizard-label">What's the occasion? (optional)</p>
          <div className="chips-row">
            {OCCASIONS.map((o) => (
              <Chip
                key={o.value}
                label={o.label}
                selected={occasion === o.value}
                onToggle={() => setOccasion((prev) => (prev === o.value ? null : o.value))}
              />
            ))}
          </div>
        </section>

        {error && <p className="text-input-error-msg">{error}</p>}

        <div className="wizard-actions">
          <div style={{ marginLeft: 'auto' }}>
            <Button type="button" disabled={!canStart} loading={starting} onClick={handleStart}>
              Start
            </Button>
          </div>
        </div>
      </div>
    </div>
  )
}
