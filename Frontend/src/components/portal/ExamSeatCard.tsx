import { Armchair, Building2, CalendarDays, Clock, DoorOpen } from 'lucide-react'
import { hhmm, longDate, relativeDays } from './format'
import type { MyExamSeat } from './types'

interface Props {
  exam: MyExamSeat
  // The next exam is shown large at the top of the overview
  featured?: boolean
}

/** Where a student sits an exam, with the seat as the most prominent thing on the card. */
export function ExamSeatCard({ exam, featured = false }: Props) {
  const soon = exam.upcoming && exam.daysAway <= 3

  return (
    <div className={`overflow-hidden rounded-xl border bg-white ${
      featured ? 'border-[#2563EB] shadow-lg shadow-[#2563EB]/10' : 'border-[#E2E8F0]'
    } ${exam.upcoming ? '' : 'opacity-60'}`}>
      {featured && (
        <div className="bg-[#2563EB] px-5 py-2 text-xs font-semibold uppercase tracking-wider text-white">
          Your next exam · {relativeDays(exam.daysAway)}
        </div>
      )}

      <div className="flex flex-col gap-5 p-5 sm:flex-row sm:items-center">
        <div className="min-w-0 flex-1">
          <div className="mb-1 flex flex-wrap items-center gap-2">
            <span className="font-mono text-sm font-bold text-[#2563EB]">{exam.moduleCode}</span>
            {exam.examType && (
              <span className="rounded bg-[#F1F5F9] px-1.5 py-0.5 text-[10px] font-semibold uppercase text-[#475569]">
                {exam.examType}
              </span>
            )}
            {!featured && (
              <span className={`rounded-full px-2 py-0.5 text-[10px] font-semibold ${
                !exam.upcoming
                  ? 'bg-[#F1F5F9] text-[#64748B]'
                  : soon
                    ? 'bg-[#FEF3C7] text-[#B45309]'
                    : 'bg-[#EFF6FF] text-[#2563EB]'
              }`}>
                {relativeDays(exam.daysAway)}
              </span>
            )}
          </div>

          <h4 className={`font-bold text-[#0F172A] ${featured ? 'text-xl' : 'text-base'}`}>
            {exam.moduleName}
          </h4>

          <div className="mt-3 grid gap-2 text-sm text-[#475569] sm:grid-cols-2">
            <span className="flex items-center gap-2">
              <CalendarDays className="h-4 w-4 text-[#94A3B8]" />
              {longDate(exam.examDate)}
            </span>
            <span className="flex items-center gap-2">
              <Clock className="h-4 w-4 text-[#94A3B8]" />
              {hhmm(exam.startTime)} – {hhmm(exam.endTime)}
              {exam.durationMinutes ? ` (${exam.durationMinutes} min)` : ''}
            </span>
            <span className="flex items-center gap-2">
              <Building2 className="h-4 w-4 text-[#94A3B8]" />
              {exam.buildingName ?? 'Block TBC'}
            </span>
            <span className="flex items-center gap-2">
              <DoorOpen className="h-4 w-4 text-[#94A3B8]" />
              {exam.roomCode ?? '—'}
              {exam.roomName ? ` · ${exam.roomName}` : ''}
            </span>
          </div>
        </div>

        {/* The seat is what a student actually needs to find, so it gets the space */}
        <div className={`flex shrink-0 flex-col items-center justify-center rounded-xl border-2 border-dashed px-6 py-4 ${
          exam.upcoming ? 'border-[#2563EB] bg-[#EFF6FF]' : 'border-[#CBD5E1] bg-[#F8FAFC]'
        }`}>
          <Armchair className={`mb-1 h-5 w-5 ${exam.upcoming ? 'text-[#2563EB]' : 'text-[#94A3B8]'}`} />
          <span className="text-[10px] font-semibold uppercase tracking-wider text-[#64748B]">Seat</span>
          <span className={`font-mono font-black ${featured ? 'text-4xl' : 'text-3xl'} ${
            exam.upcoming ? 'text-[#1D4ED8]' : 'text-[#64748B]'
          }`}>
            {exam.seatNumber}
          </span>
          {exam.rowLabel && (
            <span className="text-[11px] text-[#64748B]">
              Row {exam.rowLabel} · Desk {exam.columnNumber}
            </span>
          )}
        </div>
      </div>
    </div>
  )
}
