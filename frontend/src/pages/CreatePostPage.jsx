import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../lib/client'
import { useAuth } from '../lib/auth'

function CreatePostPage() {
  const [caption, setCaption] = useState('')
  const [dogs, setDogs] = useState([])
  const [dogId, setDogId] = useState('')
  const [loadingDogs, setLoadingDogs] = useState(true)
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const { user } = useAuth()
  const navigate = useNavigate()

  useEffect(() => {
    if (!user) return
    setLoadingDogs(true)
    api
      .get(`/users/${user.id}/dogs`, { params: { page: 0, size: 20 } })
      .then((res) => setDogs(res.data.items || []))
      .catch(() => {})
      .finally(() => setLoadingDogs(false))
  }, [user])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    setError('')
    try {
      const payload = { caption }
      if (dogId) payload.dogId = Number(dogId)
      await api.post('/posts', payload)
      navigate('/feed')
    } catch (err) {
      const message =
        err.response?.data?.message || 'Failed to create post. Please try again.'
      setError(message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="mx-auto max-w-lg rounded-2xl bg-white p-6 shadow-sm">
      <h1 className="mb-2 text-lg font-semibold text-slate-900">Create post</h1>
      <p className="mb-4 text-xs text-slate-500">
        Share an update about you or your dog. Caption is limited to 300
        characters.
      </p>

      {error && (
        <div className="mb-4 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="mb-1 block text-xs font-medium text-slate-600">
            Post as dog (optional)
          </label>
          <select
            value={dogId}
            onChange={(e) => setDogId(e.target.value)}
            className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm shadow-sm focus:border-pink-500 focus:outline-none focus:ring-1 focus:ring-pink-500"
          >
            <option value="">Just me</option>
            {dogs.map((d) => (
              <option key={d.id} value={d.id}>
                {d.name}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="mb-1 block text-xs font-medium text-slate-600">
            Caption
          </label>
          <textarea
            required
            maxLength={300}
            rows={4}
            value={caption}
            onChange={(e) => setCaption(e.target.value)}
            className="w-full resize-none rounded-lg border border-slate-200 px-3 py-2 text-sm shadow-sm focus:border-pink-500 focus:outline-none focus:ring-1 focus:ring-pink-500"
          />
          <div className="mt-1 text-right text-[11px] text-slate-400">
            {caption.length}/300
          </div>
        </div>

        <button
          type="submit"
          disabled={submitting}
          className="flex w-full items-center justify-center rounded-lg bg-pink-500 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-pink-600 disabled:opacity-60"
        >
          {submitting ? 'Posting…' : 'Post'}
        </button>
      </form>
    </div>
  )
}

export default CreatePostPage

