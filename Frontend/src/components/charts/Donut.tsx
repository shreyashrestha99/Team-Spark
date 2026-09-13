import { INK, SERIES, TABULAR } from './theme'
import type { Datum } from './BarColumns'

interface Props {
  data: Datum[]
  size?: number
  // Shown in the hole
  centerValue?: string
  centerLabel?: string
}

/**
 * Share of a whole across a handful of categories.
 * Segments carry a 2px surface gap so adjacent hues never touch, and the
 * legend direct-labels each one so identity is never colour-alone.
 */
export function Donut({ data, size = 150, centerValue, centerLabel }: Props) {
  const total = data.reduce((sum, d) => sum + d.value, 0)
  const radius = size / 2 - 10
  const circumference = 2 * Math.PI * radius
  const strokeWidth = 18

  let offset = 0

  return (
    <div className="flex items-center gap-5">
      <svg
        width={size}
        height={size}
        viewBox={`0 0 ${size} ${size}`}
        className="shrink-0"
        role="img"
        aria-label="Share of sessions by type"
      >
        <g transform={`rotate(-90 ${size / 2} ${size / 2})`}>
          {data.map((d, i) => {
            const share = total > 0 ? d.value / total : 0
            // 2px visual gap between neighbouring segments
            const dash = Math.max(share * circumference - 2, 0)
            const segment = (
              <circle
                key={d.label}
                cx={size / 2}
                cy={size / 2}
                r={radius}
                fill="none"
                stroke={SERIES[i % SERIES.length]}
                strokeWidth={strokeWidth}
                strokeDasharray={`${dash} ${circumference - dash}`}
                strokeDashoffset={-offset}
                className="transition-opacity hover:opacity-80"
              >
                <title>{`${d.label}: ${d.value} (${Math.round(share * 100)}%)`}</title>
              </circle>
            )
            offset += share * circumference
            return segment
          })}
        </g>

        {centerValue && (
          <>
            <text
              x={size / 2}
              y={size / 2 - 2}
              textAnchor="middle"
              fontSize={22}
              fontWeight={700}
              fill={INK.primary}
            >
              {centerValue}
            </text>
            <text
              x={size / 2}
              y={size / 2 + 15}
              textAnchor="middle"
              fontSize={10}
              fill={INK.muted}
            >
              {centerLabel}
            </text>
          </>
        )}
      </svg>

      {/* Legend doubles as the direct labels */}
      <ul className="min-w-0 flex-1 space-y-2">
        {data.map((d, i) => {
          const share = total > 0 ? Math.round((d.value / total) * 100) : 0
          return (
            <li key={d.label} className="flex items-center gap-2.5">
              <span
                className="h-2.5 w-2.5 shrink-0 rounded-sm"
                style={{ background: SERIES[i % SERIES.length] }}
                aria-hidden="true"
              />
              <span className="min-w-0 flex-1 truncate text-[12px] text-[#475569]">
                {d.label}
              </span>
              <span
                className="shrink-0 text-[12px] font-semibold text-[#0F172A]"
                style={TABULAR}
              >
                {share}%
              </span>
            </li>
          )
        })}
      </ul>
    </div>
  )
}
