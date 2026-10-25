import { apiRequest } from './client'

export const TMDB_POSTER_BASE = 'https://image.tmdb.org/t/p/w92'
export const TMDB_POSTER_LARGE = 'https://image.tmdb.org/t/p/w342'
export const TMDB_BACKDROP_BASE = 'https://image.tmdb.org/t/p/w1280'

export interface TitleSearchResult {
  id: number
  tmdbId: number
  mediaType: 'movie' | 'tv'
  title: string
  posterPath: string | null
  releaseDate: string | null
}

export function searchTitles(query: string, mediaType: 'movie' | 'tv') {
  return apiRequest<{ results: TitleSearchResult[] }>('/titles/search', {
    method: 'POST',
    body: JSON.stringify({ query, mediaType }),
  })
}

export interface CastMember {
  name: string
  character: string
  photoUrl: string | null
}

export interface TitleDetail {
  id: number
  tmdbId: number
  mediaType: 'movie' | 'tv'
  title: string
  overview: string | null
  posterPath: string | null
  backdropPath: string | null
  releaseDate: string | null
  runtimeMinutes: number | null
  seriesStatus: 'ongoing' | 'ended' | null
  genreNames: string[]
  cast: CastMember[]
  certificationRaw: string | null
  certificationBand: string | null
  imdbRating: number | null
  rottenTomatoesRating: number | null
  metacriticRating: number | null
  imdbId: string | null
  tmdbRating: number | null
}

export function getTitleDetail(id: number) {
  return apiRequest<TitleDetail>(`/titles/${id}`)
}
