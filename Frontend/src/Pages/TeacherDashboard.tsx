import { useCallback, useEffect, useState } from 'react'
import {
  AlertTriangle,
  BookOpen,
  CalendarDays,
  Clock,
  Gauge,
  LayoutDashboard,
  Loader2,
  ShieldCheck,
} from 'lucide-react'
import { api } from '../services/api'
import { extractError } from '../components/admin/crud/apiError'
import { PortalLayout, type PortalTab } from '../components/portal/PortalLayout'
import { WeekRoutine } from '../components/portal/WeekRoutine'
import { StatTile, TodayClasses } from '../components/portal/PortalWidgets'
import { DutyCard } from '../components/portal/DutyCard'
import { PortalAnalytics } from '../components/portal/PortalAnalytics'
import type { InvigilationDuty, TeacherDashboard as TeacherDashboardData } from '../components/portal/types'

type TeacherTab = 'OVERVIEW' | 'ROUTINE' | 'INVIGILATION'

export default function TeacherDashboard() {
  const [activeTab, setActiveTab] = useState<TeacherTab>('OVERVIEW')

  const [dashboard, setDashboard] = useState<TeacherDashboardData | null>(null)
  const [duties, setDuties] = useState<InvigilationDuty[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const fetchDashboard = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const [dashRes, dutiesRes] = await Promise.all([
        api.get('/teacher/dashboard'),
        api.get('/teacher/invigilation'),
      ])
      setDashboard(dashRes.data?.data ?? null)
      setDuties(dutiesRes.data?.data ?? [])
    } catch (err) {
      setError(extractError(err, 'Could not load your dashboard.'))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { fetchDashboard() }, [fetchDashboard])

  const profile = dashboard?.profile
  const upcomingDuties = duties.filter((duty) => duty.upcoming)
  const pastDuties = duties.filter((duty) => !duty.upcoming)
  const maxHours = profile?.maxWeeklyHours ?? 20

  const tabs: PortalTab<TeacherTab>[] = [
    { id: 'OVERVIEW', label: 'Overview', icon: LayoutDashboard },
    { id: 'ROUTINE', label: 'My Routine', icon: CalendarDays },
    { id: 'INVIGILATION', label: 'Invigilation', icon: ShieldCheck, badge: upcomingDuties.length },
  ]

  const titles: Record<TeacherTab, { title: string; subtitle: string }> = {
    OVERVIEW: {
      title: profile ? `Welcome, ${profile.fullName}` : 'Welcome',
      subtitle: 'Your teaching today, this week’s load and your next duty',
    },
    ROUTINE: { title: 'My Routine', subtitle: 'Every class you teach, week by week' },
    INVIGILATION: { title: 'Invigilation Duties', subtitle: 'Exams you are rostered to invigilate' },
  }

  return (
    <PortalLayout
      portalName="Lecturer Portal"
      tabs={tabs}
      activeTab={activeTab}
      onTabChange={setActiveTab}
      title={titles[activeTab].title}
      subtitle={titles[activeTab].subtitle}
      userDetail={profile?.department}
    >
      {loading ? (
        <div className="flex items-center justify-center py-20">
          <Loader2 className="h-8 w-8 animate-spin text-[#2563EB]" />
        </div>
      ) : error ? (
        <div className="flex items-start gap-2 rounded-xl bg-[#FEF2F2] px-4 py-3 text-sm text-[#B91C1C]">
          <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0" />
          <span>{error}</span>
        </div>
      ) : (
        <>
          {activeTab === 'OVERVIEW' && dashboard && (
            <div className="space-y-5">
              <div className="rounded-xl border border-[#E2E8F0] bg-white p-5">
                <p className="text-xs font-medium uppercase tracking-wider text-[#94A3B8]">{profile?.department}</p>
                <p className="mt-1 text-lg font-bold text-[#0F172A]">{profile?.designation}</p>
                <p className="text-xs text-[#64748B]">{profile?.email}</p>
              </div>

              <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
                <StatTile label="Classes today" value={String(dashboard.todaysClasses.length)} icon={Clock} />
                <StatTile
                  label="Teaching this week"
                  value={`${dashboard.contactHoursThisWeek} / ${maxHours} h`}
                  hint={dashboard.overloaded ? 'Over your weekly limit' : `${dashboard.classesThisWeek} class(es)`}
                  icon={Gauge}
                  tone={dashboard.overloaded ? 'red' : 'green'}
                />
                <StatTile label="Modules this week" value={String(dashboard.modulesTaught)} icon={BookOpen} />
                <StatTile
                  label="Upcoming duties"
                  value={String(dashboard.upcomingDutyCount)}
                  icon={ShieldCheck}
                  tone={dashboard.upcomingDutyCount > 0 ? 'amber' : 'green'}
                />
              </div>

              {/* Load against the weekly cap, so an overload is visible before it happens */}
              <div className="rounded-xl border border-[#E2E8F0] bg-white p-5">
                <div className="mb-2 flex items-center justify-between text-xs">
                  <span className="font-semibold text-[#0F172A]">Weekly teaching load</span>
                  <span className={dashboard.overloaded ? 'font-semibold text-[#DC2626]' : 'text-[#64748B]'}>
                    {dashboard.contactHoursThisWeek} of {maxHours} hour(s)
                  </span>
                </div>
                <div className="h-2.5 overflow-hidden rounded-full bg-[#F1F5F9]">
                  <div
                    className={`h-full rounded-full ${dashboard.overloaded ? 'bg-[#DC2626]' : 'bg-[#059669]'}`}
                    style={{ width: `${Math.min((dashboard.contactHoursThisWeek / maxHours) * 100, 100)}%` }}
                  />
                </div>
              </div>

              {dashboard.nextDuty ? (
                <DutyCard duty={dashboard.nextDuty} featured />
              ) : (
                <div className="rounded-xl border border-dashed border-[#CBD5E1] bg-white p-6 text-center text-sm text-[#64748B]">
                  You have no upcoming invigilation duties.
                </div>
              )}

              <TodayClasses sessions={dashboard.todaysClasses} perspective="teacher" />

              {/* Teaching load broken down against the contracted ceiling */}
              <PortalAnalytics
                sessions={dashboard.weekAhead}
                contactHours={dashboard.contactHoursThisWeek}
                maxWeeklyHours={maxHours}
              />
            </div>
          )}

          {activeTab === 'ROUTINE' && <WeekRoutine endpoint="/teacher/routine" perspective="teacher" />}

          {activeTab === 'INVIGILATION' && (
            <div className="space-y-6">
              <section>
                <h3 className="mb-3 text-sm font-semibold text-[#0F172A]">Upcoming ({upcomingDuties.length})</h3>
                {upcomingDuties.length === 0 ? (
                  <div className="rounded-xl border border-dashed border-[#CBD5E1] bg-white p-6 text-center text-sm text-[#64748B]">
                    No duties rostered. They appear here as soon as an exam is allocated.
                  </div>
                ) : (
                  <div className="space-y-3">
                    {upcomingDuties.map((duty) => <DutyCard key={duty.invigilatorId} duty={duty} />)}
                  </div>
                )}
              </section>

              {pastDuties.length > 0 && (
                <section>
                  <h3 className="mb-3 text-sm font-semibold text-[#0F172A]">Past ({pastDuties.length})</h3>
                  <div className="space-y-3">
                    {pastDuties.map((duty) => <DutyCard key={duty.invigilatorId} duty={duty} />)}
                  </div>
                </section>
              )}
            </div>
          )}
        </>
      )}
    </PortalLayout>
  )
}
