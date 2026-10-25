import { apiRequest } from './client'

export type EraPreference = 'mostly_newer' | 'any_era' | 'love_classics'

export interface Profile {
  name: string
  email: string
  birthYear: number
  subtitlesOk: boolean
  languages: string[]
  favoriteGenres: number[]
  neverWantGenres: number[]
  lovedTitleIds: number[]
  eraPreference: EraPreference
}

export type ProfileInput = Omit<Profile, 'name' | 'email'>

export function getProfile() {
  return apiRequest<Profile>('/profile')
}

export function updateProfile(input: ProfileInput) {
  return apiRequest<Profile>('/profile', {
    method: 'PUT',
    body: JSON.stringify(input),
  })
}
