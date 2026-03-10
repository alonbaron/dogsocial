import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, getApiErrorMessage } from '../lib/client'
import { useAuth } from '../lib/auth'
import Avatar from '../components/Avatar.jsx'

function FollowButton({ userId, initialFollowing, isMe }) {
  const [following, setFollowing] = useState(initialFollowing)
  const [loading, setLoading] = useState(false)
  if (isMe) return null

  const toggle = async () => {
    setLoading(true)
    try {
      if (following) {
        await api.delete(`/follows/${userId}`)
        setFollowing(false)
      } else {
        await api.post(`/follows/${userId}`)
        setFollowing(true)
      }
    } catch {
      /* ignore */
    } finally {
      setLoading(false)
    }
  }

  return (
    <button
      type="button"
      disabled={loading}
      onClick={(e) => { e.preventDefault(); toggle() }}
      className={`shrink-0 btn btn-sm ${following ? 'btn-secondary' : 'btn-primary'}`}
    >
      {loading ? '…' : following ? '✓ Following' : 'Follow'}
    </button>
  )
}

function UserRow({ u, meId, defaultFollowing }) {
  const label = u.username ? `@${u.username}` : `User #${u.id}`
  // u.isFollowing comes from the backend when available; fall back to the list-level default
  const initialFollowing = u.isFollowing != null ? u.isFollowing : defaultFollowing
  return (
    <div className="flex items-center gap-3 rounded-xl bg-white px-4 py-3 ring-1 ring-slate-100 shadow-sm">
      <Link to={`/users/${u.id}`} className="flex flex-1 items-center gap-3 hover:opacity-80 min-w-0 transition">
        <Avatar userId={u.id} username={u.username} size="sm" />
        <p className="truncate text-sm font-semibold text-slate-900">{label}</p>
      </Link>
      <FollowButton userId={u.id} initialFollowing={initialFollowing} isMe={u.id === meId} />
    </div>
  )
}

function List({ title, icon, endpoint, meId, defaultFollowing = false }) {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [page, setPage] = useState(0)

  useEffect(() => {
    let cancelled = false
    async function load() {
      setLoading(true)
      setError('')
      try {
        const res = await api.get(endpoint, { params: { page, size: 12 } })
        if (!cancelled) setData(res.data)
      } catch (err) {
        if (!cancelled) setError(getApiErrorMessage(err, 'Failed to load.'))
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    load()
    return () => { cancelled = true }
  }, [endpoint, page])

  return (
    <div className="card overflow-visible p-4 space-y-3">
      <div className="flex items-center justify-between">
        <h2 className="section-title">{icon} {title}</h2>
        <span className="text-xs text-slate-400">
          {data ? `${data.totalItems ?? (data.items?.length ?? 0)} total` : ''}
        </span>
      </div>

      {error && <div className="error-banner text-xs">{error}</div>}
      {loading && <div className="text-xs text-slate-400 animate-pulse">Loading…</div>}

      <div className="space-y-2">
        {(data?.items ?? []).map((u) => (
          <UserRow key={u.id} u={u} meId={meId} defaultFollowing={defaultFollowing} />
        ))}
        {data && (data.items ?? []).length === 0 && !loading && (
          <p className="rounded-xl bg-slate-50 p-4 text-xs text-center text-slate-400">Nothing here yet.</p>
        )}
      </div>

      {data && data.totalPages > 1 && (
        <div className="flex items-center justify-between pt-1">
          <button type="button" disabled={page === 0} onClick={() => setPage((p) => Math.max(0, p - 1))} className="btn-secondary btn-sm">← Prev</button>
          <span className="text-[11px] text-slate-400">{page + 1} / {data.totalPages}</span>
          <button type="button" disabled={!data?.hasNext} onClick={() => setPage((p) => p + 1)} className="btn-secondary btn-sm">Next →</button>
        </div>
      )}
    </div>
  )
}

function FriendsPage() {
  const { user } = useAuth()

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Friends</h1>
          <p className="text-sm text-slate-500 mt-0.5">Mutual follows and your social graph.</p>
        </div>
        <Link to="/browse" className="btn-secondary btn-sm">Discover people</Link>
      </div>

      <div className="grid gap-4 lg:grid-cols-3">
        <List title="Friends" icon="🤝" endpoint="/friends" meId={user?.id} defaultFollowing={true} />
        <List title="Following" icon="➡️" endpoint={`/users/${user?.id}/following`} meId={user?.id} defaultFollowing={true} />
        <List title="Followers" icon="⬅️" endpoint={`/users/${user?.id}/followers`} meId={user?.id} defaultFollowing={false} />
      </div>
    </div>
  )
}

export default FriendsPage
