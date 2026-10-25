import { useEffect, useRef, useState } from 'react'
import { Navigate, useNavigate, useParams } from 'react-router-dom'
import Button from '../components/Button'
import FilmReelSpinner from '../components/FilmReelSpinner'
import { ApiError } from '../api/client'
import { submitSession, type PersonAnswerInput } from '../api/sessions'
import { useAuth } from '../context/AuthContext'
import { useSessionFlow } from '../context/SessionFlowContext'

export default function FindingPicksPage() {
  const navigate = useNavigate()
  const { id } = useParams()
  const { user } = useAuth()
  const { setup, answers } = useSessionFlow()

  const [stillWorking, setStillWorking] = useState(false)
  const [error, setError] = useState('')
  const [empty, setEmpty] = useState(false)
  const [attempt, setAttempt] = useState(0)

  const sessionId = setup?.sessionId
  const startedKey = useRef('') // StrictMode runs effects twice in dev; submit once per session/attempt

  useEffect(() => {
    if (!sessionId) return
    const key = `${sessionId}:${attempt}`
    if (startedKey.current === key) return
    startedKey.current = key
    setError('')
    setEmpty(false)
    setStillWorking(false)

    const people: PersonAnswerInput[] = Array.from({ length: setup.groupSize }, (_, i) => {
      const draft = answers[i]
      return {
        personIndex: i,
        name: draft?.name || (i === 0 ? user?.name ?? 'You' : `Person ${i + 1}`),
        ageBand: draft?.ageBand ?? null,
        moods: draft?.moods ?? [],
        genresUpFor: draft?.genresUpFor ?? [],
        hardNoGenres: draft?.hardNoGenres ?? [],
        languagesOk: draft?.languagesOk ?? [],
        intensity: draft?.intensity ?? 'balanced',
        somethingLikeTitleId: draft?.somethingLikeTitle?.id ?? null,
      }
    })

    const stillWorkingTimer = setTimeout(() => setStillWorking(true), 10000)

    submitSession(sessionId, people)
      .then((res) => {
        if (res.results.length === 0) {
          setEmpty(true)
          return
        }
        navigate(`/session/${sessionId}/results`, { state: { results: res } })
      })
      .catch((err) => {
        setError(err instanceof ApiError ? err.message : "Couldn't reach the movie database")
      })
      .finally(() => clearTimeout(stillWorkingTimer))

    return () => clearTimeout(stillWorkingTimer)
    // re-run only on retry (attempt) or when the session context is ready (sessionId); answers/setup/etc are read once per run
  }, [sessionId, attempt])

  if (!setup || setup.sessionId !== Number(id)) return <Navigate to="/" replace />

  if (empty) {
    return (
      <div className="app-container main-content finding-picks-screen">
        <h2>Nothing fits everyone tonight</h2>
        <p style={{ color: 'var(--color-text-muted)' }}>Try loosening the time limit or one of the hard-no genres.</p>
        <Button onClick={() => navigate(`/session/new`)}>Change answers</Button>
      </div>
    )
  }

  if (error) {
    return (
      <div className="app-container main-content finding-picks-screen">
        <h2>Couldn't reach the movie database</h2>
        <p style={{ color: 'var(--color-text-muted)' }}>{error}</p>
        <Button onClick={() => setAttempt((a) => a + 1)}>Retry</Button>
      </div>
    )
  }

  return (
    <div className="app-container main-content finding-picks-screen">
      <FilmReelSpinner
        label={`Matching ${setup.groupSize} people's answers…`}
        subLabel={stillWorking ? 'Still working…' : undefined}
      />
    </div>
  )
}
