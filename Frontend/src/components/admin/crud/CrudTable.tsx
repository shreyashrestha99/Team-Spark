import React from 'react'
import {
  Search,
  X,
  Loader2,
  RefreshCw,
  Pencil,
  Trash2,
  Eye,
  Inbox,
  ArrowUpDown,
  ArrowUp,
  ArrowDown,
  AlertCircle,
} from 'lucide-react'
import { Pagination } from '../Pagination'
import type { CrudRow, ResourceConfig } from './types'

const TH_CLASS = 'px-5 py-3 text-xs font-semibold uppercase tracking-wider text-[#64748B]'

interface Props {
  config: ResourceConfig
  rows: CrudRow[]
  loading: boolean
  listError: string | null
  page: number
  pageSize: number
  totalPages: number
  totalElements: number
  sortBy: string
  sortDir: 'asc' | 'desc'
  search: string
  searchInput: string
  onSearchInputChange: (value: string) => void
  onSearchSubmit: (e: React.FormEvent) => void
  onClearSearch: () => void
  onRefresh: () => void
  onToggleSort: (field: string) => void
  onPageChange: (page: number) => void
  onView: (row: CrudRow) => void
  onEdit: (row: CrudRow) => void
  onDelete: (row: CrudRow) => void
}

export function CrudTable({
  config,
  rows,
  loading,
  listError,
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
  onEdit,
  onDelete,
}: Props) {
  const sortable = config.sortable ?? []
  const colCount = config.columns.length + 2

  // Arrow reflecting the current sort state
  const SortIcon = ({ field }: { field: string }) => {
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
          <h2 className="text-base font-bold text-[#0F172A]">{config.title} List</h2>

          <div className="flex items-center gap-3">
            {config.searchable !== false && (
              <form onSubmit={onSearchSubmit} className="relative">
                <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-[#94A3B8]" />
                <input
                  type="text"
                  value={searchInput}
                  onChange={(e) => onSearchInputChange(e.target.value)}
                  placeholder={`Search ${config.title.toLowerCase()}...`}
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
            )}
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
            Showing {rows.length} of {totalElements} {config.title.toLowerCase()}
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
              {config.columns.map((col) => {
                const canSort = sortable.includes(col.key)
                return (
                  <th
                    key={col.key}
                    className={canSort ? `cursor-pointer hover:text-[#1E293B] ${TH_CLASS}` : TH_CLASS}
                    onClick={canSort ? () => onToggleSort(col.key) : undefined}
                  >
                    {canSort ? (
                      <div className="flex items-center gap-1.5">{col.label} <SortIcon field={col.key} /></div>
                    ) : (
                      col.label
                    )}
                  </th>
                )
              })}
              <th className={TH_CLASS}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={colCount} className="px-5 py-16 text-center">
                  <div className="flex flex-col items-center gap-3">
                    <Loader2 className="h-6 w-6 animate-spin text-[#2563EB]" />
                    <span className="text-sm text-[#64748B]">Loading {config.title.toLowerCase()}...</span>
                  </div>
                </td>
              </tr>
            ) : listError ? (
              <tr>
                <td colSpan={colCount} className="px-5 py-16 text-center">
                  <div className="flex flex-col items-center gap-3">
                    <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-[#FEF2F2] text-[#EF4444]">
                      <AlertCircle className="h-7 w-7" />
                    </div>
                    <p className="text-sm font-medium text-[#475569]">{listError}</p>
                    <button
                      type="button"
                      onClick={onRefresh}
                      className="rounded-lg border border-[#E2E8F0] px-3 py-1.5 text-xs font-medium text-[#475569] hover:bg-[#F8FAFC] cursor-pointer"
                    >
                      Try again
                    </button>
                  </div>
                </td>
              </tr>
            ) : rows.length === 0 ? (
              <tr>
                <td colSpan={colCount} className="px-5 py-16 text-center">
                  <div className="flex flex-col items-center gap-3">
                    <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-[#F1F5F9] text-[#94A3B8]">
                      <Inbox className="h-7 w-7" />
                    </div>
                    <p className="text-sm font-medium text-[#475569]">No {config.title.toLowerCase()} found</p>
                    <p className="text-xs text-[#94A3B8]">
                      {search ? 'Try a different search term' : `Click "Add ${config.singular}" to create one`}
                    </p>
                  </div>
                </td>
              </tr>
            ) : (
              rows.map((row, idx) => (
                <tr key={row.id ?? idx} className="border-b border-[#F1F5F9] transition hover:bg-[#F8FAFC]">
                  <td className="px-5 py-3.5 text-xs text-[#94A3B8]">{page * pageSize + idx + 1}</td>
                  {config.columns.map((col) => (
                    <td
                      key={col.key}
                      className={`px-5 py-3.5 text-xs ${col.mono ? 'font-mono font-semibold' : ''} ${col.className || 'text-[#475569]'}`}
                    >
                      {col.render ? col.render(row) : (row[col.key] ?? '—')}
                    </td>
                  ))}
                  <td className="px-5 py-3.5">
                    <div className="flex items-center gap-1.5">
                      <button
                        type="button"
                        onClick={() => onView(row)}
                        title="View"
                        className="rounded-lg border border-[#E2E8F0] bg-white p-1.5 text-[#475569] transition hover:border-[#2563EB] hover:text-[#2563EB] cursor-pointer"
                      >
                        <Eye className="h-3.5 w-3.5" />
                      </button>
                      <button
                        type="button"
                        onClick={() => onEdit(row)}
                        title="Edit"
                        className="rounded-lg border border-[#E2E8F0] bg-white p-1.5 text-[#475569] transition hover:border-[#059669] hover:text-[#059669] cursor-pointer"
                      >
                        <Pencil className="h-3.5 w-3.5" />
                      </button>
                      <button
                        type="button"
                        onClick={() => onDelete(row)}
                        title="Delete"
                        className="rounded-lg border border-[#E2E8F0] bg-white p-1.5 text-[#475569] transition hover:border-[#EF4444] hover:text-[#EF4444] cursor-pointer"
                      >
                        <Trash2 className="h-3.5 w-3.5" />
                      </button>
                    </div>
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
