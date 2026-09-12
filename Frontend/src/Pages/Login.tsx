import { useState, useEffect } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import {
  User,
  Briefcase,
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
import { useAuth } from '../context/Auth'
import { IslingtonLogo } from '../components/IslingtonLogo'

type Tab = 'USER' | 'ADMIN'

const REMEMBERED_EMAIL_KEY = 'rte_remembered_email'

export default function LoginPage() {
  const navigate = useNavigate()
  const { login, user, isAdmin } = useAuth()

  const [tab, setTab] = useState<Tab>('ADMIN')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [remember, setRemember] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  // Redirect if already logged in (admin goes to dashboard)
  useEffect(() => {
    if (user) {
      if (isAdmin) {
        navigate('/admin/dashboard', { replace: true })
      } else {
        navigate('/', { replace: true })
      }
    }
  }, [user, isAdmin, navigate])

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

      // Admin goes to dashboard, others go home
      const userRole = loggedInUser?.role?.toUpperCase() || ''
      if (userRole.includes('ADMIN')) {
        navigate('/admin/dashboard', { replace: true })
      } else {
        navigate('/', { replace: true })
      }
    } catch (err: any) {
      setError(err.message || 'Could not sign you in. Please check your credentials.')
    } finally {
      setSubmitting(false)
    }
  }

  // Helper function to quickly fill admin credentials for testing
  const fillAdminCredentials = () => {
    setTab('ADMIN')
    setEmail('admin')
    setPassword('admin123')
    setError(null)
  }

  return (
    <div className="relative min-h-screen overflow-hidden bg-gradient-to-br from-[#F5F8FE] via-[#FAFCFF] to-[#EDF3FC] px-4 py-12">
      {/* Background ambient lighting and architectural watermark */}
      <BackgroundDecor />

      <div className="relative mx-auto w-full max-w-[560px]">
        {/* Top Header & Logo */}
        <header className="mb-9 text-center">
          <div className="mb-6 flex justify-center">
            <IslingtonLogo size="lg" />
          </div>
          <h1 className="mx-auto max-w-[480px] text-[28px] font-bold leading-[1.25] tracking-tight text-[#1B2A4A] sm:text-[32px]">
            Academic Scheduling &amp; Resource Management Platform
          </h1>
          <p className="mt-3.5 flex flex-wrap items-center justify-center gap-x-3 gap-y-1 text-[14px] text-[#7C8DB0] sm:text-[15px]">
            <span>Smarter Scheduling</span>
            <span aria-hidden="true" className="text-[#B9C6DE]">
              •
            </span>
            <span>Better Resource Utilization</span>
            <span aria-hidden="true" className="text-[#B9C6DE]">
              •
            </span>
            <span>A Connected Campus</span>
          </p>
        </header>

        {/* Login Card */}
        <div className="rounded-[24px] border border-[#EDF1F7] bg-white p-7 shadow-[0_12px_45px_-10px_rgba(26,45,85,0.1)] sm:p-9">
          {/* Tab Switcher */}
          <div
            role="tablist"
            aria-label="Account type"
            className="mb-8 flex rounded-[14px] border border-[#E4EAF4] bg-[#F8FAFC] p-1"
          >
            <TabButton
              active={tab === 'USER'}
              onClick={() => {
                setTab('USER')
                setError(null)
              }}
              icon={<User className="h-5 w-5" strokeWidth={1.75} />}
              label="Student / Staff"
            />
            <TabButton
              active={tab === 'ADMIN'}
              onClick={() => {
                setTab('ADMIN')
                setError(null)
              }}
              icon={<Briefcase className="h-5 w-5" strokeWidth={1.75} />}
              label="Administrator"
            />
          </div>

          <div className="flex items-start justify-between">
            <div>
              <h2 className="text-[24px] font-bold tracking-tight text-[#1B2A4A] sm:text-[26px]">
                Login to Your Account
              </h2>
              <p className="mt-1.5 text-[14px] text-[#8496B5] sm:text-[15px]">
                {tab === 'ADMIN'
                  ? 'Manage timetables, venues, and academic resources.'
                  : 'Access your timetable, Exams, and academic resources.'}
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
            <Field label="Email Address" htmlFor="email">
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
                placeholder={
                  tab === 'ADMIN'
                    ? 'e.g. admin or admin@islingtoncollege.edu.np'
                    : 'e.g. student@islington.edu.np'
                }
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

              <Link
                to="/"
                className="text-[14px] font-medium text-[#2563EB] hover:underline"
              >
                Back to Home
              </Link>
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
        <footer className="mt-8 text-center text-[13px] leading-relaxed text-[#9AA9C4]">
          <p>Islington College &nbsp;|&nbsp; RTE Department</p>
          <p className="mt-1">
            Automated Academic Scheduling &amp; Resource Management Platform
          </p>
        </footer>
      </div>
    </div>
  )
}

function TabButton({
  active,
  onClick,
  icon,
  label,
}: {
  active: boolean
  onClick: () => void
  icon: React.ReactNode
  label: string
}) {
  return (
    <button
      type="button"
      role="tab"
      aria-selected={active}
      onClick={onClick}
      className={`flex flex-1 items-center justify-center gap-2.5 rounded-[12px] px-4 py-3 text-[14px] font-medium transition cursor-pointer ${
        active
          ? 'bg-[#2563EB] text-white shadow-sm'
          : 'bg-transparent text-[#64748B] hover:text-[#1B2A4A]'
      }`}
    >
      {icon}
      <span>{label}</span>
    </button>
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

function BackgroundDecor() {
  return (
    <>
      <div className="pointer-events-none absolute -left-32 -top-32 h-[420px] w-[420px] rounded-full bg-[#DCE8FB] opacity-50 blur-3xl" />
      <div className="pointer-events-none absolute -bottom-40 -right-32 h-[460px] w-[460px] rounded-full bg-[#DDE8FA] opacity-60 blur-3xl" />

      {/* Clock tower campus architecture vector watermark */}
      <svg
        className="pointer-events-none absolute bottom-0 left-0 h-[320px] w-[320px] text-[#CBD5E1] opacity-35"
        viewBox="0 0 240 240"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.2"
        aria-hidden="true"
      >
        <rect x="20" y="70" width="46" height="150" />
        <rect x="32" y="46" width="22" height="24" />
        <circle cx="43" cy="58" r="7" />
        <path d="M43 54v4l3 2" />
        <path d="M32 46l11-14 11 14" />
        <rect x="66" y="108" width="120" height="112" />
        <path d="M66 108h120" />
        <path d="M66 138h120M66 168h120M66 198h120" />
        <rect x="80" y="118" width="16" height="14" />
        <rect x="110" y="118" width="16" height="14" />
        <rect x="140" y="118" width="16" height="14" />
        <rect x="80" y="148" width="16" height="14" />
        <rect x="110" y="148" width="16" height="14" />
        <rect x="140" y="148" width="16" height="14" />
        <rect x="80" y="178" width="16" height="14" />
        <rect x="110" y="178" width="16" height="14" />
        <rect x="140" y="178" width="16" height="14" />
        <rect x="30" y="96" width="14" height="16" />
        <rect x="30" y="128" width="14" height="16" />
        <rect x="30" y="160" width="14" height="16" />
        <path d="M0 220h240" />
      </svg>
    </>
  )
}
