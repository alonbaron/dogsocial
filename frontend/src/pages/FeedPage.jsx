import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../lib/client'
import { useAuth } from '../lib/auth'
import PostCard from '../components/PostCard.jsx'

function FeedPage() {
  const [feed, setFeed] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [page, setPage] = useState(0)
  const { user } = useAuth()
  const inflight = useRef(new Map())

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError('')
    api
      .get('/feed', { params: { page, size: 10 } })
      .then((res) => {
        if (!cancelled) setFeed(res.data)
      })
      .catch((err) => {
        if (!cancelled) {
          const message =
            err.response?.data?.message || 'Failed to load feed.'
          setError(message)
        }
      })
      .finally(() => !cancelled && setLoading(false))

    return () => {
      cancelled = true
    }
  }, [page])

  const updatePostLocally = (updated) => {
    setFeed((prev) => {
      if (!prev) return prev
      return {
        ...prev,
        items: prev.items.map((p) => (p.id === updated.id ? updated : p)),
      }
    })
  }

  const applyOptimisticReaction = (post, clickedType) => {
    const current = post.myReaction || 'NONE'
    const next = current === clickedType ? 'NONE' : clickedType

    let likes = post.likesCount
    let dislikes = post.dislikesCount

    if (current === 'LIKE') likes -= 1
    if (current === 'DISLIKE') dislikes -= 1
    if (next === 'LIKE') likes += 1
    if (next === 'DISLIKE') dislikes += 1

    return {
      updated: { ...post, myReaction: next, likesCount: likes, dislikesCount: dislikes },
      selection: next,
    }
  }

  const handleReactionClick = async (postId, clickedType) => {
    // optimistic update based on latest state (fixes fast double-click)
    let previous = null
    let selection = 'NONE'

    setFeed((prev) => {
      if (!prev) return prev
      const post = prev.items.find((p) => p.id === postId)
      if (!post) return prev
      previous = post
      const optimistic = applyOptimisticReaction(post, clickedType)
      selection = optimistic.selection
      return {
        ...prev,
        items: prev.items.map((p) => (p.id === postId ? optimistic.updated : p)),
      }
    })

    const reqId = (inflight.current.get(postId) || 0) + 1
    inflight.current.set(postId, reqId)

    try {
      const res = await api.put(`/posts/${postId}/reaction`, { type: selection })
      const latest = inflight.current.get(postId)
      if (latest === reqId && res.data) {
        updatePostLocally({
          ...res.data,
          myReaction: res.data.myReaction ?? 'NONE',
          likesCount: res.data.likesCount ?? 0,
          dislikesCount: res.data.dislikesCount ?? 0,
        })
      }
    } catch (err) {
      if (previous) updatePostLocally(previous)
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold text-slate-900">Your feed</h1>
          <p className="text-xs text-slate-500">
            Posts from you and people you follow.
          </p>
        </div>
        <Link
          to="/create-post"
          className="rounded-full bg-pink-500 px-4 py-2 text-xs font-medium text-white shadow-sm hover:bg-pink-600"
        >
          New post
        </Link>
      </div>

      {error && (
        <div className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
          {error}
        </div>
      )}

      {loading && (
        <div className="rounded-2xl bg-white p-4 text-sm text-slate-500 shadow-sm">
          Loading feed…
        </div>
      )}

      {feed && (feed.items ?? []).length === 0 && !loading && (
        <div className="rounded-2xl bg-white p-6 text-sm text-slate-500 shadow-sm">
          No posts yet. Share your first update about your dog!
        </div>
      )}

      <div className="space-y-3">
        {(feed?.items ?? []).map((post) => (
          <PostCard
            key={post.id}
            post={post}
            onToggleReaction={(clickedType) => handleReactionClick(post.id, clickedType)}
          />
        ))}
      </div>

      {feed && (
        <div className="mt-4 flex items-center justify-between text-xs text-slate-500">
          <button
            type="button"
            disabled={page === 0}
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            className="rounded-full border border-slate-200 px-3 py-1 text-xs font-medium hover:bg-slate-100 disabled:opacity-40"
          >
            Previous
          </button>
          <span>
            Page {feed.page + 1} of {feed.totalPages || 1}
          </span>
          <button
            type="button"
            disabled={!feed.hasNext}
            onClick={() => setPage((p) => p + 1)}
            className="rounded-full border border-slate-200 px-3 py-1 text-xs font-medium hover:bg-slate-100 disabled:opacity-40"
          >
            Next
          </button>
        </div>
      )}
    </div>
  )
}

export default FeedPage

