import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import './App.css'
import { AuthProvider } from './context/AuthContext'
import { SessionFlowProvider } from './context/SessionFlowContext'
import ProtectedRoute from './components/ProtectedRoute'
import AuthPage from './pages/AuthPage'
import ForgotPasswordPage from './pages/ForgotPasswordPage'
import ResetPasswordPage from './pages/ResetPasswordPage'
import ProfileSetupPage from './pages/ProfileSetupPage'
import HomePage from './pages/HomePage'
import SessionSetupPage from './pages/SessionSetupPage'
import HandoffPage from './pages/HandoffPage'
import PersonFormPage from './pages/PersonFormPage'
import FindingPicksPage from './pages/FindingPicksPage'
import ResultsPage from './pages/ResultsPage'
import TitleDetailPage from './pages/TitleDetailPage'
import ComingSoonPage from './pages/ComingSoonPage'

export default function App() {
  return (
    <AuthProvider>
      <SessionFlowProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<AuthPage />} />
          <Route path="/signup" element={<AuthPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/reset-password" element={<ResetPasswordPage />} />
          <Route
            path="/profile/setup"
            element={
              <ProtectedRoute>
                <ProfileSetupPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <HomePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/session/new"
            element={
              <ProtectedRoute>
                <SessionSetupPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/session/:id/handoff/:index"
            element={
              <ProtectedRoute>
                <HandoffPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/session/:id/person/:index"
            element={
              <ProtectedRoute>
                <PersonFormPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/session/:id/loading"
            element={
              <ProtectedRoute>
                <FindingPicksPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/session/:id/results"
            element={
              <ProtectedRoute>
                <ResultsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/session/:id/title/:titleId"
            element={
              <ProtectedRoute>
                <TitleDetailPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/session/:id/*"
            element={
              <ProtectedRoute>
                <ComingSoonPage />
              </ProtectedRoute>
            }
          />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
      </SessionFlowProvider>
    </AuthProvider>
  )
}
