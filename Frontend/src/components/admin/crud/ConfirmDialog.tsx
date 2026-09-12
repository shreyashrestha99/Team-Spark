import { AlertTriangle, Loader2, Trash2 } from 'lucide-react'

interface Props {
  title: string
  message: string
  error: string | null
  busy: boolean
  onConfirm: () => void
  onCancel: () => void
}

export function ConfirmDialog({ title, message, error, busy, onConfirm, onCancel }: Props) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4 backdrop-blur-sm">
      <div className="w-full max-w-md rounded-2xl border border-[#E2E8F0] bg-white p-6 shadow-2xl">
        <div className="mb-4 flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#FEF2F2] text-[#EF4444]">
            <AlertTriangle className="h-5 w-5" />
          </div>
          <h2 className="text-lg font-bold text-[#0F172A]">{title}</h2>
        </div>

        <p className="text-sm text-[#475569]">{message}</p>

        {error && (
          <div className="mt-4 rounded-xl bg-[#FEF2F2] px-3 py-2.5 text-sm text-[#DC2626]">
            {error}
          </div>
        )}

        <div className="mt-6 flex items-center justify-end gap-3">
          <button
            type="button"
            onClick={onCancel}
            className="rounded-xl border border-[#E2E8F0] px-5 py-2.5 text-sm font-medium text-[#475569] transition hover:bg-[#F8FAFC] cursor-pointer"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={busy}
            className="inline-flex items-center gap-2 rounded-xl bg-[#EF4444] px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-[#DC2626] disabled:opacity-60 cursor-pointer"
          >
            {busy ? (
              <><Loader2 className="h-4 w-4 animate-spin" /><span>Deleting...</span></>
            ) : (
              <><Trash2 className="h-4 w-4" /><span>Delete</span></>
            )}
          </button>
        </div>
      </div>
    </div>
  )
}
