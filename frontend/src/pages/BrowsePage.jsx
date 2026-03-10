import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, getApiErrorMessage } from '../lib/client'
import Avatar from '../components/Avatar.jsx'
import PostCard from '../components/PostCard.jsx'

// ─── User search ──────────────────────────────────────────────────────────────

function UserSearchRow({ u }) {
  const [following, setFollowing] = useState(u.isFollowing)
  const [loading, setLoading] = useState(false)
  const label = u.username ? `@${u.username}` : `User #${u.id}`

  const toggle = async () => {
    setLoading(true)
    try {
      if (following) {
        await api.delete(`/follows/${u.id}`)
        setFollowing(false)
      } else {
        await api.post(`/follows/${u.id}`)
        setFollowing(true)
      }
    } catch {
      /* ignore */
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex items-center gap-3 rounded-xl bg-slate-50 px-3 py-2.5">
      <Link to={`/users/${u.id}`} className="flex flex-1 items-center gap-2.5 hover:opacity-80 min-w-0 transition">
        <Avatar userId={u.id} username={u.username} size="sm" />
        <p className="truncate text-sm font-semibold text-slate-900">{label}</p>
      </Link>
      {!u.isMe && (
        <button
          type="button"
          disabled={loading}
          onClick={toggle}
          className={`shrink-0 btn btn-sm ${following ? 'btn-secondary' : 'btn-primary'}`}
        >
          {loading ? '…' : following ? '✓ Following' : 'Follow'}
        </button>
      )}
    </div>
  )
}

function UserSearch() {
  const [q, setQ] = useState('')
  const [results, setResults] = useState(null)
  const [loading, setLoading] = useState(false)
  const debounce = useRef(null)

  useEffect(() => {
    if (debounce.current) clearTimeout(debounce.current)
    if (!q.trim()) { setResults(null); return }
    debounce.current = setTimeout(async () => {
      setLoading(true)
      try {
        const res = await api.get('/users', { params: { q: q.trim(), size: 8 } })
        setResults(res.data.items || [])
      } catch {
        setResults([])
      } finally {
        setLoading(false)
      }
    }, 300)
    return () => clearTimeout(debounce.current)
  }, [q])

  return (
    <div className="card p-4">
      <h2 className="section-title mb-3">🔍 Find people</h2>
      <input
        type="search"
        value={q}
        onChange={(e) => setQ(e.target.value)}
        placeholder="Search by username or email…"
        className="input"
      />
      {loading && <p className="mt-2 text-xs text-slate-400">Searching…</p>}
      {results && results.length === 0 && !loading && (
        <p className="mt-3 text-sm text-slate-400">No users found for "{q}"</p>
      )}
      {results && results.length > 0 && (
        <div className="mt-3 space-y-1.5">
          {results.map((u) => <UserSearchRow key={u.id} u={u} />)}
        </div>
      )}
    </div>
  )
}

// ─── Posts ────────────────────────────────────────────────────────────────────

function BrowsePage() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [page, setPage] = useState(0)
  const inflight = useRef(new Map())

  useEffect(() => {
    let cancelled = false
    async function load() {
      setLoading(true)
      setError('')
      try {
        const res = await api.get('/browse/posts', { params: { page, size: 10 } })
        if (!cancelled) setData(res.data)
      } catch (err) {
        if (!cancelled) setError(getApiErrorMessage(err, 'Failed to load posts.'))
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    load()
    return () => { cancelled = true }
  }, [page])

  const updatePostLocally = (updated) => {
    setData((prev) => {
      if (!prev) return prev
      return { ...prev, items: (prev.items || []).map((p) => (p.id === updated.id ? updated : p)) }
    })
  }

  const deletePostLocally = (postId) => {
    setData((prev) => {
      if (!prev) return prev
      return { ...prev, items: (prev.items || []).filter((p) => p.id !== postId) }
    })
  }

  const handleReactionClick = async (postId, clickedType) => {
    const post = (data?.items ?? []).find((p) => p.id === postId)
    if (!post) return
    const current = post.myReaction || 'NONE'
    const next = current === clickedType ? 'NONE' : clickedType
    let likes = post.likesCount ?? 0
    let dislikes = post.dislikesCount ?? 0
    if (current === 'LIKE') likes -= 1
    if (current === 'DISLIKE') dislikes -= 1
    if (next === 'LIKE') likes += 1
    if (next === 'DISLIKE') dislikes += 1
    const optimistic = { ...post, myReaction: next, likesCount: likes, dislikesCount: dislikes }

    setData((prev) =>
      prev ? { ...prev, items: (prev.items || []).map((p) => (p.id === postId ? optimistic : p)) } : prev
    )

    const reqId = (inflight.current.get(postId) || 0) + 1
    inflight.current.set(postId, reqId)
    try {
      const res = await api.put(`/posts/${postId}/reaction`, { type: next })
      if (inflight.current.get(postId) === reqId && res.data) {
        updatePostLocally({ ...res.data, myReaction: res.data.myReaction ?? 'NONE', likesCount: res.data.likesCount ?? 0, dislikesCount: res.data.dislikesCount ?? 0 })
      }
    } catch {
      updatePostLocally(post)
    }
  }

  const items = data?.items ?? []

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Browse</h1>
        <p className="text-sm text-slate-500 mt-0.5">Discover posts from the entire community.</p>
      </div>

      <UserSearch />

      {error && <div className="error-banner">{error}</div>}

      {loading && (
        <div className="space-y-4">
          {[1, 2].map((i) => (
            <div key={i} className="card p-4 space-y-3 animate-pulse">
              <div className="flex items-center gap-3">
                <div className="h-8 w-8 rounded-full bg-slate-200" />
                <div className="h-3 w-32 rounded bg-slate-200" />
              </div>
              <div className="h-3 w-full rounded bg-slate-100" />
            </div>
          ))}
        </div>
      )}

      {!loading && items.length === 0 && !error && (
        <div className="card p-12 text-center">
          <div className="text-5xl mb-4">🐾</div>
          <p className="text-slate-500">No posts yet. Be the first to share!</p>
          <Link to="/create-post" className="btn-primary mt-4 inline-flex">Create a post</Link>
        </div>
      )}

      <div className="space-y-4">
        {items.map((post) => (
          <PostCard
            key={post.id}
            post={post}
            onToggleReaction={(clickedType) => handleReactionClick(post.id, clickedType)}
            onUpdate={updatePostLocally}
            onDelete={deletePostLocally}
          />
        ))}
      </div>

      {data && data.totalPages > 1 && (
        <div className="flex items-center justify-between pt-2">
          <button type="button" disabled={page === 0} onClick={() => setPage((p) => Math.max(0, p - 1))} className="btn-secondary btn-sm">← Previous</button>
          <span className="text-xs text-slate-500 font-medium">Page {(data.page ?? 0) + 1} of {data.totalPages ?? 1}</span>
          <button type="button" disabled={!data.hasNext} onClick={() => setPage((p) => p + 1)} className="btn-secondary btn-sm">Next →</button>
        </div>
      )}
    </div>
  )
}

export default BrowsePage
