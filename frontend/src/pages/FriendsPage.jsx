import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, getApiErrorMessage } from '../lib/client'
import { useAuth } from '../lib/auth'

function List({ title, endpoint }) {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [page, setPage] = useState(0)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError('')
    api
      .get(endpoint, { params: { page, size: 12 } })
      .then((res) => !cancelled && setData(res.data))
      .catch((err) => {
        if (cancelled) return
        setError(getApiErrorMessage(err, 'Failed to load. Check that you are logged in.'))
      })
      .finally(() => !cancelled && setLoading(false))
    return () => {
      cancelled = true
    }
  }, [endpoint, page])

  return (
    <div className="space-y-3">
      <div className="flex items-end justify-between">
        <div>
          <h2 className="text-sm font-semibold text-slate-900">{title}</h2>
          <p className="text-[11px] text-slate-500">Page {page + 1}</p>
        </div>
        <div className="flex gap-2">
          <button
            type="button"
            disabled={page === 0}
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            className="rounded-full border border-slate-200 px-3 py-1 text-[11px] font-medium hover:bg-slate-100 disabled:opacity-40"
          >
            Prev
          </button>
          <button
            type="button"
            disabled={!data?.hasNext}
            onClick={() => setPage((p) => p + 1)}
            className="rounded-full border border-slate-200 px-3 py-1 text-[11px] font-medium hover:bg-slate-100 disabled:opacity-40"
          >
            Next
          </button>
        </div>
      </div>

      {error && (
        <div className="rounded-lg bg-red-50 px-3 py-2 text-xs text-red-700">
          {error}
        </div>
      )}

      {loading && (
        <div className="rounded-2xl bg-white p-4 text-xs text-slate-500 shadow-sm">
          Loading…
        </div>
      )}

      <div className="space-y-2">
        {(data?.items ?? []).map((u) => (
          <Link
            key={u.id}
            to={`/users/${u.id}`}
            className="flex items-center justify-between rounded-2xl bg-white px-4 py-3 text-sm shadow-sm ring-1 ring-slate-100 hover:bg-slate-50"
          >
            <span className="font-medium text-slate-900">{u.email}</span>
            <span className="text-[11px] text-slate-500">ID {u.id}</span>
          </Link>
        ))}

        {data && (data.items ?? []).length === 0 && !loading && (
          <div className="rounded-2xl bg-white p-4 text-xs text-slate-500 shadow-sm">
            Nothing here yet.
          </div>
        )}
      </div>
    </div>
  )
}

function FriendsPage() {
  const { user } = useAuth()

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold text-slate-900">Friends</h1>
          <p className="text-xs text-slate-500">
            Mutual follows (friends), plus followers and following.
          </p>
        </div>
        <Link
          to="/browse"
          className="rounded-full border border-slate-200 bg-white px-4 py-2 text-xs font-medium text-slate-700 hover:bg-slate-50"
        >
          Browse posts
        </Link>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        <div className="md:col-span-1">
          <List title="Friends (mutual)" endpoint="/friends" />
        </div>
        <div className="md:col-span-1">
          <List title="Following" endpoint={`/users/${user.id}/following`} />
        </div>
        <div className="md:col-span-1">
          <List title="Followers" endpoint={`/users/${user.id}/followers`} />
        </div>
      </div>
    </div>
  )
}

export default FriendsPage

