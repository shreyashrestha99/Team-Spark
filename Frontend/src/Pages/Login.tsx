import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Mail,
  Lock,
  Eye,
  EyeOff,
  Check,
  ArrowRight,
  Loader2,
  AlertCircle,
  KeyRound,
} from 'lucide-react'
import { homePathFor, useAuth } from '../context/Auth'
import { IslingtonLogo } from '../components/IslingtonLogo'

const REMEMBERED_EMAIL_KEY = 'rte_remembered_email'

export default function LoginPage() {
  const navigate = useNavigate()
  const { login, user } = useAuth()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [remember, setRemember] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  // Already signed in, so go straight to that role's portal
  useEffect(() => {
    if (user) {
      navigate(homePathFor(user.role), { replace: true })
    }
  }, [user, navigate])

  // Restore remembered email on mount (purely non-sensitive convenience data)
  useEffect(() => {
    const savedEmail = localStorage.getItem(REMEMBERED_EMAIL_KEY)
    if (savedEmail) {
      setEmail(savedEmail)
      setRemember(true)
    }
  }, [])

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)

    const cleanEmail = email.trim()
    if (!cleanEmail) {
      return setError('Please enter your username or email address')
    }
    if (!password) {
      return setError('Please enter your password')
    }

    setSubmitting(true)
    try {
      // Connect to Spring Boot backend via AuthContext
      const loggedInUser = await login({
        usernameOrEmail: cleanEmail,
        password,
      })

      // Handle "Remember me" purely for non-sensitive email/username
      if (remember) {
        localStorage.setItem(REMEMBERED_EMAIL_KEY, cleanEmail)
      } else {
        localStorage.removeItem(REMEMBERED_EMAIL_KEY)
      }

      // Each role lands on its own portal
      navigate(homePathFor(loggedInUser?.role), { replace: true })
    } catch (err: any) {
      setError(err.message || 'Could not sign you in. Please check your credentials.')
    } finally {
      setSubmitting(false)
    }
  }

  // Helper function to quickly fill admin credentials for testing
  const fillAdminCredentials = () => {
    setEmail('admin')
    setPassword('admin123')
    setError(null)
  }

  return (
    <div className="relative min-h-screen overflow-hidden bg-[#0F1E3D] px-4 py-12">
      {/* Islington College campus behind a dark wash so the card stays readable */}
      <div
        className="pointer-events-none absolute inset-0 bg-cover bg-center"
        style={{ backgroundImage: "url('/islington-campus.jpg')" }}
        aria-hidden="true"
      />
      <div
        className="pointer-events-none absolute inset-0 bg-gradient-to-b from-[#0F1E3D]/80 via-[#0F1E3D]/60 to-[#0F1E3D]/85"
        aria-hidden="true"
      />

      <div className="relative mx-auto w-full max-w-[560px]">
        {/* Top Header & Logo */}
        <header className="mb-9 text-center">
          <div className="mb-6 flex justify-center">
            <div className="rounded-2xl bg-white/95 px-5 py-3 shadow-lg">
              <IslingtonLogo size="lg" />
            </div>
          </div>
          <h1 className="mx-auto max-w-[480px] text-[28px] font-bold leading-[1.25] tracking-tight text-white drop-shadow sm:text-[32px]">
            Academic Scheduling &amp; Resource Management Platform
          </h1>
          <p className="mt-3.5 flex flex-wrap items-center justify-center gap-x-3 gap-y-1 text-[14px] text-[#D6E2F5] sm:text-[15px]">
            <span>Smarter Scheduling</span>
            <span aria-hidden="true" className="text-[#8FA6CC]">
              •
            </span>
            <span>Better Resource Utilization</span>
            <span aria-hidden="true" className="text-[#8FA6CC]">
              •
            </span>
            <span>A Connected Campus</span>
          </p>
        </header>

        {/* Login Card */}
        <div className="rounded-[24px] border border-[#EDF1F7] bg-white p-7 shadow-[0_12px_45px_-10px_rgba(26,45,85,0.1)] sm:p-9">

          <div className="flex items-start justify-between">
            <div>
              <h2 className="text-[24px] font-bold tracking-tight text-[#1B2A4A] sm:text-[26px]">
                Login to Your Account
              </h2>
              <p className="mt-1.5 text-[14px] text-[#8496B5] sm:text-[15px]">
                Students, lecturers and administrators all sign in here.
              </p>
            </div>

            {/* Quick Demo Pre-fill Pill for testing */}
            <button
              type="button"
              onClick={fillAdminCredentials}
              className="inline-flex items-center gap-1.5 rounded-lg border border-[#DBEAFE] bg-[#EFF6FF] px-2.5 py-1 text-xs font-semibold text-[#2563EB] transition hover:bg-[#DBEAFE]"
              title="Click to fill default admin credentials (admin / admin123)"
            >
              <KeyRound className="h-3.5 w-3.5" />
              <span>Fill Admin</span>
            </button>
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit} className="mt-7 space-y-5" noValidate>
            <Field label="Username or Email" htmlFor="email">
              <Mail
                className="pointer-events-none absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-[#94A3B8]"
                strokeWidth={1.75}
                aria-hidden="true"
              />
              <input
                id="email"
                type="text"
                autoComplete="username"
                value={email}
                onChange={(e) => {
                  setEmail(e.target.value)
                  setError(null)
                }}
                placeholder="e.g. your.username or name@islington.edu.np"
                className="h-[56px] w-full rounded-[14px] border border-[#E4EAF4] pl-12 pr-4 text-[15px] text-[#1B2A4A] outline-none transition placeholder:text-[#A9B6CC] focus:border-[#2563EB] focus:ring-4 focus:ring-[#2563EB]/10"
              />
            </Field>

            <Field label="Password" htmlFor="password">
              <Lock
                className="pointer-events-none absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-[#94A3B8]"
                strokeWidth={1.75}
                aria-hidden="true"
              />
              <input
                id="password"
                type={showPassword ? 'text' : 'password'}
                autoComplete="current-password"
                value={password}
                onChange={(e) => {
                  setPassword(e.target.value)
                  setError(null)
                }}
                placeholder="Enter your password"
                className="h-[56px] w-full rounded-[14px] border border-[#E4EAF4] pl-12 pr-12 text-[15px] text-[#1B2A4A] outline-none transition placeholder:text-[#A9B6CC] focus:border-[#2563EB] focus:ring-4 focus:ring-[#2563EB]/10"
              />
              <button
                type="button"
                onClick={() => setShowPassword((v) => !v)}
                aria-label={showPassword ? 'Hide password' : 'Show password'}
                className="absolute right-3 top-1/2 -translate-y-1/2 rounded-lg p-2 text-[#94A3B8] transition hover:text-[#1B2A4A] focus:outline-none focus-visible:ring-2 focus-visible:ring-[#2563EB]"
              >
                {showPassword ? (
                  <EyeOff className="h-5 w-5" strokeWidth={1.75} />
                ) : (
                  <Eye className="h-5 w-5" strokeWidth={1.75} />
                )}
              </button>
            </Field>

            {/* Remember Me Checkbox */}
            <div className="flex items-center justify-between">
              <label className="flex w-fit cursor-pointer select-none items-center gap-3">
                <span className="relative flex h-5 w-5 items-center justify-center">
                  <input
                    type="checkbox"
                    checked={remember}
                    onChange={(e) => setRemember(e.target.checked)}
                    className="peer absolute h-full w-full cursor-pointer opacity-0"
                  />
                  <span className="flex h-5 w-5 items-center justify-center rounded-[6px] border-2 border-[#CBD5E1] transition peer-checked:border-[#2563EB] peer-checked:bg-[#2563EB] peer-focus-visible:ring-2 peer-focus-visible:ring-[#2563EB] peer-focus-visible:ring-offset-2">
                    {remember && (
                      <Check className="h-3.5 w-3.5 text-white" strokeWidth={3} />
                    )}
                  </span>
                </span>
                <span className="text-[14px] text-[#475467]">Remember me</span>
              </label>
            </div>

            {/* Error Message */}
            {error && (
              <div className="flex items-center gap-2 rounded-xl bg-[#FEF2F2] p-3 text-[14px] text-[#DC2626]">
                <AlertCircle className="h-4 w-4 shrink-0" />
                <span>{error}</span>
              </div>
            )}

            {/* Submit Button */}
            <button
              type="submit"
              disabled={submitting}
              className="flex h-[56px] w-full items-center justify-center gap-2.5 rounded-[14px] bg-[#2563EB] text-[16px] font-semibold text-white shadow-md shadow-[#2563EB]/20 transition hover:bg-[#1D4ED8] hover:shadow-lg hover:shadow-[#2563EB]/30 focus:outline-none focus-visible:ring-4 focus-visible:ring-[#2563EB]/30 disabled:opacity-60 cursor-pointer"
            >
              {submitting ? (
                <>
                  <Loader2 className="h-5 w-5 animate-spin" strokeWidth={2} />
                  <span>Authenticating with Backend...</span>
                </>
              ) : (
                <>
                  <span>Log In</span>
                  <ArrowRight className="h-5 w-5" strokeWidth={2.25} />
                </>
              )}
            </button>
          </form>
        </div>

        {/* Footer */}
        <footer className="mt-8 text-center text-[13px] leading-relaxed text-[#C5D3EA]">
          <p>Islington College &nbsp;|&nbsp; RTE Department</p>
          <p className="mt-1">
            Automated Academic Scheduling &amp; Resource Management Platform
          </p>
        </footer>
      </div>
    </div>
  )
}


function Field({
  label,
  htmlFor,
  children,
}: {
  label: string
  htmlFor: string
  children: React.ReactNode
}) {
  return (
    <div>
      <label
        htmlFor={htmlFor}
        className="mb-2 block text-[14px] font-semibold text-[#1B2A4A]"
      >
        {label}
      </label>
      <div className="relative">{children}</div>
    </div>
  )
}
