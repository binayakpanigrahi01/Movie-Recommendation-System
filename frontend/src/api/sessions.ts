import { apiRequest } from './client'

export type SessionMediaType = 'movie' | 'series' | 'either'
export type TimeLimit =
  | 'under_90'
  | '90_120'
  | '2_2.5'
  | 'no_limit'
  | 'episode_or_two'
  | 'binge'
  | 'new_show'
export type Occasion = 'friends_hangout' | 'date_night' | 'family_time' | 'solo_chill'

export interface SessionStartInput {
  mediaType: SessionMediaType
  groupSize: number
  timeLimit: TimeLimit
  occasion?: Occasion | null
}

export function startSession(input: SessionStartInput) {
  return apiRequest<{ sessionId: number }>('/sessions/start', {
    method: 'POST',
    body: JSON.stringify(input),
  })
}

export type AgeBand = 'under_7' | '7_12' | '13_15' | '16_17' | '18_plus'
export type Intensity = 'light' | 'balanced' | 'intense'

export interface PersonAnswerInput {
  personIndex: number
  name: string
  ageBand: AgeBand | null
  moods: string[]
  genresUpFor: number[]
  hardNoGenres: number[]
  languagesOk: string[]
  intensity: Intensity
  somethingLikeTitleId: number | null
}

export interface SessionResult {
  rank: number
  titleId: number
  title: string
  posterPath: string | null
  mediaType: 'movie' | 'tv'
  year: number | null
  runtimeMinutes: number | null
  certificationRaw: string | null
  certificationBand: string | null
  groupScore: number
  badge: 'none' | 'compromise' | 'not_rated'
  aiExplanation: string
  personScores: { personIndex: number; score: number }[]
}

export interface SessionResultsResponse {
  sessionId: number
  people: { personIndex: number; name: string }[]
  results: SessionResult[]
}

export function submitSession(sessionId: number, people: PersonAnswerInput[]) {
  return apiRequest<SessionResultsResponse>(`/sessions/${sessionId}/submit`, {
    method: 'POST',
    body: JSON.stringify({ people }),
  })
}

export function getMoreResults(sessionId: number) {
  return apiRequest<SessionResultsResponse>(`/sessions/${sessionId}/more`, { method: 'POST' })
}

export function getSessionResults(sessionId: number) {
  return apiRequest<SessionResultsResponse>(`/sessions/${sessionId}/results`)
}

export const MEDIA_TYPE_LABELS: Record<SessionMediaType, string> = {
  movie: 'Movie',
  series: 'Series',
  either: 'Movie or series',
}

export const TIME_LIMIT_LABELS: Record<TimeLimit, string> = {
  under_90: 'Under 90 min',
  '90_120': '90–120 min',
  '2_2.5': '2–2.5 hrs',
  no_limit: 'No limit',
  episode_or_two: 'An episode or two',
  binge: 'A binge session',
  new_show: 'A new show to follow for weeks',
}
