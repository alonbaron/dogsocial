import { useEffect, useState } from 'react'
import { api, getApiErrorMessage } from '../lib/client'

function PlaydatesList({ title, endpoint }) {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError('')
    api
      .get(endpoint, { params: { page: 0, size: 10 } })
      .then((res) => {
        if (!cancelled) setData(res.data)
      })
      .catch((err) => {
        if (!cancelled) {
          setError(getApiErrorMessage(err, 'Failed to load playdates. Check that you are logged in.'))
        }
      })
      .finally(() => !cancelled && setLoading(false))
    return () => {
      cancelled = true
    }
  }, [endpoint])

  return (
    <section className="space-y-2">
      <h2 className="text-sm font-semibold text-slate-900">{title}</h2>
      {error && (
        <div className="rounded-lg bg-red-50 px-3 py-2 text-xs text-red-700">
          {error}
        </div>
      )}
      {loading && (
        <div className="rounded-2xl bg-white p-3 text-xs text-slate-500 shadow-sm">
          Loading…
        </div>
      )}
      <div className="space-y-2">
        {(data?.items ?? []).map((pd) => (
          <div
            key={pd.id}
            className="flex items-center justify-between rounded-2xl bg-white px-4 py-3 text-xs text-slate-700 shadow-sm ring-1 ring-slate-100"
          >
            <div>
              <p className="font-medium">
                {pd.isRequester ? 'You' : pd.requester.email} ↔{' '}
                {pd.isRequester ? pd.recipient.email : 'You'}
              </p>
              <p className="text-[11px] text-slate-500">
                {new Date(pd.scheduledAt).toLocaleString()} ·{' '}
                <span className="uppercase tracking-wide text-pink-600">
                  {pd.status}
                </span>
              </p>
              {pd.note && (
                <p className="mt-1 text-[11px] text-slate-600">{pd.note}</p>
              )}
            </div>
          </div>
        ))}
        {data && (data.items ?? []).length === 0 && !loading && (
          <div className="rounded-2xl bg-white p-3 text-xs text-slate-500 shadow-sm">
            Nothing here yet.
          </div>
        )}
      </div>
    </section>
  )
}

function PlaydatesPage() {
  const [recipientId, setRecipientId] = useState('')
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
        recipientId: Number(recipientId),
        scheduledAt: new Date(scheduledAt).toISOString(),
        note: note || null,
      })
      setSuccess('Playdate request sent.')
      setRecipientId('')
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
      <section className="rounded-2xl bg-white p-4 shadow-sm ring-1 ring-slate-100">
        <h1 className="mb-2 text-lg font-semibold text-slate-900">
          Playdates
        </h1>
        <p className="mb-4 text-xs text-slate-500">
          Schedule future playdates with other dog owners.
        </p>

        {error && (
          <div className="mb-3 rounded-lg bg-red-50 px-3 py-2 text-xs text-red-700">
            {error}
          </div>
        )}
        {success && (
          <div className="mb-3 rounded-lg bg-emerald-50 px-3 py-2 text-xs text-emerald-700">
            {success}
          </div>
        )}

        <form onSubmit={handleCreate} className="grid gap-3 text-xs md:grid-cols-3">
          <div className="md:col-span-1">
            <label className="mb-1 block font-medium text-slate-600">
              Recipient user ID
            </label>
            <input
              type="number"
              required
              min={1}
              value={recipientId}
              onChange={(e) => setRecipientId(e.target.value)}
              className="w-full rounded-lg border border-slate-200 px-3 py-2 text-xs shadow-sm focus:border-pink-500 focus:outline-none focus:ring-1 focus:ring-pink-500"
            />
          </div>
          <div className="md:col-span-1">
            <label className="mb-1 block font-medium text-slate-600">
              Date &amp; time
            </label>
            <input
              type="datetime-local"
              required
              value={scheduledAt}
              onChange={(e) => setScheduledAt(e.target.value)}
              className="w-full rounded-lg border border-slate-200 px-3 py-2 text-xs shadow-sm focus:border-pink-500 focus:outline-none focus:ring-1 focus:ring-pink-500"
            />
          </div>
          <div className="md:col-span-1">
            <label className="mb-1 block font-medium text-slate-600">
              Note (optional)
            </label>
            <input
              type="text"
              maxLength={300}
              value={note}
              onChange={(e) => setNote(e.target.value)}
              className="w-full rounded-lg border border-slate-200 px-3 py-2 text-xs shadow-sm focus:border-pink-500 focus:outline-none focus:ring-1 focus:ring-pink-500"
            />
          </div>
          <div className="md:col-span-3 flex justify-end">
            <button
              type="submit"
              disabled={creating}
              className="rounded-full bg-pink-500 px-4 py-2 text-xs font-medium text-white shadow-sm hover:bg-pink-600 disabled:opacity-60"
            >
              {creating ? 'Sending…' : 'Send request'}
            </button>
          </div>
        </form>
      </section>

      <div className="grid gap-4 md:grid-cols-3">
        <PlaydatesList title="Incoming requests" endpoint="/playdates/incoming" />
        <PlaydatesList title="Upcoming" endpoint="/playdates/upcoming" />
        <PlaydatesList title="Past" endpoint="/playdates/past" />
      </div>
    </div>
  )
}

export default PlaydatesPage

