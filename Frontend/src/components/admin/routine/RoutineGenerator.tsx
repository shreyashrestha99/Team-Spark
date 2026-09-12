import { useCallback, useEffect, useMemo, useState } from 'react'
import { Loader2, Wand2, AlertTriangle, CheckCircle2, Undo2, Clock, TrendingDown } from 'lucide-react'
import { api } from '../../../services/api'
import { extractError } from '../crud/apiError'
import { ConfirmDialog } from '../crud/ConfirmDialog'
import { RoutineGrid } from './RoutineGrid'
import type { GenerationResult, GenerationRun } from './types'

// Nepal teaches Sunday to Friday, so the week starts on Sunday
const DEFAULT_WEEKS = 14

// yyyy-mm-dd for a date input
function isoDate(date: Date): string {
  return date.toISOString().slice(0, 10)
}

export function RoutineGenerator() {
  const [batches, setBatches] = useState<any[]>([])
  const [batchId, setBatchId] = useState('')
  const [fromDate, setFromDate] = useState(isoDate(new Date()))
  const [toDate, setToDate] = useState(() => {
    const end = new Date()
    end.setDate(end.getDate() + DEFAULT_WEEKS * 7)
    return isoDate(end)
  })

  const [optimise, setOptimise] = useState(true)
  const [replaceExisting, setReplaceExisting] = useState(true)

  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [result, setResult] = useState<GenerationResult | null>(null)

  const [runs, setRuns] = useState<GenerationRun[]>([])
  const [rollingBack, setRollingBack] = useState<GenerationRun | null>(null)
  const [rollbackBusy, setRollbackBusy] = useState(false)
  const [rollbackError, setRollbackError] = useState<string | null>(null)

  const fetchBatches = useCallback(async () => {
    try {
      const res = await api.get('/admin/batches/active')
      const rows = res.data?.data ?? []
      setBatches(rows)
      if (rows.length && !batchId) setBatchId(rows[0].batchId)
    } catch {
      // Non-fatal: the selector simply stays empty
    }
  }, [batchId])

  const fetchRuns = useCallback(async () => {
    try {
      const res = await api.get('/admin/routine/runs', { params: { size: 5 } })
      setRuns(res.data?.data?.content ?? [])
    } catch {
      setRuns([])
    }
  }, [])

  useEffect(() => { fetchBatches() }, [fetchBatches])
  useEffect(() => { fetchRuns() }, [fetchRuns])

  const generate = async () => {
    if (!batchId) return setError('Pick a batch first.')

    setBusy(true)
    setError(null)
    setResult(null)
    try {
      const res = await api.post('/admin/routine/generate', {
        batchId,
        fromDate,
        toDate,
        optimise,
        replaceExisting,
      })
      setResult(res.data?.data ?? null)
      fetchRuns()
    } catch (err) {
      setError(extractError(err, 'Could not generate the routine.'))
    } finally {
      setBusy(false)
    }
  }

  const confirmRollback = async () => {
    if (!rollingBack) return
    setRollbackBusy(true)
    setRollbackError(null)
    try {
      await api.delete(`/admin/routine/runs/${rollingBack.runId}`)
      setRollingBack(null)
      setResult(null)
      fetchRuns()
    } catch (err) {
      setRollbackError(extractError(err, 'Could not roll the routine back.'))
    } finally {
      setRollbackBusy(false)
    }
  }

  const selectedBatch = useMemo(
    () => batches.find((batch) => batch.batchId === batchId),
    [batches, batchId],
  )

  return (
    <>
      <div className="mb-4">
        <h2 className="text-lg font-bold text-[#0F172A]">Generate Routine</h2>
        <p className="text-xs text-[#94A3B8]">
          Builds a clash-free weekly pattern from each module&apos;s delivery plan, then repeats it
          across the semester and skips holidays. Runs entirely on your own data.
        </p>
      </div>

      {/* ─── Controls ─────────────────────────────────────────────── */}
      <div className="mb-5 rounded-xl border border-[#E2E8F0] bg-white p-5">
        <div className="grid gap-4 md:grid-cols-4">
          <label className="block">
            <span className="mb-1 block text-xs font-medium text-[#475569]">Batch</span>
            <select
              value={batchId}
              onChange={(e) => setBatchId(e.target.value)}
              className="w-full rounded-lg border border-[#E2E8F0] px-3 py-2 text-sm text-[#1E293B] outline-none focus:border-[#2563EB]"
            >
              {batches.length === 0 && <option value="">No active batches</option>}
              {batches.map((batch) => (
                <option key={batch.batchId} value={batch.batchId}>{batch.label ?? batch.batchName}</option>
              ))}
            </select>
          </label>

          <label className="block">
            <span className="mb-1 block text-xs font-medium text-[#475569]">Semester starts</span>
            <input
              type="date"
              value={fromDate}
              onChange={(e) => setFromDate(e.target.value)}
              className="w-full rounded-lg border border-[#E2E8F0] px-3 py-2 text-sm text-[#1E293B] outline-none focus:border-[#2563EB]"
            />
          </label>

          <label className="block">
            <span className="mb-1 block text-xs font-medium text-[#475569]">Semester ends</span>
            <input
              type="date"
              value={toDate}
              onChange={(e) => setToDate(e.target.value)}
              className="w-full rounded-lg border border-[#E2E8F0] px-3 py-2 text-sm text-[#1E293B] outline-none focus:border-[#2563EB]"
            />
          </label>

          <div className="flex flex-col justify-end gap-2">
            <label className="flex items-center gap-2 text-xs text-[#475569]">
              <input type="checkbox" checked={optimise} onChange={(e) => setOptimise(e.target.checked)} />
              Optimise to cut idle gaps
            </label>
            <label className="flex items-center gap-2 text-xs text-[#475569]">
              <input
                type="checkbox"
                checked={replaceExisting}
                onChange={(e) => setReplaceExisting(e.target.checked)}
              />
              Replace existing sessions
            </label>
          </div>
        </div>

        {selectedBatch && (
          <p className="mt-3 text-xs text-[#94A3B8]">
            {selectedBatch.studentCount} students in {selectedBatch.groupCount || 0} group(s).
            Lectures use the whole cohort, tutorials and workshops run per group.
          </p>
        )}

        <div className="mt-4 flex items-center gap-3">
          <button
            type="button"
            onClick={generate}
            disabled={busy || !batchId}
            className="inline-flex items-center gap-2 rounded-lg bg-[#2563EB] px-4 py-2 text-sm font-medium text-white transition hover:bg-[#1D4ED8] disabled:cursor-not-allowed disabled:opacity-60"
          >
            {busy ? <Loader2 className="h-4 w-4 animate-spin" /> : <Wand2 className="h-4 w-4" />}
            {busy ? 'Solving…' : 'Generate Routine'}
          </button>
          {busy && (
            <span className="text-xs text-[#94A3B8]">
              Placing hardest-to-fit sessions first, then trimming gaps.
            </span>
          )}
        </div>

        {error && (
          <div className="mt-4 flex items-start gap-2 rounded-lg bg-[#FEF2F2] px-3 py-2 text-sm text-[#B91C1C]">
            <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}
      </div>

      {/* ─── Result ───────────────────────────────────────────────── */}
      {result && (
        <>
          <div className="mb-5 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
            <StatCard
              label="Sessions placed"
              value={`${result.run.requirementsPlaced} / ${result.run.requirementsTotal}`}
              hint={`${result.run.sessionsCreated} across the semester`}
              tone={result.run.status === 'COMPLETED' ? 'good' : 'warn'}
              icon={result.run.status === 'COMPLETED' ? CheckCircle2 : AlertTriangle}
            />
            <StatCard
              label="Student idle gaps"
              value={`${result.idleGapsBefore} → ${result.idleGapsAfter}`}
              hint="Free periods stranded between classes"
              tone={result.idleGapsAfter <= result.idleGapsBefore ? 'good' : 'warn'}
              icon={TrendingDown}
            />
            <StatCard
              label="Quality score"
              value={`${result.penaltyBeforeOptimise} → ${result.penaltyAfterOptimise}`}
              hint="Soft-constraint penalty, lower is better"
              tone="neutral"
              icon={TrendingDown}
            />
            <StatCard
              label="Solve time"
              value={`${(result.generationMillis / 1000).toFixed(1)}s`}
              hint="No AI service, no network call"
              tone="neutral"
              icon={Clock}
            />
          </div>

          {result.unplaced.length > 0 && (
            <div className="mb-5 rounded-xl border border-[#FCD34D] bg-[#FFFBEB] p-4">
              <h3 className="mb-2 flex items-center gap-2 text-sm font-semibold text-[#B45309]">
                <AlertTriangle className="h-4 w-4" />
                {result.unplaced.length} session(s) could not be placed
              </h3>
              <ul className="space-y-1.5">
                {result.unplaced.map((item, index) => (
                  <li key={index} className="text-xs text-[#78350F]">
                    <span className="font-medium">
                      {item.moduleCode} {item.sessionType.toLowerCase()}
                      {item.groupName ? ` (${item.groupName})` : ''}
                    </span>
                    {' — '}{item.reason}
                  </li>
                ))}
              </ul>
            </div>
          )}

          <RoutineGrid sessions={result.weeklyPattern} />
        </>
      )}

      {/* ─── Recent runs ──────────────────────────────────────────── */}
      {runs.length > 0 && (
        <div className="mt-6 rounded-xl border border-[#E2E8F0] bg-white p-5">
          <h3 className="mb-3 text-sm font-semibold text-[#0F172A]">Recent runs</h3>
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead>
                <tr className="border-b border-[#E2E8F0] text-[#94A3B8]">
                  <th className="pb-2 pr-4 font-medium">Batch</th>
                  <th className="pb-2 pr-4 font-medium">Window</th>
                  <th className="pb-2 pr-4 font-medium">Placed</th>
                  <th className="pb-2 pr-4 font-medium">Sessions</th>
                  <th className="pb-2 pr-4 font-medium">Status</th>
                  <th className="pb-2 font-medium" />
                </tr>
              </thead>
              <tbody>
                {runs.map((run) => (
                  <tr key={run.runId} className="border-b border-[#F1F5F9] last:border-0">
                    <td className="py-2 pr-4 font-medium text-[#1E293B]">{run.batchName}</td>
                    <td className="py-2 pr-4 text-[#64748B]">{run.fromDate} → {run.toDate}</td>
                    <td className="py-2 pr-4 text-[#64748B]">
                      {run.requirementsPlaced}/{run.requirementsTotal}
                    </td>
                    <td className="py-2 pr-4 text-[#64748B]">{run.sessionsCreated}</td>
                    <td className="py-2 pr-4">
                      <span className={`rounded-full px-2 py-0.5 text-[11px] font-medium ${
                        run.status === 'COMPLETED'
                          ? 'bg-[#ECFDF5] text-[#059669]'
                          : 'bg-[#FFFBEB] text-[#B45309]'
                      }`}>
                        {run.status}
                      </span>
                    </td>
                    <td className="py-2">
                      <button
                        type="button"
                        onClick={() => setRollingBack(run)}
                        className="inline-flex items-center gap-1 text-[#EF4444] transition hover:underline"
                      >
                        <Undo2 className="h-3.5 w-3.5" />
                        Roll back
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {rollingBack && (
        <ConfirmDialog
          title="Roll back this routine?"
          message={`This deletes all ${rollingBack.sessionsCreated} session(s) created by that run for ${rollingBack.batchName}. It cannot be undone.`}
          busy={rollbackBusy}
          error={rollbackError}
          onConfirm={confirmRollback}
          onCancel={() => { setRollingBack(null); setRollbackError(null) }}
        />
      )}
    </>
  )
}

// ─── Result stat tile ─────────────────────────────────────────────────────────

interface StatCardProps {
  label: string
  value: string
  hint: string
  tone: 'good' | 'warn' | 'neutral'
  icon: typeof Clock
}

function StatCard({ label, value, hint, tone, icon: Icon }: StatCardProps) {
  const toneClass =
    tone === 'good' ? 'text-[#059669]' : tone === 'warn' ? 'text-[#B45309]' : 'text-[#2563EB]'

  return (
    <div className="rounded-xl border border-[#E2E8F0] bg-white p-4">
      <div className="mb-1 flex items-center gap-2">
        <Icon className={`h-4 w-4 ${toneClass}`} />
        <span className="text-xs font-medium text-[#64748B]">{label}</span>
      </div>
      <p className={`text-xl font-bold ${toneClass}`}>{value}</p>
      <p className="mt-0.5 text-[11px] text-[#94A3B8]">{hint}</p>
    </div>
  )
}
