import { useEffect, useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import Button from '../components/Button'
import { ApiError } from '../api/client'
import { getSessionResults, type SessionResult } from '../api/sessions'
import { getTitleDetail, TMDB_BACKDROP_BASE, TMDB_POSTER_LARGE, type TitleDetail } from '../api/titles'

interface NavState {
  result?: SessionResult
  people?: { personIndex: number; name: string }[]
}

export default function TitleDetailPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { id, titleId } = useParams()
  const sessionId = Number(id)
  const titleIdNum = Number(titleId)
  const navState = (location.state as NavState | null) ?? {}

  const [detail, setDetail] = useState<TitleDetail | null>(null)
  const [result, setResult] = useState<SessionResult | null>(navState.result ?? null)
  const [people, setPeople] = useState(navState.people ?? [])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const goBack = () => navigate(-1)
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') goBack()
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [navigate])

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError('')
    const needsResult = !result

    Promise.all([
      getTitleDetail(titleIdNum),
      needsResult ? getSessionResults(sessionId) : Promise.resolve(null),
    ])
      .then(([detailRes, resultsRes]) => {
        if (cancelled) return
        setDetail(detailRes)
        if (resultsRes) {
          setPeople(resultsRes.people)
          setResult(resultsRes.results.find((r) => r.titleId === titleIdNum) ?? null)
        }
      })
      .catch((err) => {
        if (!cancelled) setError(err instanceof ApiError ? err.message : 'Could not load this title.')
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
    // result/people are read once from nav state; only re-fetch when the ids in the URL change
  }, [titleIdNum, sessionId])

  if (loading) return null

  if (error || !detail) {
    return (
      <div className="app-container main-content finding-picks-screen">
        <h2>Couldn't load this title</h2>
        <p style={{ color: 'var(--color-text-muted)' }}>{error}</p>
        <Button onClick={() => navigate(-1)}>Back to results</Button>
      </div>
    )
  }

  const year = detail.releaseDate ? detail.releaseDate.slice(0, 4) : '—'
  const cert = detail.certificationRaw ?? 'Not rated'
  const runtime = detail.runtimeMinutes ? `${detail.runtimeMinutes} min` : '—'

  return (
    <div className="title-detail-page">
      <div className="title-detail-backdrop">
        {detail.backdropPath && <img src={`${TMDB_BACKDROP_BASE}${detail.backdropPath}`} alt="" />}
      </div>

      <div className="app-container main-content">
        <Button variant="secondary" onClick={() => navigate(-1)}>
          &larr; Back to results
        </Button>

        <div className="title-detail-main">
          <div className="title-detail-poster">
            {detail.posterPath && <img src={`${TMDB_POSTER_LARGE}${detail.posterPath}`} alt="" />}
          </div>
          <div className="title-detail-info">
            <h1>
              {detail.imdbId ? (
                <a href={`https://www.imdb.com/title/${detail.imdbId}/`} target="_blank" rel="noopener noreferrer">
                  {detail.title}
                </a>
              ) : (
                detail.title
              )}
            </h1>
            <p className="result-card-meta">
              {year} · {cert} · {runtime}
            </p>
            <p style={{ color: 'var(--color-text-muted)' }}>{detail.genreNames.join(', ')}</p>
            <p className="result-card-meta">
              {[
                detail.imdbRating != null && `IMDb ${detail.imdbRating}/10`,
                detail.rottenTomatoesRating != null && `Rotten Tomatoes ${detail.rottenTomatoesRating}%`,
                detail.metacriticRating != null && `Metacritic ${detail.metacriticRating}`,
                detail.imdbRating == null && detail.tmdbRating != null && `TMDB ${Number(detail.tmdbRating).toFixed(1)}/10`,
              ]
                .filter(Boolean)
                .join(' · ')}
            </p>
            <p>{detail.overview}</p>
          </div>
        </div>

        {detail.cast.length > 0 && (
          <section>
            <h3>Cast</h3>
            <div className="title-detail-cast">
              {detail.cast.map((c) => (
                <a
                  key={c.name}
                  className="title-detail-cast-member"
                  href={`https://www.imdb.com/find/?q=${encodeURIComponent(c.name)}&s=nm`}
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  {c.photoUrl && <img src={`${TMDB_POSTER_LARGE}${c.photoUrl}`} alt="" />}
                  <span>{c.name}</span>
                </a>
              ))}
            </div>
          </section>
        )}

        {result && (
          <section className="glass-surface title-detail-why">
            <h3>Why this pick</h3>
            <p>{result.aiExplanation}</p>
            <ul>
              {result.personScores.map((ps) => {
                const name = people.find((p) => p.personIndex === ps.personIndex)?.name ?? '?'
                return (
                  <li key={ps.personIndex}>
                    {name}: {ps.score}% match
                  </li>
                )
              })}
            </ul>
          </section>
        )}
      </div>
    </div>
  )
}
