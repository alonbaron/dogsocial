import { useState, useEffect, useRef } from 'react'
import { Link } from 'react-router-dom'
import { api, apiBaseUrl, getApiErrorMessage } from '../lib/client'
import { useAuth } from '../lib/auth'
import Avatar from './Avatar.jsx'

function relativeTime(iso) {
  if (!iso) return ''
  const diff = Date.now() - new Date(iso).getTime()
  const m = Math.floor(diff / 60000)
  if (m < 1) return 'just now'
  if (m < 60) return `${m}m ago`
  const h = Math.floor(m / 60)
  if (h < 24) return `${h}h ago`
  const d = Math.floor(h / 24)
  if (d < 7) return `${d}d ago`
  return new Date(iso).toLocaleDateString()
}

function CommentsSection({ postId }) {
  const { user } = useAuth()
  const [comments, setComments] = useState([])
  const [loading, setLoading] = useState(true)
  const [newComment, setNewComment] = useState('')
  const [posting, setPosting] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const [editContent, setEditContent] = useState('')
  const loaded = useRef(false)

  useEffect(() => {
    if (loaded.current) return
    loaded.current = true
    async function load() {
      try {
        const res = await api.get(`/posts/${postId}/comments`, { params: { size: 50 } })
        setComments(res.data.items ?? [])
      } catch { /* ignore */ }
      finally { setLoading(false) }
    }
    load()
  }, [postId])

  const handlePost = async (e) => {
    e.preventDefault()
    if (!newComment.trim()) return
    setPosting(true)
    try {
      const res = await api.post(`/posts/${postId}/comments`, { content: newComment.trim() })
      setComments((prev) => [...prev, res.data])
      setNewComment('')
    } catch { /* ignore */ }
    finally { setPosting(false) }
  }

  const handleDelete = async (commentId) => {
    try {
      await api.delete(`/comments/${commentId}`)
      setComments((prev) => prev.filter((c) => c.id !== commentId))
    } catch { /* ignore */ }
  }

  const handleEditSave = async (commentId) => {
    if (!editContent.trim()) return
    try {
      const res = await api.put(`/comments/${commentId}`, { content: editContent.trim() })
      setComments((prev) => prev.map((c) => (c.id === commentId ? res.data : c)))
      setEditingId(null)
    } catch { /* ignore */ }
  }

  return (
    <div className="border-t border-slate-100 px-4 py-3 space-y-3">
      {loading && <p className="text-xs text-slate-400 animate-pulse">Loading comments...</p>}

      {!loading && comments.length === 0 && (
        <p className="text-xs text-slate-400 text-center py-1">No comments yet. Be the first!</p>
      )}

      <div className="space-y-2 max-h-64 overflow-y-auto">
        {comments.map((c) => {
          const isOwner = user && user.id === c.author.id
          const label = c.author.username ? `@${c.author.username}` : `User #${c.author.id}`
          return (
            <div key={c.id} className="flex gap-2 group/comment">
              <Avatar userId={c.author.id} username={c.author.username} size="xs" />
              <div className="flex-1 min-w-0">
                <div className="flex items-baseline gap-2">
                  <Link to={`/users/${c.author.id}`} className="text-xs font-semibold text-slate-700 hover:text-pink-600 transition">{label}</Link>
                  <span className="text-[10px] text-slate-400">{relativeTime(c.createdAt)}</span>
                </div>
                {editingId === c.id ? (
                  <div className="mt-1 flex gap-1.5">
                    <input
                      value={editContent}
                      onChange={(e) => setEditContent(e.target.value)}
                      className="input text-xs py-1 px-2"
                      maxLength={300}
                    />
                    <button type="button" onClick={() => handleEditSave(c.id)} className="btn btn-primary btn-sm text-[10px]">Save</button>
                    <button type="button" onClick={() => setEditingId(null)} className="btn btn-secondary btn-sm text-[10px]">Cancel</button>
                  </div>
                ) : (
                  <p className="text-xs text-slate-600 leading-relaxed">{c.content}</p>
                )}
                {isOwner && editingId !== c.id && (
                  <div className="flex gap-2 mt-0.5 opacity-0 group-hover/comment:opacity-100 transition-opacity">
                    <button type="button" onClick={() => { setEditingId(c.id); setEditContent(c.content) }} className="text-[10px] text-slate-400 hover:text-slate-600">Edit</button>
                    <button type="button" onClick={() => handleDelete(c.id)} className="text-[10px] text-slate-400 hover:text-red-600">Delete</button>
                  </div>
                )}
              </div>
            </div>
          )
        })}
      </div>

      <form onSubmit={handlePost} className="flex gap-2">
        <input
          value={newComment}
          onChange={(e) => setNewComment(e.target.value)}
          placeholder="Write a comment..."
          maxLength={300}
          className="input text-xs py-2 px-3 flex-1"
        />
        <button type="submit" disabled={posting || !newComment.trim()} className="btn btn-primary btn-sm shrink-0">
          {posting ? '...' : 'Post'}
        </button>
      </form>
    </div>
  )
}

