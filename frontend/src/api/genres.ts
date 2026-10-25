import { apiRequest } from './client'

export interface Genre {
  id: number
  name: string
  mediaType: 'movie' | 'tv'
}

export function getGenres() {
  return apiRequest<{ genres: Genre[] }>('/genres')
}
