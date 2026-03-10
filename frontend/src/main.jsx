import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import './index.css'
import App from './App.jsx'
import { AuthProvider } from './lib/auth.js'
import { ProtectedRoute, PublicOnly } from './components/RouteGuards.jsx'
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
