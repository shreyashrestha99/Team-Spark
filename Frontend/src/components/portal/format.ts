// Shared formatting for the student and lecturer portals

// "15:00:00" -> "15:00"
export function hhmm(time?: string | null): string {
  return time ? time.slice(0, 5) : '—'
}

/**
 * yyyy-mm-dd in local time. toISOString would give the UTC date, which in Nepal
 * (UTC+5:45) is still yesterday for the first hours of every morning.
 */
export function localIsoDate(date: Date): string {
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${date.getFullYear()}-${month}-${day}`
}

// Parses yyyy-mm-dd as a local date rather than UTC midnight
export function parseLocalDate(iso: string): Date {
  const [year, month, day] = iso.split('-').map(Number)
  return new Date(year, month - 1, day)
}

// Nepal teaches Sunday to Friday, so a week starts on Sunday
export function startOfWeek(date: Date): Date {
  const start = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  start.setDate(start.getDate() - start.getDay())
  return start
}

export function addDays(date: Date, days: number): Date {
  const next = new Date(date)
  next.setDate(next.getDate() + days)
  return next
}

// "Fri, 18 Sep 2026"
export function longDate(iso: string): string {
  return parseLocalDate(iso).toLocaleDateString('en-GB', {
    weekday: 'short', day: 'numeric', month: 'short', year: 'numeric',
  })
}

// "18 Sep"
export function shortDate(date: Date): string {
  return date.toLocaleDateString('en-GB', { day: 'numeric', month: 'short' })
}

// "Today", "Tomorrow", "In 5 days", "3 days ago"
export function relativeDays(daysAway: number): string {
  if (daysAway === 0) return 'Today'
  if (daysAway === 1) return 'Tomorrow'
  if (daysAway > 1) return `In ${daysAway} days`
  if (daysAway === -1) return 'Yesterday'
  return `${Math.abs(daysAway)} days ago`
}

// A colour per session type, matching the admin routine grid
export const SESSION_TYPE_STYLES: Record<string, string> = {
  LECTURE: 'bg-[#EFF6FF] border-[#BFDBFE] text-[#1D4ED8]',
  TUTORIAL: 'bg-[#F0FDF4] border-[#BBF7D0] text-[#15803D]',
  WORKSHOP: 'bg-[#FEF3C7] border-[#FDE68A] text-[#B45309]',
  LAB: 'bg-[#FEF3C7] border-[#FDE68A] text-[#B45309]',
}

export function sessionTypeStyle(type?: string | null): string {
  return SESSION_TYPE_STYLES[(type ?? '').toUpperCase()] ?? 'bg-[#F8FAFC] border-[#E2E8F0] text-[#475569]'
}
