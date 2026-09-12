import React, { useState, useRef, useEffect } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { IslingtonLogo } from './IslingtonLogo'
import { useAuth } from '../context/Auth'
import {
  User,
  ChevronDown,
  LogOut,
  Shield,
  GraduationCap,
  Briefcase,
  Home as HomeIcon,
  LayoutDashboard,
} from 'lucide-react'

export const Navbar: React.FC = () => {
  const { user, logout, isAdmin } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const [dropdownOpen, setDropdownOpen] = useState(false)
  const dropdownRef = useRef<HTMLDivElement>(null)

  const isHome = location.pathname === '/'

  // Close dropdown when clicking outside
  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setDropdownOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  const handleLogout = async () => {
    setDropdownOpen(false)
    await logout()
    navigate('/')
  }

  const getRoleBadge = (roleName?: string) => {
    if (!roleName) return null
    if (roleName.includes('ADMIN')) return 'Admin'
    if (roleName.includes('STUDENT')) return 'Student'
    if (roleName.includes('TEACHER')) return 'Lecturer'
    return 'Staff'
  }

  return (
    <header className="sticky top-0 z-40 w-full border-b border-[#EEF2F6] bg-white/95 backdrop-blur-md transition-all">
      <div className="mx-auto flex h-20 max-w-7xl items-center justify-between px-6 sm:px-8">
        {/* Logo */}
        <IslingtonLogo size="md" />

        {/* Navigation items */}
        <nav className="flex items-center gap-8 text-[15px] font-medium">
          <Link
            to="/"
            className={`relative flex items-center gap-1.5 py-2 transition-colors ${
              isHome
                ? 'font-semibold text-[#2563EB]'
                : 'text-[#475467] hover:text-[#1B2A4A]'
            }`}
          >
            <HomeIcon className="h-4 w-4" />
            <span>Home</span>
            {isHome && (
              <span className="absolute bottom-0 left-0 h-[2.5px] w-full rounded-full bg-[#2563EB]" />
            )}
          </Link>

          <a
            href="#about"
            className="py-2 text-[#475467] transition-colors hover:text-[#1B2A4A]"
            onClick={(e) => {
              e.preventDefault()
              const el = document.getElementById('features')
              el?.scrollIntoView({ behavior: 'smooth' })
            }}
          >
            About
          </a>

          {/* Admin Dashboard Link */}
          {user && isAdmin && (
            <Link
              to="/admin/dashboard"
              className={`relative flex items-center gap-1.5 py-2 transition-colors ${
                location.pathname === '/admin/dashboard'
                  ? 'font-semibold text-[#2563EB]'
                  : 'text-[#475467] hover:text-[#1B2A4A]'
              }`}
            >
              <LayoutDashboard className="h-4 w-4" />
              <span>Dashboard</span>
              {location.pathname === '/admin/dashboard' && (
                <span className="absolute bottom-0 left-0 h-[2.5px] w-full rounded-full bg-[#2563EB]" />
              )}
            </Link>
          )}

          <a
            href="#help"
            className="py-2 text-[#475467] transition-colors hover:text-[#1B2A4A]"
            onClick={(e) => {
              e.preventDefault()
              alert('For assistance, contact RTE Department: rte@islingtoncollege.edu.np')
            }}
          >
            Help
          </a>

          {/* User profile dropdown or Login button */}
          {user ? (
            <div className="relative" ref={dropdownRef}>
              <button
                type="button"
                onClick={() => setDropdownOpen(!dropdownOpen)}
                className="flex items-center gap-2.5 rounded-full border border-[#E2E8F0] bg-[#F8FAFC] py-1.5 pl-2 pr-3.5 text-[14px] font-semibold text-[#1E293B] transition hover:bg-[#F1F5F9] focus:outline-none focus:ring-2 focus:ring-[#2563EB]/20"
              >
                <div className="flex h-8 w-8 items-center justify-center rounded-full bg-[#2563EB] text-white">
                  <User className="h-4 w-4" />
                </div>
                <span>{getRoleBadge(user.role) || user.fullName || 'User'}</span>
                <ChevronDown
                  className={`h-4 w-4 text-[#64748B] transition-transform duration-200 ${
                    dropdownOpen ? 'rotate-180' : ''
                  }`}
                />
              </button>

              {/* Dropdown Card */}
              {dropdownOpen && (
                <div className="absolute right-0 mt-2 w-64 origin-top-right rounded-2xl border border-[#E2E8F0] bg-white p-2 shadow-xl ring-1 ring-black/5 animate-in fade-in zoom-in-95">
                  <div className="border-b border-[#F1F5F9] px-3 py-2.5">
                    <p className="text-xs font-medium uppercase tracking-wider text-[#94A3B8]">
                      Signed in as
                    </p>
                    <p className="truncate text-sm font-bold text-[#1E293B]">
                      {user.fullName || user.username}
                    </p>
                    <p className="truncate text-xs text-[#64748B]">{user.email}</p>
                    <div className="mt-2 inline-flex items-center gap-1 rounded-md bg-[#EFF6FF] px-2 py-0.5 text-[11px] font-semibold text-[#2563EB]">
                      {isAdmin ? (
                        <Shield className="h-3 w-3" />
                      ) : user.role.includes('STUDENT') ? (
                        <GraduationCap className="h-3 w-3" />
                      ) : (
                        <Briefcase className="h-3 w-3" />
                      )}
                      <span>{user.role}</span>
                    </div>
                  </div>

                  <div className="p-1">
                    <button
                      type="button"
                      onClick={handleLogout}
                      className="flex w-full items-center gap-2.5 rounded-xl px-3 py-2 text-left text-sm font-medium text-[#EF4444] transition hover:bg-[#FEF2F2]"
                    >
                      <LogOut className="h-4 w-4" />
                      <span>Log Out</span>
                    </button>
                  </div>
                </div>
              )}
            </div>
          ) : (
            <Link
              to="/login"
              className="flex items-center gap-2 rounded-xl bg-[#2563EB] px-5 py-2.5 text-[14px] font-semibold text-white shadow-sm transition hover:bg-[#1D4ED8] focus:outline-none focus:ring-4 focus:ring-[#2563EB]/20"
            >
              <User className="h-4 w-4" />
              <span>Log In</span>
            </Link>
          )}
        </nav>
      </div>
    </header>
  )
}
