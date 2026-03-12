import { useEffect, useRef, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api, apiBaseUrl, getApiErrorMessage } from '../lib/client'
import { useAuth } from '../lib/auth'
import PostCard from '../components/PostCard.jsx'
import Avatar from '../components/Avatar.jsx'
import ImageCropModal from '../components/ImageCropModal.jsx'

// ─── Dog Management ──────────────────────────────────────────────────────────

function DogForm({ initial, onSave, onCancel, saving }) {
  const [name, setName] = useState(initial?.name ?? '')
  const [breed, setBreed] = useState(initial?.breed ?? '')
  const [bio, setBio] = useState(initial?.bio ?? '')
  const [photoFile, setPhotoFile] = useState(null)
  const [photoPreview, setPhotoPreview] = useState(null)
  const [cropSrc, setCropSrc] = useState(null)
  const fileRef = useRef(null)

  const handlePhotoChange = (e) => {
    const file = e.target.files?.[0]
    if (!file) return
    setCropSrc(URL.createObjectURL(file))
    if (fileRef.current) fileRef.current.value = ''
  }

  const handleCropDone = (blob) => {
    setPhotoFile(blob)
    setPhotoPreview(URL.createObjectURL(blob))
    setCropSrc(null)
  }

  const clearPhoto = () => {
    setPhotoFile(null)
    setPhotoPreview(null)
    setCropSrc(null)
    if (fileRef.current) fileRef.current.value = ''
  }

  return (
    <>
    {cropSrc && (
      <ImageCropModal
        imageSrc={cropSrc}
        aspect={4 / 3}
        title="Crop dog photo"
        onCrop={handleCropDone}
        onCancel={() => { setCropSrc(null) }}
      />
    )}
    <form
      onSubmit={(e) => { e.preventDefault(); onSave({ name, breed: breed || null, bio: bio || null, photoFile }) }}
      className="space-y-3 rounded-xl border-2 border-dashed border-pink-200 bg-pink-50/40 p-4"
    >
      <h3 className="text-sm font-semibold text-slate-700">
        {initial ? '✏️ Edit dog' : '🐶 Add a dog'}
      </h3>
      <div className="grid gap-3 sm:grid-cols-2">
        <div>
          <label className="label">Dog&apos;s name *</label>
          <input required maxLength={80} value={name} onChange={(e) => setName(e.target.value)} placeholder="Buddy" className="input" />
        </div>
        <div>
          <label className="label">Breed</label>
          <input maxLength={80} value={breed} onChange={(e) => setBreed(e.target.value)} placeholder="Golden Retriever" className="input" />
        </div>
      </div>
      <div>
        <label className="label">About</label>
        <textarea maxLength={300} rows={2} value={bio} onChange={(e) => setBio(e.target.value)} placeholder="Loves long walks and belly rubs…" className="input resize-none" />
      </div>
      {/* Photo upload */}
      <div>
        <label className="label">Photo (optional · max 5 MB)</label>
        {photoPreview ? (
          <div className="relative overflow-hidden rounded-xl border border-slate-200">
            <img src={photoPreview} alt="Preview" className="max-h-40 w-full object-cover" />
            <button type="button" onClick={clearPhoto} className="absolute right-2 top-2 flex h-6 w-6 items-center justify-center rounded-full bg-black/60 text-xs text-white hover:bg-black/80">✕</button>
          </div>
        ) : (
          <label className="flex cursor-pointer items-center justify-center gap-2 rounded-xl border-2 border-dashed border-slate-200 bg-slate-50 py-4 text-sm text-slate-400 hover:border-pink-300 hover:bg-pink-50/30 transition">
            <span>📷</span>
            <span>Choose a photo</span>
            <input ref={fileRef} type="file" accept="image/jpeg,image/png,image/gif,image/webp" className="hidden" onChange={handlePhotoChange} />
          </label>
        )}
      </div>
      <div className="flex gap-2">
        <button type="submit" disabled={saving} className="btn-primary btn-sm">
          {saving ? 'Saving…' : initial ? 'Save changes' : 'Add dog'}
        </button>
        <button type="button" onClick={onCancel} className="btn-secondary btn-sm">Cancel</button>
      </div>
    </form>
    </>
  )
}

