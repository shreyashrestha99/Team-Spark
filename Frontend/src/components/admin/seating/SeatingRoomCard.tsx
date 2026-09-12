import { DoorOpen } from 'lucide-react'
import type { SeatingRoom } from './types'

interface Props {
  room: SeatingRoom
  // Module this plan belongs to, so desks held by another exam read differently
  moduleCode: string
}

export function SeatingRoomCard({ room, moduleCode }: Props) {
  // Desks grouped into rows, so the grid reads the way the hall is laid out
  const rows = new Map<string, typeof room.cells>()
  room.cells.forEach((cell) => {
    const bucket = rows.get(cell.rowLabel)
    if (bucket) bucket.push(cell)
    else rows.set(cell.rowLabel, [cell])
  })
  rows.forEach((cells) => cells.sort((left, right) => left.columnNumber - right.columnNumber))

  const shared = room.cells.some((cell) => cell.otherExam)

  return (
    <div className="rounded-xl border border-[#E2E8F0] bg-white p-5">
      <div className="mb-3 flex flex-wrap items-start justify-between gap-3">
        <div>
          <h4 className="flex items-center gap-2 text-sm font-bold text-[#0F172A]">
            <DoorOpen className="h-4 w-4 text-[#2563EB]" />
            {room.roomCode} — {room.roomName}
          </h4>
          <p className="mt-0.5 text-[11px] text-[#94A3B8]">
            {room.buildingName ?? 'Unassigned block'} · grid {room.seatRows} × {room.seatsPerRow} ·{' '}
            {room.capacity} seats for teaching, {room.examCapacity} spaced for exams
          </p>
        </div>
        <span className="rounded-full bg-[#EFF6FF] px-2.5 py-1 text-xs font-semibold text-[#2563EB]">
          {room.seatedHere} candidate(s) here
        </span>
      </div>

      <div className="mb-3 flex flex-wrap items-center gap-3 text-[11px] text-[#64748B]">
        <span className="flex items-center gap-1.5">
          <span className="h-2.5 w-2.5 rounded-sm border border-[#BFDBFE] bg-[#EFF6FF]" />
          {moduleCode}
        </span>
        {shared && (
          <span className="flex items-center gap-1.5">
            <span className="h-2.5 w-2.5 rounded-sm border border-[#FDE68A] bg-[#FEF3C7]" />
            Another exam sharing this hall
          </span>
        )}
        <span className="flex items-center gap-1.5">
          <span className="h-2.5 w-2.5 rounded-sm border border-dashed border-[#CBD5E1]" />
          Left empty for spacing
        </span>
      </div>

      {/* A wide hall scrolls inside its own card rather than stretching the page */}
      <div className="overflow-x-auto">
        <div className="inline-block min-w-full">
          {[...rows.entries()].map(([rowLabel, cells]) => (
            <div key={rowLabel} className="mb-1.5 flex items-stretch gap-1.5">
              <div className="flex w-7 shrink-0 items-center justify-center rounded-md bg-[#F8FAFC] text-xs font-semibold text-[#475569]">
                {rowLabel}
              </div>

              {cells.map((cell) => {
                if (!cell.occupied) {
                  return (
                    <div
                      key={cell.seatNumber}
                      className="flex h-12 w-24 shrink-0 flex-col items-center justify-center rounded-md border border-dashed border-[#CBD5E1] text-[10px] text-[#CBD5E1]"
                    >
                      <span className="font-mono">{cell.seatNumber}</span>
                      <span>empty</span>
                    </div>
                  )
                }

                const tone = cell.otherExam
                  ? 'border-[#FDE68A] bg-[#FEF3C7] text-[#B45309]'
                  : 'border-[#BFDBFE] bg-[#EFF6FF] text-[#1D4ED8]'

                return (
                  <div
                    key={cell.seatNumber}
                    title={`${cell.seatNumber} · ${cell.studentName ?? ''} · ${cell.studentNumber ?? ''} · ${cell.moduleCode ?? ''}`}
                    className={`flex h-12 w-24 shrink-0 flex-col justify-center rounded-md border px-1.5 ${tone}`}
                  >
                    <span className="font-mono text-[10px] font-bold">{cell.seatNumber}</span>
                    <span className="truncate text-[10px] font-medium leading-tight">
                      {cell.studentName ?? '—'}
                    </span>
                    <span className="truncate font-mono text-[9px] opacity-70">
                      {cell.otherExam ? cell.moduleCode : cell.studentNumber}
                    </span>
                  </div>
                )
              })}
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
