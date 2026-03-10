import { Link } from 'react-router-dom'
import Avatar from './Avatar.jsx'

function ReactionButton({ active, type, count, onClick }) {
  const base =
    'inline-flex items-center gap-1 rounded-full px-3 py-1 text-xs font-medium transition-colors'
  const activeLike =
    'bg-pink-100 text-pink-700 border border-pink-200 hover:bg-pink-200'
  const inactive =
    'bg-slate-100 text-slate-600 border border-slate-200 hover:bg-slate-200'

  return (
    <button
      type="button"
      onClick={onClick}
      className={`${base} ${active ? activeLike : inactive}`}
    >
      <span>{type === 'LIKE' ? '👍' : '👎'}</span>
      <span>{count}</span>
    </button>
  )
}

function PostCard({ post, onToggleReaction }) {
  const my = post.myReaction || 'NONE'

  return (
    <article className="rounded-2xl bg-white p-4 shadow-sm ring-1 ring-slate-100 transition hover:-translate-y-0.5 hover:shadow-md">
      <header className="mb-2 flex items-center justify-between text-xs text-slate-500">
        <div className="flex items-center gap-2">
          <Avatar userId={post.author.id} email={post.author.email} size="sm" />
          <span className="font-medium text-slate-800">
            <Link to={`/users/${post.author.id}`}>{post.author.email}</Link>
          </span>
          {post.dog && (
            <span className="text-slate-400">·</span>
          )}
          {post.dog && (
            <Link
              to={`/dogs/${post.dog.id}`}
              className="rounded-full bg-slate-100 px-2 py-0.5 text-[11px] font-medium text-slate-700"
            >
              {post.dog.name}
            </Link>
          )}
        </div>
        <span>{new Date(post.createdAt).toLocaleString()}</span>
      </header>

      <p className="mb-3 text-sm text-slate-800">{post.caption}</p>

      <div className="flex items-center gap-2 text-xs text-slate-500">
        <ReactionButton
          type="LIKE"
          count={post.likesCount}
          active={my === 'LIKE'}
          onClick={() => onToggleReaction('LIKE')}
        />
        <ReactionButton
          type="DISLIKE"
          count={post.dislikesCount}
          active={my === 'DISLIKE'}
          onClick={() => onToggleReaction('DISLIKE')}
        />
      </div>
    </article>
  )
}

export default PostCard