function DogCard({ dog, isOwner, onEdit, onDelete, deleting }) {
  const photoUrl = dog.photoUrl ? `${apiBaseUrl}${dog.photoUrl}` : null

  return (
    <div className="overflow-hidden rounded-xl bg-white ring-1 ring-slate-100 shadow-sm">
      {photoUrl && (
        <div className="relative w-full" style={{ paddingBottom: '75%' }}>
          <img src={photoUrl} alt={dog.name} className="absolute inset-0 w-full h-full object-cover" />
        </div>
      )}
      <div className="flex items-start gap-3 px-4 py-3">
        {!photoUrl && (
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-amber-100 text-xl">🐾</div>
        )}
        <div className="flex-1 min-w-0">
          <p className="font-semibold text-slate-900">{dog.name}</p>
          {dog.breed && <p className="text-xs text-slate-500 mt-0.5">{dog.breed}</p>}
          {dog.bio && <p className="mt-1 text-sm text-slate-600">{dog.bio}</p>}
        </div>
        {isOwner && (
          <div className="flex shrink-0 gap-1.5">
            <button type="button" onClick={() => onEdit(dog)} className="btn-secondary btn-sm">Edit</button>
            <button type="button" onClick={() => onDelete(dog.id)} disabled={deleting === dog.id} className="btn-danger btn-sm">
              {deleting === dog.id ? '…' : 'Delete'}
            </button>
          </div>
        )}
      </div>
    </div>
  )
}

