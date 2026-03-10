import { useEffect, useState } from 'react'
import { api, getApiErrorMessage } from '../lib/client'
import Avatar from '../components/Avatar.jsx'

const STATUS_STYLES = {
  PENDING:  'badge-amber',
  APPROVED: 'badge-green',
  DECLINED: 'badge-red',
  CANCELED: 'badge-slate',
}

function PlaydateCard({ pd, onStatusChange }) {
  const [loading, setLoading] = useState(false)
  const other = pd.isRequester ? pd.recipient : pd.requester
  const otherLabel = other.username ? `@${other.username}` : other.email

  const changeStatus = async (status) => {
    setLoading(true)
    try {
      const res = await api.put(`/playdates/${pd.id}/status`, { status })
      onStatusChange(res.data)
    } catch {
      /* ignore */
    } finally {
      setLoading(false)
    }
  }

  const canAcceptDecline = !pd.isRequester && pd.status === 'PENDING'
  const canCancel = pd.status === 'PENDING' || pd.status === 'APPROVED'
  const dateStr = new Date(pd.scheduledAt).toLocaleString(undefined, {
    weekday: 'short', month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })

  return (
    <div className="card p-4 space-y-3">
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-center gap-3">
          <Avatar userId={other.id} email={other.email} size="md" />
          <div>
            <p className="font-semibold text-slate-900 text-sm">{otherLabel}</p>
            <p className="text-xs text-slate-500 mt-0.5">📅 {dateStr}</p>
            {pd.note && <p className="text-xs text-slate-600 mt-1 italic">"{pd.note}"</p>}
          </div>
        </div>
        <span className={`badge ${STATUS_STYLES[pd.status] ?? 'badge-slate'} uppercase shrink-0`}>
          {pd.status.toLowerCase()}
        </span>
      </div>

      {(canAcceptDecline || canCancel) && (
        <div className="flex gap-2 pt-1 border-t border-slate-50">
          {canAcceptDecline && (
            <>
              <button type="button" disabled={loading} onClick={() => changeStatus('APPROVED')}
                className="btn btn-sm bg-emerald-500 text-white hover:bg-emerald-600">
                ✓ Accept
              </button>
              <button type="button" disabled={loading} onClick={() => changeStatus('DECLINED')}
                className="btn-danger btn-sm">
                ✗ Decline
              </button>
            </>
          )}
          {!canAcceptDecline && canCancel && (
            <button type="button" disabled={loading} onClick={() => changeStatus('CANCELED')}
              className="btn-secondary btn-sm text-slate-500">
              Cancel
            </button>
          )}
        </div>
      )}
    </div>
  )
}

function PlaydatesList({ title, icon, endpoint }) {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false
    async function load() {
      setLoading(true)
      setError('')
      try {
        const res = await api.get(endpoint, { params: { page: 0, size: 10 } })
        if (!cancelled) setItems(res.data.items || [])
      } catch (err) {
        if (!cancelled) setError(getApiErrorMessage(err, 'Failed to load.'))
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    load()
    return () => { cancelled = true }
  }, [endpoint])

  const handleStatusChange = (updated) => {
    setItems((prev) => prev.map((p) => (p.id === updated.id ? updated : p)))
  }

  return (
    <div className="space-y-3">
      <h2 className="section-title">{icon} {title}</h2>
      {error && <div className="error-banner text-xs">{error}</div>}
      {loading && <div className="card p-4 text-xs text-slate-400 animate-pulse">Loading…</div>}
      <div className="space-y-3">
        {items.map((pd) => (
          <PlaydateCard key={pd.id} pd={pd} onStatusChange={handleStatusChange} />
        ))}
        {!loading && items.length === 0 && !error && (
          <div className="card p-6 text-center text-sm text-slate-400">Nothing here yet.</div>
        )}
      </div>
    </div>
  )
}

function PlaydatesPage() {
  const [recipientUsername, setRecipientUsername] = useState('')
  const [scheduledAt, setScheduledAt] = useState('')
  const [note, setNote] = useState('')
  const [creating, setCreating] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const handleCreate = async (e) => {
    e.preventDefault()
    setCreating(true)
    setError('')
    setSuccess('')
    try {
      await api.post('/playdates', {
        recipientUsername: recipientUsername.replace(/^@/, ''),
        scheduledAt: new Date(scheduledAt).toISOString(),
        note: note || null,
      })
      setSuccess('Playdate request sent! 🐾')
      setRecipientUsername('')
      setScheduledAt('')
      setNote('')
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to create playdate.'))
    } finally {
      setCreating(false)
    }
  }

  return (
    <div className="space-y-6">
      {/* Create form */}
      <div className="card p-6">
        <h1 className="text-2xl font-bold text-slate-900 mb-1">Playdates 🐾</h1>
        <p className="text-sm text-slate-500 mb-6">Schedule a playdate with another dog owner by their username.</p>

        {error && <div className="error-banner mb-4">{error}</div>}
        {success && <div className="success-banner mb-4">{success}</div>}

        <form onSubmit={handleCreate} className="space-y-4">
          <div className="grid gap-4 sm:grid-cols-3">
            <div>
              <label className="label">Recipient username</label>
              <div className="relative">
                <span className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 text-sm font-medium">@</span>
                <input
                  type="text"
                  required
                  value={recipientUsername}
                  onChange={(e) => setRecipientUsername(e.target.value.replace(/^@/, ''))}
                  placeholder="their_username"
                  className="input pl-8"
                />
              </div>
            </div>
            <div>
              <label className="label">Date &amp; time</label>
              <input
                type="datetime-local"
                required
                value={scheduledAt}
                onChange={(e) => setScheduledAt(e.target.value)}
                className="input"
              />
            </div>
            <div>
              <label className="label">Note (optional)</label>
              <input
                type="text"
                maxLength={300}
                value={note}
                onChange={(e) => setNote(e.target.value)}
                placeholder="Bring the frisbee!"
                className="input"
              />
            </div>
          </div>
          <div className="flex justify-end">
            <button type="submit" disabled={creating} className="btn-primary">
              {creating ? 'Sending…' : 'Send request'}
            </button>
          </div>
        </form>
      </div>

      {/* Lists */}
      <div className="grid gap-6 lg:grid-cols-3">
        <PlaydatesList title="Incoming" icon="📬" endpoint="/playdates/incoming" />
        <PlaydatesList title="Upcoming" icon="🗓️" endpoint="/playdates/upcoming" />
        <PlaydatesList title="Past" icon="📖" endpoint="/playdates/past" />
      </div>
    </div>
  )
}

export default PlaydatesPage
