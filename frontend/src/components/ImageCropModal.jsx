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

function getRadianAngle(deg) {
  return (deg * Math.PI) / 180
}

async function getCroppedBlob(imageSrc, pixelCrop, rotation = 0) {
  const image = await createImage(imageSrc)
  const canvas = document.createElement('canvas')
  const ctx = canvas.getContext('2d')

  const maxSide = Math.max(image.width, image.height)
  const safeArea = 2 * ((maxSide / 2) * Math.sqrt(2))

  canvas.width = safeArea
  canvas.height = safeArea

  ctx.translate(safeArea / 2, safeArea / 2)
  ctx.rotate(getRadianAngle(rotation))
  ctx.translate(-safeArea / 2, -safeArea / 2)

  ctx.drawImage(
    image,
    safeArea / 2 - image.width / 2,
    safeArea / 2 - image.height / 2,
  )

  const data = ctx.getImageData(0, 0, safeArea, safeArea)

  canvas.width = pixelCrop.width
  canvas.height = pixelCrop.height

  ctx.putImageData(
    data,
    Math.round(0 - safeArea / 2 + image.width / 2 - pixelCrop.x),
    Math.round(0 - safeArea / 2 + image.height / 2 - pixelCrop.y),
  )

  return new Promise((resolve) => canvas.toBlob(resolve, 'image/jpeg', 0.92))
}

/**
 * Modal crop UI with rotation support.
 * @param {string}   imageSrc       - Object URL of the original image
 * @param {number}   aspect         - Crop aspect ratio (e.g. 1, 4/3). undefined = free.
 * @param {string}   title          - Header label
 * @param {boolean}  showRotation   - Show rotation slider (default true)
 * @param {function} onCrop         - Called with the cropped Blob
 * @param {function} onCancel       - Called when user cancels
 */
function ImageCropModal({ imageSrc, aspect, title = 'Crop photo', showRotation = true, onCrop, onCancel }) {
  const [crop, setCrop] = useState({ x: 0, y: 0 })
  const [zoom, setZoom] = useState(1)
  const [rotation, setRotation] = useState(0)
  const [croppedAreaPixels, setCroppedAreaPixels] = useState(null)
  const [processing, setProcessing] = useState(false)

  const onCropComplete = useCallback((_area, pixels) => {
    setCroppedAreaPixels(pixels)
  }, [])

  const handleConfirm = async () => {
    if (!croppedAreaPixels) return
    setProcessing(true)
    try {
      const blob = await getCroppedBlob(imageSrc, croppedAreaPixels, rotation)
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
              Drag to reposition · zoom and rotate to adjust
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
            rotation={rotation}
            aspect={aspect}
            onCropChange={setCrop}
            onZoomChange={setZoom}
            onRotationChange={setRotation}
            onCropComplete={onCropComplete}
            style={{
              containerStyle: { borderRadius: 0 },
            }}
          />
        </div>

        {/* Controls */}
        <div className="space-y-3 border-t border-slate-100 px-5 py-4">
          {/* Zoom */}
          <div className="flex items-center gap-3">
            <span className="text-[11px] font-semibold text-slate-500 w-14 shrink-0">Zoom</span>
            <input
              type="range"
              min={1}
              max={3}
              step={0.01}
              value={zoom}
              onChange={(e) => setZoom(Number(e.target.value))}
              className="w-full accent-pink-500"
            />
          </div>

          {/* Rotation */}
          {showRotation && (
            <div className="flex items-center gap-3">
              <span className="text-[11px] font-semibold text-slate-500 w-14 shrink-0">Rotate</span>
              <input
                type="range"
                min={-180}
                max={180}
                step={1}
                value={rotation}
                onChange={(e) => setRotation(Number(e.target.value))}
                className="w-full accent-pink-500"
              />
              <span className="text-[11px] text-slate-400 w-10 text-right shrink-0">{rotation}°</span>
            </div>
          )}

          {/* Actions */}
          <div className="flex justify-end gap-2 pt-1">
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
