import { useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api, getApiErrorMessage } from '../lib/client'
import ImageCropModal from '../components/ImageCropModal.jsx'

function CreatePostPage() {
  const [caption, setCaption] = useState('')
  const [image, setImage] = useState(null)       // final cropped Blob/File
  const [imagePreview, setImagePreview] = useState(null)  // preview of cropped result
  const [cropSrc, setCropSrc] = useState(null)   // original src for crop modal
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const fileRef = useRef(null)
  const navigate = useNavigate()

  // Open crop modal when user picks a file
  const handleImageChange = (e) => {
    const file = e.target.files?.[0]
    if (!file) return
    setCropSrc(URL.createObjectURL(file))
    if (fileRef.current) fileRef.current.value = ''
  }

  // Receive cropped blob from modal
  const handleCropDone = (blob) => {
    setImage(blob)
    setImagePreview(URL.createObjectURL(blob))
    setCropSrc(null)
  }

  const clearImage = () => {
    setImage(null)
    setImagePreview(null)
    setCropSrc(null)
    if (fileRef.current) fileRef.current.value = ''
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!caption.trim()) return
    setSubmitting(true)
    setError('')
    try {
      const formData = new FormData()
      formData.append('caption', caption.trim())
      if (image) formData.append('image', image)
      await api.post('/posts', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      navigate('/feed')
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to create post. Please try again.'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <>
    {cropSrc && (
      <ImageCropModal
        imageSrc={cropSrc}
        aspect={4 / 3}
        title="Crop post photo"
        onCrop={handleCropDone}
        onCancel={() => { setCropSrc(null); if (fileRef.current) fileRef.current.value = '' }}
      />
    )}
    <div className="mx-auto max-w-xl">
      <div className="card p-6">
        <h1 className="mb-1 text-2xl font-bold text-slate-900">New post</h1>
        <p className="mb-6 text-sm text-slate-500">
          Share what your pup is up to — up to 300 characters and an optional photo.
        </p>

        {error && <div className="error-banner mb-5">{error}</div>}

        <form onSubmit={handleSubmit} className="space-y-5">
          {/* Caption */}
          <div>
            <label className="label">Caption</label>
            <textarea
              required
              maxLength={300}
              rows={4}
              value={caption}
              onChange={(e) => setCaption(e.target.value)}
              placeholder="What's your dog up to?"
              className="input resize-none"
            />
            <div className={`mt-1.5 text-right text-[11px] font-medium ${caption.length > 270 ? 'text-amber-500' : 'text-slate-400'}`}>
              {caption.length} / 300
            </div>
          </div>

          {/* Photo */}
          <div>
            <label className="label">Photo (optional · max 5 MB)</label>
            {imagePreview ? (
              <div className="relative overflow-hidden rounded-xl border border-slate-200 bg-slate-50">
                <img
                  src={imagePreview}
                  alt="Preview"
                  className="max-h-72 w-full object-cover"
                />
                <button
                  type="button"
                  onClick={clearImage}
                  className="absolute right-3 top-3 flex h-7 w-7 items-center justify-center rounded-full bg-black/60 text-xs text-white hover:bg-black/80 transition"
                >
                  ✕
                </button>
              </div>
            ) : (
              <label className="flex cursor-pointer flex-col items-center justify-center gap-2 rounded-xl border-2 border-dashed border-slate-200 bg-slate-50 py-10 text-center hover:border-pink-300 hover:bg-pink-50/30 transition">
                <span className="text-3xl">📷</span>
                <span className="text-sm font-medium text-slate-500">Click to choose a photo</span>
                <span className="text-xs text-slate-400">JPEG, PNG, GIF, WebP — max 5 MB</span>
                <input
                  ref={fileRef}
                  type="file"
                  accept="image/jpeg,image/png,image/gif,image/webp"
                  className="hidden"
                  onChange={handleImageChange}
                />
              </label>
            )}
          </div>

          <button
            type="submit"
            disabled={submitting || !caption.trim()}
            className="btn-primary w-full"
          >
            {submitting ? 'Publishing…' : 'Publish post'}
          </button>
        </form>
      </div>
    </div>
    </>
  )
}

export default CreatePostPage
