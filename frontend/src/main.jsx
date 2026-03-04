import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import './index.css'
import App from './App.jsx'
import { AuthProvider, useAuth } from './lib/auth.js'
import LoginPage from './pages/LoginPage.jsx'
import RegisterPage from './pages/RegisterPage.jsx'
import LandingPage from './pages/LandingPage.jsx'
import FeedPage from './pages/FeedPage.jsx'
import UserProfilePage from './pages/UserProfilePage.jsx'
import DogProfilePage from './pages/DogProfilePage.jsx'
import CreatePostPage from './pages/CreatePostPage.jsx'
import PlaydatesPage from './pages/PlaydatesPage.jsx'
import BrowsePage from './pages/BrowsePage.jsx'
import FriendsPage from './pages/FriendsPage.jsx'

function ProtectedRoute({ children }) {
  const { user, loading } = useAuth()

  if (loading) {
    return (
      <div className="flex h-[60vh] items-center justify-center text-slate-500">
        Loading...
      </div>
    )
  }

  if (!user) {
    return <Navigate to="/login" replace />
  }

  return children
}

function PublicOnly({ children }) {
  const { user, loading } = useAuth()
  if (loading) return null
  if (user) return <Navigate to="/feed" replace />
  return children
}

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route
            element={
              <ProtectedRoute>
                <App />
              </ProtectedRoute>
            }
          >
            <Route path="/feed" element={<FeedPage />} />
            <Route path="/users/:userId" element={<UserProfilePage />} />
            <Route path="/dogs/:dogId" element={<DogProfilePage />} />
            <Route path="/create-post" element={<CreatePostPage />} />
            <Route path="/playdates" element={<PlaydatesPage />} />
            <Route path="/browse" element={<BrowsePage />} />
            <Route path="/friends" element={<FriendsPage />} />
          </Route>
          <Route
            path="/"
            element={
              <PublicOnly>
                <LandingPage />
              </PublicOnly>
            }
          />
          <Route
            path="/login"
            element={
              <PublicOnly>
                <LoginPage />
              </PublicOnly>
            }
          />
          <Route
            path="/register"
            element={
              <PublicOnly>
                <RegisterPage />
              </PublicOnly>
            }
          />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  </StrictMode>,
)
