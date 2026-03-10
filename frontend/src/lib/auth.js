import { createContext, useContext, useEffect, useRef, useState, createElement } from 'react'
import { api } from './client'

const AuthContext = createContext(null)

const TOKEN_KEY = 'dogsocial_token'

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => sessionStorage.getItem(TOKEN_KEY) || '')
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(!!token)

  useEffect(() => {
    let cancelled = false
    async function fetchMe() {
      if (!token) {
        setUser(null)
        setLoading(false)
        return
      }
      try {
        const res = await api.get('/auth/me', { headers: { Authorization: `Bearer ${token}` } })
        if (!cancelled) setUser(res.data)
      } catch {
        sessionStorage.removeItem(TOKEN_KEY)
        if (!cancelled) {
          setToken('')
          setUser(null)
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    fetchMe()
    return () => { cancelled = true }
  }, [token])

  const login = (tokenValue, me) => {
    sessionStorage.setItem(TOKEN_KEY, tokenValue)
    setToken(tokenValue)
    setUser(me)
  }

  const logout = () => {
    sessionStorage.removeItem(TOKEN_KEY)
    setToken('')
    setUser(null)
  }

  const value = { token, user, loading, login, logout }
  const logoutRef = useRef(logout)

  useEffect(() => {
    logoutRef.current = logout
  })

  useEffect(() => {
    window.__logout = () => logoutRef.current()
    return () => { delete window.__logout }
  }, [])

  return createElement(AuthContext.Provider, { value }, children)
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider')
  return ctx
}
