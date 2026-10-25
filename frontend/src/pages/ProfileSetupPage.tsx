import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Button from '../components/Button'
import Chip from '../components/Chip'
import TextInput from '../components/TextInput'
import Slider3Step from '../components/Slider3Step'
import TitleSearchPicker from '../components/TitleSearchPicker'
import { ApiError } from '../api/client'
import { getGenres, type Genre } from '../api/genres'
import { getProfile, updateProfile, type EraPreference } from '../api/profile'
import type { TitleSearchResult } from '../api/titles'
import { useAuth } from '../context/AuthContext'
import { groupGenresByName, type GenreGroup } from '../utils/genres'
import { POPULAR_LANGUAGES } from '../utils/languages'

const ERA_STEPS = [
  { value: 'mostly_newer' as EraPreference, label: 'Mostly newer' },
  { value: 'any_era' as EraPreference, label: 'Any era' },
  { value: 'love_classics' as EraPreference, label: 'Love classics' },
] as const

const CURRENT_YEAR = new Date().getFullYear()
const BIRTH_YEARS = Array.from({ length: CURRENT_YEAR - 1930 + 1 }, (_, i) => CURRENT_YEAR - i)

export default function ProfileSetupPage() {
  const navigate = useNavigate()
  const { user } = useAuth()

  const [step, setStep] = useState(1)
  const [loaded, setLoaded] = useState(false)
  const [genres, setGenres] = useState<Genre[]>([])

  const [birthYear, setBirthYear] = useState<number | ''>('')
  const [languages, setLanguages] = useState<string[]>([])
  const [customLanguage, setCustomLanguage] = useState('')
  const [subtitlesOk, setSubtitlesOk] = useState(true)
  const [favoriteGenres, setFavoriteGenres] = useState<number[]>([])
  const [neverWantGenres, setNeverWantGenres] = useState<number[]>([])
  const [lovedTitles, setLovedTitles] = useState<TitleSearchResult[]>([])
  const [lovedTitleMediaType, setLovedTitleMediaType] = useState<'movie' | 'tv'>('movie')
  const [eraPreference, setEraPreference] = useState<EraPreference>('any_era')

  const [stepError, setStepError] = useState('')
  const [submitError, setSubmitError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    Promise.all([getGenres(), getProfile().catch((err) => (err instanceof ApiError && err.code === 'PROFILE_NOT_FOUND' ? null : Promise.reject(err)))])
      .then(([genreRes, profile]) => {
        setGenres(genreRes.genres)
        if (profile) {
          setBirthYear(profile.birthYear)
          setLanguages(profile.languages)
          setSubtitlesOk(profile.subtitlesOk)
          setFavoriteGenres(profile.favoriteGenres)
          setNeverWantGenres(profile.neverWantGenres)
          setEraPreference(profile.eraPreference)
        }
      })
      .finally(() => setLoaded(true))
  }, [])

  const genreGroups = useMemo(() => groupGenresByName(genres), [genres])

  const toggleGroup = (group: GenreGroup, selectedIds: number[], setter: (ids: number[]) => void) => {
    const isSelected = group.ids.some((id) => selectedIds.includes(id))
    if (isSelected) {
      setter(selectedIds.filter((id) => !group.ids.includes(id)))
    } else {
      setter([...selectedIds, group.representativeId])
    }
  }

  const toggleLanguage = (code: string) => {
    setLanguages((prev) => (prev.includes(code) ? prev.filter((l) => l !== code) : [...prev, code]))
  }

  const addCustomLanguage = () => {
    const code = customLanguage.trim().toLowerCase()
    if (code && !languages.includes(code)) setLanguages((prev) => [...prev, code])
    setCustomLanguage('')
  }

  const goNext = () => {
    setStepError('')
    if (step === 1 && birthYear === '') {
      setStepError('Birth year is required')
      return
    }
    if (step === 2 && languages.length === 0) {
      setStepError('Pick at least one language')
      return
    }
    setStep((s) => Math.min(3, s + 1))
  }

  const goBack = () => {
    setStepError('')
    setStep((s) => Math.max(1, s - 1))
  }

  const handleFinish = async () => {
    setSubmitError('')
    setSubmitting(true)
    try {
      await updateProfile({
        birthYear: birthYear as number,
        subtitlesOk,
        languages,
        favoriteGenres,
        neverWantGenres,
        lovedTitleIds: lovedTitles.map((t) => t.id),
        eraPreference,
      })
      navigate('/')
    } catch (err) {
      setSubmitError(err instanceof ApiError ? err.message : 'Something went wrong. Try again.')
    } finally {
      setSubmitting(false)
    }
  }

  if (!loaded) return null

  return (
    <div className="app-container main-content">
      <div className="form-container wizard">
        <div className="wizard-progress">
          {[1, 2, 3].map((n) => (
            <span key={n} className={`wizard-dot ${n === step ? 'active' : n < step ? 'done' : ''}`} />
          ))}
          <span className="wizard-step-label">
            Step {step} of 3: {step === 1 ? 'About you' : step === 2 ? 'Languages' : 'Taste'}
          </span>
        </div>

        {step === 1 && (
          <section className="wizard-step">
            <TextInput label="Display name" value={user?.name ?? ''} disabled readOnly />
            <div className="text-input">
              <label htmlFor="birth-year">Birth year</label>
              <select id="birth-year" value={birthYear} onChange={(e) => setBirthYear(Number(e.target.value))}>
                <option value="" disabled>
                  Select a year
                </option>
                {BIRTH_YEARS.map((y) => (
                  <option key={y} value={y}>
                    {y}
                  </option>
                ))}
              </select>
            </div>
          </section>
        )}

        {step === 2 && (
          <section className="wizard-step">
            <p className="wizard-label">Languages you understand</p>
            <div className="chips-row">
              {POPULAR_LANGUAGES.map((lang) => (
                <Chip key={lang.code} label={lang.label} selected={languages.includes(lang.code)} onToggle={() => toggleLanguage(lang.code)} />
              ))}
              {languages
                .filter((code) => !POPULAR_LANGUAGES.some((l) => l.code === code))
                .map((code) => (
                  <Chip key={code} label={code} selected onToggle={() => toggleLanguage(code)} />
                ))}
            </div>
            <div className="wizard-inline-add">
              <input
                type="text"
                placeholder="Other language code (e.g. ja)"
                value={customLanguage}
                onChange={(e) => setCustomLanguage(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && (e.preventDefault(), addCustomLanguage())}
                maxLength={5}
              />
              <Button type="button" variant="secondary" onClick={addCustomLanguage}>
                Add
              </Button>
            </div>

            <p className="wizard-label">Okay with subtitles?</p>
            <div className="chips-row">
              <Chip label="Yes" selected={subtitlesOk} onToggle={() => setSubtitlesOk(true)} />
              <Chip label="No" selected={!subtitlesOk} onToggle={() => setSubtitlesOk(false)} />
            </div>
          </section>
        )}

        {step === 3 && (
          <section className="wizard-step">
            <p className="wizard-label">Favourite genres (up to 5)</p>
            <div className="chips-row">
              {genreGroups.map((group) => {
                const isSelected = group.ids.some((id) => favoriteGenres.includes(id))
                const atMax = favoriteGenres.length >= 5 && !isSelected
                return (
                  <Chip
                    key={group.name}
                    label={group.name}
                    selected={isSelected}
                    disabled={atMax}
                    onToggle={() => toggleGroup(group, favoriteGenres, setFavoriteGenres)}
                  />
                )
              })}
            </div>

            <p className="wizard-label">Genres you never want</p>
            <div className="chips-row">
              {genreGroups.map((group) => (
                <Chip
                  key={group.name}
                  label={group.name}
                  selected={group.ids.some((id) => neverWantGenres.includes(id))}
                  onToggle={() => toggleGroup(group, neverWantGenres, setNeverWantGenres)}
                />
              ))}
            </div>

            <p className="wizard-label">Titles you loved (optional, up to 5)</p>
            <div className="chips-row" style={{ marginBottom: '0.5rem' }}>
              <Chip label="Movies" selected={lovedTitleMediaType === 'movie'} onToggle={() => setLovedTitleMediaType('movie')} />
              <Chip label="Series" selected={lovedTitleMediaType === 'tv'} onToggle={() => setLovedTitleMediaType('tv')} />
            </div>
            <TitleSearchPicker mediaType={lovedTitleMediaType} selected={lovedTitles} onChange={setLovedTitles} max={5} />

            <p className="wizard-label">Newer or classic?</p>
            <Slider3Step steps={ERA_STEPS} value={eraPreference} onChange={setEraPreference} />
          </section>
        )}

        {stepError && <p className="text-input-error-msg">{stepError}</p>}
        {submitError && <p className="text-input-error-msg">{submitError}</p>}

        <div className="wizard-actions">
          {step > 1 && (
            <Button type="button" variant="secondary" onClick={goBack}>
              Back
            </Button>
          )}
          <div style={{ marginLeft: 'auto' }}>
            {step < 3 ? (
              <Button type="button" onClick={goNext}>
                Next
              </Button>
            ) : (
              <Button type="button" loading={submitting} onClick={handleFinish}>
                Finish
              </Button>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
