import type React from 'react'
import { TABULAR } from './theme'

interface Props {
  label: string
  value: string | number
  // One line of context under the figure
  hint?: string
  icon?: React.ReactNode
  accent?: string
}

/**
 * A single headline figure. No plot, so no hover layer — the number is the chart.
 */
export function StatTile({ label, value, hint, icon, accent = '#2a78d6' }: Props) {
  return (
    <div className="rounded-2xl border border-[#E2E8F0] bg-white p-4 shadow-sm">
      <div className="flex items-start justify-between gap-2">
        <p className="text-[11px] font-semibold uppercase tracking-wider text-[#94A3B8]">
          {label}
        </p>
        {icon && (
          <span
            className="flex h-7 w-7 shrink-0 items-center justify-center rounded-lg"
            style={{ background: `${accent}14`, color: accent }}
          >
            {icon}
          </span>
        )}
      </div>

      <p
        className="mt-2 text-[26px] font-bold leading-none tracking-tight text-[#0F172A]"
        style={TABULAR}
      >
        {value}
      </p>

      {hint && <p className="mt-1.5 text-[11px] text-[#94A3B8]">{hint}</p>}
    </div>
  )
}
