import { useState } from 'react'
import { apiBaseUrl } from '../lib/client'

function Avatar({ userId, email, username, size = 'md', className = '', version }) {
  const [failedSrc, setFailedSrc] = useState(null)
  const q = version != null ? `?t=${version}` : ''
  const src = `${apiBaseUrl}/users/${userId}/avatar${q}`
  const failed = failedSrc === src

  const sizeClasses = {
    sm: 'h-8 w-8 text-xs',
    md: 'h-10 w-10 text-sm',
    lg: 'h-16 w-16 text-lg',
  }
  const s = sizeClasses[size] || sizeClasses.md

  function initials() {
    if (username) return username.slice(0, 2).toUpperCase()
    if (email && typeof email === 'string') {
      const parts = email.trim().split(/@|\.|\s+/).filter(Boolean)
      if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase()
      return email.slice(0, 2).toUpperCase()
    }
    return '?'
  }

  if (failed) {
    return (
      <div
        className={`flex shrink-0 items-center justify-center rounded-full bg-pink-200 font-semibold text-pink-800 ${s} ${className}`}
        title={username ? `@${username}` : email}
      >
        {initials()}
      </div>
    )
  }

  return (
    <img
      src={src}
      alt=""
      className={`shrink-0 rounded-full object-cover ${s} ${className}`}
      onError={() => setFailedSrc(src)}
    />
  )
}

export default Avatar
