import { useCallback, useEffect, useState } from 'react'
import { Loader2, Armchair, AlertTriangle, CheckCircle2, Trash2, Search } from 'lucide-react'
import { api } from '../../../services/api'
import { extractError } from '../crud/apiError'
import { SeatingRoomCard } from './SeatingRoomCard'
import type { SeatingPlan } from './types'

export function ExamSeating() {
  const [exams, setExams] = useState<any[]>([])
  const [examId, setExamId] = useState('')

  const [plan, setPlan] = useState<SeatingPlan | null>(null)
  const [loading, setLoading] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  // Find one candidate without reading every desk
  const [lookup, setLookup] = useState('')

  const fetchExams = useCallback(async () => {
    try {
      const res = await api.get('/admin/exams', { params: { size: 100, sortBy: 'examDate', sortDirection: 'asc' } })
      const rows = res.data?.data?.content ?? []
      setExams(rows)
      if (rows.length && !examId) setExamId(rows[0].examId)
    } catch {
      // Non-fatal: the selector stays empty
    }
  }, [examId])

  const fetchPlan = useCallback(async (id: string) => {
    if (!id) return
    setLoading(true)
    setError(null)
    try {
      const res = await api.get(`/admin/exams/${id}/seating`)
      setPlan(res.data?.data ?? null)
    } catch (err) {
      setPlan(null)
      setError(extractError(err, 'Could not load the seating plan.'))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { fetchExams() }, [fetchExams])
  useEffect(() => { if (examId) fetchPlan(examId) }, [examId, fetchPlan])

  const allocate = async () => {
    if (!examId) return
    setBusy(true)
    setError(null)
    try {
      const res = await api.post(`/admin/exams/${examId}/seating/auto-allocate`)
      setPlan(res.data?.data ?? null)
    } catch (err) {
      setError(extractError(err, 'Could not allocate seating.'))
    } finally {
      setBusy(false)
    }
  }

  const clear = async () => {
    if (!examId) return
    setBusy(true)
    setError(null)
    try {
      await api.delete(`/admin/exams/${examId}/seating`)
      fetchPlan(examId)
    } catch (err) {
      setError(extractError(err, 'Could not clear the seating.'))
    } finally {
      setBusy(false)
    }
  }

  // Where one candidate is sitting, by name or student number
  const match = (() => {
    const needle = lookup.trim().toLowerCase()
    if (!needle || !plan) return null

    for (const room of plan.rooms) {
      for (const cell of room.cells) {
        if (!cell.occupied || cell.otherExam) continue
        const name = (cell.studentName ?? '').toLowerCase()
        const number = (cell.studentNumber ?? '').toLowerCase()
        if (name.includes(needle) || number.includes(needle)) {
          return { room, cell }
        }
      }
    }
    return null
  })()

  return (
    <>
      <div className="mb-4">
        <h2 className="text-lg font-bold text-[#0F172A]">Exam Seating</h2>
        <p className="text-xs text-[#94A3B8]">
          Halls are chosen and every candidate is given a desk automatically. Seat names come from
          the room&apos;s own grid, so nobody types them.
        </p>
      </div>

      <div className="mb-5 rounded-xl border border-[#E2E8F0] bg-white p-5">
        <div className="grid gap-4 md:grid-cols-[2fr_1fr]">
          <label className="block">
            <span className="mb-1 block text-xs font-medium text-[#475569]">Exam</span>
            <select
              value={examId}
              onChange={(e) => setExamId(e.target.value)}
              className="w-full rounded-lg border border-[#E2E8F0] px-3 py-2 text-sm text-[#1E293B] outline-none focus:border-[#2563EB]"
            >
              {exams.length === 0 && <option value="">No exams scheduled</option>}
              {exams.map((exam) => (
                <option key={exam.examId} value={exam.examId}>
                  {exam.moduleCode} — {exam.batchName} — {exam.examDate} {String(exam.startTime).slice(0, 5)}
                </option>
              ))}
            </select>
          </label>

          <label className="block">
            <span className="mb-1 block text-xs font-medium text-[#475569]">Find a candidate</span>
            <div className="relative">
              <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-[#94A3B8]" />
              <input
                value={lookup}
                onChange={(e) => setLookup(e.target.value)}
                placeholder="Name or student number"
                className="w-full rounded-lg border border-[#E2E8F0] py-2 pl-9 pr-3 text-sm text-[#1E293B] outline-none focus:border-[#2563EB]"
              />
            </div>
          </label>
        </div>

        <div className="mt-4 flex flex-wrap items-center gap-3">
          <button
            type="button"
            onClick={allocate}
            disabled={busy || !examId}
            className="inline-flex items-center gap-2 rounded-lg bg-[#2563EB] px-4 py-2 text-sm font-medium text-white transition hover:bg-[#1D4ED8] disabled:cursor-not-allowed disabled:opacity-60"
          >
            {busy ? <Loader2 className="h-4 w-4 animate-spin" /> : <Armchair className="h-4 w-4" />}
            {busy ? 'Allocating…' : 'Auto-allocate Seating'}
          </button>

          {plan && plan.rooms.length > 0 && (
            <button
              type="button"
              onClick={clear}
              disabled={busy}
              className="inline-flex items-center gap-2 rounded-lg border border-[#E2E8F0] px-4 py-2 text-sm font-medium text-[#EF4444] transition hover:bg-[#FEF2F2] disabled:opacity-60"
            >
              <Trash2 className="h-4 w-4" />
              Clear
            </button>
          )}
        </div>

        {match && (
          <div className="mt-4 rounded-lg bg-[#ECFDF5] px-3 py-2.5 text-sm text-[#065F46]">
            <span className="font-semibold">{match.cell.studentName}</span>
            {' ('}{match.cell.studentNumber}{') — '}
            {match.room.buildingName ? `${match.room.buildingName}, ` : ''}
            Room {match.room.roomCode}, Row {match.cell.rowLabel}, Seat{' '}
            <span className="font-mono font-bold">{match.cell.seatNumber}</span>
          </div>
        )}

        {lookup.trim() && !match && (
          <div className="mt-4 rounded-lg bg-[#FFFBEB] px-3 py-2.5 text-sm text-[#B45309]">
            No candidate in this exam matches “{lookup.trim()}”.
          </div>
        )}

        {error && (
          <div className="mt-4 flex items-start gap-2 rounded-lg bg-[#FEF2F2] px-3 py-2 text-sm text-[#B91C1C]">
            <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}
      </div>

      {loading && (
        <div className="flex items-center justify-center py-10">
          <Loader2 className="h-6 w-6 animate-spin text-[#2563EB]" />
        </div>
      )}

      {!loading && plan && (
        <>
          <div className="mb-5 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
            <Tile
              label="Candidates seated"
              value={`${plan.seatedStudents} / ${plan.totalStudents}`}
              hint={plan.unseatedStudents === 0 ? 'Everyone has a desk' : `${plan.unseatedStudents} without a desk`}
              good={plan.unseatedStudents === 0}
            />
            <Tile label="Halls used" value={String(plan.rooms.length)} hint="Kept in one block where possible" good />
            <Tile
              label="Exam"
              value={plan.moduleCode}
              hint={`${plan.batchName} · ${plan.examDate}`}
              good
            />
            <Tile
              label="Sitting"
              value={`${String(plan.startTime).slice(0, 5)}–${String(plan.endTime).slice(0, 5)}`}
              hint={plan.allocationMillis ? `Allocated in ${plan.allocationMillis} ms` : 'Saved plan'}
              good
            />
          </div>

          {plan.warnings?.length > 0 && (
            <div className="mb-5 rounded-xl border border-[#FCD34D] bg-[#FFFBEB] p-4">
              <h3 className="mb-2 flex items-center gap-2 text-sm font-semibold text-[#B45309]">
                <AlertTriangle className="h-4 w-4" />
                Worth knowing
              </h3>
              <ul className="space-y-1.5">
                {plan.warnings.map((warning, index) => (
                  <li key={index} className="text-xs text-[#78350F]">{warning}</li>
                ))}
              </ul>
            </div>
          )}

          {plan.rooms.length === 0 ? (
            <div className="rounded-xl border border-dashed border-[#CBD5E1] bg-white p-10 text-center">
              <Armchair className="mx-auto mb-2 h-6 w-6 text-[#CBD5E1]" />
              <p className="text-sm text-[#64748B]">No halls allocated yet.</p>
              <p className="text-xs text-[#94A3B8]">Use Auto-allocate Seating to build the plan.</p>
            </div>
          ) : (
            <div className="space-y-5">
              {plan.rooms.map((room) => (
                <SeatingRoomCard key={room.examRoomId} room={room} moduleCode={plan.moduleCode} />
              ))}
            </div>
          )}
        </>
      )}
    </>
  )
}

// ─── Summary tile ─────────────────────────────────────────────────────────────

function Tile({ label, value, hint, good }: { label: string; value: string; hint: string; good: boolean }) {
  return (
    <div className="rounded-xl border border-[#E2E8F0] bg-white p-4">
      <div className="mb-1 flex items-center gap-2">
        {good
          ? <CheckCircle2 className="h-4 w-4 text-[#059669]" />
          : <AlertTriangle className="h-4 w-4 text-[#B45309]" />}
        <span className="text-xs font-medium text-[#64748B]">{label}</span>
      </div>
      <p className={`text-xl font-bold ${good ? 'text-[#059669]' : 'text-[#B45309]'}`}>{value}</p>
      <p className="mt-0.5 text-[11px] text-[#94A3B8]">{hint}</p>
    </div>
  )
}
