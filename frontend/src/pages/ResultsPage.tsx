import { useEffect, useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import Button from '../components/Button'
import PersonScoreCircles from '../components/PersonScoreCircles'
import { ApiError } from '../api/client'
import {
  getMoreResults,
  getSessionResults,
  MEDIA_TYPE_LABELS,
  TIME_LIMIT_LABELS,
  type SessionResult,
  type SessionResultsResponse,
} from '../api/sessions'
import { TMDB_POSTER_LARGE } from '../api/titles'
import { useSessionFlow } from '../context/SessionFlowContext'

function formatNames(people: { name: string }[]) {
  const names = people.map((p) => p.name)
  if (names.length <= 3) return names.join(', ')
  return `${names.slice(0, 3).join(', ')} + ${names.length - 3}`
}

const BADGE_LABEL: Record<string, string> = {
  compromise: 'Compromise pick',
  not_rated: 'Not rated',
}

export default function ResultsPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { id } = useParams()
  const { setup, clear } = useSessionFlow()
  const sessionId = Number(id)

  const [data, setData] = useState<SessionResultsResponse | null>(
    (location.state as { results?: SessionResultsResponse } | null)?.results ?? null,
  )
  const [loading, setLoading] = useState(!data)
  const [error, setError] = useState('')
  const [moreLoading, setMoreLoading] = useState(false)
  const [moreError, setMoreError] = useState('')
  const [showMoreDone, setShowMoreDone] = useState(false)

  useEffect(() => {
    if (data || !sessionId) return
    setLoading(true)
    getSessionResults(sessionId)
      .then(setData)
      .catch((err) => setError(err instanceof ApiError ? err.message : 'Could not load your results.'))
      .finally(() => setLoading(false))
  }, [sessionId, data])

  const handleShowMore = () => {
    setMoreLoading(true)
    setMoreError('')
    getMoreResults(sessionId)
      .then((res) => {
        // /more returns the full accumulated result list (same as GET /results), not just the new batch
        setData(res)
        setShowMoreDone(true)
      })
      .catch((err) => setMoreError(err instanceof ApiError ? err.message : 'Could not load more picks.'))
      .finally(() => setMoreLoading(false))
  }

  const handleStartOver = () => {
    clear()
    navigate('/')
  }

  const openTitle = (result: SessionResult) => {
    navigate(`/session/${sessionId}/title/${result.titleId}`, {
      state: { result, people: data?.people ?? [] },
    })
  }

  if (loading) return null

  if (error) {
    return (
      <div className="app-container main-content finding-picks-screen">
        <h2>Couldn't load your results</h2>
        <p style={{ color: 'var(--color-text-muted)' }}>{error}</p>
        <Button onClick={() => navigate(`/session/new`)}>Change answers</Button>
      </div>
    )
  }

  if (!data) return null

  const showSummary = setup && setup.sessionId === sessionId
  const firstFive = data.results.filter((r) => r.rank <= 5)
  const rest = data.results.filter((r) => r.rank > 5)

  return (
    <div className="app-container main-content">
      <header className="results-header">
        <h2>Top 5 for {formatNames(data.people)}</h2>
        {showSummary && (
          <p className="results-summary">
            {MEDIA_TYPE_LABELS[setup.mediaType]} · {TIME_LIMIT_LABELS[setup.timeLimit]}
          </p>
        )}
      </header>

      <div className="results-grid">
        {firstFive.map((r) => (
          <ResultCard key={r.titleId} result={r} people={data.people} onClick={() => openTitle(r)} />
        ))}
      </div>

      {rest.length > 0 && (
        <div className="results-grid">
          {rest.map((r) => (
            <ResultCard key={r.titleId} result={r} people={data.people} onClick={() => openTitle(r)} />
          ))}
        </div>
      )}

      {moreError && <p className="text-input-error-msg">{moreError}</p>}

      {!showMoreDone && data.results.length <= 5 && (
        <div style={{ display: 'flex', justifyContent: 'center' }}>
          <Button variant="secondary" loading={moreLoading} onClick={handleShowMore}>
            Show more
          </Button>
        </div>
      )}

      <div className="results-actions">
        <Button variant="secondary" onClick={() => navigate('/session/new')}>
          Change answers
        </Button>
        <Button variant="secondary" onClick={handleStartOver}>
          Start over
        </Button>
      </div>
    </div>
  )
}

function ResultCard({
  result,
  people,
  onClick,
}: {
  result: SessionResult
  people: { personIndex: number; name: string }[]
  onClick: () => void
}) {
  const year = result.year ?? '—'
  const runtime = result.runtimeMinutes ? `${result.runtimeMinutes} min` : '—'
  const cert = result.certificationRaw ?? 'Not rated'
  const scores = result.personScores.map((ps) => ({
    personIndex: ps.personIndex,
    score: ps.score,
    name: people.find((p) => p.personIndex === ps.personIndex)?.name ?? '?',
  }))

  return (
    <button type="button" className="result-card" onClick={onClick}>
      <div className="result-card-poster">
        {result.posterPath ? (
          <img src={`${TMDB_POSTER_LARGE}${result.posterPath}`} alt="" />
        ) : (
          <div className="result-card-poster-fallback" />
        )}
      </div>
      <div className="result-card-body">
        <h3 className="result-card-title">{result.title}</h3>
        <p className="result-card-meta">
          {year} · {runtime} · {cert}
        </p>
        <p className="result-card-score">{Math.round(result.groupScore)}% match</p>
        <PersonScoreCircles scores={scores} />
        {result.badge !== 'none' && (
          <span className={`badge badge-${result.badge.replace('_', '-')}`}>{BADGE_LABEL[result.badge]}</span>
        )}
        <p className="result-card-explanation">{result.aiExplanation}</p>
      </div>
    </button>
  )
}
