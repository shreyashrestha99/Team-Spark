import { useCallback, useEffect, useMemo, useState } from 'react'
import { AlertTriangle, CalendarDays, ChevronLeft, ChevronRight, Loader2 } from 'lucide-react'
import { api } from '../../services/api'
import { extractError } from '../admin/crud/apiError'
import { addDays, hhmm, localIsoDate, sessionTypeStyle, shortDate, startOfWeek } from './format'
import type { PortalSession } from './types'

interface Props {
  // "/student/routine" or "/teacher/routine"
  endpoint: string
  // A student wants to know who teaches, a lecturer wants to know who is in the room
  perspective: 'student' | 'teacher'
}

// Nepal teaches Sunday to Friday
const TEACHING_DAYS = 6

/** One week of a personal timetable, with previous and next week navigation. */
export function WeekRoutine({ endpoint, perspective }: Props) {
  const [weekStart, setWeekStart] = useState(() => startOfWeek(new Date()))
  const [sessions, setSessions] = useState<PortalSession[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const days = useMemo(
    () => Array.from({ length: TEACHING_DAYS }, (_, index) => addDays(weekStart, index)),
    [weekStart],
  )

  const fetchWeek = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const res = await api.get(endpoint, {
        params: {
          fromDate: localIsoDate(weekStart),
          toDate: localIsoDate(addDays(weekStart, TEACHING_DAYS)),
        },
      })
      setSessions(res.data?.data ?? [])
    } catch (err) {
      setSessions([])
      setError(extractError(err, 'Could not load your routine.'))
    } finally {
      setLoading(false)
    }
  }, [endpoint, weekStart])

  useEffect(() => { fetchWeek() }, [fetchWeek])

  // Rows are the distinct start times in the week, so hand-made sessions show too
  const startTimes = useMemo(
    () => [...new Set(sessions.map((session) => hhmm(session.startTime)))].sort(),
    [sessions],
  )

  const byCell = useMemo(() => {
    const map = new Map<string, PortalSession[]>()
    sessions.forEach((session) => {
      const key = `${session.sessionDate}|${hhmm(session.startTime)}`
      const bucket = map.get(key)
      if (bucket) bucket.push(session)
      else map.set(key, [session])
    })
    return map
  }, [sessions])

  const totalHours = sessions.reduce((sum, session) => sum + (session.durationMinutes || 0), 0) / 60
  const todayIso = localIsoDate(new Date())
  const isCurrentWeek = localIsoDate(weekStart) === localIsoDate(startOfWeek(new Date()))

  return (
    <div className="rounded-xl border border-[#E2E8F0] bg-white p-5">
      <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h3 className="text-sm font-semibold text-[#0F172A]">
            {shortDate(days[0])} – {shortDate(days[TEACHING_DAYS - 1])}
          </h3>
          <p className="text-[11px] text-[#94A3B8]">
            {sessions.length} class(es) · {totalHours.toFixed(1)} hour(s)
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => setWeekStart((current) => addDays(current, -7))}
            className="rounded-lg border border-[#E2E8F0] p-2 text-[#475569] transition hover:bg-[#F8FAFC] cursor-pointer"
            title="Previous week"
          >
            <ChevronLeft className="h-4 w-4" />
          </button>
          <button
            type="button"
            onClick={() => setWeekStart(startOfWeek(new Date()))}
            disabled={isCurrentWeek}
            className="rounded-lg border border-[#E2E8F0] px-3 py-1.5 text-xs font-medium text-[#475569] transition hover:bg-[#F8FAFC] disabled:opacity-50 cursor-pointer disabled:cursor-default"
          >
            This week
          </button>
          <button
            type="button"
            onClick={() => setWeekStart((current) => addDays(current, 7))}
            className="rounded-lg border border-[#E2E8F0] p-2 text-[#475569] transition hover:bg-[#F8FAFC] cursor-pointer"
            title="Next week"
          >
            <ChevronRight className="h-4 w-4" />
          </button>
        </div>
      </div>

      <div className="mb-3 flex flex-wrap items-center gap-3 text-[11px] text-[#64748B]">
        {['LECTURE', 'TUTORIAL', 'WORKSHOP'].map((type) => (
          <span key={type} className="flex items-center gap-1.5">
            <span className={`h-2.5 w-2.5 rounded-sm border ${sessionTypeStyle(type)}`} />
            {type[0] + type.slice(1).toLowerCase()}
          </span>
        ))}
      </div>

      {error && (
        <div className="mb-3 flex items-start gap-2 rounded-lg bg-[#FEF2F2] px-3 py-2 text-sm text-[#B91C1C]">
          <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {loading ? (
        <div className="flex items-center justify-center py-12">
          <Loader2 className="h-6 w-6 animate-spin text-[#2563EB]" />
        </div>
      ) : sessions.length === 0 ? (
        <div className="rounded-lg border border-dashed border-[#CBD5E1] py-12 text-center">
          <CalendarDays className="mx-auto mb-2 h-6 w-6 text-[#CBD5E1]" />
          <p className="text-sm text-[#64748B]">No classes scheduled this week.</p>
        </div>
      ) : (
        // A full week is wide, so it scrolls inside the card rather than the page
        <div className="overflow-x-auto">
          <table className="w-full min-w-[860px] border-separate border-spacing-1 text-left">
            <thead>
              <tr>
                <th className="w-16 pb-1 text-[11px] font-medium text-[#94A3B8]">Time</th>
                {days.map((day) => {
                  const iso = localIsoDate(day)
                  const isToday = iso === todayIso
                  return (
                    <th key={iso} className="pb-1">
                      <div className={`rounded-md px-2 py-1 text-[11px] ${
                        isToday ? 'bg-[#2563EB] text-white' : 'text-[#94A3B8]'
                      }`}>
                        <span className="font-semibold">
                          {day.toLocaleDateString('en-GB', { weekday: 'short' })}
                        </span>{' '}
                        {shortDate(day)}
                      </div>
                    </th>
                  )
                })}
              </tr>
            </thead>
            <tbody>
              {startTimes.map((time) => (
                <tr key={time}>
                  <td className="align-top">
                    <div className="rounded-md bg-[#F8FAFC] px-2 py-2 text-center font-mono text-xs font-semibold text-[#475569]">
                      {time}
                    </div>
                  </td>

                  {days.map((day) => {
                    const cell = byCell.get(`${localIsoDate(day)}|${time}`) ?? []
                    return (
                      <td key={localIsoDate(day)} className="align-top">
                        {cell.length === 0 ? (
                          <div className="min-h-[58px] rounded-md border border-dashed border-[#F1F5F9]" />
                        ) : (
                          <div className="space-y-1">
                            {cell.map((session) => (
                              <div
                                key={session.sessionId}
                                title={session.moduleName}
                                className={`rounded-md border px-2 py-1.5 ${sessionTypeStyle(session.sessionType)}`}
                              >
                                <div className="flex items-center justify-between gap-1">
                                  <span className="font-mono text-[11px] font-semibold">{session.moduleCode}</span>
                                  <span className="text-[9px] uppercase opacity-70">{session.sessionType}</span>
                                </div>
                                <div className="truncate text-[10px] opacity-80">
                                  {perspective === 'student'
                                    ? session.teacherName ?? 'Lecturer TBC'
                                    : `${session.batchName} · ${session.groupName ?? 'Whole batch'}`}
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
      )}
    </div>
  )
}
