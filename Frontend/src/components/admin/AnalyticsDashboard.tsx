import { useCallback, useEffect, useState } from 'react'
import {
  GraduationCap,
  Briefcase,
  DoorOpen,
  BookOpen,
  Loader2,
  AlertCircle,
  RefreshCw,
  AlertTriangle,
} from 'lucide-react'
import { api } from '../../services/api'
import { ChartCard, ChartEmpty } from '../charts/ChartCard'
import { BarColumns, type Datum } from '../charts/BarColumns'
import { BarRows } from '../charts/BarRows'
import { Donut } from '../charts/Donut'
import { StatTile } from '../charts/StatTile'
import { STATUS, TABULAR, rampStep } from '../charts/theme'

interface TeacherLoad {
  name: string
  department: string | null
  weeklyHours: number
  maxWeeklyHours: number
  utilisationPct: number
  overloaded: boolean
}

interface AdminAnalytics {
  students: number
  teachers: number
  staff: number
  rooms: number
  modules: number
  programmes: number
  batches: number
  sessions: number
  exams: number

  roomUtilisationPct: number
  teachingSlotsPerWeek: number
  weeksScheduled: number
  roomUtilisation: Datum[]

  sessionsByDay: Datum[]
  sessionsByPeriod: Datum[]
  sessionTypeSplit: Datum[]
  studentsByProgramme: Datum[]

  teacherWorkload: TeacherLoad[]
  overloadedTeachers: number
  averageWeeklyHours: number

  totalSeats: number
  largestRoomCapacity: number
  averageRoomFillPct: number
}

