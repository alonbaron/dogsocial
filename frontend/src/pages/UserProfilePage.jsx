import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { api } from '../lib/client'
import PostCard from '../components/PostCard.jsx'

function UserProfilePage() {
  const { userId } = useParams()
  const [profile, setProfile] = useState(null)
  const [posts, setPosts] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false
    async function load() {
      setLoading(true)
      setError('')
      try {
        const [profileRes, postsRes] = await Promise.all([
          api.get(`/users/${userId}`),
          api.get('/feed', { params: { page: 0, size: 10 } }),
        ])
        if (!cancelled) {
          setProfile(profileRes.data)
          setPosts(postsRes.data)
        }
      } catch (err) {
        if (!cancelled) {
          const message =
            err.response?.data?.message || 'Failed to load profile.'
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
  }, [userId])

  return (
    <div className="space-y-4">
      {error && (
        <div className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
          {error}
        </div>
      )}

      {profile && (
        <section className="rounded-2xl bg-white p-4 shadow-sm ring-1 ring-slate-100">
          <h1 className="text-lg font-semibold text-slate-900">
            {profile.email}
          </h1>
          <p className="mt-1 text-xs text-slate-500">
            Joined {profile.createdAt && new Date(profile.createdAt).toLocaleDateString()}
          </p>
          <p className="mt-2 text-xs text-slate-500">
            Followers: <span className="font-medium">{profile.followersCount}</span>{' '}
            · Following:{' '}
            <span className="font-medium">{profile.followingCount}</span>
          </p>
        </section>
      )}

      <section className="space-y-3">
        <h2 className="text-sm font-semibold text-slate-900">Recent posts</h2>
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

export default UserProfilePage

