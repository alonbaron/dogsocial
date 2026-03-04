import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { api } from '../lib/client'
import PostCard from '../components/PostCard.jsx'

function DogProfilePage() {
  const { dogId } = useParams()
  const [dog, setDog] = useState(null)
  const [posts, setPosts] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false
    async function load() {
      setLoading(true)
      setError('')
      try {
        const [dogRes, postsRes] = await Promise.all([
          api.get(`/dogs/${dogId}`),
          api.get(`/dogs/${dogId}/posts`, { params: { page: 0, size: 10 } }),
        ])
        if (!cancelled) {
          setDog(dogRes.data)
          setPosts(postsRes.data)
        }
      } catch (err) {
        if (!cancelled) {
          const message =
            err.response?.data?.message || 'Failed to load dog profile.'
          setError(message)
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    load()
    return () => {
      cancelled = true
    }
  }, [dogId])

  return (
    <div className="space-y-4">
      {error && (
        <div className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
          {error}
        </div>
      )}

      {dog && (
        <section className="rounded-2xl bg-white p-4 shadow-sm ring-1 ring-slate-100">
          <h1 className="text-lg font-semibold text-slate-900">{dog.name}</h1>
          <p className="mt-1 text-xs text-slate-500">
            Owner: <span className="font-medium">{dog.owner.email}</span>
          </p>
          {dog.breed && (
            <p className="mt-1 text-xs text-slate-500">Breed: {dog.breed}</p>
          )}
          {dog.bio && (
            <p className="mt-2 text-sm text-slate-700">{dog.bio}</p>
          )}
        </section>
      )}

      <section className="space-y-3">
        <h2 className="text-sm font-semibold text-slate-900">Dog posts</h2>
        {loading && (
          <div className="rounded-2xl bg-white p-4 text-sm text-slate-500 shadow-sm">
            Loading posts…
          </div>
        )}
        {posts?.items.map((post) => (
          <PostCard
            key={post.id}
            post={post}
            onToggleReaction={() => {}}
          />
        ))}
      </section>
    </div>
  )
}

export default DogProfilePage

