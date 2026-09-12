import React from 'react'
import {
  Users,
  Search,
  X,
  Loader2,
  Eye,
  RefreshCw,
  GraduationCap,
  ArrowUpDown,
  ArrowUp,
  ArrowDown,
} from 'lucide-react'
import { Pagination } from './Pagination'
import { initialsOf, tabLabel, type ActiveTab, type SortDir, type SortField, type UserProfile } from './types'

const TH_CLASS = 'px-5 py-3 text-xs font-semibold uppercase tracking-wider text-[#64748B]'

interface Props {
  activeTab: ActiveTab
  users: UserProfile[]
  loading: boolean
  page: number
  pageSize: number
  totalPages: number
  totalElements: number
  sortBy: SortField
  sortDir: SortDir
  search: string
  searchInput: string
  onSearchInputChange: (value: string) => void
  onSearchSubmit: (e: React.FormEvent) => void
  onClearSearch: () => void
  onRefresh: () => void
  onToggleSort: (field: SortField) => void
  onPageChange: (page: number) => void
  onView: (user: UserProfile) => void
}

export function UsersTable({
  activeTab,
  users,
  loading,
  page,
  pageSize,
  totalPages,
  totalElements,
  sortBy,
  sortDir,
  search,
  searchInput,
  onSearchInputChange,
  onSearchSubmit,
  onClearSearch,
  onRefresh,
  onToggleSort,
  onPageChange,
  onView,
}: Props) {
  const label = tabLabel(activeTab)

  // Arrow reflecting the current sort state
  const SortIcon = ({ field }: { field: SortField }) => {
    if (sortBy !== field) return <ArrowUpDown className="h-3.5 w-3.5 text-[#94A3B8]" />
    return sortDir === 'asc'
      ? <ArrowUp className="h-3.5 w-3.5 text-[#2563EB]" />
      : <ArrowDown className="h-3.5 w-3.5 text-[#2563EB]" />
  }

  return (
    <div className="overflow-hidden rounded-2xl border border-[#E2E8F0] bg-white shadow-sm">
      {/* Toolbar: title, search, refresh */}
      <div className="border-b border-[#E2E8F0] px-5 py-4">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <h2 className="text-base font-bold text-[#0F172A]">{label} List</h2>

          <div className="flex items-center gap-3">
            <form onSubmit={onSearchSubmit} className="relative">
              <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-[#94A3B8]" />
              <input
                type="text"
                value={searchInput}
                onChange={(e) => onSearchInputChange(e.target.value)}
                placeholder={`Search by name, email${activeTab === 'STUDENTS' ? ', student #' : ''}...`}
                className="h-10 w-72 rounded-xl border border-[#E2E8F0] bg-[#F8FAFC] pl-10 pr-4 text-sm text-[#1E293B] outline-none transition placeholder:text-[#94A3B8] focus:border-[#2563EB] focus:ring-2 focus:ring-[#2563EB]/10"
              />
              {search && (
                <button
                  type="button"
                  onClick={onClearSearch}
                  className="absolute right-2 top-1/2 -translate-y-1/2 rounded p-1 text-[#94A3B8] hover:text-[#1E293B] cursor-pointer"
                >
                  <X className="h-3.5 w-3.5" />
                </button>
              )}
            </form>
            <button
              type="button"
              onClick={onRefresh}
              className="flex h-10 w-10 items-center justify-center rounded-xl border border-[#E2E8F0] bg-[#F8FAFC] text-[#64748B] transition hover:bg-[#F1F5F9] hover:text-[#1E293B] cursor-pointer"
              title="Refresh"
            >
              <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
            </button>
          </div>
        </div>

        {/* Result summary */}
        <div className="mt-3 flex items-center justify-between text-xs text-[#64748B]">
          <span>
            Showing {users.length} of {totalElements} {label.toLowerCase()}
            {totalElements !== 1 ? 's' : ''}
            {search && (
              <span> matching "<span className="font-semibold text-[#2563EB]">{search}</span>"</span>
            )}
          </span>
          <span>Page {page + 1} of {Math.max(totalPages, 1)}</span>
        </div>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm">
          <thead>
            <tr className="border-b border-[#E2E8F0] bg-[#FAFBFC]">
              <th className={TH_CLASS}>#</th>
              <th className={`cursor-pointer hover:text-[#1E293B] ${TH_CLASS}`} onClick={() => onToggleSort('fullName')}>
                <div className="flex items-center gap-1.5">Full Name <SortIcon field="fullName" /></div>
              </th>

              {/* Columns differ per role */}
              {activeTab === 'STUDENTS' ? (
                <>
                  <th className={TH_CLASS}>Student No.</th>
                  <th className={TH_CLASS}>Reg No.</th>
                </>
              ) : (
                <>
                  <th className={TH_CLASS}>Department</th>
                  <th className={TH_CLASS}>Designation</th>
                </>
              )}

              <th className={`cursor-pointer hover:text-[#1E293B] ${TH_CLASS}`} onClick={() => onToggleSort('email')}>
                <div className="flex items-center gap-1.5">Email <SortIcon field="email" /></div>
              </th>
              <th className={TH_CLASS}>Status</th>
              <th className={TH_CLASS}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={7} className="px-5 py-16 text-center">
                  <div className="flex flex-col items-center gap-3">
                    <Loader2 className="h-6 w-6 animate-spin text-[#2563EB]" />
                    <span className="text-sm text-[#64748B]">Loading {label.toLowerCase()}s...</span>
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
                    <p className="text-sm font-medium text-[#475569]">No {label.toLowerCase()}s found</p>
                    <p className="text-xs text-[#94A3B8]">
                      {search ? 'Try a different search term' : `Click "Add ${label}" to create one`}
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
                        {initialsOf(u.fullName)}
                      </div>
                      <div>
                        <p className="font-medium text-[#1E293B]">{u.fullName}</p>
                        <p className="text-xs text-[#94A3B8]">@{u.username}</p>
                      </div>
                    </div>
                  </td>

                  {activeTab === 'STUDENTS' && (
                    <>
                      <td className="px-5 py-3.5 font-mono text-xs font-semibold text-[#2563EB]">{u.studentNumber || '—'}</td>
                      <td className="px-5 py-3.5 text-xs text-[#475569]">
                        <div>{u.registrationNumber || '—'}</div>
                        {u.batchName && (
                          <div className="mt-0.5 inline-flex items-center gap-1 rounded-md bg-[#EFF6FF] px-1.5 py-0.5 text-[10px] font-medium text-[#2563EB]">
                            <GraduationCap className="h-3 w-3" />
                            {u.batchName}
                          </div>
                        )}
                      </td>
                    </>
                  )}
                  {activeTab === 'TEACHERS' && (
                    <>
                      <td className="px-5 py-3.5 text-xs font-medium text-[#059669]">{u.teacherDepartment || '—'}</td>
                      <td className="px-5 py-3.5 text-xs text-[#475569]">{u.teacherDesignation || '—'}</td>
                    </>
                  )}
                  {activeTab === 'STAFF' && (
                    <>
                      <td className="px-5 py-3.5 text-xs font-medium text-[#7C3AED]">{u.staffDepartment || '—'}</td>
                      <td className="px-5 py-3.5 text-xs text-[#475569]">{u.staffDesignation || '—'}</td>
                    </>
                  )}

                  <td className="px-5 py-3.5 text-[#475569]">{u.email}</td>
                  <td className="px-5 py-3.5">
                    <span className={`inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-medium ${
                      u.isActive ? 'bg-[#ECFDF5] text-[#059669]' : 'bg-[#FEF2F2] text-[#EF4444]'
                    }`}>
                      <span className={`h-1.5 w-1.5 rounded-full ${u.isActive ? 'bg-[#059669]' : 'bg-[#EF4444]'}`} />
                      {activeTab === 'STUDENTS' && u.studentStatus ? u.studentStatus : u.isActive ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="px-5 py-3.5">
                    <button
                      type="button"
                      onClick={() => onView(u)}
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

      <Pagination page={page} totalPages={totalPages} onPageChange={onPageChange} />
    </div>
  )
}
