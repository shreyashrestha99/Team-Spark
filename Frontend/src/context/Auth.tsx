import { createContext, useContext, useState, useEffect, type ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { api, setAuthToken } from '../services/api'

export interface UserProfile {
  userId: string
  fullName: string
  username: string
  email: string
  phoneNumber?: string
  role: string
  isActive?: boolean
}

interface LoginCredentials {
  usernameOrEmail: string
  password: string
}

interface AuthContextValue {
  user: UserProfile | null
  token: string | null
  loading: boolean
  login: (credentials: LoginCredentials) => Promise<UserProfile>
  logout: () => Promise<void>
  isAdmin: boolean
  isStudent: boolean
  isLecturer: boolean
}

const AuthContext = createContext<AuthContextValue | null>(null)

/**
 * Where each role lands after signing in. Staff have no personal timetable,
 * so they stay on the public home page.
 */
export function homePathFor(role?: string): string {
  const upper = role?.toUpperCase() || ''
  if (upper.includes('ADMIN')) return '/admin/dashboard'
  if (upper.includes('STUDENT')) return '/student/dashboard'
  if (upper.includes('TEACHER')) return '/teacher/dashboard'
  return '/'
}

const SESSION_TOKEN_KEY = 'rte_session_token'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserProfile | null>(null)
  const [token, setToken] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  // Initialize and verify existing session without storing passwords or permanent disk secrets
  useEffect(() => {
    async function initSession() {
      try {
        const storedToken = sessionStorage.getItem(SESSION_TOKEN_KEY)
        if (storedToken) {
          setToken(storedToken)
          setAuthToken(storedToken)

          // Verify token validity with backend
          const res = await api.get('/auth/me')
          if (res.data?.success && res.data?.data) {
            setUser(res.data.data)
          } else {
            // Token was invalid or expired
            sessionStorage.removeItem(SESSION_TOKEN_KEY)
            setAuthToken(null)
            setToken(null)
            setUser(null)
          }
        }
      } catch {
        sessionStorage.removeItem(SESSION_TOKEN_KEY)
        setAuthToken(null)
        setToken(null)
        setUser(null)
      } finally {
        setLoading(false)
      }
    }

    initSession()
  }, [])

  // Functional login integrated directly with Spring Boot backend
  const login = async (credentials: LoginCredentials): Promise<UserProfile> => {
    try {
      const response = await api.post('/auth/login', {
        usernameOrEmail: credentials.usernameOrEmail.trim(),
        password: credentials.password,
      })

      const data = response.data?.data
      if (!data || !data.token) {
        throw new Error(response.data?.message || 'Invalid credentials or server response')
      }

      const receivedToken = data.token
      const receivedUser: UserProfile = data.user

      // Secure handling: keep in memory and session scope only (cleared on browser/tab close)
      // Never store sensitive passwords or long-term secrets in localStorage
      setToken(receivedToken)
      sessionStorage.setItem(SESSION_TOKEN_KEY, receivedToken)
      setAuthToken(receivedToken)
      setUser(receivedUser)

      return receivedUser
    } catch (err: any) {
      const message =
        err.response?.data?.message ||
        err.response?.data?.errors?.[0] ||
        err.message ||
        'Authentication failed. Please check your credentials.'
      throw new Error(message)
    }
  }

  // Functional logout communicating with backend and clearing all sensitive credentials
  const logout = async () => {
    try {
      if (token) {
        await api.post('/auth/logout').catch(() => {
          // Ignore network errors during logout to guarantee clean client-side exit
        })
      }
    } finally {
      // Always purge all session information
      sessionStorage.removeItem(SESSION_TOKEN_KEY)
      setAuthToken(null)
      setToken(null)
      setUser(null)
    }
  }

  const role = user?.role?.toUpperCase() || ''
  const isAdmin = role.includes('ADMIN')
  const isStudent = role.includes('STUDENT')
  const isLecturer = role.includes('TEACHER') || role.includes('LECTURER') || role.includes('STAFF')

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        login,
        logout,
        isAdmin,
        isStudent,
        isLecturer,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside <AuthProvider>')
  return ctx
}

export function RequireRole({
  roles,
  children,
}: {
  roles: string[]
  children: ReactNode
}) {
  const { user, loading } = useAuth()

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-[#F8FAFC]">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-[#2B6BF3] border-t-transparent" />
      </div>
    )
  }

  if (!user) return <Navigate to="/login" replace />

  const userRole = user.role.toUpperCase()
  const hasRole = roles.some((r) => userRole.includes(r.toUpperCase()))
  if (!hasRole) return <Navigate to="/" replace />

  return <>{children}</>
}
