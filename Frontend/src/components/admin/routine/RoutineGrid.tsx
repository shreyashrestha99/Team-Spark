import { useMemo } from 'react'
import type { RoutineSession } from './types'

interface Props {
  sessions: RoutineSession[]
}

// Nepal teaches Sunday to Friday, with Saturday the weekend
const DAYS = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY']

const DAY_LABELS: Record<string, string> = {
  SUNDAY: 'Sun', MONDAY: 'Mon', TUESDAY: 'Tue',
  WEDNESDAY: 'Wed', THURSDAY: 'Thu', FRIDAY: 'Fri',
}

// A colour per session type, so the shape of the week reads at a glance
const TYPE_STYLES: Record<string, string> = {
  LECTURE: 'bg-[#EFF6FF] border-[#BFDBFE] text-[#1D4ED8]',
  TUTORIAL: 'bg-[#F0FDF4] border-[#BBF7D0] text-[#15803D]',
  WORKSHOP: 'bg-[#FEF3C7] border-[#FDE68A] text-[#B45309]',
  LAB: 'bg-[#FEF3C7] border-[#FDE68A] text-[#B45309]',
}

function hhmm(time: string): string {
  return time ? time.slice(0, 5) : ''
}

export function RoutineGrid({ sessions }: Props) {
  // Periods present in the result, so the grid never draws empty trailing rows
  const periods = useMemo(() => {
    const found = new Set<number>()
    sessions.forEach((session) => {
      if (session.periodNumber != null) found.add(session.periodNumber)
    })
    return [...found].sort((left, right) => left - right)
  }, [sessions])

  // Fast lookup keyed by day and period
  const byCell = useMemo(() => {
    const map = new Map<string, RoutineSession[]>()
    sessions.forEach((session) => {
      const key = `${session.dayOfWeek}|${session.periodNumber}`
      const bucket = map.get(key)
      if (bucket) bucket.push(session)
      else map.set(key, [session])
    })
    return map
  }, [sessions])

  // The time label for a period, taken from whichever session sits there
  const periodTime = useMemo(() => {
    const map = new Map<number, string>()
    sessions.forEach((session) => {
      if (session.periodNumber != null && !map.has(session.periodNumber)) {
        map.set(session.periodNumber, hhmm(session.startTime))
      }
    })
    return map
  }, [sessions])

  if (sessions.length === 0) {
    return null
  }

  return (
    <div className="rounded-xl border border-[#E2E8F0] bg-white p-5">
      <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
        <div>
          <h3 className="text-sm font-semibold text-[#0F172A]">Weekly pattern</h3>
          <p className="text-[11px] text-[#94A3B8]">
            This one week repeats across the semester, skipping holidays.
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-3 text-[11px]">
          {['LECTURE', 'TUTORIAL', 'WORKSHOP'].map((type) => (
            <span key={type} className="flex items-center gap-1.5">
              <span className={`h-2.5 w-2.5 rounded-sm border ${TYPE_STYLES[type]}`} />
              <span className="text-[#64748B]">{type[0] + type.slice(1).toLowerCase()}</span>
            </span>
          ))}
        </div>
      </div>

      {/* Wide grids scroll inside this container rather than the page */}
      <div className="overflow-x-auto">
        <table className="w-full min-w-[900px] border-separate border-spacing-1 text-left">
          <thead>
            <tr>
              <th className="w-16 pb-1 text-[11px] font-medium text-[#94A3B8]">Period</th>
              {DAYS.map((day) => (
                <th key={day} className="pb-1 text-[11px] font-medium text-[#94A3B8]">
                  {DAY_LABELS[day]}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {periods.map((period) => (
              <tr key={period}>
                <td className="align-top">
                  <div className="rounded-md bg-[#F8FAFC] px-2 py-2 text-center">
                    <div className="text-xs font-semibold text-[#475569]">P{period}</div>
                    <div className="font-mono text-[10px] text-[#94A3B8]">
                      {periodTime.get(period) ?? ''}
                    </div>
                  </div>
                </td>

                {DAYS.map((day) => {
                  const cell = byCell.get(`${day}|${period}`) ?? []
                  return (
                    <td key={day} className="align-top">
                      {cell.length === 0 ? (
                        <div className="h-full min-h-[54px] rounded-md border border-dashed border-[#F1F5F9]" />
                      ) : (
                        <div className="space-y-1">
                          {cell.map((session, index) => (
                            <div
                              key={`${session.moduleCode}-${session.groupName ?? 'all'}-${index}`}
                              className={`rounded-md border px-2 py-1.5 ${
                                TYPE_STYLES[session.sessionType] ?? 'bg-[#F8FAFC] border-[#E2E8F0] text-[#475569]'
                              }`}
                            >
                              <div className="font-mono text-[11px] font-semibold">
                                {session.moduleCode}
                              </div>
                              <div className="text-[10px] opacity-80">
                                {session.groupName ?? 'Whole batch'}
                              </div>
                              <div className="font-mono text-[10px] opacity-70">
                                {session.roomCode} · {hhmm(session.startTime)}–{hhmm(session.endTime)}
                              </div>
                            </div>
                          ))}
                        </div>
                      )}
                    </td>
                  )
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
