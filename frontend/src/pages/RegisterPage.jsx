import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api } from '../lib/client'
import { useAuth } from '../lib/auth'

function RegisterPage() {
  const [email, setEmail] = useState('')
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const { login } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (password !== confirm) { setError('Passwords do not match.'); return }
    if (!/^[a-zA-Z0-9_]{3,30}$/.test(username)) {
      setError('Username must be 3–30 characters: letters, numbers, or underscores only.')
      return
    }
    setSubmitting(true)
    setError('')
    try {
      const res = await api.post('/auth/register', { email, username, password })
      login(res.data.token, res.data.me)
      navigate('/feed')
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.detail || 'Unable to register. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="flex min-h-[80vh] items-center justify-center px-4">
      <div className="w-full max-w-sm">
        {/* Brand mark */}
        <div className="mb-8 text-center">
          <div className="inline-flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-pink-400 to-rose-500 text-4xl shadow-lg mb-4">
            🐾
          </div>
          <h1 className="text-2xl font-bold text-slate-900">Join the dog park</h1>
          <p className="mt-1 text-sm text-slate-500">Share your dogs and schedule playdates.</p>
        </div>

        <div className="card p-6 shadow-md">
          {error && <div className="error-banner mb-5">{error}</div>}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="label">Email</label>
              <input
                type="email"
                required
                autoComplete="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
                className="input"
              />
            </div>
            <div>
              <label className="label">Username</label>
              <div className="relative">
                <span className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 font-medium text-sm">@</span>
                <input
                  type="text"
                  required
                  minLength={3}
                  maxLength={30}
                  pattern="[a-zA-Z0-9_]+"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="your_username"
                  className="input pl-8"
                />
              </div>
              <p className="mt-1.5 text-[11px] text-slate-400">Letters, numbers and underscores · 3–30 chars</p>
            </div>
            <div>
              <label className="label">Password</label>
              <input
                type="password"
                required
                minLength={8}
                autoComplete="new-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="input"
              />
            </div>
            <div>
              <label className="label">Confirm password</label>
              <input
                type="password"
                required
                autoComplete="new-password"
                value={confirm}
                onChange={(e) => setConfirm(e.target.value)}
                placeholder="••••••••"
                className="input"
              />
            </div>
            <button
              type="submit"
              disabled={submitting}
              className="btn-primary w-full mt-2"
            >
              {submitting ? 'Creating account…' : 'Create account'}
            </button>
          </form>

          <p className="mt-5 text-center text-sm text-slate-500">
            Already have an account?{' '}
            <Link to="/login" className="font-semibold text-pink-600 hover:text-pink-700">
              Log in
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}

export default RegisterPage
