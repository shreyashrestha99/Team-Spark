import { Plus } from 'lucide-react'

interface Props {
  title: string
  subtitle: string
  // Only user tabs need an action here; CRUD sections have their own
  addLabel?: string
  onAddClick?: () => void
}

export function AdminTopbar({ title, subtitle, addLabel, onAddClick }: Props) {
  return (
    <header className="sticky top-0 z-20 flex h-[72px] items-center justify-between border-b border-[#E2E8F0] bg-white/95 px-6 backdrop-blur-md">
      <div>
        <h1 className="text-xl font-bold text-[#0F172A]">{title}</h1>
        <p className="text-xs text-[#94A3B8]">{subtitle}</p>
      </div>

      {onAddClick && addLabel && (
        <button
          type="button"
          onClick={onAddClick}
          className="inline-flex items-center gap-2 rounded-xl bg-[#2563EB] px-5 py-2.5 text-sm font-semibold text-white shadow-md shadow-[#2563EB]/20 transition hover:bg-[#1D4ED8] hover:shadow-lg focus:outline-none focus:ring-4 focus:ring-[#2563EB]/20 cursor-pointer"
        >
          <Plus className="h-4 w-4" />
          <span>Add {addLabel}</span>
        </button>
      )}
    </header>
  )
}
