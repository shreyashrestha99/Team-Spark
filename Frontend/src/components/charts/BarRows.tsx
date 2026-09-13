import { TABULAR, barPath, rampStep } from './theme'
import type { Datum } from './BarColumns'

interface Props {
  data: Datum[]
  // Fixed ceiling, e.g. 100 for percentages; defaults to the largest value
  max?: number
  unit?: string
  // Width reserved for the category labels
  labelWidth?: number
  // Override the ramp with one flat colour per row
  colorFor?: (d: Datum) => string
}

/**
 * Horizontal bars for categories with long names — programmes, rooms, lecturers.
 * Every row is direct-labelled, which also satisfies the contrast relief rule.
 */
export function BarRows({ data, max, unit = '', labelWidth = 116, colorFor }: Props) {
  const ceiling = max ?? Math.max(...data.map((d) => d.value), 1)
  const rowHeight = 30
  const barHeight = 14

  return (
    <div className="w-full">
      {data.map((d) => {
        const pct = Math.min(Math.max(d.value / ceiling, 0), 1)

        return (
          <div
            key={d.label}
            className="group flex items-center gap-3"
            style={{ height: rowHeight }}
            title={`${d.label}: ${d.value}${unit}${d.detail ? ` — ${d.detail}` : ''}`}
          >
            <span
              className="shrink-0 truncate text-[12px] text-[#475569]"
              style={{ width: labelWidth }}
            >
              {d.label}
            </span>

            <svg
              height={barHeight}
              className="min-w-0 flex-1"
              preserveAspectRatio="none"
              viewBox={`0 0 100 ${barHeight}`}
              role="img"
              aria-label={`${d.label}: ${d.value}${unit}`}
            >
              {/* Track shows the unused remainder */}
              <rect x={0} y={0} width={100} height={barHeight} rx={4} fill="#F1F5F9" />
              <path
                d={barPath(0, 0, Math.max(pct * 100, d.value > 0 ? 1.5 : 0), barHeight, 4, false)}
                fill={colorFor ? colorFor(d) : rampStep(d.value, ceiling)}
                className="transition-opacity group-hover:opacity-80"
              />
            </svg>

            <span
              className="w-[62px] shrink-0 text-right text-[12px] font-semibold text-[#0F172A]"
              style={TABULAR}
            >
              {d.value}
              {unit}
            </span>
          </div>
        )
      })}
    </div>
  )
}
