import { createContext, useContext, useState, type ReactNode } from 'react'
import { setAuthToken } from '../api/client'
import type { AuthUser } from '../api/auth'

interface AuthContextValue {
  user: AuthUser | null
  login: (token: string, user: AuthUser) => void
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null)

  const login = (token: string, nextUser: AuthUser) => {
    setAuthToken(token)
    setUser(nextUser)
  }

  const logout = () => {
    setAuthToken(null)
    setUser(null)
  }

  return <AuthContext.Provider value={{ user, login, logout }}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
