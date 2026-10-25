import { useEffect, useState } from 'react'
import { searchTitles, TMDB_POSTER_BASE, type TitleSearchResult } from '../api/titles'

interface TitleSearchPickerProps {
  mediaType: 'movie' | 'tv'
  selected: TitleSearchResult[]
  onChange: (titles: TitleSearchResult[]) => void
  max?: number
}

export default function TitleSearchPicker({ mediaType, selected, onChange, max = 5 }: TitleSearchPickerProps) {
  const [query, setQuery] = useState('')
  const [rawResults, setResults] = useState<TitleSearchResult[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(false)

  const searchable = query.trim().length >= 2
  const results = searchable ? rawResults : []

  useEffect(() => {
    if (!searchable) return
    const timer = setTimeout(() => {
      setLoading(true)
      setError(false)
      searchTitles(query, mediaType)
        .then((res) => setResults(res.results))
        .catch(() => setError(true))
        .finally(() => setLoading(false))
    }, 350)
    return () => clearTimeout(timer)
  }, [query, mediaType, searchable])

  const atMax = selected.length >= max

  const addTitle = (title: TitleSearchResult) => {
    if (atMax || selected.some((t) => t.id === title.id)) return
    onChange([...selected, title])
    setQuery('')
    setResults([])
  }

  const removeTitle = (id: number) => {
    onChange(selected.filter((t) => t.id !== id))
  }

  return (
    <div className="title-search-picker">
      <input
        type="text"
        placeholder={atMax ? `Up to ${max} titles picked` : 'Search a title...'}
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        disabled={atMax}
        aria-label="Search titles"
      />
      {loading && <p className="title-search-status">Searching...</p>}
      {error && <p className="title-search-status title-search-error">No matches — try again</p>}
      {results.length > 0 && (
        <ul className="title-search-results">
          {results.map((title) => (
            <li key={title.id} onClick={() => addTitle(title)}>
              {title.posterPath && <img src={`${TMDB_POSTER_BASE}${title.posterPath}`} alt="" />}
              <span>
                {title.title}
                {title.releaseDate ? ` (${title.releaseDate.slice(0, 4)})` : ''}
              </span>
            </li>
          ))}
        </ul>
      )}
      {selected.length > 0 && (
        <div className="title-search-selected">
          {selected.map((title) => (
            <span key={title.id} className="chip active">
              {title.title}
              <button type="button" aria-label={`Remove ${title.title}`} onClick={() => removeTitle(title.id)}>
                &times;
              </button>
            </span>
          ))}
        </div>
      )}
    </div>
  )
}
