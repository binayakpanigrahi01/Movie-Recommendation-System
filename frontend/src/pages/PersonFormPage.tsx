import { useEffect, useMemo, useRef, useState, type FormEvent } from 'react'
import { Navigate, useNavigate, useParams } from 'react-router-dom'
import Button from '../components/Button'
import Chip from '../components/Chip'
import TextInput from '../components/TextInput'
import Slider3Step from '../components/Slider3Step'
import TitleSearchPicker from '../components/TitleSearchPicker'
import { getGenres, type Genre } from '../api/genres'
import { getProfile } from '../api/profile'
import type { AgeBand, Intensity } from '../api/sessions'
import { useAuth } from '../context/AuthContext'
import { useSessionFlow, type PersonAnswerDraft } from '../context/SessionFlowContext'
import { groupGenresByName, type GenreGroup } from '../utils/genres'
import { POPULAR_LANGUAGES } from '../utils/languages'

const AGE_BANDS: { value: AgeBand; label: string }[] = [
  { value: 'under_7', label: 'Under 7' },
  { value: '7_12', label: '7–12' },
  { value: '13_15', label: '13–15' },
  { value: '16_17', label: '16–17' },
  { value: '18_plus', label: '18+' },
]

const MOODS = ['Laugh', 'Thrill', 'Feel something', 'Think', 'Get scared', 'Comfort', 'Adventure', 'Romance']

const INTENSITY_STEPS = [
  { value: 'light' as Intensity, label: 'Light' },
  { value: 'balanced' as Intensity, label: 'Balanced' },
  { value: 'intense' as Intensity, label: 'Intense' },
] as const

