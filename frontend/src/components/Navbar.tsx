import { useState } from 'react'

interface NavbarProps {
  userName: string
  onEditProfile: () => void
  onLogout: () => void
}

export default function Navbar({ userName, onEditProfile, onLogout }: NavbarProps) {
  const [open, setOpen] = useState(false)

  return (
    <header className="navbar">
      <span className="navbar-brand">Movie Recommender</span>
      <div className="navbar-menu" onKeyDown={(e) => e.key === 'Escape' && setOpen(false)}>
        <button
          type="button"
          className="navbar-avatar-btn"
          onClick={() => setOpen((o) => !o)}
          aria-expanded={open}
          aria-haspopup="menu"
        >
          <span className="navbar-avatar" aria-hidden="true">
            {userName.charAt(0).toUpperCase()}
          </span>
          <span>{userName}</span>
        </button>
        {open && (
          <div className="navbar-dropdown" role="menu">
            <button
              type="button"
              role="menuitem"
              onClick={() => {
                setOpen(false)
                onEditProfile()
              }}
            >
              Edit profile
            </button>
            <button
              type="button"
              role="menuitem"
              onClick={() => {
                setOpen(false)
                onLogout()
              }}
            >
              Log out
            </button>
          </div>
        )}
      </div>
    </header>
  )
}