export function AnalyticsDashboard() {
  const [data, setData] = useState<AdminAnalytics | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const fetchAnalytics = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const res = await api.get('/admin/analytics')
      if (res.data?.success && res.data?.data) {
        setData(res.data.data)
      } else {
        setError('Analytics are unavailable right now.')
      }
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Could not load analytics.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchAnalytics()
  }, [fetchAnalytics])

  if (loading) {
    return (
      <div className="flex h-[320px] items-center justify-center rounded-2xl border border-[#E2E8F0] bg-white">
        <div className="flex flex-col items-center gap-3">
          <Loader2 className="h-6 w-6 animate-spin text-[#2563EB]" />
          <span className="text-sm text-[#64748B]">Computing analytics…</span>
        </div>
      </div>
    )
  }

  if (error || !data) {
    return (
      <div className="flex h-[320px] flex-col items-center justify-center gap-3 rounded-2xl border border-[#E2E8F0] bg-white">
        <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-[#FEF2F2] text-[#EF4444]">
          <AlertCircle className="h-6 w-6" />
        </div>
        <p className="text-sm text-[#475569]">{error}</p>
        <button
          type="button"
          onClick={fetchAnalytics}
          className="rounded-lg border border-[#E2E8F0] px-3 py-1.5 text-xs font-medium text-[#475569] hover:bg-[#F8FAFC] cursor-pointer"
        >
          Try again
        </button>
      </div>
    )
  }

  // Overload can push a lecturer past 100%, so let the axis grow to show it
  const workloadCeiling = Math.max(
    100,
    ...data.teacherWorkload.map((t) => t.utilisationPct),
  )

  const workloadRows: Datum[] = data.teacherWorkload.map((t) => ({
    label: t.name,
    value: t.utilisationPct,
    detail: `${t.weeklyHours}h of ${t.maxWeeklyHours}h contracted`,
  }))

  const totalSessions = data.sessionTypeSplit.reduce((sum, d) => sum + d.value, 0)

  return (
    <div className="space-y-5">
      {/* Scale of the institution */}
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatTile
          label="Students"
          value={data.students.toLocaleString()}
          hint={`${data.batches} cohorts · ${data.programmes} programmes`}
          icon={<GraduationCap className="h-4 w-4" />}
          accent="#2a78d6"
        />
        <StatTile
          label="Lecturers"
          value={data.teachers}
          hint={`${data.staff} RTE staff · ${data.modules} modules`}
          icon={<Briefcase className="h-4 w-4" />}
          accent="#1baf7a"
        />
        <StatTile
          label="Rooms"
          value={data.rooms}
          hint={`${data.totalSeats.toLocaleString()} seats · largest ${data.largestRoomCapacity}`}
          icon={<DoorOpen className="h-4 w-4" />}
          accent="#eb6834"
        />
        <StatTile
          label="Scheduled Sessions"
          value={data.sessions.toLocaleString()}
          hint={`${data.exams} exams · ${data.weeksScheduled} weeks planned`}
          icon={<BookOpen className="h-4 w-4" />}
          accent="#4a3aa7"
        />
      </div>

      {/* Efficiency — the numbers the RTE department is actually judged on */}
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        <ChartCard
          title="Room utilisation"
          subtitle={`Share of the ${data.teachingSlotsPerWeek} weekly teaching slots booked, per room`}
          aside={
            <>
              <p className="text-[22px] font-bold leading-none text-[#0F172A]" style={TABULAR}>
                {data.roomUtilisationPct}%
              </p>
              <p className="text-[11px] text-[#94A3B8]">estate-wide</p>
            </>
          }
          className="lg:col-span-2"
        >
          {data.roomUtilisation.length === 0 ? (
            <ChartEmpty message="No sessions scheduled yet — generate a routine to see utilisation." />
          ) : (
            <BarRows data={data.roomUtilisation} max={100} unit="%" labelWidth={92} />
          )}
        </ChartCard>

        <ChartCard
          title="Session mix"
          subtitle="Delivery pattern across the timetable"
        >
          {totalSessions === 0 ? (
            <ChartEmpty message="Nothing scheduled yet." />
          ) : (
            <Donut
              data={data.sessionTypeSplit}
              centerValue={totalSessions.toLocaleString()}
              centerLabel="sessions"
            />
          )}
        </ChartCard>
      </div>

      {/* Rhythm of the week */}
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <ChartCard
          title="Load by weekday"
          subtitle="Where the teaching week concentrates"
        >
          {data.sessionsByDay.length === 0 ? (
            <ChartEmpty message="No sessions scheduled yet." />
          ) : (
            <BarColumns data={data.sessionsByDay} />
          )}
        </ChartCard>

        <ChartCard
          title="Load by period"
          subtitle="Peak hours across the teaching day"
        >
          {data.sessionsByPeriod.length === 0 ? (
            <ChartEmpty message="No sessions scheduled yet." />
          ) : (
            <BarColumns data={data.sessionsByPeriod} />
          )}
        </ChartCard>
      </div>

      {/* Cohorts and faculty */}
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <ChartCard
          title="Students by programme"
          subtitle="Cohort distribution across degrees"
          aside={
            <>
              <p className="text-[22px] font-bold leading-none text-[#0F172A]" style={TABULAR}>
                {data.averageRoomFillPct}%
              </p>
              <p className="text-[11px] text-[#94A3B8]">avg room fill</p>
            </>
          }
        >
          {data.studentsByProgramme.length === 0 ? (
            <ChartEmpty message="No students enrolled yet." />
          ) : (
            <BarRows data={data.studentsByProgramme} labelWidth={168} />
          )}
        </ChartCard>

        <ChartCard
          title="Lecturer workload"
          subtitle="Weekly contact hours against contracted ceiling"
          aside={
            <>
              <p className="text-[22px] font-bold leading-none text-[#0F172A]" style={TABULAR}>
                {data.averageWeeklyHours}h
              </p>
              <p className="text-[11px] text-[#94A3B8]">average load</p>
            </>
          }
        >
          {workloadRows.length === 0 ? (
            <ChartEmpty message="No teaching assigned yet." />
          ) : (
            <>
              <BarRows
                data={workloadRows}
                max={workloadCeiling}
                unit="%"
                labelWidth={140}
                colorFor={(d) =>
                  d.value > 100 ? STATUS.critical : rampStep(d.value, workloadCeiling)
                }
              />
              {/* Status never rides on colour alone */}
              <p className="mt-3 flex items-center gap-2 border-t border-[#F1F5F9] pt-3 text-[11px] text-[#64748B]">
                {data.overloadedTeachers > 0 ? (
                  <>
                    <AlertTriangle
                      className="h-3.5 w-3.5 shrink-0"
                      style={{ color: STATUS.critical }}
                    />
                    <span>
                      <strong className="font-semibold text-[#0F172A]">
                        {data.overloadedTeachers}
                      </strong>{' '}
                      lecturer{data.overloadedTeachers === 1 ? '' : 's'} over contracted hours
                    </span>
                  </>
                ) : (
                  <span>All lecturers within their contracted hours.</span>
                )}
              </p>
            </>
          )}
        </ChartCard>
      </div>

      <div className="flex justify-end">
        <button
          type="button"
          onClick={fetchAnalytics}
          className="inline-flex items-center gap-2 rounded-xl border border-[#E2E8F0] bg-white px-3.5 py-2 text-xs font-medium text-[#475569] transition hover:bg-[#F8FAFC] cursor-pointer"
        >
          <RefreshCw className="h-3.5 w-3.5" />
          Recalculate
        </button>
      </div>
    </div>
  )
}