export default function PersonFormPage() {
  const navigate = useNavigate()
  const { user } = useAuth()
  const { id, index } = useParams()
  const { setup, answers, saveAnswer } = useSessionFlow()

  const personIndex = Number(index)
  const isHost = personIndex === 0
  const isSolo = setup?.groupSize === 1

  const [genres, setGenres] = useState<Genre[]>([])
  const [loaded, setLoaded] = useState(false)

  const [name, setName] = useState('')
  const [ageBand, setAgeBand] = useState<AgeBand | null>(null)
  const [moods, setMoods] = useState<string[]>([])
  const [genresUpFor, setGenresUpFor] = useState<number[]>([])
  const [hardNoGenres, setHardNoGenres] = useState<number[]>([])
  const [languagesOk, setLanguagesOk] = useState<string[]>([])
  const [intensity, setIntensity] = useState<Intensity>('balanced')
  const [somethingLike, setSomethingLike] = useState<PersonAnswerDraft['somethingLikeTitle']>(null)
  const [somethingLikeType, setSomethingLikeType] = useState<'movie' | 'tv'>(setup?.mediaType === 'series' ? 'tv' : 'movie')

  const [errors, setErrors] = useState<Record<string, string>>({})

  const nameRef = useRef<HTMLElement>(null)
  const ageRef = useRef<HTMLElement>(null)
  const moodRef = useRef<HTMLElement>(null)
  const languageRef = useRef<HTMLElement>(null)

  useEffect(() => {
    if (!setup || !Number.isInteger(personIndex)) return
    const existing = answers[personIndex]
    let alive = true
    Promise.all([getGenres(), isHost && !existing ? getProfile().catch(() => null) : Promise.resolve(null)]).then(
      ([genreRes, profile]) => {
        if (!alive) return
        setGenres(genreRes.genres)
        if (existing) {
          setName(existing.name)
          setAgeBand(existing.ageBand)
          setMoods(existing.moods)
          setGenresUpFor(existing.genresUpFor)
          setHardNoGenres(existing.hardNoGenres)
          setLanguagesOk(existing.languagesOk)
          setIntensity(existing.intensity)
          setSomethingLike(existing.somethingLikeTitle)
        } else if (profile) {
          setName(user?.name ?? '')
          setGenresUpFor(profile.favoriteGenres.slice(0, 3))
          setHardNoGenres(profile.neverWantGenres)
          setLanguagesOk(profile.languages)
        } else if (isHost) {
          setName(user?.name ?? '')
        }
        setLoaded(true)
      },
    )
    return () => {
      alive = false
    }
    // only re-load when navigating to a different person; answers/setup/user are read once per load
  }, [personIndex])

  const genreGroups = useMemo(() => groupGenresByName(genres), [genres])

  const skipNameAge = isSolo

  const toggleMood = (m: string) => {
    setMoods((prev) => {
      if (prev.includes(m)) return prev.filter((x) => x !== m)
      if (prev.length >= 2) return prev
      return [...prev, m]
    })
  }

  const toggleGenreUpFor = (group: GenreGroup) => {
    setGenresUpFor((prev) => {
      const isSelected = group.ids.some((gid) => prev.includes(gid))
      if (isSelected) return prev.filter((gid) => !group.ids.includes(gid))
      if (prev.length >= 3) return prev
      return [...prev, group.representativeId]
    })
  }

  const toggleHardNo = (group: GenreGroup) => {
    setHardNoGenres((prev) =>
      group.ids.some((gid) => prev.includes(gid)) ? prev.filter((gid) => !group.ids.includes(gid)) : [...prev, group.representativeId],
    )
  }

  const toggleLanguage = (code: string) => {
    setLanguagesOk((prev) => (prev.includes(code) ? prev.filter((l) => l !== code) : [...prev, code]))
  }

  const handleDone = (e: FormEvent) => {
    e.preventDefault()
    if (!setup) return

    const errs: Record<string, string> = {}
    if (!skipNameAge) {
      const trimmed = name.trim()
      if (!trimmed) {
        errs.name = 'Your name is required'
      } else {
        const taken = Object.values(answers).some(
          (a) => a.personIndex !== personIndex && a.name.trim().toLowerCase() === trimmed.toLowerCase(),
        )
        if (taken) errs.name = 'Someone already used this name — pick another'
      }
      if (!ageBand) errs.ageBand = 'Pick your age group'
    }
    if (moods.length === 0) errs.moods = 'Pick at least one mood'
    if (languagesOk.length === 0) errs.languagesOk = 'Pick at least one language'

    setErrors(errs)
    if (Object.keys(errs).length > 0) {
      const ref = errs.name ? nameRef : errs.ageBand ? ageRef : errs.moods ? moodRef : languageRef
      ref.current?.scrollIntoView({ behavior: 'smooth', block: 'center' })
      return
    }

    const draft: PersonAnswerDraft = {
      personIndex,
      name: skipNameAge ? user?.name ?? 'You' : name.trim(),
      ageBand: skipNameAge ? null : ageBand,
      moods,
      genresUpFor,
      hardNoGenres,
      languagesOk,
      intensity,
      somethingLikeTitle: somethingLike,
    }
    saveAnswer(draft)

    if (personIndex === setup.groupSize - 1) {
      navigate(`/session/${id}/loading`)
    } else {
      navigate(`/session/${id}/handoff/${personIndex + 1}`)
    }
  }

  if (!setup || setup.sessionId !== Number(id)) return <Navigate to="/" replace />
  if (!Number.isInteger(personIndex) || personIndex < 0 || personIndex >= setup.groupSize) return <Navigate to="/" replace />
  if (!loaded) return null

  return (
    <div className="app-container main-content">
      <div className="form-container wizard">
        {!isSolo && (
          <div className="person-form-header">
            <span>
              Person {personIndex + 1} of {setup.groupSize}
            </span>
            <div className="person-form-progress-track">
              <div className="person-form-progress-fill" style={{ width: `${((personIndex + 1) / setup.groupSize) * 100}%` }} />
            </div>
          </div>
        )}

        <form onSubmit={handleDone}>
          {!skipNameAge && (
            <>
              <section className="wizard-step" ref={nameRef}>
                <TextInput label="Your name" value={name} onChange={(e) => setName(e.target.value)} error={errors.name} />
              </section>

              <section className="wizard-step" ref={ageRef}>
                <p className="wizard-label">Your age group</p>
                <div className="chips-row">
                  {AGE_BANDS.map((b) => (
                    <Chip key={b.value} label={b.label} selected={ageBand === b.value} onToggle={() => setAgeBand(b.value)} />
                  ))}
                </div>
                {errors.ageBand && <p className="text-input-error-msg">{errors.ageBand}</p>}
              </section>
            </>
          )}

          <section className="wizard-step" ref={moodRef}>
            <p className="wizard-label">What's your mood tonight? (pick 1–2)</p>
            <div className="chips-row">
              {MOODS.map((m) => (
                <Chip key={m} label={m} selected={moods.includes(m)} disabled={!moods.includes(m) && moods.length >= 2} onToggle={() => toggleMood(m)} />
              ))}
            </div>
            {errors.moods && <p className="text-input-error-msg">{errors.moods}</p>}
          </section>

          <section className="wizard-step">
            <p className="wizard-label">Genres you're up for (up to 3)</p>
            <div className="chips-row">
              {genreGroups.map((group) => {
                const isSelected = group.ids.some((gid) => genresUpFor.includes(gid))
                return (
                  <Chip
                    key={group.name}
                    label={group.name}
                    selected={isSelected}
                    disabled={genresUpFor.length >= 3 && !isSelected}
                    onToggle={() => toggleGenreUpFor(group)}
                  />
                )
              })}
            </div>
          </section>

          <section className="wizard-step">
            <p className="wizard-label">Hard no tonight</p>
            <div className="chips-row">
              {genreGroups.map((group) => (
                <Chip
                  key={group.name}
                  label={group.name}
                  selected={group.ids.some((gid) => hardNoGenres.includes(gid))}
                  onToggle={() => toggleHardNo(group)}
                />
              ))}
            </div>
          </section>

          <section className="wizard-step" ref={languageRef}>
            <p className="wizard-label">Languages you're okay with</p>
            <div className="chips-row">
              {POPULAR_LANGUAGES.map((lang) => (
                <Chip key={lang.code} label={lang.label} selected={languagesOk.includes(lang.code)} onToggle={() => toggleLanguage(lang.code)} />
              ))}
            </div>
            {errors.languagesOk && <p className="text-input-error-msg">{errors.languagesOk}</p>}
          </section>

          <section className="wizard-step">
            <p className="wizard-label">Light or intense?</p>
            <Slider3Step steps={INTENSITY_STEPS} value={intensity} onChange={setIntensity} />
          </section>

          <section className="wizard-step">
            <p className="wizard-label">Something like... (optional)</p>
            {setup.mediaType === 'either' && (
              <div className="chips-row" style={{ marginBottom: '0.5rem' }}>
                <Chip label="Movies" selected={somethingLikeType === 'movie'} onToggle={() => setSomethingLikeType('movie')} />
                <Chip label="Series" selected={somethingLikeType === 'tv'} onToggle={() => setSomethingLikeType('tv')} />
              </div>
            )}
            <div style={{ display: 'flex', alignItems: 'flex-start', gap: '0.75rem' }}>
              <div style={{ flex: 1 }}>
                <TitleSearchPicker
                  mediaType={somethingLikeType}
                  selected={somethingLike ? [somethingLike] : []}
                  onChange={(titles) => setSomethingLike(titles[titles.length - 1] ?? null)}
                  max={1}
                />
              </div>
              <button type="button" className="auth-inline-link" onClick={() => setSomethingLike(null)}>
                Skip
              </button>
            </div>
          </section>

          <div className="wizard-actions">
            <div style={{ marginLeft: 'auto' }}>
              <Button type="submit">Done</Button>
            </div>
          </div>
        </form>
      </div>
    </div>
  )
}
