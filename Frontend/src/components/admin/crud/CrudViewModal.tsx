import { X, FileText } from 'lucide-react'
import type { CrudRow, ResourceConfig } from './types'

interface Props {
  config: ResourceConfig
  row: CrudRow
  onClose: () => void
}

// Turns "programmeCode" into "Programme Code"
function humanize(key: string): string {
  return key
    .replace(/([A-Z])/g, ' $1')
    .replace(/^./, (c) => c.toUpperCase())
    .trim()
}

// Renders any value as readable text
function display(value: unknown): string {
  if (value === null || value === undefined || value === '') return '—'
  if (typeof value === 'boolean') return value ? 'Yes' : 'No'
  return String(value)
}

export function CrudViewModal({ config, row, onClose }: Props) {
  // Internal ids and arrays add noise to the detail view
  const entries = Object.entries(row).filter(
    ([key, value]) => !key.toLowerCase().endsWith('id') && !Array.isArray(value) && typeof value !== 'object'
  )

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto bg-black/40 px-4 py-6 backdrop-blur-sm">
      <div className="relative max-h-[90vh] w-full max-w-lg overflow-y-auto rounded-2xl border border-[#E2E8F0] bg-white p-6 shadow-2xl sm:p-8">
        <button
          type="button"
          onClick={onClose}
          className="absolute right-4 top-4 rounded-lg p-1.5 text-[#94A3B8] transition hover:bg-[#F1F5F9] hover:text-[#1E293B] cursor-pointer"
        >
          <X className="h-5 w-5" />
        </button>

        <div className="mb-6 flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#EFF6FF] text-[#2563EB]">
            <FileText className="h-5 w-5" />
          </div>
          <div>
            <h2 className="text-xl font-bold text-[#0F172A]">{config.singular} Details</h2>
            <p className="text-sm text-[#64748B]">Full record as stored in the system.</p>
          </div>
        </div>

        <div className="space-y-2">
          {entries.map(([key, value]) => (
            <div key={key} className="flex items-start justify-between gap-4 rounded-xl bg-[#F8FAFC] px-4 py-2.5">
              <p className="text-xs text-[#94A3B8]">{humanize(key)}</p>
              <p className="text-right text-sm font-medium text-[#1E293B]">{display(value)}</p>
            </div>
          ))}
        </div>

        <button
          type="button"
          onClick={onClose}
          className="mt-6 w-full rounded-xl border border-[#E2E8F0] py-2.5 text-sm font-medium text-[#475569] transition hover:bg-[#F8FAFC] cursor-pointer"
        >
          Close
        </button>
      </div>
    </div>
  )
}
