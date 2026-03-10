import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { api, getApiErrorMessage } from '../lib/client'
import PostCard from '../components/PostCard.jsx'
import Avatar from '../components/Avatar.jsx'

function UserProfilePage() {
  const { userId } = useParams()
  const [profile, setProfile] = useState(null)
  const [posts, setPosts] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [uploading, setUploading] = useState(false)
  const [uploadError, setUploadError] = useState('')
  const [avatarKey, setAvatarKey] = useState(0)

  useEffect(() => {
    let cancelled = false
    async function load() {
      setLoading(true)
      setError('')
      try {
        const [profileRes, postsRes] = await Promise.all([
          api.get(`/users/${userId}`),
          api.get('/feed', { params: { page: 0, size: 10 } }),
        ])
        if (!cancelled) {
          setProfile(profileRes.data)
          setPosts(postsRes.data)
        }
      } catch (err) {
        if (!cancelled) {
          setError(getApiErrorMessage(err, 'Failed to load profile.'))
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    load()
    return () => {
      cancelled = true
    }
  }, [userId])

  const handleAvatarChange = async (e) => {
    const file = e.target.files?.[0]
    if (!file) return
    setUploadError('')
    setUploading(true)
    try {
      const formData = new FormData()
      formData.append('file', file)
      await api.post('/users/me/avatar', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      setProfile((prev) => prev && { ...prev })
      setAvatarKey(Date.now())
    } catch (err) {
      setUploadError(getApiErrorMessage(err, 'Upload failed.'))
    } finally {
      setUploading(false)
      e.target.value = ''
    }
  }

  return (
    <div className="space-y-4">
      {error && (
        <div className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
          {error}
        </div>
      )}

      {profile && (
        <section className="rounded-2xl bg-white p-4 shadow-sm ring-1 ring-slate-100">
          <div className="flex flex-wrap items-start gap-4">
            <div className="relative">
              <Avatar userId={profile.id} email={profile.email} size="lg" version={avatarKey} />
              {profile.isMe && (
                <label className="absolute bottom-0 right-0 flex h-7 w-7 cursor-pointer items-center justify-center rounded-full bg-pink-500 text-white shadow transition hover:bg-pink-600">
                  <span className="text-xs">+</span>
                  <input
                    type="file"
                    accept="image/jpeg,image/png,image/gif,image/webp"
                    className="hidden"
                    disabled={uploading}
                    onChange={handleAvatarChange}
                  />
                </label>
              )}
            </div>
            <div>
              <h1 className="text-lg font-semibold text-slate-900">
                {profile.email}
              </h1>
              <p className="mt-1 text-xs text-slate-500">
                Joined {profile.createdAt && new Date(profile.createdAt).toLocaleDateString()}
              </p>
              <p className="mt-2 text-xs text-slate-500">
                Followers: <span className="font-medium">{profile.followersCount}</span>{' '}
                · Following:{' '}
                <span className="font-medium">{profile.followingCount}</span>
              </p>
              {uploadError && (
                <p className="mt-2 text-xs text-red-600">{uploadError}</p>
              )}
            </div>
          </div>
        </section>
      )}

      <section className="space-y-3">
        <h2 className="text-sm font-semibold text-slate-900">Recent posts</h2>
        {loading && (
          <div className="rounded-2xl bg-white p-4 text-sm text-slate-500 shadow-sm">
            Loading posts…
          </div>
        )}
        {posts?.items.map((post) => (
          <PostCard
            key={post.id}
            post={post}
            onToggleReaction={() => {}}
          />
        ))}
      </section>
    </div>
  )
}

export default UserProfilePage

