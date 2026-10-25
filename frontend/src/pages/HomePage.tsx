import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Button from '../components/Button'
import Navbar from '../components/Navbar'
import { ApiError } from '../api/client'
import { getGenres, type Genre } from '../api/genres'
import { getProfile, type Profile } from '../api/profile'
import { useAuth } from '../context/AuthContext'

export default function HomePage() {
  const navigate = useNavigate()
  const { user, logout } = useAuth()
  const [profile, setProfile] = useState<Profile | null>(null)
  const [genres, setGenres] = useState<Genre[]>([])
  const [loaded, setLoaded] = useState(false)

  useEffect(() => {
    Promise.all([getProfile(), getGenres()])
      .then(([profileRes, genreRes]) => {
        setProfile(profileRes)
        setGenres(genreRes.genres)
      })
      .catch((err) => {
        if (err instanceof ApiError && err.code === 'PROFILE_NOT_FOUND') {
          navigate('/profile/setup')
        }
      })
      .finally(() => setLoaded(true))
  }, [navigate])

  const genreName = (id: number) => genres.find((g) => g.id === id)?.name ?? String(id)

  if (!loaded) return null

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <Navbar
        userName={user?.name ?? ''}
        onEditProfile={() => navigate('/profile/setup')}
        onLogout={() => {
          logout()
          navigate('/login')
        }}
      />
      <main className="app-container main-content home-page">
        <h1 className="home-greeting">Hi {user?.name}</h1>
        <Button className="home-cta" onClick={() => navigate('/session/new')}>
          Find something to watch
        </Button>

        {profile && (
          <div className="preview-card home-profile-card">
            <div className="preview-card-header">
              <h3 className="preview-card-title">Your profile</h3>
              <Button variant="secondary" onClick={() => navigate('/profile/setup')}>
                Edit
              </Button>
            </div>
            <p style={{ color: 'var(--color-text-muted)' }}>
              Languages: {profile.languages.join(', ') || 'None set'}
            </p>
            <p style={{ color: 'var(--color-text-muted)' }}>
              Favourites: {profile.favoriteGenres.length ? profile.favoriteGenres.map(genreName).join(', ') : 'None set'}
            </p>
          </div>
        )}
      </main>
    </div>
  )
}
