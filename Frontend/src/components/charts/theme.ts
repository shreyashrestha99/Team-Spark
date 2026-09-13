// Chart palette — validated for colour-vision deficiency against a white card
// surface (adjacent CVD ΔE 9.2, normal-vision ΔE 27.6). Slot order is the
// safety mechanism, so assign in order and never cycle.
export const SERIES = ['#2a78d6', '#eb6834', '#1baf7a'] as const

// Single-hue ramp for magnitude, light → dark
export const SEQUENTIAL = ['#9ec5f4', '#5598e7', '#2a78d6', '#256abf', '#184f95'] as const

// Reserved for state only, never as "series 4". Always paired with a label.
export const STATUS = {
  good: '#0ca30c',
  warning: '#fab219',
  critical: '#d03b3b',
} as const

// Chrome kept recessive so the data carries the eye
export const INK = {
  primary: '#0F172A',
  secondary: '#64748B',
  muted: '#94A3B8',
  grid: '#EEF1F6',
  baseline: '#CBD5E1',
} as const

/** Picks a ramp step by magnitude, so bigger reads darker. */
export function rampStep(value: number, max: number): string {
  if (max <= 0) return SEQUENTIAL[2]
  const ratio = Math.min(Math.max(value / max, 0), 1)
  const index = Math.min(
    SEQUENTIAL.length - 1,
    Math.floor(ratio * SEQUENTIAL.length),
  )
  return SEQUENTIAL[index]
}

/**
 * A bar with a flat base and a rounded growing end.
 * `vertical` bars grow up from the baseline, `horizontal` bars grow right.
 */
export function barPath(
  x: number,
  y: number,
  width: number,
  height: number,
  radius = 4,
  vertical = true,
): string {
  if (width <= 0 || height <= 0) return ''
  const r = Math.max(0, Math.min(radius, vertical ? width / 2 : height / 2, vertical ? height : width))

  if (vertical) {
    // flat bottom, rounded top
    return `M${x},${y + height} L${x},${y + r} Q${x},${y} ${x + r},${y} L${x + width - r},${y} Q${x + width},${y} ${x + width},${y + r} L${x + width},${y + height} Z`
  }
  // flat left, rounded right
  return `M${x},${y} L${x + width - r},${y} Q${x + width},${y} ${x + width},${y + r} L${x + width},${y + height - r} Q${x + width},${y + height} ${x + width - r},${y + height} L${x},${y + height} Z`
}

// Keeps axis ticks and table columns aligned
export const TABULAR = { fontVariantNumeric: 'tabular-nums' } as const
