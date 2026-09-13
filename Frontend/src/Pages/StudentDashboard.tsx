import { useCallback, useEffect, useState } from 'react'
import {
  AlertTriangle,
  BookOpen,
  CalendarDays,
  Clock,
  FileSpreadsheet,
  LayoutDashboard,
  Loader2,
  Users,
} from 'lucide-react'
import { api } from '../services/api'
import { extractError } from '../components/admin/crud/apiError'
import { PortalLayout, type PortalTab } from '../components/portal/PortalLayout'
import { WeekRoutine } from '../components/portal/WeekRoutine'
import { StatTile, TodayClasses } from '../components/portal/PortalWidgets'
import { ExamSeatCard } from '../components/portal/ExamSeatCard'
import { PortalAnalytics } from '../components/portal/PortalAnalytics'
import type { MyExamSeat, StudentDashboard as StudentDashboardData } from '../components/portal/types'

type StudentTab = 'OVERVIEW' | 'ROUTINE' | 'EXAMS'

export default function StudentDashboard() {
  const [activeTab, setActiveTab] = useState<StudentTab>('OVERVIEW')

  const [dashboard, setDashboard] = useState<StudentDashboardData | null>(null)
  const [exams, setExams] = useState<MyExamSeat[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const fetchDashboard = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const [dashRes, examsRes] = await Promise.all([
        api.get('/student/dashboard'),
        api.get('/student/exams'),
      ])
      setDashboard(dashRes.data?.data ?? null)
      setExams(examsRes.data?.data ?? [])
    } catch (err) {
      setError(extractError(err, 'Could not load your dashboard.'))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { fetchDashboard() }, [fetchDashboard])

  const profile = dashboard?.profile
  const upcomingExams = exams.filter((exam) => exam.upcoming)
  const pastExams = exams.filter((exam) => !exam.upcoming)

  const tabs: PortalTab<StudentTab>[] = [
    { id: 'OVERVIEW', label: 'Overview', icon: LayoutDashboard },
    { id: 'ROUTINE', label: 'My Routine', icon: CalendarDays },
    { id: 'EXAMS', label: 'My Exams', icon: FileSpreadsheet, badge: upcomingExams.length },
  ]

  const titles: Record<StudentTab, { title: string; subtitle: string }> = {
    OVERVIEW: {
      title: profile ? `Welcome, ${profile.fullName.split(' ')[0]}` : 'Welcome',
      subtitle: 'Your classes today and your next exam at a glance',
    },
    ROUTINE: { title: 'My Routine', subtitle: 'Your lectures plus your own group’s tutorials and workshops' },
    EXAMS: { title: 'My Exams', subtitle: 'Where to go and where to sit for every exam' },
  }

  return (
    <PortalLayout
      portalName="Student Portal"
      tabs={tabs}
      activeTab={activeTab}
      onTabChange={setActiveTab}
      title={titles[activeTab].title}
      subtitle={titles[activeTab].subtitle}
      userDetail={profile?.studentNumber}
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
                <p className="text-xs font-medium uppercase tracking-wider text-[#94A3B8]">
                  {profile?.programmeName}
                </p>
                <p className="mt-1 text-lg font-bold text-[#0F172A]">
                  {profile?.batchName}
                  {profile?.groupName && (
                    <span className="ml-2 rounded-md bg-[#EFF6FF] px-2 py-0.5 text-sm font-semibold text-[#2563EB]">
                      {profile.groupName}
                    </span>
                  )}
                </p>
                <p className="text-xs text-[#64748B]">
                  Year {profile?.yearOfStudy} · Semester {profile?.semester} · {profile?.studentNumber}
                </p>
              </div>

              <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
                <StatTile label="Classes today" value={String(dashboard.todaysClasses.length)} icon={Clock} />
                <StatTile
                  label="Classes this week"
                  value={String(dashboard.classesThisWeek)}
                  hint={`${dashboard.contactHoursThisWeek} contact hour(s)`}
                  icon={BookOpen}
                  tone="green"
                />
                <StatTile
                  label="Upcoming exams"
                  value={String(dashboard.upcomingExamCount)}
                  icon={FileSpreadsheet}
                  tone={dashboard.upcomingExamCount > 0 ? 'amber' : 'green'}
                />
                <StatTile label="My group" value={profile?.groupName ?? 'Whole batch'} icon={Users} />
              </div>

              {dashboard.nextExam ? (
                <ExamSeatCard exam={dashboard.nextExam} featured />
              ) : (
                <div className="rounded-xl border border-dashed border-[#CBD5E1] bg-white p-6 text-center text-sm text-[#64748B]">
                  No upcoming exams have been seated for you yet.
                </div>
              )}

              <TodayClasses sessions={dashboard.todaysClasses} perspective="student" />

              {/* How this student's own week is shaped */}
              <PortalAnalytics
                sessions={dashboard.weekAhead}
                contactHours={dashboard.contactHoursThisWeek}
              />
            </div>
          )}

          {activeTab === 'ROUTINE' && <WeekRoutine endpoint="/student/routine" perspective="student" />}

          {activeTab === 'EXAMS' && (
            <div className="space-y-6">
              <section>
                <h3 className="mb-3 text-sm font-semibold text-[#0F172A]">
                  Upcoming ({upcomingExams.length})
                </h3>
                {upcomingExams.length === 0 ? (
                  <div className="rounded-xl border border-dashed border-[#CBD5E1] bg-white p-6 text-center text-sm text-[#64748B]">
                    Nothing scheduled. Your seat appears here as soon as the exam is allocated.
                  </div>
                ) : (
                  <div className="space-y-3">
                    {upcomingExams.map((exam) => <ExamSeatCard key={exam.examId} exam={exam} />)}
                  </div>
                )}
              </section>

              {pastExams.length > 0 && (
                <section>
                  <h3 className="mb-3 text-sm font-semibold text-[#0F172A]">Past ({pastExams.length})</h3>
                  <div className="space-y-3">
                    {pastExams.map((exam) => <ExamSeatCard key={exam.examId} exam={exam} />)}
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
