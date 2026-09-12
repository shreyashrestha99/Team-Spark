import { CalendarDays, Clock, DoorOpen, ShieldCheck, Users } from 'lucide-react'
import { hhmm, longDate, relativeDays } from './format'
import type { InvigilationDuty } from './types'

interface Props {
  duty: InvigilationDuty
  // The next duty is shown large at the top of the overview
  featured?: boolean
}

/** One invigilation duty: when, which halls, how many candidates, and in what role. */
export function DutyCard({ duty, featured = false }: Props) {
  const chief = duty.role?.toUpperCase() === 'CHIEF'

  return (
    <div className={`overflow-hidden rounded-xl border bg-white ${
      featured ? 'border-[#7C3AED] shadow-lg shadow-[#7C3AED]/10' : 'border-[#E2E8F0]'
    } ${duty.upcoming ? '' : 'opacity-60'}`}>
      {featured && (
        <div className="bg-[#7C3AED] px-5 py-2 text-xs font-semibold uppercase tracking-wider text-white">
          Your next invigilation · {relativeDays(duty.daysAway)}
        </div>
      )}

      <div className="p-5">
        <div className="mb-1 flex flex-wrap items-center gap-2">
          <span className="font-mono text-sm font-bold text-[#7C3AED]">{duty.moduleCode}</span>
          <span className={`inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-[10px] font-bold uppercase ${
            chief ? 'bg-[#7C3AED] text-white' : 'bg-[#F3E8FF] text-[#7C3AED]'
          }`}>
            <ShieldCheck className="h-3 w-3" />
            {chief ? 'Chief invigilator' : 'Invigilator'}
          </span>
          {!featured && (
            <span className="rounded-full bg-[#F1F5F9] px-2 py-0.5 text-[10px] font-semibold text-[#475569]">
              {relativeDays(duty.daysAway)}
            </span>
          )}
        </div>

        <h4 className={`font-bold text-[#0F172A] ${featured ? 'text-xl' : 'text-base'}`}>{duty.moduleName}</h4>
        <p className="text-xs text-[#64748B]">{duty.batchName}</p>

        <div className="mt-3 grid gap-2 text-sm text-[#475569] sm:grid-cols-3">
          <span className="flex items-center gap-2">
            <CalendarDays className="h-4 w-4 text-[#94A3B8]" />
            {longDate(duty.examDate)}
          </span>
          <span className="flex items-center gap-2">
            <Clock className="h-4 w-4 text-[#94A3B8]" />
            {hhmm(duty.startTime)} – {hhmm(duty.endTime)}
          </span>
          <span className="flex items-center gap-2">
            <Users className="h-4 w-4 text-[#94A3B8]" />
            {duty.candidateCount} candidate(s)
          </span>
        </div>

        <div className="mt-3 flex flex-wrap items-center gap-2">
          <DoorOpen className="h-4 w-4 text-[#94A3B8]" />
          {duty.rooms.length === 0 ? (
            <span className="text-sm text-[#94A3B8]">Halls not allocated yet</span>
          ) : (
            duty.rooms.map((room) => (
              <span key={room} className="rounded-md border border-[#E2E8F0] bg-[#F8FAFC] px-2 py-1 font-mono text-xs font-semibold text-[#1E293B]">
                {room}
              </span>
            ))
          )}
        </div>
      </div>
    </div>
  )
}
