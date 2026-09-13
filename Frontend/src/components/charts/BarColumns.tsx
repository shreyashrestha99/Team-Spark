import { INK, TABULAR, barPath, rampStep } from './theme'

export interface Datum {
  label: string
  value: number
  detail?: string
}

interface Props {
  data: Datum[]
  height?: number
  unit?: string
  // Label every bar rather than relying on the axis alone
  showValues?: boolean
}

/**
 * Vertical bars for an ordered category — weekdays, periods of the day.
 * One series, one hue ramp, so no legend is needed: the card title names it.
 */
export function BarColumns({ data, height = 190, unit = '', showValues = true }: Props) {
  const max = Math.max(...data.map((d) => d.value), 1)
  const plotHeight = height - 34
  const gap = 10

  return (
    <div className="w-full">
      <svg
        viewBox={`0 0 ${data.length * 60} ${height}`}
        className="h-auto w-full"
        role="img"
        aria-label={`Bar chart across ${data.length} categories`}
        preserveAspectRatio="none"
      >
        {/* Recessive gridlines at quarters */}
        {[0.25, 0.5, 0.75, 1].map((tick) => (
          <line
            key={tick}
            x1={0}
            x2={data.length * 60}
            y1={plotHeight - plotHeight * tick}
            y2={plotHeight - plotHeight * tick}
            stroke={INK.grid}
            strokeWidth={1}
          />
        ))}

        {data.map((d, i) => {
          const barHeight = Math.max((d.value / max) * plotHeight, d.value > 0 ? 3 : 0)
          const x = i * 60 + gap / 2
          const width = 60 - gap
          const y = plotHeight - barHeight

          return (
            <g key={d.label} className="group">
              <path
                d={barPath(x, y, width, barHeight, 4, true)}
                fill={rampStep(d.value, max)}
                className="transition-opacity group-hover:opacity-80"
              >
                <title>{`${d.label}: ${d.value}${unit}${d.detail ? ` — ${d.detail}` : ''}`}</title>
              </path>

              {showValues && d.value > 0 && (
                <text
                  x={x + width / 2}
                  y={y - 6}
                  textAnchor="middle"
                  fontSize={11}
                  fontWeight={600}
                  fill={INK.secondary}
                  style={TABULAR}
                >
                  {d.value}
                </text>
              )}

              <text
                x={x + width / 2}
                y={height - 8}
                textAnchor="middle"
                fontSize={11}
                fill={INK.muted}
              >
                {d.label}
              </text>
            </g>
          )
        })}

        <line
          x1={0}
          x2={data.length * 60}
          y1={plotHeight}
          y2={plotHeight}
          stroke={INK.baseline}
          strokeWidth={1}
        />
      </svg>
    </div>
  )
}
