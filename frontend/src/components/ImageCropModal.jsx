import { useState, useCallback } from 'react'
import Cropper from 'react-easy-crop'

async function createImage(url) {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.addEventListener('load', () => resolve(img))
    img.addEventListener('error', (e) => reject(e))
    img.setAttribute('crossOrigin', 'anonymous')
    img.src = url
  })
}

async function getCroppedBlob(imageSrc, pixelCrop) {
  const image = await createImage(imageSrc)
  const canvas = document.createElement('canvas')
  canvas.width = pixelCrop.width
  canvas.height = pixelCrop.height
  const ctx = canvas.getContext('2d')
  ctx.drawImage(
    image,
    pixelCrop.x,
    pixelCrop.y,
    pixelCrop.width,
    pixelCrop.height,
    0,
    0,
    pixelCrop.width,
    pixelCrop.height,
  )
  return new Promise((resolve) => canvas.toBlob(resolve, 'image/jpeg', 0.92))
}

/**
 * Modal crop UI.
 * @param {string}   imageSrc  - Object URL of the original image
 * @param {number}   aspect    - Crop aspect ratio (e.g. 1 for square, 4/3, 16/9). Pass undefined for free.
 * @param {string}   title     - Header label
 * @param {function} onCrop    - Called with the cropped Blob
 * @param {function} onCancel  - Called when user cancels
 */
function ImageCropModal({ imageSrc, aspect, title = 'Crop photo', onCrop, onCancel }) {
  const [crop, setCrop] = useState({ x: 0, y: 0 })
  const [zoom, setZoom] = useState(1)
  const [croppedAreaPixels, setCroppedAreaPixels] = useState(null)
  const [processing, setProcessing] = useState(false)

  const onCropComplete = useCallback((_area, pixels) => {
    setCroppedAreaPixels(pixels)
  }, [])

  const handleConfirm = async () => {
    if (!croppedAreaPixels) return
    setProcessing(true)
    try {
      const blob = await getCroppedBlob(imageSrc, croppedAreaPixels)
      onCrop(blob)
    } finally {
      setProcessing(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 p-4">
      <div className="flex w-full max-w-lg flex-col rounded-2xl bg-white shadow-2xl">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-slate-100 px-5 py-4">
          <div>
            <h2 className="text-base font-bold text-slate-900">{title}</h2>
            <p className="mt-0.5 text-[11px] text-slate-400">
              Drag to reposition · scroll or use slider to zoom
            </p>
          </div>
          <button
            type="button"
            onClick={onCancel}
            className="flex h-8 w-8 items-center justify-center rounded-full text-slate-400 hover:bg-slate-100 hover:text-slate-600 transition"
          >
            ✕
          </button>
        </div>

        {/* Crop area */}
        <div className="relative bg-black" style={{ height: 320 }}>
          <Cropper
            image={imageSrc}
            crop={crop}
            zoom={zoom}
            aspect={aspect}
            onCropChange={setCrop}
            onZoomChange={setZoom}
            onCropComplete={onCropComplete}
            style={{
              containerStyle: { borderRadius: 0 },
            }}
          />
        </div>

        {/* Zoom slider + actions */}
        <div className="flex items-center justify-between gap-4 border-t border-slate-100 px-5 py-4">
          <div className="flex flex-1 items-center gap-3">
            <span className="text-xs text-slate-400">🔍</span>
            <input
              type="range"
              min={1}
              max={3}
              step={0.01}
              value={zoom}
              onChange={(e) => setZoom(Number(e.target.value))}
              className="w-full accent-pink-500"
            />
            <span className="text-xs text-slate-400">🔎</span>
          </div>
          <div className="flex shrink-0 gap-2">
            <button type="button" onClick={onCancel} className="btn-secondary btn-sm">
              Cancel
            </button>
            <button
              type="button"
              disabled={processing}
              onClick={handleConfirm}
              className="btn-primary btn-sm"
            >
              {processing ? 'Processing…' : 'Use crop'}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default ImageCropModal
