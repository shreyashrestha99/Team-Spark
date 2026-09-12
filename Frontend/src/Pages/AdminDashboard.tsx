import React, { useState, useEffect, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { Loader2 } from 'lucide-react'
import { useAuth } from '../context/Auth'
import { api } from '../services/api'
import { AdminSidebar } from '../components/admin/AdminSidebar'
import { AdminTopbar } from '../components/admin/AdminTopbar'
import { StatsCards } from '../components/admin/StatsCards'
import { UsersTable } from '../components/admin/UsersTable'
import { AddUserModal } from '../components/admin/AddUserModal'
import { ViewUserModal } from '../components/admin/ViewUserModal'
import {
  EMPTY_ADD_FORM,
  tabLabel,
  tabToEndpoint,
  tabToRole,
  type ActiveTab,
  type AddUserForm,
  type Batch,
  type DashboardStats,
  type PageResponse,
  type SortDir,
  type SortField,
  type UserProfile,
} from '../components/admin/types'

const PAGE_SIZE = 10

export default function AdminDashboard() {
  const { user, isAdmin, loading: authLoading, logout } = useAuth()
  const navigate = useNavigate()

  // Layout
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false)

  // Dashboard stats
  const [stats, setStats] = useState<DashboardStats | null>(null)
  const [statsLoading, setStatsLoading] = useState(true)

  // Users table
  const [activeTab, setActiveTab] = useState<ActiveTab>('STUDENTS')
  const [users, setUsers] = useState<UserProfile[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [sortBy, setSortBy] = useState<SortField>('createdAt')
  const [sortDir, setSortDir] = useState<SortDir>('desc')
  const [search, setSearch] = useState('')
  const [searchInput, setSearchInput] = useState('')
  const [tableLoading, setTableLoading] = useState(false)

  // Create-user modal
  const [showAddModal, setShowAddModal] = useState(false)
  const [addForm, setAddForm] = useState<AddUserForm>(EMPTY_ADD_FORM)
  const [batches, setBatches] = useState<Batch[]>([])
  const [addLoading, setAddLoading] = useState(false)
  const [addError, setAddError] = useState<string | null>(null)
  const [addSuccess, setAddSuccess] = useState<string | null>(null)

  // View-user modal
  const [viewUser, setViewUser] = useState<UserProfile | null>(null)

  // Redirect non-admins
  useEffect(() => {
    if (!authLoading && (!user || !isAdmin)) {
      navigate('/', { replace: true })
    }
  }, [authLoading, user, isAdmin, navigate])

  const handleLogout = async () => {
    await logout()
    navigate('/login', { replace: true })
  }

  const fetchStats = useCallback(async () => {
    setStatsLoading(true)
    try {
      const res = await api.get('/admin/users/stats')
      if (res.data?.success && res.data?.data) setStats(res.data.data)
    } catch {
      // Non-fatal: cards fall back to em dashes
    } finally {
      setStatsLoading(false)
    }
  }, [])

  // Batches feed the student selector
  const fetchBatches = useCallback(async () => {
    try {
      const res = await api.get('/admin/users/batches')
      if (res.data?.success && Array.isArray(res.data?.data)) setBatches(res.data.data)
    } catch {
      // Non-fatal: selector shows an empty state
    }
  }, [])

  const fetchUsers = useCallback(async () => {
    setTableLoading(true)
    try {
      const params: Record<string, string | number> = {
        role: tabToRole(activeTab),
        page,
        size: PAGE_SIZE,
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
  }, [activeTab, page, sortBy, sortDir, search])

  useEffect(() => {
    if (user && isAdmin) {
      fetchStats()
      fetchBatches()
    }
  }, [user, isAdmin, fetchStats, fetchBatches])

  useEffect(() => {
    if (user && isAdmin) fetchUsers()
  }, [user, isAdmin, fetchUsers])

  // Back to first page when the view changes
  useEffect(() => { setPage(0) }, [activeTab, search])

  const toggleSort = (field: SortField) => {
    if (sortBy === field) {
      setSortDir((prev) => (prev === 'asc' ? 'desc' : 'asc'))
    } else {
      setSortBy(field)
      setSortDir('asc')
    }
  }

  const openAddModal = () => {
    setAddForm(EMPTY_ADD_FORM)
    setAddError(null)
    setAddSuccess(null)
    setShowAddModal(true)
  }

  // Merge a field change and clear any stale error
  const patchAddForm = (patch: Partial<AddUserForm>) => {
    setAddForm((prev) => ({ ...prev, ...patch }))
    setAddError(null)
  }

  // Returns an error message, or null when valid
  const validateAddForm = (): string | null => {
    const name = addForm.fullName.trim()
    const username = addForm.username.trim()
    const email = addForm.email.trim()

    if (!name) return 'Full name is required'
    if (name.length > 100) return 'Full name must be under 100 characters'
    if (!username) return 'Username is required'
    if (username.length < 3) return 'Username must be at least 3 characters'
    if (username.length > 50) return 'Username must be under 50 characters'
    if (!email) return 'Email is required'
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) return 'Please enter a valid email address'
    if (!addForm.password) return 'Password is required'
    if (addForm.password.length < 6) return 'Password must be at least 6 characters'
    // Students must join a batch when batches exist
    if (activeTab === 'STUDENTS' && batches.length > 0 && !addForm.batchId) return 'Please select a batch'
    return null
  }

  const handleAddUser = async (e: React.FormEvent) => {
    e.preventDefault()
    setAddSuccess(null)

    const validationError = validateAddForm()
    if (validationError) return setAddError(validationError)
    setAddError(null)

    setAddLoading(true)
    try {
      const payload: Record<string, unknown> = {
        fullName: addForm.fullName.trim(),
        username: addForm.username.trim().toLowerCase(),
        email: addForm.email.trim().toLowerCase(),
        password: addForm.password,
        phoneNumber: addForm.phoneNumber.trim() || null,
      }

      if (activeTab === 'STUDENTS') {
        if (addForm.studentNumber.trim()) payload.studentNumber = addForm.studentNumber.trim()
        if (addForm.registrationNumber.trim()) payload.registrationNumber = addForm.registrationNumber.trim()
        if (addForm.batchId) payload.batchId = addForm.batchId
      } else {
        // Teachers and staff share these fields
        if (addForm.department.trim()) payload.department = addForm.department.trim()
        if (addForm.designation.trim()) payload.designation = addForm.designation.trim()
      }

      const res = await api.post(tabToEndpoint(activeTab), payload)

      if (res.data?.success) {
        setAddSuccess(`${tabLabel(activeTab)} created successfully!`)
        setAddForm(EMPTY_ADD_FORM)
        fetchUsers()
        fetchStats()
        setTimeout(() => {
          setShowAddModal(false)
          setAddSuccess(null)
        }, 1500)
      }
    } catch (err: any) {
      setAddError(
        err.response?.data?.message ||
        err.response?.data?.errors?.[0] ||
        err.message ||
        'Failed to create user. Please try again.'
      )
    } finally {
      setAddLoading(false)
    }
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
      <AdminSidebar
        activeTab={activeTab}
        onTabChange={setActiveTab}
        collapsed={sidebarCollapsed}
        onCollapsedChange={setSidebarCollapsed}
        stats={stats}
        userName={user?.fullName}
        userEmail={user?.email}
        onLogout={handleLogout}
      />

      <main className="flex-1 overflow-y-auto">
        <AdminTopbar activeTab={activeTab} onAddClick={openAddModal} />

        <div className="p-6">
          <StatsCards
            stats={stats}
            loading={statsLoading}
            activeTab={activeTab}
            onTabChange={setActiveTab}
          />

          <UsersTable
            activeTab={activeTab}
            users={users}
            loading={tableLoading}
            page={page}
            pageSize={PAGE_SIZE}
            totalPages={totalPages}
            totalElements={totalElements}
            sortBy={sortBy}
            sortDir={sortDir}
            search={search}
            searchInput={searchInput}
            onSearchInputChange={setSearchInput}
            onSearchSubmit={(e) => { e.preventDefault(); setSearch(searchInput) }}
            onClearSearch={() => { setSearchInput(''); setSearch('') }}
            onRefresh={() => { fetchUsers(); fetchStats() }}
            onToggleSort={toggleSort}
            onPageChange={setPage}
            onView={setViewUser}
          />
        </div>
      </main>

      {showAddModal && (
        <AddUserModal
          activeTab={activeTab}
          form={addForm}
          onFormChange={patchAddForm}
          batches={batches}
          loading={addLoading}
          error={addError}
          success={addSuccess}
          onSubmit={handleAddUser}
          onClose={() => setShowAddModal(false)}
        />
      )}

      {viewUser && <ViewUserModal user={viewUser} onClose={() => setViewUser(null)} />}
    </div>
  )
}