function PostCard({ post, onToggleReaction, onUpdate, onDelete }) {
  const { user } = useAuth()
  const [editing, setEditing] = useState(false)
  const [editCaption, setEditCaption] = useState(post.caption)
  const [saving, setSaving] = useState(false)
  const [editError, setEditError] = useState('')
  const [showComments, setShowComments] = useState(false)

  const isOwner = user && user.id === post.author.id
  const my = post.myReaction || 'NONE'
  const authorLabel = post.author.username
    ? `@${post.author.username}`
    : `User #${post.author.id}`

  const imageUrl = post.imageUrl ? `${apiBaseUrl}${post.imageUrl}` : null

  const handleSaveEdit = async () => {
    if (!editCaption.trim()) return
    setSaving(true)
    setEditError('')
    try {
      const res = await api.put(`/posts/${post.id}`, { caption: editCaption.trim() })
      if (onUpdate) onUpdate(res.data)
      setEditing(false)
    } catch (err) {
      setEditError(getApiErrorMessage(err, 'Failed to save.'))
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async () => {
    if (!window.confirm('Delete this post? This cannot be undone.')) return
    try {
      await api.delete(`/posts/${post.id}`)
      if (onDelete) onDelete(post.id)
    } catch {
      /* ignore */
    }
  }

  const handleCancelEdit = () => {
    setEditing(false)
    setEditCaption(post.caption)
    setEditError('')
  }

  return (
    <article className="card group overflow-hidden transition-shadow duration-200 hover:shadow-md">
      {/* Author header */}
      <div className="flex items-start justify-between gap-3 p-4 pb-2">
        <Link
          to={`/users/${post.author.id}`}
          className="flex items-center gap-3 hover:opacity-90 transition-opacity"
        >
          <Avatar userId={post.author.id} username={post.author.username} size="sm" />
          <div>
            <p className="text-sm font-semibold text-slate-900 leading-tight">{authorLabel}</p>
            <p className="text-[11px] text-slate-400 leading-tight">{relativeTime(post.createdAt)}</p>
          </div>
        </Link>

        <div className="flex items-center gap-1 shrink-0">
          {post.dog && (
            <Link
              to={`/dogs/${post.dog.id}`}
              className="badge badge-pink mr-1"
            >
              🐾 {post.dog.name}
            </Link>
          )}
          {isOwner && !editing && (onUpdate || onDelete) && (
            <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity duration-150">
              {onUpdate && (
                <button
                  type="button"
                  onClick={() => setEditing(true)}
                  className="rounded-lg px-2.5 py-1.5 text-[11px] font-medium text-slate-500 hover:bg-slate-100 hover:text-slate-700 transition"
                >
                  Edit
                </button>
              )}
              {onDelete && (
                <button
                  type="button"
                  onClick={handleDelete}
                  className="rounded-lg px-2.5 py-1.5 text-[11px] font-medium text-slate-500 hover:bg-red-50 hover:text-red-600 transition"
                >
                  Delete
                </button>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Caption */}
      <div className="px-4 py-2">
        {editing ? (
          <div className="space-y-2">
            <textarea
              value={editCaption}
              onChange={(e) => setEditCaption(e.target.value)}
              maxLength={300}
              rows={3}
              className="input resize-none text-sm"
            />
            <div className="flex items-center justify-between">
              <span className="text-[11px] text-slate-400">{editCaption.length}/300</span>
              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={handleCancelEdit}
                  className="btn btn-secondary btn-sm"
                >
                  Cancel
                </button>
                <button
                  type="button"
                  disabled={saving || !editCaption.trim()}
                  onClick={handleSaveEdit}
                  className="btn btn-primary btn-sm"
                >
                  {saving ? 'Saving…' : 'Save'}
                </button>
              </div>
            </div>
            {editError && <p className="text-xs text-red-600">{editError}</p>}
          </div>
        ) : (
          <p className="text-sm leading-relaxed text-slate-800">{post.caption}</p>
        )}
      </div>

      {/* Image */}
      {imageUrl && !editing && (
        <div className="mt-1">
          <img
            src={imageUrl}
            alt="Post photo"
            className="w-full max-h-[480px] object-cover"
          />
        </div>
      )}

      {/* Reactions + comment toggle */}
      <div className="flex items-center gap-2 px-4 py-3 border-t border-slate-50">
        {['LIKE', 'DISLIKE'].map((type) => {
          const active = my === type
          const count = type === 'LIKE' ? post.likesCount : post.dislikesCount
          return (
            <button
              key={type}
              type="button"
              onClick={() => onToggleReaction(type)}
              className={`inline-flex items-center gap-1.5 rounded-full px-3 py-1.5 text-xs font-semibold transition-all duration-150 active:scale-95 ${
                active
                  ? 'bg-pink-100 text-pink-700 ring-1 ring-pink-200'
                  : 'bg-slate-50 text-slate-500 hover:bg-slate-100'
              }`}
            >
              <span>{type === 'LIKE' ? '🦴' : '💩'}</span>
              <span>{count}</span>
            </button>
          )
        })}
        <button
          type="button"
          onClick={() => setShowComments((v) => !v)}
          className="inline-flex items-center gap-1.5 rounded-full px-3 py-1.5 text-xs font-semibold bg-slate-50 text-slate-500 hover:bg-slate-100 transition-all duration-150 active:scale-95 ml-auto"
        >
          <span>💬</span>
          <span>{showComments ? 'Hide' : 'Comments'}</span>
        </button>
      </div>

      {/* Comments section */}
      {showComments && <CommentsSection postId={post.id} />}
    </article>
  )
}

export default PostCard
