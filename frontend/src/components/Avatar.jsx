import { useState } from 'react'
import { apiBaseUrl } from '../lib/client'

function Avatar({ userId, email, size = 'md', className = '', version }) {
  const [failed, setFailed] = useState(false)
  const q = version != null ? `?t=${version}` : ''
  const src = `${apiBaseUrl}/users/${userId}/avatar${q}`
  const sizeClasses = {
    sm: 'h-8 w-8 text-xs',
    md: 'h-10 w-10 text-sm',
    lg: 'h-16 w-16 text-lg',
  }
  const s = sizeClasses[size] || sizeClasses.md

  function initials(e) {
    if (!e || typeof e !== 'string') return '?'
    const parts = e.trim().split(/@|\.|\s+/).filter(Boolean)
    if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase().slice(0, 2)
    return e.slice(0, 2).toUpperCase()
  }

  if (failed) {
    return (
      <div
        className={`flex shrink-0 items-center justify-center rounded-full bg-pink-200 font-medium text-pink-800 ${s} ${className}`}
        title={email}
      >
        {initials(email)}
      </div>
    )
  }

  return (
    <img
      src={src}
      alt=""
      className={`shrink-0 rounded-full object-cover ${s} ${className}`}
      onError={() => setFailed(true)}
    />
  )
}

export default Avatar
