import { CalendarCheck, type LucideIcon } from 'lucide-react'
import { hhmm, sessionTypeStyle } from './format'
import type { PortalSession } from './types'

// ─── Summary tile ─────────────────────────────────────────────────────────────

interface StatTileProps {
  label: string
  value: string
  hint?: string
  icon: LucideIcon
  tone?: 'blue' | 'green' | 'amber' | 'red'
}

const TONES = {
  blue: 'bg-[#EFF6FF] text-[#2563EB]',
  green: 'bg-[#ECFDF5] text-[#059669]',
  amber: 'bg-[#FFFBEB] text-[#B45309]',
  red: 'bg-[#FEF2F2] text-[#DC2626]',
}

export function StatTile({ label, value, hint, icon: Icon, tone = 'blue' }: StatTileProps) {
  return (
    <div className="flex items-center gap-4 rounded-xl border border-[#E2E8F0] bg-white p-4">
      <div className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-xl ${TONES[tone]}`}>
        <Icon className="h-5 w-5" />
      </div>
      <div className="min-w-0">
        <p className="text-xs font-medium text-[#64748B]">{label}</p>
        <p className="truncate text-xl font-bold text-[#0F172A]">{value}</p>
        {hint && <p className="truncate text-[11px] text-[#94A3B8]">{hint}</p>}
      </div>
    </div>
  )
}

// ─── Today's classes ──────────────────────────────────────────────────────────

interface TodayClassesProps {
  sessions: PortalSession[]
  perspective: 'student' | 'teacher'
}

/** Ordered list of today's classes, with the current one highlighted. */
export function TodayClasses({ sessions, perspective }: TodayClassesProps) {
  const now = new Date()
  const nowTime = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`

  const ordered = [...sessions].sort((left, right) => left.startTime.localeCompare(right.startTime))

  return (
    <div className="rounded-xl border border-[#E2E8F0] bg-white p-5">
      <h3 className="mb-3 text-sm font-semibold text-[#0F172A]">Today&apos;s classes</h3>

      {ordered.length === 0 ? (
        <div className="rounded-lg border border-dashed border-[#CBD5E1] py-8 text-center">
          <CalendarCheck className="mx-auto mb-2 h-6 w-6 text-[#CBD5E1]" />
          <p className="text-sm text-[#64748B]">No classes today.</p>
        </div>
      ) : (
        <ul className="space-y-2">
          {ordered.map((session) => {
            const start = hhmm(session.startTime)
            const end = hhmm(session.endTime)
            const live = nowTime >= start && nowTime < end
            const over = nowTime >= end

            return (
              <li
                key={session.sessionId}
                className={`flex items-center gap-3 rounded-lg border p-3 ${
                  live ? 'border-[#2563EB] bg-[#EFF6FF]' : 'border-[#F1F5F9]'
                } ${over ? 'opacity-60' : ''}`}
              >
                <div className="w-20 shrink-0 font-mono text-xs text-[#475569]">
                  <div className="font-semibold">{start}</div>
                  <div className="text-[#94A3B8]">{end}</div>
                </div>

                <div className="min-w-0 flex-1">
                  <div className="flex items-center gap-2">
                    <span className="font-mono text-xs font-bold text-[#2563EB]">{session.moduleCode}</span>
                    <span className={`rounded border px-1.5 py-0.5 text-[9px] font-semibold uppercase ${sessionTypeStyle(session.sessionType)}`}>
                      {session.sessionType}
                    </span>
                    {live && (
                      <span className="rounded-full bg-[#2563EB] px-2 py-0.5 text-[9px] font-bold uppercase text-white">
                        Now
                      </span>
                    )}
                  </div>
                  <p className="truncate text-sm font-medium text-[#1E293B]">{session.moduleName}</p>
                  <p className="truncate text-[11px] text-[#94A3B8]">
                    {perspective === 'student'
                      ? session.teacherName ?? 'Lecturer TBC'
                      : `${session.batchName} · ${session.groupName ?? 'Whole batch'}`}
                  </p>
                </div>

                <div className="shrink-0 text-right">
                  <div className="font-mono text-sm font-bold text-[#0F172A]">{session.roomCode}</div>
                  <div className="text-[11px] text-[#94A3B8]">{session.roomName}</div>
                </div>
              </li>
            )
          })}
        </ul>
      )}
    </div>
  )
}
