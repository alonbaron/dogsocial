import { Navigate } from 'react-router-dom'
import { useAuth } from '../lib/auth'

export function ProtectedRoute({ children }) {
  const { user, loading } = useAuth()
  if (loading) {
    return (
      <div className="flex h-[60vh] items-center justify-center text-slate-500">
        Loading...
      </div>
    )
  }
  if (!user) return <Navigate to="/login" replace />
  return children
}

export function PublicOnly({ children }) {
  const { user, loading } = useAuth()
  if (loading) return null
  if (user) return <Navigate to="/feed" replace />
  return children
}
