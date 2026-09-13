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
} from 'lucide-react'
import { homePathFor, useAuth } from '../context/Auth'
import { IslingtonLogo } from '../components/IslingtonLogo'
import campusImage from '../assets/kumari-four.jpg'

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

  return (
    <div className="relative flex min-h-screen items-center justify-center overflow-hidden px-4 py-8">
      {/* Kumari Block at dusk, behind a deep navy wash so the card stays readable */}
      <img
        src={campusImage}
        alt=""
        aria-hidden="true"
        className="pointer-events-none absolute inset-0 h-full w-full object-cover"
      />
      <div
        className="pointer-events-none absolute inset-0 bg-gradient-to-br from-[#08152B]/88 via-[#0B1B34]/72 to-[#08152B]/92"
        aria-hidden="true"
      />

      <div className="relative w-full max-w-[430px]">
        {/* Brand mark and platform name */}
        <header className="mb-6 text-center">
          <div className="mb-4 inline-flex rounded-2xl bg-white px-5 py-3 shadow-lg shadow-black/20 ring-1 ring-white/10">
            <IslingtonLogo size="md" />
          </div>
          <h1 className="text-[19px] font-semibold leading-snug tracking-tight text-white">
            Academic Scheduling &amp; Resource Management
          </h1>
          <p className="mt-1.5 text-[13px] text-[#9DB3D4]">
            Routine, Timetable &amp; Examination Department
          </p>
        </header>

        {/* Sign-in card */}
        <div className="rounded-[20px] border border-white/60 bg-white p-7 shadow-[0_24px_60px_-15px_rgba(0,0,0,0.5)]">
          {/* Thin brand rule for a more formal, institutional feel */}
          <div className="mb-5 flex items-center gap-3">
            <span className="h-[3px] w-9 rounded-full bg-[#D92D20]" aria-hidden="true" />
            <div>
              <h2 className="text-[20px] font-bold leading-tight tracking-tight text-[#1B2A4A]">
                Sign in
              </h2>
              <p className="mt-0.5 text-[13px] text-[#8496B5]">
                Students, lecturers and administrators.
              </p>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4" noValidate>
            <Field label="Username or Email" htmlFor="email">
              <Mail
                className="pointer-events-none absolute left-4 top-1/2 h-[18px] w-[18px] -translate-y-1/2 text-[#94A3B8]"
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
                placeholder="name@islington.edu.np"
                className={INPUT_CLASS}
              />
            </Field>

            <Field label="Password" htmlFor="password">
              <Lock
                className="pointer-events-none absolute left-4 top-1/2 h-[18px] w-[18px] -translate-y-1/2 text-[#94A3B8]"
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
                className={`${INPUT_CLASS} pr-12`}
              />
              <button
                type="button"
                onClick={() => setShowPassword((v) => !v)}
                aria-label={showPassword ? 'Hide password' : 'Show password'}
                className="absolute right-2.5 top-1/2 -translate-y-1/2 rounded-lg p-2 text-[#94A3B8] transition hover:text-[#1B2A4A] focus:outline-none focus-visible:ring-2 focus-visible:ring-[#2563EB] cursor-pointer"
              >
                {showPassword ? (
                  <EyeOff className="h-[18px] w-[18px]" strokeWidth={1.75} />
                ) : (
                  <Eye className="h-[18px] w-[18px]" strokeWidth={1.75} />
                )}
              </button>
            </Field>

            <label className="flex w-fit cursor-pointer select-none items-center gap-2.5">
              <span className="relative flex h-[18px] w-[18px] items-center justify-center">
                <input
                  type="checkbox"
                  checked={remember}
                  onChange={(e) => setRemember(e.target.checked)}
                  className="peer absolute h-full w-full cursor-pointer opacity-0"
                />
                <span className="flex h-[18px] w-[18px] items-center justify-center rounded-[5px] border-2 border-[#CBD5E1] transition peer-checked:border-[#2563EB] peer-checked:bg-[#2563EB] peer-focus-visible:ring-2 peer-focus-visible:ring-[#2563EB] peer-focus-visible:ring-offset-2">
                  {remember && <Check className="h-3 w-3 text-white" strokeWidth={3} />}
                </span>
              </span>
              <span className="text-[13px] text-[#475467]">Remember me</span>
            </label>

            {error && (
              <div className="flex items-start gap-2 rounded-xl bg-[#FEF2F2] px-3 py-2.5 text-[13px] text-[#DC2626]">
                <AlertCircle className="mt-px h-4 w-4 shrink-0" />
                <span>{error}</span>
              </div>
            )}

            <button
              type="submit"
              disabled={submitting}
              className="flex h-[50px] w-full items-center justify-center gap-2 rounded-[12px] bg-[#2563EB] text-[15px] font-semibold text-white shadow-md shadow-[#2563EB]/25 transition hover:bg-[#1D4ED8] hover:shadow-lg hover:shadow-[#2563EB]/30 focus:outline-none focus-visible:ring-4 focus-visible:ring-[#2563EB]/30 disabled:opacity-60 cursor-pointer"
            >
              {submitting ? (
                <>
                  <Loader2 className="h-[18px] w-[18px] animate-spin" strokeWidth={2} />
                  <span>Signing in…</span>
                </>
              ) : (
                <>
                  <span>Sign In</span>
                  <ArrowRight className="h-[18px] w-[18px]" strokeWidth={2.25} />
                </>
              )}
            </button>
          </form>
        </div>

        <footer className="mt-5 text-center text-[12px] text-[#8FA6CC]">
          Islington College &nbsp;·&nbsp; Kathmandu, Nepal
        </footer>
      </div>
    </div>
  )
}

// Shared styling for the two credential inputs
const INPUT_CLASS =
  'h-[50px] w-full rounded-[12px] border border-[#E4EAF4] bg-[#FBFCFE] pl-11 pr-4 text-[14px] text-[#1B2A4A] outline-none transition placeholder:text-[#A9B6CC] focus:border-[#2563EB] focus:bg-white focus:ring-4 focus:ring-[#2563EB]/10'

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
        className="mb-1.5 block text-[13px] font-semibold text-[#1B2A4A]"
      >
        {label}
      </label>
      <div className="relative">{children}</div>
    </div>
  )
}