function DogsSection({ userId, isOwner }) {
  const [dogs, setDogs] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showAddForm, setShowAddForm] = useState(false)
  const [editingDog, setEditingDog] = useState(null)
  const [saving, setSaving] = useState(false)
  const [deleting, setDeleting] = useState(null)
  const [saveError, setSaveError] = useState('')

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    api.get(`/users/${userId}/dogs`, { params: { page: 0, size: 50 } })
      .then((res) => !cancelled && setDogs(res.data.items || []))
      .catch(() => !cancelled && setError('Failed to load dogs.'))
      .finally(() => !cancelled && setLoading(false))
    return () => { cancelled = true }
  }, [userId])

  const uploadDogPhoto = async (dogId, photoFile) => {
    const formData = new FormData()
    formData.append('file', photoFile)
    const res = await api.post(`/dogs/${dogId}/photo`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    return res.data  // DogResponse with photoUrl set
  }

  const handleAdd = async ({ photoFile, ...rest }) => {
    setSaving(true)
    setSaveError('')
    try {
      const createRes = await api.post('/dogs', rest)
      const finalDog = photoFile
        ? await uploadDogPhoto(createRes.data.id, photoFile)
        : createRes.data
      setDogs((prev) => [...prev, finalDog])
      setShowAddForm(false)
    } catch (err) {
      setSaveError(getApiErrorMessage(err, 'Failed to add dog.'))
    } finally {
      setSaving(false)
    }
  }

  const handleEdit = async ({ photoFile, ...rest }) => {
    setSaving(true)
    setSaveError('')
    try {
      const updateRes = await api.put(`/dogs/${editingDog.id}`, rest)
      const finalDog = photoFile
        ? await uploadDogPhoto(editingDog.id, photoFile)
        : updateRes.data
      setDogs((prev) => prev.map((d) => (d.id === editingDog.id ? finalDog : d)))
      setEditingDog(null)
    } catch (err) {
      setSaveError(getApiErrorMessage(err, 'Failed to update dog.'))
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (dogId) => {
    if (!window.confirm('Delete this dog profile?')) return
    setDeleting(dogId)
    try {
      await api.delete(`/dogs/${dogId}`)
      setDogs((prev) => prev.filter((d) => d.id !== dogId))
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to delete dog.'))
    } finally {
      setDeleting(null)
    }
  }

  return (
    <div className="card p-4 space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="section-title">🐶 Dogs {dogs.length > 0 && <span className="ml-1.5 badge badge-pink">{dogs.length}</span>}</h2>
        {isOwner && !showAddForm && !editingDog && (
          <button type="button" onClick={() => setShowAddForm(true)} className="btn-primary btn-sm">
            + Add dog
          </button>
        )}
      </div>

      {(saveError || error) && <p className="text-xs text-red-600">{saveError || error}</p>}

      {isOwner && showAddForm && (
        <DogForm onSave={handleAdd} onCancel={() => setShowAddForm(false)} saving={saving} />
      )}

      {loading ? (
        <p className="text-sm text-slate-400 animate-pulse">Loading dogs…</p>
      ) : dogs.length === 0 && !showAddForm ? (
        <div className="rounded-xl bg-slate-50 p-6 text-center">
          <p className="text-sm text-slate-400">No dogs added yet.</p>
          {isOwner && (
            <button type="button" onClick={() => setShowAddForm(true)} className="btn-primary btn-sm mt-3">
              Add your first dog 🐾
            </button>
          )}
        </div>
      ) : (
        <div className="space-y-2">
          {dogs.map((dog) =>
            editingDog?.id === dog.id ? (
              <DogForm key={dog.id} initial={dog} onSave={handleEdit} onCancel={() => setEditingDog(null)} saving={saving} />
            ) : (
              <DogCard key={dog.id} dog={dog} isOwner={isOwner} onEdit={setEditingDog} onDelete={handleDelete} deleting={deleting} />
            )
          )}
        </div>
      )}
    </div>
  )
}

// ─── Edit Profile ─────────────────────────────────────────────────────────────

function EditProfileSection({ profile, onSaved, onCancel }) {
  const [username, setUsername] = useState(profile.username ?? '')
  const [bio, setBio] = useState(profile.bio ?? '')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (username && !/^[a-zA-Z0-9_]{3,30}$/.test(username)) {
      setError('Username must be 3–30 characters: letters, numbers, underscores only')
      return
    }
    setSaving(true)
    setError('')
    try {
      const res = await api.put('/users/me/profile', { username: username || null, bio: bio || null })
      onSaved(res.data)
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to save profile.'))
    } finally {
      setSaving(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4 rounded-xl border-2 border-dashed border-slate-200 p-4">
      <h3 className="text-sm font-semibold text-slate-700">✏️ Edit profile</h3>
      {error && <div className="error-banner text-xs">{error}</div>}
      <div>
        <label className="label">Username</label>
        <div className="relative">
          <span className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 font-medium">@</span>
          <input
            type="text"
            maxLength={30}
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="your_username"
            className="input pl-8"
          />
        </div>
        <p className="mt-1 text-[11px] text-slate-400">Letters, numbers and underscores. 3–30 characters.</p>
      </div>
      <div>
        <label className="label">Bio</label>
        <textarea
          maxLength={300}
          rows={3}
          value={bio}
          onChange={(e) => setBio(e.target.value)}
          placeholder="Dog lover living in…"
          className="input resize-none"
        />
        <p className="mt-1 text-right text-[11px] text-slate-400">{bio.length}/300</p>
      </div>
      <div className="flex gap-2">
        <button type="submit" disabled={saving} className="btn-primary btn-sm">
          {saving ? 'Saving…' : 'Save changes'}
        </button>
        <button type="button" onClick={onCancel} className="btn-secondary btn-sm">Cancel</button>
      </div>
    </form>
  )
}

// ─── Avatar Upload ────────────────────────────────────────────────────────────

function AvatarUploadSection({ userId, avatarKey, onUploaded }) {
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState('')
  const [cropSrc, setCropSrc] = useState(null)
  const fileRef = useRef(null)

  const handleFileChange = (e) => {
    const file = e.target.files?.[0]
    if (!file) return
    setCropSrc(URL.createObjectURL(file))
    if (fileRef.current) fileRef.current.value = ''
  }

  const handleCropDone = async (blob) => {
    setCropSrc(null)
    setError('')
    setUploading(true)
    try {
      const formData = new FormData()
      formData.append('file', blob, 'avatar.jpg')
      await api.post('/users/me/avatar', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      onUploaded()
    } catch (err) {
      setError(getApiErrorMessage(err, 'Upload failed.'))
    } finally {
      setUploading(false)
    }
  }

  return (
    <>
      {cropSrc && (
        <ImageCropModal
          imageSrc={cropSrc}
          aspect={1}
          title="Crop profile photo"
          onCrop={handleCropDone}
          onCancel={() => { setCropSrc(null) }}
        />
      )}
      <div className="relative">
        <Avatar key={avatarKey} userId={userId} email="" size="lg" version={avatarKey} />
        <label className={`absolute bottom-0 right-0 flex h-8 w-8 cursor-pointer items-center justify-center rounded-full shadow-md transition ${uploading ? 'bg-slate-400 pointer-events-none' : 'bg-pink-500 hover:bg-pink-600'}`}>
          <span className="text-sm text-white">{uploading ? '…' : '📷'}</span>
          <input
            ref={fileRef}
            type="file"
            accept="image/jpeg,image/png,image/gif,image/webp"
            className="hidden"
            disabled={uploading}
            onChange={handleFileChange}
          />
        </label>
        {error && <p className="mt-1 text-[11px] text-red-600 w-24 text-center">{error}</p>}
      </div>
    </>
  )
}

// ─── Follow List Modal (Instagram-style) ──────────────────────────────────────

function FollowListModal({ title, endpoint, meId, onClose }) {
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [hasMore, setHasMore] = useState(false)
  const [followState, setFollowState] = useState({})

  useEffect(() => {
    let cancelled = false
    async function load() {
      setLoading(true)
      try {
        const res = await api.get(endpoint, { params: { page, size: 20 } })
        const items = res.data.items ?? []
        if (!cancelled) {
          setUsers((prev) => page === 0 ? items : [...prev, ...items])
          setHasMore(res.data.hasNext ?? false)
          const initial = {}
          items.forEach((u) => { initial[u.id] = !!u.isFollowing })
          setFollowState((prev) => ({ ...prev, ...initial }))
        }
      } catch { /* ignore */ }
      finally { if (!cancelled) setLoading(false) }
    }
    load()
    return () => { cancelled = true }
  }, [endpoint, page])

  const toggleFollow = async (userId) => {
    const currently = followState[userId]
    setFollowState((prev) => ({ ...prev, [userId]: !currently }))
    try {
      if (currently) {
        await api.delete(`/follows/${userId}`)
      } else {
        await api.post(`/follows/${userId}`)
      }
    } catch {
      setFollowState((prev) => ({ ...prev, [userId]: currently }))
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4" onClick={onClose}>
      <div className="w-full max-w-sm rounded-2xl bg-white shadow-2xl flex flex-col max-h-[70vh]" onClick={(e) => e.stopPropagation()}>
        <div className="flex items-center justify-between border-b border-slate-100 px-4 py-3">
          <h2 className="text-base font-bold text-slate-900">{title}</h2>
          <button type="button" onClick={onClose} className="flex h-8 w-8 items-center justify-center rounded-full text-slate-400 hover:bg-slate-100 hover:text-slate-600 transition">✕</button>
        </div>

        <div className="flex-1 overflow-y-auto divide-y divide-slate-50">
          {users.map((u) => {
            const isMe = u.id === meId
            const label = u.username ? `@${u.username}` : `User #${u.id}`
            const isFollowing = followState[u.id]
            return (
              <div key={u.id} className="flex items-center gap-3 px-4 py-2.5">
                <Link to={`/users/${u.id}`} onClick={onClose} className="flex items-center gap-3 flex-1 min-w-0 hover:opacity-80 transition">
                  <Avatar userId={u.id} username={u.username} size="sm" />
                  <p className="truncate text-sm font-semibold text-slate-900">{label}</p>
                </Link>
                {!isMe && (
                  <button
                    type="button"
                    onClick={() => toggleFollow(u.id)}
                    className={`shrink-0 btn btn-sm ${isFollowing ? 'btn-secondary' : 'btn-primary'}`}
                  >
                    {isFollowing ? '✓ Following' : 'Follow'}
                  </button>
                )}
              </div>
            )
          })}

          {loading && (
            <div className="px-4 py-4 text-center">
              <p className="text-xs text-slate-400 animate-pulse">Loading...</p>
            </div>
          )}

          {!loading && users.length === 0 && (
            <div className="px-4 py-8 text-center">
              <p className="text-sm text-slate-400">No one here yet.</p>
            </div>
          )}
        </div>

        {hasMore && !loading && (
          <div className="border-t border-slate-100 px-4 py-2.5 text-center">
            <button type="button" onClick={() => setPage((p) => p + 1)} className="text-xs font-semibold text-pink-600 hover:text-pink-700">
              Load more
            </button>
          </div>
        )}
      </div>
    </div>
  )
}

// ─── Main Page ────────────────────────────────────────────────────────────────

function UserProfilePage() {
  const { userId } = useParams()
  const { user: me, login } = useAuth()
  const [profile, setProfile] = useState(null)
  const [posts, setPosts] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [avatarKey, setAvatarKey] = useState(0)
  const [following, setFollowing] = useState(false)
  const [followLoading, setFollowLoading] = useState(false)
  const [showEditProfile, setShowEditProfile] = useState(false)
  const [followListModal, setFollowListModal] = useState(null)

  useEffect(() => {
    let cancelled = false
    async function load() {
      setLoading(true)
      setError('')
      try {
        const profileRes = await api.get(`/users/${userId}`)
        if (cancelled) return
        setProfile(profileRes.data)
        setFollowing(profileRes.data.isFollowing)
        setLoading(false)

        // Load posts separately so a missing/failing endpoint never breaks the profile
        try {
          const postsRes = await api.get(`/users/${userId}/posts`, { params: { page: 0, size: 20 } })
          if (!cancelled) setPosts(postsRes.data)
        } catch {
          // Endpoint may not exist yet; show empty posts instead of crashing
          if (!cancelled) setPosts({ items: [], totalItems: 0 })
        }
      } catch (err) {
        if (!cancelled) {
          setError(getApiErrorMessage(err, 'Failed to load profile.'))
          setLoading(false)
        }
      }
    }
    load()
    return () => { cancelled = true }
  }, [userId])

  const handleFollow = async () => {
    if (!profile) return
    setFollowLoading(true)
    try {
      if (following) {
        await api.delete(`/follows/${profile.id}`)
        setFollowing(false)
        setProfile((p) => p && ({ ...p, followersCount: p.followersCount - 1 }))
      } else {
        await api.post(`/follows/${profile.id}`)
        setFollowing(true)
        setProfile((p) => p && ({ ...p, followersCount: p.followersCount + 1 }))
      }
    } catch {
      /* ignore */
    } finally {
      setFollowLoading(false)
    }
  }

  const handleProfileSaved = (updated) => {
    setProfile((p) => p && ({ ...p, ...updated }))
    setShowEditProfile(false)
    if (me && updated.id === me.id) {
      login(sessionStorage.getItem('dogsocial_token'), { ...me, username: updated.username, bio: updated.bio })
    }
  }

  if (loading) {
    return (
      <div className="space-y-4 animate-pulse">
        <div className="card p-6 flex gap-4">
          <div className="h-16 w-16 rounded-full bg-slate-200 shrink-0" />
          <div className="flex-1 space-y-2 pt-1">
            <div className="h-4 w-40 rounded bg-slate-200" />
            <div className="h-3 w-24 rounded bg-slate-100" />
            <div className="h-3 w-32 rounded bg-slate-100" />
          </div>
        </div>
      </div>
    )
  }

  if (error) {
    return <div className="error-banner">{error}</div>
  }

  if (!profile) return null

  return (
    <div className="space-y-5">
      {/* Profile header */}
      <div className="card overflow-hidden">
        {/* Cover gradient */}
        <div className="h-24 bg-gradient-to-br from-pink-400 via-rose-400 to-orange-300" />

        <div className="px-5 pb-5">
          {/* Avatar row */}
          <div className="flex items-end justify-between -mt-10 mb-4">
            <div className="ring-4 ring-white rounded-full">
              {profile.isMe ? (
                <AvatarUploadSection
                  userId={profile.id}
                  avatarKey={avatarKey}
                  onUploaded={() => setAvatarKey(Date.now())}
                />
              ) : (
                <Avatar userId={profile.id} username={profile.username} size="lg" version={avatarKey} />
              )}
            </div>

            <div className="flex gap-2 mb-1">
              {/* Guard with both isMe flag AND id comparison in case of JSON deserialization issues */}
              {!profile.isMe && String(profile.id) !== String(me?.id) && (
                <button
                  type="button"
                  disabled={followLoading}
                  onClick={handleFollow}
                  className={`btn btn-sm ${following ? 'btn-secondary' : 'btn-primary'}`}
                >
                  {followLoading ? '…' : following ? '✓ Following' : 'Follow'}
                </button>
              )}
              {(profile.isMe || String(profile.id) === String(me?.id)) && !showEditProfile && (
                <button
                  type="button"
                  onClick={() => setShowEditProfile(true)}
                  className="btn-secondary btn-sm"
                >
                  ✏️ Edit profile
                </button>
              )}
            </div>
          </div>

          {/* Name & info */}
          <div className="space-y-1">
            {profile.username ? (
              <h1 className="text-xl font-bold text-slate-900">@{profile.username}</h1>
            ) : (
              <h1 className="text-xl font-bold text-slate-900">
                {profile.isMe || String(profile.id) === String(me?.id) ? profile.email : `User #${profile.id}`}
              </h1>
            )}
            {/* Only show email to the owner */}
            {profile.email && (profile.isMe || String(profile.id) === String(me?.id)) && (
              <p className="text-sm text-slate-500">{profile.email}</p>
            )}
            {profile.bio && (
              <p className="text-sm text-slate-700 pt-1 leading-relaxed">{profile.bio}</p>
            )}
            {(profile.isMe || String(profile.id) === String(me?.id)) && !profile.username && (
              <button
                type="button"
                onClick={() => setShowEditProfile(true)}
                className="mt-2 text-xs text-pink-600 hover:text-pink-700 font-medium"
              >
                + Set a username
              </button>
            )}
          </div>

          {/* Stats */}
          <div className="mt-4 flex gap-6 text-sm border-t border-slate-100 pt-4">
            <button
              type="button"
              onClick={() => setFollowListModal({ title: 'Followers', endpoint: `/users/${profile.id}/followers` })}
              className="text-center hover:opacity-70 transition"
            >
              <p className="font-bold text-slate-900">{profile.followersCount}</p>
              <p className="text-xs text-slate-500">Followers</p>
            </button>
            <button
              type="button"
              onClick={() => setFollowListModal({ title: 'Following', endpoint: `/users/${profile.id}/following` })}
              className="text-center hover:opacity-70 transition"
            >
              <p className="font-bold text-slate-900">{profile.followingCount}</p>
              <p className="text-xs text-slate-500">Following</p>
            </button>
            <div className="text-center">
              <p className="font-bold text-slate-900">{posts?.totalItems ?? posts?.items?.length ?? '—'}</p>
              <p className="text-xs text-slate-500">Posts</p>
            </div>
            {profile.createdAt && (
              <div className="ml-auto text-right">
                <p className="text-xs text-slate-400">Joined</p>
                <p className="text-xs font-medium text-slate-500">
                  {new Date(profile.createdAt).toLocaleDateString(undefined, { month: 'short', year: 'numeric' })}
                </p>
              </div>
            )}
          </div>

          {/* Edit profile form */}
          {showEditProfile && profile.isMe && (
            <div className="mt-4 border-t border-slate-100 pt-4">
              <EditProfileSection
                profile={profile}
                onSaved={handleProfileSaved}
                onCancel={() => setShowEditProfile(false)}
              />
            </div>
          )}
        </div>
      </div>

      {/* Dogs section */}
      <DogsSection userId={profile.id} isOwner={profile.isMe} />

      {/* Posts */}
      <div className="space-y-4">
        <h2 className="section-title">📝 Posts</h2>
        {!loading && posts?.items?.length === 0 && (
          <div className="card p-8 text-center">
            <p className="text-sm text-slate-400">No posts yet.</p>
          </div>
        )}
        {(posts?.items ?? []).map((post) => (
          <PostCard
            key={post.id}
            post={post}
            onToggleReaction={() => {}}
            onUpdate={profile.isMe ? (updated) => setPosts((p) => p && ({ ...p, items: p.items.map((x) => x.id === updated.id ? updated : x) })) : undefined}
            onDelete={profile.isMe ? (postId) => setPosts((p) => p && ({ ...p, items: p.items.filter((x) => x.id !== postId) })) : undefined}
          />
        ))}
      </div>

      {/* Follow list modal */}
      {followListModal && (
        <FollowListModal
          title={followListModal.title}
          endpoint={followListModal.endpoint}
          meId={me?.id}
          onClose={() => setFollowListModal(null)}
        />
      )}
    </div>
  )
}

export default UserProfilePage
