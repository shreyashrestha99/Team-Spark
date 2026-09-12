import React, { useState, useEffect, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/Auth'
import { api } from '../services/api'
import { IslingtonLogo } from '../components/IslingtonLogo'
import {
  Users,
  GraduationCap,
  Briefcase,
  UserCog,
  Plus,
  Search,
  ChevronLeft,
  ChevronRight,
  ArrowUpDown,
  ArrowUp,
  ArrowDown,
  X,
  Loader2,
  AlertCircle,
  CheckCircle,
  Eye,
  UserPlus,
  LayoutDashboard,
  Mail,
  Phone,
  Shield,
  RefreshCw,
  LogOut,
  DoorOpen,
  BarChart3,
  User,
} from 'lucide-react'

// ─── Types ────────────────────────────────────────────────────────────────────

interface UserProfile {
  userId: string
  fullName: string
  username: string
  email: string
  phoneNumber?: string
  role: string
  isActive?: boolean
}

interface PageResponse {
  content: UserProfile[]
  pageNumber: number
  pageSize: number
  totalElements: number
  totalPages: number
  last: boolean
}

interface DashboardStats {
  students: number
  teachers: number
  staff: number
  rooms: number
  conflicts: number
}

type ActiveTab = 'STUDENTS' | 'TEACHERS' | 'STAFF'
type SortField = 'fullName' | 'username' | 'email' | 'createdAt'
type SortDir = 'asc' | 'desc'

// ─── Sidebar Navigation Items ─────────────────────────────────────────────────

const sidebarItems = [
  { id: 'STUDENTS' as ActiveTab, label: 'Students', icon: GraduationCap, color: 'text-[#2563EB]', bg: 'bg-[#EFF6FF]' },
  { id: 'TEACHERS' as ActiveTab, label: 'Teachers', icon: Briefcase, color: 'text-[#059669]', bg: 'bg-[#ECFDF5]' },
  { id: 'STAFF' as ActiveTab, label: 'Staff', icon: UserCog, color: 'text-[#7C3AED]', bg: 'bg-[#F5F3FF]' },
]

// ─── Main Component ───────────────────────────────────────────────────────────

export default function AdminDashboard() {
  const { user, isAdmin, loading: authLoading, logout } = useAuth()
  const navigate = useNavigate()

  // Dashboard stats
  const [stats, setStats] = useState<DashboardStats | null>(null)
  const [statsLoading, setStatsLoading] = useState(true)

  // Users table
  const [activeTab, setActiveTab] = useState<ActiveTab>('STUDENTS')
  const [users, setUsers] = useState<UserProfile[]>([])
  const [page, setPage] = useState(0)
  const [pageSize] = useState(10)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [sortBy, setSortBy] = useState<SortField>('createdAt')
  const [sortDir, setSortDir] = useState<SortDir>('desc')
  const [search, setSearch] = useState('')
  const [searchInput, setSearchInput] = useState('')
  const [tableLoading, setTableLoading] = useState(false)

  // Add user modal
  const [showAddModal, setShowAddModal] = useState(false)
  const [addForm, setAddForm] = useState({
    fullName: '',
    username: '',
    email: '',
    password: '',
    phoneNumber: '',
  })
  const [addLoading, setAddLoading] = useState(false)
  const [addError, setAddError] = useState<string | null>(null)
  const [addSuccess, setAddSuccess] = useState<string | null>(null)

  // View user modal
  const [viewUser, setViewUser] = useState<UserProfile | null>(null)

  // Sidebar collapsed state
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false)

  // Redirect non-admins
  useEffect(() => {
    if (!authLoading && (!user || !isAdmin)) {
      navigate('/', { replace: true })
    }
  }, [authLoading, user, isAdmin, navigate])

  // Handle logout
  const handleLogout = async () => {
    await logout()
    navigate('/login', { replace: true })
  }

  // Fetch dashboard stats
  const fetchStats = useCallback(async () => {
    setStatsLoading(true)
    try {
      const res = await api.get('/admin/users/stats')
      if (res.data?.success && res.data?.data) {
        setStats(res.data.data)
      }
    } catch {
      // Silently handle
    } finally {
      setStatsLoading(false)
    }
  }, [])

  useEffect(() => {
    if (user && isAdmin) {
      fetchStats()
    }
  }, [user, isAdmin, fetchStats])

  // Map tab to backend role
  const tabToRole = (tab: ActiveTab): string => {
    switch (tab) {
      case 'STUDENTS': return 'ROLE_STUDENT'
      case 'TEACHERS': return 'ROLE_TEACHER'
      case 'STAFF': return 'ROLE_STAFF'
    }
  }

  // Fetch users
  const fetchUsers = useCallback(async () => {
    setTableLoading(true)
    try {
      const params: Record<string, string | number> = {
        role: tabToRole(activeTab),
        page,
        size: pageSize,
        sortBy,
        sortDirection: sortDir,
      }
      if (search.trim()) params.search = search.trim()

      const res = await api.get('/admin/users', { params })
      if (res.data?.success && res.data?.data) {
        const pageData: PageResponse = res.data.data
        setUsers(pageData.content || [])
        setTotalPages(pageData.totalPages || 0)
        setTotalElements(pageData.totalElements || 0)
      }
    } catch {
      setUsers([])
      setTotalPages(0)
      setTotalElements(0)
    } finally {
      setTableLoading(false)
    }
  }, [activeTab, page, pageSize, sortBy, sortDir, search])

  useEffect(() => {
    if (user && isAdmin) fetchUsers()
  }, [user, isAdmin, fetchUsers])

  // Reset page on tab/search change
  useEffect(() => { setPage(0) }, [activeTab, search])

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    setSearch(searchInput)
  }

  const toggleSort = (field: SortField) => {
    if (sortBy === field) {
      setSortDir((prev) => (prev === 'asc' ? 'desc' : 'asc'))
    } else {
      setSortBy(field)
      setSortDir('asc')
    }
  }

  // Handle add user
  const handleAddUser = async (e: React.FormEvent) => {
    e.preventDefault()
    setAddError(null)
    setAddSuccess(null)

    // Validate all required fields
    if (!addForm.fullName.trim()) return setAddError('Full name is required')
    if (addForm.fullName.trim().length > 100) return setAddError('Full name must be under 100 characters')
    if (!addForm.username.trim()) return setAddError('Username is required')
    if (addForm.username.trim().length < 3) return setAddError('Username must be at least 3 characters')
    if (addForm.username.trim().length > 50) return setAddError('Username must be under 50 characters')
    if (!addForm.email.trim()) return setAddError('Email is required')
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(addForm.email.trim())) return setAddError('Please enter a valid email address')
    if (!addForm.password) return setAddError('Password is required')
    if (addForm.password.length < 6) return setAddError('Password must be at least 6 characters')

    setAddLoading(true)
    try {
      const endpoint =
        activeTab === 'STUDENTS' ? '/admin/users/student'
          : activeTab === 'TEACHERS' ? '/admin/users/teacher'
            : '/admin/users/staff'

      const res = await api.post(endpoint, {
        fullName: addForm.fullName.trim(),
        username: addForm.username.trim().toLowerCase(),
        email: addForm.email.trim().toLowerCase(),
        password: addForm.password,
        phoneNumber: addForm.phoneNumber.trim() || null,
      })

      if (res.data?.success) {
        const label = activeTab === 'STUDENTS' ? 'Student' : activeTab === 'TEACHERS' ? 'Teacher' : 'Staff'
        setAddSuccess(`${label} created successfully!`)
        setAddForm({ fullName: '', username: '', email: '', password: '', phoneNumber: '' })
        fetchUsers()
        fetchStats()
        setTimeout(() => {
          setShowAddModal(false)
          setAddSuccess(null)
        }, 1500)
      }
    } catch (err: any) {
      const message =
        err.response?.data?.message ||
        err.response?.data?.errors?.[0] ||
        err.message ||
        'Failed to create user. Please try again.'
      setAddError(message)
    } finally {
      setAddLoading(false)
    }
  }

  const tabLabel = (tab: ActiveTab): string => {
    switch (tab) {
      case 'STUDENTS': return 'Student'
      case 'TEACHERS': return 'Teacher'
      case 'STAFF': return 'Staff'
    }
  }

  const SortIcon = ({ field }: { field: SortField }) => {
    if (sortBy !== field) return <ArrowUpDown className="h-3.5 w-3.5 text-[#94A3B8]" />
    return sortDir === 'asc'
      ? <ArrowUp className="h-3.5 w-3.5 text-[#2563EB]" />
      : <ArrowDown className="h-3.5 w-3.5 text-[#2563EB]" />
  }

  if (authLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-[#F8FAFC]">
        <Loader2 className="h-8 w-8 animate-spin text-[#2563EB]" />
      </div>
    )
  }

  return (
    <div className="flex h-screen overflow-hidden bg-[#F5F7FA]">

      {/* ─── Sidebar ─────────────────────────────────────────────────── */}
      <aside
        className={`flex flex-col border-r border-[#E2E8F0] bg-white transition-all duration-300 ${
          sidebarCollapsed ? 'w-[72px]' : 'w-[260px]'
        }`}
      >
        {/* Logo / Brand */}
        <div className="flex h-[72px] items-center border-b border-[#E2E8F0] px-4">
          {sidebarCollapsed ? (
            <button
              onClick={() => setSidebarCollapsed(false)}
              className="mx-auto flex h-10 w-10 items-center justify-center rounded-xl bg-[#2563EB] text-white cursor-pointer"
              title="Expand sidebar"
            >
              <LayoutDashboard className="h-5 w-5" />
            </button>
          ) : (
            <div className="flex w-full items-center justify-between">
              <div className="flex items-center gap-2.5">
                <IslingtonLogo size="sm" />
                <div className="leading-tight">
                  <p className="text-sm font-bold text-[#0F172A]">RTE Admin</p>
                  <p className="text-[11px] text-[#94A3B8]">Dashboard</p>
                </div>
              </div>
              <button
                onClick={() => setSidebarCollapsed(true)}
                className="rounded-lg p-1.5 text-[#94A3B8] transition hover:bg-[#F1F5F9] hover:text-[#475569] cursor-pointer"
                title="Collapse sidebar"
              >
                <ChevronLeft className="h-4 w-4" />
              </button>
            </div>
          )}
        </div>

        {/* Navigation */}
        <nav className="flex-1 overflow-y-auto px-3 py-4">
          {/* Overview */}
          {!sidebarCollapsed && (
            <p className="mb-2 px-3 text-[10px] font-bold uppercase tracking-widest text-[#94A3B8]">
              Overview
            </p>
          )}
          <button
            onClick={() => setActiveTab('STUDENTS')}
            className={`mb-1 flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition cursor-pointer ${
              activeTab === 'STUDENTS' && !sidebarCollapsed
                ? 'bg-[#2563EB] text-white shadow-md shadow-[#2563EB]/20'
                : activeTab === 'STUDENTS' && sidebarCollapsed
                  ? 'bg-[#2563EB] text-white'
                  : 'text-[#64748B] hover:bg-[#F8FAFC] hover:text-[#1E293B]'
            }`}
            title="Students"
          >
            <LayoutDashboard className="h-5 w-5 shrink-0" />
            {!sidebarCollapsed && <span>Dashboard</span>}
          </button>

          {!sidebarCollapsed && (
            <p className="mb-2 mt-6 px-3 text-[10px] font-bold uppercase tracking-widest text-[#94A3B8]">
              User Management
            </p>
          )}

          {sidebarCollapsed && <div className="my-3 border-t border-[#F1F5F9]" />}

          {sidebarItems.map((item) => {
            const Icon = item.icon
            const isActive = activeTab === item.id
            return (
              <button
                key={item.id}
                onClick={() => setActiveTab(item.id)}
                className={`mb-1 flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition cursor-pointer ${
                  isActive
                    ? 'bg-[#F8FAFC] text-[#1E293B] shadow-sm ring-1 ring-[#E2E8F0]'
                    : 'text-[#64748B] hover:bg-[#F8FAFC] hover:text-[#1E293B]'
                }`}
                title={item.label}
              >
                <div className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-lg ${isActive ? item.bg : ''}`}>
                  <Icon className={`h-4.5 w-4.5 ${isActive ? item.color : ''}`} />
                </div>
                {!sidebarCollapsed && (
                  <div className="flex flex-1 items-center justify-between">
                    <span>{item.label}</span>
                    <span className={`rounded-md px-2 py-0.5 text-[11px] font-semibold ${
                      isActive ? 'bg-white text-[#1E293B]' : 'bg-[#F1F5F9] text-[#94A3B8]'
                    }`}>
                      {item.id === 'STUDENTS' ? stats?.students ?? '—'
                        : item.id === 'TEACHERS' ? stats?.teachers ?? '—'
                          : stats?.staff ?? '—'}
                    </span>
                  </div>
                )}
              </button>
            )
          })}

          {sidebarCollapsed && <div className="my-3 border-t border-[#F1F5F9]" />}

          {!sidebarCollapsed && (
            <p className="mb-2 mt-6 px-3 text-[10px] font-bold uppercase tracking-widest text-[#94A3B8]">
              Resources
            </p>
          )}

          {/* Rooms - static */}
          <button
            className="mb-1 flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium text-[#64748B] transition cursor-pointer hover:bg-[#F8FAFC] hover:text-[#1E293B]"
            title="Rooms"
          >
            <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg">
              <DoorOpen className="h-4.5 w-4.5" />
            </div>
            {!sidebarCollapsed && (
              <div className="flex flex-1 items-center justify-between">
                <span>Rooms</span>
                <span className="rounded-md bg-[#F1F5F9] px-2 py-0.5 text-[11px] font-semibold text-[#94A3B8]">
                  {stats?.rooms ?? '—'}
                </span>
              </div>
            )}
          </button>

          {/* Reports - static */}
          <button
            className="mb-1 flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium text-[#64748B] transition cursor-pointer hover:bg-[#F8FAFC] hover:text-[#1E293B]"
            title="Reports"
          >
            <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg">
              <BarChart3 className="h-4.5 w-4.5" />
            </div>
            {!sidebarCollapsed && <span>Reports</span>}
          </button>
        </nav>

        {/* Admin Profile + Logout at bottom */}
        <div className="border-t border-[#E2E8F0] p-3">
          {sidebarCollapsed ? (
            <button
              onClick={handleLogout}
              className="flex w-full items-center justify-center rounded-xl bg-[#FEF2F2] p-2.5 text-[#EF4444] transition hover:bg-[#FEE2E2] cursor-pointer"
              title="Log Out"
            >
              <LogOut className="h-5 w-5" />
            </button>
          ) : (
            <div className="space-y-2">
              {/* User info */}
              <div className="flex items-center gap-3 rounded-xl bg-[#F8FAFC] p-3">
                <div className="flex h-9 w-9 items-center justify-center rounded-full bg-gradient-to-br from-[#2563EB] to-[#7C3AED] text-xs font-bold text-white">
                  {user?.fullName?.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase() || 'A'}
                </div>
                <div className="flex-1 overflow-hidden">
                  <p className="truncate text-sm font-semibold text-[#1E293B]">{user?.fullName || 'Admin'}</p>
                  <p className="truncate text-[11px] text-[#94A3B8]">{user?.email || ''}</p>
                </div>
              </div>

              {/* Logout button */}
              <button
                onClick={handleLogout}
                className="flex w-full items-center justify-center gap-2 rounded-xl bg-[#FEF2F2] px-4 py-2.5 text-sm font-semibold text-[#EF4444] transition hover:bg-[#FEE2E2] cursor-pointer"
              >
                <LogOut className="h-4 w-4" />
                <span>Log Out</span>
              </button>
            </div>
          )}
        </div>
      </aside>

      {/* ─── Main Content ────────────────────────────────────────────── */}
      <main className="flex-1 overflow-y-auto">
        {/* Top Bar */}
        <header className="sticky top-0 z-20 flex h-[72px] items-center justify-between border-b border-[#E2E8F0] bg-white/95 px-6 backdrop-blur-md">
          <div>
            <h1 className="text-xl font-bold text-[#0F172A]">
              {tabLabel(activeTab)} Management
            </h1>
            <p className="text-xs text-[#94A3B8]">
              View and manage all {tabLabel(activeTab).toLowerCase()} accounts
            </p>
          </div>

          <div className="flex items-center gap-3">
            <button
              type="button"
              onClick={() => {
                setAddError(null)
                setAddSuccess(null)
                setAddForm({ fullName: '', username: '', email: '', password: '', phoneNumber: '' })
                setShowAddModal(true)
              }}
              className="inline-flex items-center gap-2 rounded-xl bg-[#2563EB] px-5 py-2.5 text-sm font-semibold text-white shadow-md shadow-[#2563EB]/20 transition hover:bg-[#1D4ED8] hover:shadow-lg focus:outline-none focus:ring-4 focus:ring-[#2563EB]/20 cursor-pointer"
            >
              <Plus className="h-4 w-4" />
              <span>Add {tabLabel(activeTab)}</span>
            </button>
          </div>
        </header>

        <div className="p-6">
          {/* Stats Cards Row */}
          <div className="mb-6 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <StatCard
              icon={<GraduationCap className="h-6 w-6 text-[#2563EB]" />}
              iconBg="bg-[#EFF6FF]"
              label="Total Students"
              value={stats?.students}
              loading={statsLoading}
              active={activeTab === 'STUDENTS'}
              onClick={() => setActiveTab('STUDENTS')}
            />
            <StatCard
              icon={<Briefcase className="h-6 w-6 text-[#059669]" />}
              iconBg="bg-[#ECFDF5]"
              label="Total Teachers"
              value={stats?.teachers}
              loading={statsLoading}
              active={activeTab === 'TEACHERS'}
              onClick={() => setActiveTab('TEACHERS')}
            />
            <StatCard
              icon={<UserCog className="h-6 w-6 text-[#7C3AED]" />}
              iconBg="bg-[#F5F3FF]"
              label="Total Staff"
              value={stats?.staff}
              loading={statsLoading}
              active={activeTab === 'STAFF'}
              onClick={() => setActiveTab('STAFF')}
            />
            <StatCard
              icon={<DoorOpen className="h-6 w-6 text-[#D97706]" />}
              iconBg="bg-[#FEF3C7]"
              label="Total Rooms"
              value={stats?.rooms}
              loading={statsLoading}
            />
          </div>

          {/* Users Table Card */}
          <div className="overflow-hidden rounded-2xl border border-[#E2E8F0] bg-white shadow-sm">
            {/* Table Toolbar */}
            <div className="border-b border-[#E2E8F0] px-5 py-4">
              <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <h2 className="text-base font-bold text-[#0F172A]">
                  {tabLabel(activeTab)} List
                </h2>

                {/* Search + Refresh */}
                <div className="flex items-center gap-3">
                  <form onSubmit={handleSearch} className="relative">
                    <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-[#94A3B8]" />
                    <input
                      type="text"
                      value={searchInput}
                      onChange={(e) => setSearchInput(e.target.value)}
                      placeholder={`Search ${tabLabel(activeTab).toLowerCase()}s...`}
                      className="h-10 w-60 rounded-xl border border-[#E2E8F0] bg-[#F8FAFC] pl-10 pr-4 text-sm text-[#1E293B] outline-none transition placeholder:text-[#94A3B8] focus:border-[#2563EB] focus:ring-2 focus:ring-[#2563EB]/10"
                    />
                    {search && (
                      <button
                        type="button"
                        onClick={() => { setSearchInput(''); setSearch('') }}
                        className="absolute right-2 top-1/2 -translate-y-1/2 rounded p-1 text-[#94A3B8] hover:text-[#1E293B] cursor-pointer"
                      >
                        <X className="h-3.5 w-3.5" />
                      </button>
                    )}
                  </form>
                  <button
                    type="button"
                    onClick={() => { fetchUsers(); fetchStats() }}
                    className="flex h-10 w-10 items-center justify-center rounded-xl border border-[#E2E8F0] bg-[#F8FAFC] text-[#64748B] transition hover:bg-[#F1F5F9] hover:text-[#1E293B] cursor-pointer"
                    title="Refresh"
                  >
                    <RefreshCw className={`h-4 w-4 ${tableLoading ? 'animate-spin' : ''}`} />
                  </button>
                </div>
              </div>

              {/* Result summary */}
              <div className="mt-3 flex items-center justify-between text-xs text-[#64748B]">
                <span>
                  Showing {users.length} of {totalElements} {tabLabel(activeTab).toLowerCase()}
                  {totalElements !== 1 ? 's' : ''}
                  {search && (
                    <span>
                      {' '}matching "<span className="font-semibold text-[#2563EB]">{search}</span>"
                    </span>
                  )}
                </span>
                <span>Page {page + 1} of {Math.max(totalPages, 1)}</span>
              </div>
            </div>

            {/* Table */}
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead>
                  <tr className="border-b border-[#E2E8F0] bg-[#FAFBFC]">
                    <th className="px-5 py-3 text-xs font-semibold uppercase tracking-wider text-[#64748B]">#</th>
                    <th className="cursor-pointer px-5 py-3 text-xs font-semibold uppercase tracking-wider text-[#64748B] hover:text-[#1E293B]" onClick={() => toggleSort('fullName')}>
                      <div className="flex items-center gap-1.5">Full Name <SortIcon field="fullName" /></div>
                    </th>
                    <th className="cursor-pointer px-5 py-3 text-xs font-semibold uppercase tracking-wider text-[#64748B] hover:text-[#1E293B]" onClick={() => toggleSort('username')}>
                      <div className="flex items-center gap-1.5">Username <SortIcon field="username" /></div>
                    </th>
                    <th className="cursor-pointer px-5 py-3 text-xs font-semibold uppercase tracking-wider text-[#64748B] hover:text-[#1E293B]" onClick={() => toggleSort('email')}>
                      <div className="flex items-center gap-1.5">Email <SortIcon field="email" /></div>
                    </th>
                    <th className="px-5 py-3 text-xs font-semibold uppercase tracking-wider text-[#64748B]">Phone</th>
                    <th className="px-5 py-3 text-xs font-semibold uppercase tracking-wider text-[#64748B]">Status</th>
                    <th className="px-5 py-3 text-xs font-semibold uppercase tracking-wider text-[#64748B]">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {tableLoading ? (
                    <tr>
                      <td colSpan={7} className="px-5 py-16 text-center">
                        <div className="flex flex-col items-center gap-3">
                          <Loader2 className="h-6 w-6 animate-spin text-[#2563EB]" />
                          <span className="text-sm text-[#64748B]">Loading {tabLabel(activeTab).toLowerCase()}s...</span>
                        </div>
                      </td>
                    </tr>
                  ) : users.length === 0 ? (
                    <tr>
                      <td colSpan={7} className="px-5 py-16 text-center">
                        <div className="flex flex-col items-center gap-3">
                          <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-[#F1F5F9] text-[#94A3B8]">
                            <Users className="h-7 w-7" />
                          </div>
                          <p className="text-sm font-medium text-[#475569]">No {tabLabel(activeTab).toLowerCase()}s found</p>
                          <p className="text-xs text-[#94A3B8]">
                            {search ? 'Try a different search term' : `Click "Add ${tabLabel(activeTab)}" to create one`}
                          </p>
                        </div>
                      </td>
                    </tr>
                  ) : (
                    users.map((u, idx) => (
                      <tr key={u.userId} className="border-b border-[#F1F5F9] transition hover:bg-[#F8FAFC]">
                        <td className="px-5 py-3.5 text-xs text-[#94A3B8]">{page * pageSize + idx + 1}</td>
                        <td className="px-5 py-3.5">
                          <div className="flex items-center gap-3">
                            <div className="flex h-9 w-9 items-center justify-center rounded-full bg-gradient-to-br from-[#2563EB] to-[#7C3AED] text-xs font-bold text-white">
                              {u.fullName?.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase() || '?'}
                            </div>
                            <span className="font-medium text-[#1E293B]">{u.fullName}</span>
                          </div>
                        </td>
                        <td className="px-5 py-3.5 text-[#475569]">{u.username}</td>
                        <td className="px-5 py-3.5 text-[#475569]">{u.email}</td>
                        <td className="px-5 py-3.5 text-[#475569]">{u.phoneNumber || '—'}</td>
                        <td className="px-5 py-3.5">
                          <span className={`inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-medium ${
                            u.isActive ? 'bg-[#ECFDF5] text-[#059669]' : 'bg-[#FEF2F2] text-[#EF4444]'
                          }`}>
                            <span className={`h-1.5 w-1.5 rounded-full ${u.isActive ? 'bg-[#059669]' : 'bg-[#EF4444]'}`} />
                            {u.isActive ? 'Active' : 'Inactive'}
                          </span>
                        </td>
                        <td className="px-5 py-3.5">
                          <button
                            type="button"
                            onClick={() => setViewUser(u)}
                            className="inline-flex items-center gap-1.5 rounded-lg border border-[#E2E8F0] bg-white px-3 py-1.5 text-xs font-medium text-[#475569] transition hover:border-[#2563EB] hover:text-[#2563EB] cursor-pointer"
                          >
                            <Eye className="h-3.5 w-3.5" />
                            View
                          </button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>

            {/* Pagination */}
            {totalPages > 0 && (
              <div className="flex items-center justify-between border-t border-[#E2E8F0] px-5 py-3">
                <button
                  type="button"
                  disabled={page <= 0}
                  onClick={() => setPage(p => Math.max(0, p - 1))}
                  className="inline-flex items-center gap-1.5 rounded-lg border border-[#E2E8F0] px-3 py-1.5 text-sm font-medium text-[#475569] transition hover:bg-[#F8FAFC] disabled:opacity-40 disabled:cursor-not-allowed cursor-pointer"
                >
                  <ChevronLeft className="h-4 w-4" />
                  Previous
                </button>

                <div className="flex items-center gap-1">
                  {Array.from({ length: Math.min(totalPages, 7) }, (_, i) => {
                    let pageNum: number
                    if (totalPages <= 7) pageNum = i
                    else if (page < 3) pageNum = i
                    else if (page > totalPages - 4) pageNum = totalPages - 7 + i
                    else pageNum = page - 3 + i
                    return (
                      <button
                        key={pageNum}
                        type="button"
                        onClick={() => setPage(pageNum)}
                        className={`flex h-8 w-8 items-center justify-center rounded-lg text-sm font-medium transition cursor-pointer ${
                          page === pageNum ? 'bg-[#2563EB] text-white shadow-sm' : 'text-[#64748B] hover:bg-[#F1F5F9]'
                        }`}
                      >
                        {pageNum + 1}
                      </button>
                    )
                  })}
                </div>

                <button
                  type="button"
                  disabled={page >= totalPages - 1}
                  onClick={() => setPage(p => p + 1)}
                  className="inline-flex items-center gap-1.5 rounded-lg border border-[#E2E8F0] px-3 py-1.5 text-sm font-medium text-[#475569] transition hover:bg-[#F8FAFC] disabled:opacity-40 disabled:cursor-not-allowed cursor-pointer"
                >
                  Next
                  <ChevronRight className="h-4 w-4" />
                </button>
              </div>
            )}
          </div>
        </div>
      </main>

      {/* ─── Add User Modal ────────────────────────────────────────── */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm px-4">
          <div className="relative w-full max-w-lg rounded-2xl border border-[#E2E8F0] bg-white p-6 shadow-2xl sm:p-8">
            <button
              type="button"
              onClick={() => setShowAddModal(false)}
              className="absolute right-4 top-4 rounded-lg p-1.5 text-[#94A3B8] transition hover:bg-[#F1F5F9] hover:text-[#1E293B] cursor-pointer"
            >
              <X className="h-5 w-5" />
            </button>

            <div className="mb-6 flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#EFF6FF] text-[#2563EB]">
                <UserPlus className="h-5 w-5" />
              </div>
              <div>
                <h2 className="text-xl font-bold text-[#0F172A]">Add New {tabLabel(activeTab)}</h2>
                <p className="text-sm text-[#64748B]">Fill in all required fields to create a new account.</p>
              </div>
            </div>

            <form onSubmit={handleAddUser} className="space-y-4" noValidate>
              {/* Full Name */}
              <div>
                <label htmlFor="add-fullName" className="mb-1.5 block text-sm font-semibold text-[#1E293B]">
                  Full Name <span className="text-[#EF4444]">*</span>
                </label>
                <div className="relative">
                  <User className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-[#94A3B8]" />
                  <input
                    id="add-fullName"
                    type="text"
                    value={addForm.fullName}
                    onChange={(e) => { setAddForm({ ...addForm, fullName: e.target.value }); setAddError(null) }}
                    placeholder="e.g. John Doe"
                    className="h-11 w-full rounded-xl border border-[#E2E8F0] pl-10 pr-4 text-sm text-[#1E293B] outline-none transition placeholder:text-[#94A3B8] focus:border-[#2563EB] focus:ring-2 focus:ring-[#2563EB]/10"
                    maxLength={100}
                  />
                </div>
                <p className="mt-1 text-[11px] text-[#94A3B8]">Max 100 characters</p>
              </div>

              {/* Username */}
              <div>
                <label htmlFor="add-username" className="mb-1.5 block text-sm font-semibold text-[#1E293B]">
                  Username <span className="text-[#EF4444]">*</span>
                </label>
                <div className="relative">
                  <Shield className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-[#94A3B8]" />
                  <input
                    id="add-username"
                    type="text"
                    value={addForm.username}
                    onChange={(e) => { setAddForm({ ...addForm, username: e.target.value }); setAddError(null) }}
                    placeholder="e.g. johndoe"
                    className="h-11 w-full rounded-xl border border-[#E2E8F0] pl-10 pr-4 text-sm text-[#1E293B] outline-none transition placeholder:text-[#94A3B8] focus:border-[#2563EB] focus:ring-2 focus:ring-[#2563EB]/10"
                    minLength={3}
                    maxLength={50}
                  />
                </div>
                <p className="mt-1 text-[11px] text-[#94A3B8]">3–50 characters, must be unique</p>
              </div>

              {/* Email */}
              <div>
                <label htmlFor="add-email" className="mb-1.5 block text-sm font-semibold text-[#1E293B]">
                  Email Address <span className="text-[#EF4444]">*</span>
                </label>
                <div className="relative">
                  <Mail className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-[#94A3B8]" />
                  <input
                    id="add-email"
                    type="email"
                    value={addForm.email}
                    onChange={(e) => { setAddForm({ ...addForm, email: e.target.value }); setAddError(null) }}
                    placeholder="e.g. john@islington.edu.np"
                    className="h-11 w-full rounded-xl border border-[#E2E8F0] pl-10 pr-4 text-sm text-[#1E293B] outline-none transition placeholder:text-[#94A3B8] focus:border-[#2563EB] focus:ring-2 focus:ring-[#2563EB]/10"
                    maxLength={100}
                  />
                </div>
                <p className="mt-1 text-[11px] text-[#94A3B8]">Must be a valid email, must be unique</p>
              </div>

              {/* Password */}
              <div>
                <label htmlFor="add-password" className="mb-1.5 block text-sm font-semibold text-[#1E293B]">
                  Password <span className="text-[#EF4444]">*</span>
                </label>
                <div className="relative">
                  <Shield className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-[#94A3B8]" />
                  <input
                    id="add-password"
                    type="password"
                    value={addForm.password}
                    onChange={(e) => { setAddForm({ ...addForm, password: e.target.value }); setAddError(null) }}
                    placeholder="Minimum 6 characters"
                    className="h-11 w-full rounded-xl border border-[#E2E8F0] pl-10 pr-4 text-sm text-[#1E293B] outline-none transition placeholder:text-[#94A3B8] focus:border-[#2563EB] focus:ring-2 focus:ring-[#2563EB]/10"
                    minLength={6}
                  />
                </div>
                <p className="mt-1 text-[11px] text-[#94A3B8]">At least 6 characters</p>
              </div>

              {/* Phone Number */}
              <div>
                <label htmlFor="add-phone" className="mb-1.5 block text-sm font-semibold text-[#1E293B]">
                  Phone Number <span className="text-[#94A3B8] font-normal">(optional)</span>
                </label>
                <div className="relative">
                  <Phone className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-[#94A3B8]" />
                  <input
                    id="add-phone"
                    type="tel"
                    value={addForm.phoneNumber}
                    onChange={(e) => setAddForm({ ...addForm, phoneNumber: e.target.value })}
                    placeholder="e.g. +977-9841234567"
                    className="h-11 w-full rounded-xl border border-[#E2E8F0] pl-10 pr-4 text-sm text-[#1E293B] outline-none transition placeholder:text-[#94A3B8] focus:border-[#2563EB] focus:ring-2 focus:ring-[#2563EB]/10"
                    maxLength={20}
                  />
                </div>
              </div>

              {/* Error / Success */}
              {addError && (
                <div className="flex items-center gap-2 rounded-xl bg-[#FEF2F2] px-3 py-2.5 text-sm text-[#DC2626]">
                  <AlertCircle className="h-4 w-4 shrink-0" />
                  <span>{addError}</span>
                </div>
              )}
              {addSuccess && (
                <div className="flex items-center gap-2 rounded-xl bg-[#ECFDF5] px-3 py-2.5 text-sm text-[#059669]">
                  <CheckCircle className="h-4 w-4 shrink-0" />
                  <span>{addSuccess}</span>
                </div>
              )}

              {/* Buttons */}
              <div className="flex items-center justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setShowAddModal(false)}
                  className="rounded-xl border border-[#E2E8F0] px-5 py-2.5 text-sm font-medium text-[#475569] transition hover:bg-[#F8FAFC] cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={addLoading}
                  className="inline-flex items-center gap-2 rounded-xl bg-[#2563EB] px-5 py-2.5 text-sm font-semibold text-white shadow-md shadow-[#2563EB]/20 transition hover:bg-[#1D4ED8] disabled:opacity-60 cursor-pointer"
                >
                  {addLoading ? (
                    <><Loader2 className="h-4 w-4 animate-spin" /><span>Creating...</span></>
                  ) : (
                    <><Plus className="h-4 w-4" /><span>Create {tabLabel(activeTab)}</span></>
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ─── View User Modal ───────────────────────────────────────── */}
      {viewUser && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm px-4">
          <div className="relative w-full max-w-md rounded-2xl border border-[#E2E8F0] bg-white p-6 shadow-2xl sm:p-8">
            <button
              type="button"
              onClick={() => setViewUser(null)}
              className="absolute right-4 top-4 rounded-lg p-1.5 text-[#94A3B8] transition hover:bg-[#F1F5F9] hover:text-[#1E293B] cursor-pointer"
            >
              <X className="h-5 w-5" />
            </button>

            <div className="mb-6 flex flex-col items-center text-center">
              <div className="flex h-16 w-16 items-center justify-center rounded-full bg-gradient-to-br from-[#2563EB] to-[#7C3AED] text-xl font-bold text-white shadow-lg shadow-[#2563EB]/25">
                {viewUser.fullName?.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase() || '?'}
              </div>
              <h2 className="mt-3 text-xl font-bold text-[#0F172A]">{viewUser.fullName}</h2>
              <span className="mt-1 inline-flex items-center gap-1 rounded-full bg-[#EFF6FF] px-2.5 py-0.5 text-xs font-semibold text-[#2563EB]">
                {viewUser.role}
              </span>
            </div>

            <div className="space-y-3">
              <DetailRow icon={<Users className="h-4 w-4" />} label="Username" value={viewUser.username} />
              <DetailRow icon={<Mail className="h-4 w-4" />} label="Email" value={viewUser.email} />
              <DetailRow icon={<Phone className="h-4 w-4" />} label="Phone" value={viewUser.phoneNumber || 'Not provided'} />
              <DetailRow
                icon={<CheckCircle className="h-4 w-4" />}
                label="Status"
                value={viewUser.isActive ? 'Active' : 'Inactive'}
                valueColor={viewUser.isActive ? 'text-[#059669]' : 'text-[#EF4444]'}
              />
            </div>

            <button
              type="button"
              onClick={() => setViewUser(null)}
              className="mt-6 w-full rounded-xl border border-[#E2E8F0] py-2.5 text-sm font-medium text-[#475569] transition hover:bg-[#F8FAFC] cursor-pointer"
            >
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  )
}

// ─── Sub-components ──────────────────────────────────────────────────────────

function StatCard({
  icon, iconBg, label, value, loading, active, onClick,
}: {
  icon: React.ReactNode; iconBg: string; label: string; value?: number; loading: boolean; active?: boolean; onClick?: () => void
}) {
  return (
    <div
      role={onClick ? 'button' : undefined}
      tabIndex={onClick ? 0 : undefined}
      onClick={onClick}
      onKeyDown={(e) => { if (onClick && (e.key === 'Enter' || e.key === ' ')) { e.preventDefault(); onClick() } }}
      className={`flex items-center gap-4 rounded-2xl border p-5 transition-all ${
        active ? 'border-[#2563EB]/40 bg-[#EFF6FF] shadow-md shadow-[#2563EB]/10' : 'border-[#E2E8F0] bg-white hover:shadow-sm'
      } ${onClick ? 'cursor-pointer' : ''}`}
    >
      <div className={`flex h-12 w-12 items-center justify-center rounded-xl ${iconBg}`}>{icon}</div>
      <div>
        <p className="text-xs font-medium text-[#64748B]">{label}</p>
        {loading ? (
          <div className="mt-1 h-6 w-12 animate-pulse rounded bg-[#E2E8F0]" />
        ) : (
          <p className="text-2xl font-bold text-[#0F172A]">{value?.toLocaleString() ?? '—'}</p>
        )}
      </div>
    </div>
  )
}

function DetailRow({ icon, label, value, valueColor }: { icon: React.ReactNode; label: string; value: string; valueColor?: string }) {
  return (
    <div className="flex items-center gap-3 rounded-xl bg-[#F8FAFC] px-4 py-3">
      <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-white text-[#64748B] shadow-sm">{icon}</div>
      <div className="flex-1">
        <p className="text-xs text-[#94A3B8]">{label}</p>
        <p className={`text-sm font-medium ${valueColor || 'text-[#1E293B]'}`}>{value}</p>
      </div>
    </div>
  )
}
