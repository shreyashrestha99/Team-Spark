import axios from 'axios'

// Axios instance with base URL pointing to the relative /api (handled by Vite proxy in dev)
export const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
})

// Dynamically set auth token for outgoing requests without storing in global scope
export function setAuthToken(token: string | null) {
  if (token) {
    api.defaults.headers.common['Authorization'] = `Bearer ${token}`
  } else {
    delete api.defaults.headers.common['Authorization']
  }
}
