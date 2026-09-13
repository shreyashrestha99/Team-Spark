import { ChartCard, ChartEmpty } from '../charts/ChartCard'
import { BarColumns, type Datum } from '../charts/BarColumns'
import { Donut } from '../charts/Donut'
import { BarRows } from '../charts/BarRows'
import { STATUS, TABULAR, rampStep } from '../charts/theme'
import type { PortalSession } from './types'

// Nepali academic week: Sunday through Friday, Saturday is the weekend
const WEEK = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY']

interface Props {
  sessions: PortalSession[]
  contactHours: number
  // Only lecturers have a contracted ceiling to measure against
  maxWeeklyHours?: number
}

/**
 * The personal equivalent of the admin dashboard: how one person's own week is
 * shaped. Everything is derived from the sessions already on screen, so it needs
 * no extra request.
 */
export function PortalAnalytics({ sessions, contactHours, maxWeeklyHours }: Props) {
  const byDay = weekdayLoad(sessions)
  const byType = typeMix(sessions)
  const byModule = moduleLoad(sessions)

  const totalSessions = sessions.length
  const loadPct = maxWeeklyHours && maxWeeklyHours > 0
    ? Math.round((contactHours / maxWeeklyHours) * 100)
    : null
  const overloaded = loadPct !== null && loadPct > 100

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <ChartCard
          title="My week by day"
          subtitle="Where your classes concentrate"
          aside={
            <>
              <p className="text-[20px] font-bold leading-none text-[#0F172A]" style={TABULAR}>
                {contactHours}h
              </p>
              <p className="text-[11px] text-[#94A3B8]">contact time</p>
            </>
          }
        >
          {byDay.length === 0 ? (
            <ChartEmpty message="Nothing scheduled in the week ahead." />
          ) : (
            <BarColumns data={byDay} height={170} />
          )}
        </ChartCard>

        <ChartCard title="Session mix" subtitle="How your week is delivered">
          {totalSessions === 0 ? (
            <ChartEmpty message="Nothing scheduled yet." />
          ) : (
            <Donut data={byType} centerValue={String(totalSessions)} centerLabel="classes" />
          )}
        </ChartCard>
      </div>

      <ChartCard
        title="Hours by module"
        subtitle="Where your contact time actually goes"
        aside={
          loadPct !== null ? (
            <>
              <p
                className="text-[20px] font-bold leading-none"
                style={{ ...TABULAR, color: overloaded ? STATUS.critical : '#0F172A' }}
              >
                {loadPct}%
              </p>
              <p className="text-[11px] text-[#94A3B8]">of {maxWeeklyHours}h ceiling</p>
            </>
          ) : undefined
        }
      >
        {byModule.length === 0 ? (
          <ChartEmpty message="No modules scheduled yet." />
        ) : (
          <BarRows data={byModule} unit="h" labelWidth={150} />
        )}

        {overloaded && (
          <p className="mt-3 border-t border-[#F1F5F9] pt-3 text-[11px] font-medium" style={{ color: STATUS.critical }}>
            Teaching load is above the contracted weekly ceiling.
          </p>
        )}
      </ChartCard>
    </div>
  )
}

// Sessions per weekday, in academic order
function weekdayLoad(sessions: PortalSession[]): Datum[] {
  const counts = new Map<string, number>()
  for (const session of sessions) {
    counts.set(session.dayOfWeek, (counts.get(session.dayOfWeek) ?? 0) + 1)
  }

  return WEEK.filter((day) => day !== 'SATURDAY' || (counts.get(day) ?? 0) > 0).map((day) => ({
    label: day.slice(0, 3),
    value: counts.get(day) ?? 0,
  }))
}

// Lecture vs tutorial vs workshop
function typeMix(sessions: PortalSession[]): Datum[] {
  const counts = new Map<string, number>()
  for (const session of sessions) {
    const key = session.sessionType || 'OTHER'
    counts.set(key, (counts.get(key) ?? 0) + 1)
  }

  return [...counts.entries()]
    .sort((a, b) => b[1] - a[1])
    .map(([label, value]) => ({ label, value }))
}

// Contact hours per module, biggest first
function moduleLoad(sessions: PortalSession[]): Datum[] {
  const minutes = new Map<string, number>()
  for (const session of sessions) {
    const key = `${session.moduleCode} — ${session.moduleName}`
    minutes.set(key, (minutes.get(key) ?? 0) + session.durationMinutes)
  }

  return [...minutes.entries()]
    .map(([label, mins]) => ({ label, value: Math.round((mins / 60) * 10) / 10 }))
    .sort((a, b) => b.value - a.value)
    .slice(0, 8)
}

// Kept so the ramp import is used consistently across portal charts
export const portalRamp = rampStep
