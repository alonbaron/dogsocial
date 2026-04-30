import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../lib/client'

function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    setMessage('')
    setError('')
    try {
      await api.post('/auth/forgot-password', { email })
      setMessage('If that email exists, a reset link has been sent.')
    } catch (err) {
      setError(err.response?.data?.message || 'Unable to request a reset link right now.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="flex min-h-[80vh] items-center justify-center px-4">
      <div className="w-full max-w-sm">
        <div className="mb-8 text-center">
          <div className="inline-flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-pink-400 to-rose-500 text-4xl shadow-lg mb-4">
            P
          </div>
          <h1 className="text-2xl font-bold text-slate-900">Reset password</h1>
          <p className="mt-1 text-sm text-slate-500">Enter your email and we will send you a reset link.</p>
        </div>

        <div className="card p-6 shadow-md">
          {message && <div className="success-banner mb-5">{message}</div>}
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
            <button type="submit" disabled={submitting} className="btn-primary w-full mt-2">
              {submitting ? 'Sending...' : 'Send reset link'}
            </button>
          </form>

          <p className="mt-5 text-center text-sm text-slate-500">
            Remembered it?{' '}
            <Link to="/login" className="font-semibold text-pink-600 hover:text-pink-700">
              Log in
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}

export default ForgotPasswordPage
