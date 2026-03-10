import axios from 'axios'

const baseURL =
  import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

export const apiBaseUrl = baseURL

export const api = axios.create({
  baseURL,
})

const TOKEN_KEY = 'dogsocial_token'

api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// On 401, clear token and notify auth so user is sent to login
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      sessionStorage.removeItem(TOKEN_KEY)
      if (typeof window.__logout === 'function') window.__logout()
    }
    return Promise.reject(err)
  }
)

/**
 * Get a user-friendly error message from an API error response.
 * Uses backend detail (e.g. exception message) when present for 500s.
 */
export function getApiErrorMessage(err, fallback = 'Something went wrong') {
  const d = err.response?.data
  if (!d) return err.message || fallback
  if (d.detail) return `${d.message || 'Error'}: ${d.detail}`
  return d.message || err.message || fallback
}

