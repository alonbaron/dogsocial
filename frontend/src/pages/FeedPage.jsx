import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, getApiErrorMessage } from '../lib/client'
import PostCard from '../components/PostCard.jsx'

function FeedPage() {
  const [feed, setFeed] = useState(null)
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
        const res = await api.get('/feed', { params: { page, size: 10 } })
        if (!cancelled) setFeed(res.data)
      } catch (err) {
        if (!cancelled) setError(getApiErrorMessage(err, 'Failed to load feed.'))
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    load()
    return () => { cancelled = true }
  }, [page])

  const updatePostLocally = (updated) => {
    setFeed((prev) => {
      if (!prev) return prev
      return { ...prev, items: prev.items.map((p) => (p.id === updated.id ? updated : p)) }
    })
  }

  const deletePostLocally = (postId) => {
    setFeed((prev) => {
      if (!prev) return prev
      return { ...prev, items: prev.items.filter((p) => p.id !== postId) }
    })
  }

  const handleReactionClick = async (postId, clickedType) => {
    // Read current post state BEFORE setState (updater callbacks run asynchronously)
    const post = (feed?.items ?? []).find((p) => p.id === postId)
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

    setFeed((prev) =>
      prev ? { ...prev, items: prev.items.map((p) => (p.id === postId ? optimistic : p)) } : prev
    )

    const reqId = (inflight.current.get(postId) || 0) + 1
    inflight.current.set(postId, reqId)
    try {
      const res = await api.put(`/posts/${postId}/reaction`, { type: next })
      if (inflight.current.get(postId) === reqId && res.data) {
        updatePostLocally({ ...res.data, myReaction: res.data.myReaction ?? 'NONE', likesCount: res.data.likesCount ?? 0, dislikesCount: res.data.dislikesCount ?? 0 })
      }
    } catch {
      updatePostLocally(post) // revert to original
    }
  }

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Your Feed</h1>
          <p className="text-sm text-slate-500 mt-0.5">Posts from you and the people you follow.</p>
        </div>
        <Link to="/create-post" className="btn-primary">
          + New post
        </Link>
      </div>

      {error && <div className="error-banner">{error}</div>}

      {loading && (
        <div className="space-y-4">
          {[1, 2, 3].map((i) => (
            <div key={i} className="card p-4 space-y-3 animate-pulse">
              <div className="flex items-center gap-3">
                <div className="h-8 w-8 rounded-full bg-slate-200" />
                <div className="space-y-1.5">
                  <div className="h-3 w-32 rounded bg-slate-200" />
                  <div className="h-2.5 w-20 rounded bg-slate-100" />
                </div>
              </div>
              <div className="space-y-2">
                <div className="h-3 w-full rounded bg-slate-100" />
                <div className="h-3 w-4/5 rounded bg-slate-100" />
              </div>
            </div>
          ))}
        </div>
      )}

      {!loading && feed && (feed.items ?? []).length === 0 && (
        <div className="card p-12 text-center">
          <div className="text-5xl mb-4">🐾</div>
          <h2 className="text-lg font-semibold text-slate-900 mb-2">Your feed is empty</h2>
          <p className="text-sm text-slate-500 mb-6">Follow other dog owners or share your first post!</p>
          <div className="flex gap-3 justify-center">
            <Link to="/create-post" className="btn-primary">Share a post</Link>
            <Link to="/browse" className="btn-secondary">Discover people</Link>
          </div>
        </div>
      )}

      <div className="space-y-4">
        {(feed?.items ?? []).map((post) => (
          <PostCard
            key={post.id}
            post={post}
            onToggleReaction={(clickedType) => handleReactionClick(post.id, clickedType)}
            onUpdate={updatePostLocally}
            onDelete={deletePostLocally}
          />
        ))}
      </div>

      {feed && feed.totalPages > 1 && (
        <div className="flex items-center justify-between pt-2">
          <button
            type="button"
            disabled={page === 0}
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            className="btn-secondary btn-sm"
          >
            ← Previous
          </button>
          <span className="text-xs text-slate-500 font-medium">
            Page {feed.page + 1} of {feed.totalPages}
          </span>
          <button
            type="button"
            disabled={!feed.hasNext}
            onClick={() => setPage((p) => p + 1)}
            className="btn-secondary btn-sm"
          >
            Next →
          </button>
        </div>
      )}
    </div>
  )
}

export default FeedPage
