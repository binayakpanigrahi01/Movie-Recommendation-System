import type { Genre } from '../api/genres'

export interface GenreGroup {
  name: string
  ids: number[]
  representativeId: number
}

export function groupGenresByName(genres: Genre[]): GenreGroup[] {
  const byName = new Map<string, number[]>()
  for (const g of genres) {
    const ids = byName.get(g.name) ?? []
    ids.push(g.id)
    byName.set(g.name, ids)
  }
  return Array.from(byName.entries())
    .map(([name, ids]) => ({ name, ids, representativeId: ids[0] }))
    .sort((a, b) => a.name.localeCompare(b.name))
}
