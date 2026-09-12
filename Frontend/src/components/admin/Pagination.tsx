import { ChevronLeft, ChevronRight } from 'lucide-react'

const MAX_PAGE_BUTTONS = 7

interface Props {
  page: number
  totalPages: number
  onPageChange: (page: number) => void
}

export function Pagination({ page, totalPages, onPageChange }: Props) {
  if (totalPages <= 0) return null

  // Window of page numbers around the current page
  const buttons = Array.from({ length: Math.min(totalPages, MAX_PAGE_BUTTONS) }, (_, i) => {
    if (totalPages <= MAX_PAGE_BUTTONS) return i
    if (page < 3) return i
    if (page > totalPages - 4) return totalPages - MAX_PAGE_BUTTONS + i
    return page - 3 + i
  })

  return (
    <div className="flex items-center justify-between border-t border-[#E2E8F0] px-5 py-3">
      <button
        type="button"
        disabled={page <= 0}
        onClick={() => onPageChange(Math.max(0, page - 1))}
        className="inline-flex items-center gap-1.5 rounded-lg border border-[#E2E8F0] px-3 py-1.5 text-sm font-medium text-[#475569] transition hover:bg-[#F8FAFC] disabled:opacity-40 disabled:cursor-not-allowed cursor-pointer"
      >
        <ChevronLeft className="h-4 w-4" />
        Previous
      </button>

      <div className="flex items-center gap-1">
        {buttons.map((pageNum) => (
          <button
            key={pageNum}
            type="button"
            onClick={() => onPageChange(pageNum)}
            className={`flex h-8 w-8 items-center justify-center rounded-lg text-sm font-medium transition cursor-pointer ${
              page === pageNum ? 'bg-[#2563EB] text-white shadow-sm' : 'text-[#64748B] hover:bg-[#F1F5F9]'
            }`}
          >
            {pageNum + 1}
          </button>
        ))}
      </div>

      <button
        type="button"
        disabled={page >= totalPages - 1}
        onClick={() => onPageChange(page + 1)}
        className="inline-flex items-center gap-1.5 rounded-lg border border-[#E2E8F0] px-3 py-1.5 text-sm font-medium text-[#475569] transition hover:bg-[#F8FAFC] disabled:opacity-40 disabled:cursor-not-allowed cursor-pointer"
      >
        Next
        <ChevronRight className="h-4 w-4" />
      </button>
    </div>
  )
}
