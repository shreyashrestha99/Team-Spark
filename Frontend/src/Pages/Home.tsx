import React, { useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Navbar } from '../components/Navbar'
import { useAuth } from '../context/Auth'
import {
  Calendar,
  FileText,
  Users,
  BarChart3,
  ArrowRight,
  GraduationCap,
  Building2,
  Key,
  UserCheck,
  TrendingUp,
  LayoutDashboard,
  DoorOpen,
} from 'lucide-react'

export default function HomePage() {
  const { user, isAdmin } = useAuth()
  const navigate = useNavigate()

  // Admin users are locked to dashboard - redirect them
  useEffect(() => {
    if (user && isAdmin) {
      navigate('/admin/dashboard', { replace: true })
    }
  }, [user, isAdmin, navigate])

  return (
    <div className="min-h-screen bg-[#FAFCFF] text-[#1E293B]">
      {/* Top Navigation */}
      <Navbar />

      {/* Hero Section */}
      <section className="relative overflow-hidden pt-8 pb-20 lg:pt-14 lg:pb-28">
        {/* Subtle background gradient glows */}
        <div className="pointer-events-none absolute -left-40 top-0 h-[500px] w-[500px] rounded-full bg-[#EBF3FF] opacity-70 blur-3xl" />
        <div className="pointer-events-none absolute right-0 top-10 h-[550px] w-[550px] rounded-full bg-[#E8F1FD] opacity-60 blur-3xl" />

        <div className="mx-auto max-w-7xl px-6 sm:px-8">
          <div className="grid grid-cols-1 items-center gap-12 lg:grid-cols-12 lg:gap-8">
            
            {/* Left Column: Copy & CTAs */}
            <div className="lg:col-span-6">
              {/* Badge */}
              <div className="inline-flex items-center gap-2 rounded-full border border-[#DCE8FC] bg-[#EFF6FF] px-3.5 py-1.5 text-[13px] font-semibold text-[#2563EB]">
                <GraduationCap className="h-4 w-4" />
                <span>RTE Management System</span>
              </div>

              {/* Main Headline */}
              <h1 className="mt-6 text-4xl font-extrabold tracking-tight text-[#0F172A] sm:text-5xl lg:text-[56px] lg:leading-[1.15]">
                Welcome to <br />
                <span className="text-[#2563EB]">Islington College</span>
              </h1>

              {/* Subtitle */}
              <p className="mt-5 max-w-xl text-lg leading-relaxed text-[#64748B]">
                Automated academic scheduling, exam management, seating allocation and
                resource planning — all in one place.
              </p>

              {/* Feature Pills */}
              <div className="mt-7 flex flex-wrap items-center gap-2.5 sm:gap-3">
                <div className="flex items-center gap-2 rounded-xl border border-[#E2E8F0] bg-white px-3.5 py-2 text-sm font-medium text-[#334155] shadow-xs">
                  <Calendar className="h-4 w-4 text-[#2563EB]" />
                  <span>Timetable</span>
                </div>
                <div className="flex items-center gap-2 rounded-xl border border-[#E2E8F0] bg-white px-3.5 py-2 text-sm font-medium text-[#334155] shadow-xs">
                  <FileText className="h-4 w-4 text-[#059669]" />
                  <span>Exams</span>
                </div>
                <div className="flex items-center gap-2 rounded-xl border border-[#E2E8F0] bg-white px-3.5 py-2 text-sm font-medium text-[#334155] shadow-xs">
                  <Users className="h-4 w-4 text-[#7C3AED]" />
                  <span>Resources</span>
                </div>
                <div className="flex items-center gap-2 rounded-xl border border-[#E2E8F0] bg-white px-3.5 py-2 text-sm font-medium text-[#334155] shadow-xs">
                  <BarChart3 className="h-4 w-4 text-[#EA580C]" />
                  <span>Analytics</span>
                </div>
              </div>

              {/* Primary CTA Button */}
              <div className="mt-9 flex items-center gap-4">
                <Link
                  to={user ? '/#features' : '/login'}
                  className="group inline-flex items-center gap-3 rounded-2xl bg-[#2563EB] px-7 py-3.5 text-base font-semibold text-white shadow-lg shadow-[#2563EB]/25 transition hover:bg-[#1D4ED8] hover:shadow-xl hover:shadow-[#2563EB]/30 focus:outline-none focus:ring-4 focus:ring-[#2563EB]/20"
                >
                  <span>{user ? 'Explore Platform' : 'Get Started'}</span>
                  <ArrowRight className="h-5 w-5 transition-transform duration-200 group-hover:translate-x-1" />
                </Link>

                {user && (
                  <div className="text-sm font-medium text-[#64748B]">
                    Signed in as <span className="font-semibold text-[#1E293B]">{user.fullName}</span>
                  </div>
                )}
              </div>
            </div>

            {/* Right Column: Hero Visual with Campus Photo + Floating Schedule Widget */}
            <div className="relative lg:col-span-6">
              {/* Playful top-right doodle rays */}
              <div className="pointer-events-none absolute -top-8 right-2 flex gap-1 text-[#3B82F6]">
                <span className="h-4 w-1 -rotate-45 rounded-full bg-[#3B82F6]" />
                <span className="h-6 w-1 -rotate-12 rounded-full bg-[#3B82F6]" />
                <span className="h-4 w-1 rotate-25 rounded-full bg-[#3B82F6]" />
              </div>

              <div className="relative mx-auto max-w-[580px]">
                {/* Organic curved frame for campus photo */}
                <div className="relative overflow-hidden rounded-[40px] border-4 border-white bg-slate-100 shadow-[0_20px_50px_-15px_rgba(37,99,235,0.15)]">
                  <img
                    src="/islington-campus.jpg"
                    alt="Islington College Campus"
                    className="h-[420px] w-full object-cover object-center transition duration-500 hover:scale-105"
                  />
                  {/* Subtle inner shadow overlay */}
                  <div className="pointer-events-none absolute inset-0 bg-gradient-to-t from-black/30 via-transparent to-transparent" />
                </div>

                {/* Floating "Today's Schedule" Dashboard Card */}
                <div className="relative -mt-44 ml-auto w-[94%] rounded-2xl border border-[#E2E8F0] bg-white p-4 shadow-[0_15px_35px_-5px_rgba(15,23,42,0.15)] backdrop-blur-xs transition sm:w-[90%] sm:p-5">
                  <div className="flex gap-4">
                    {/* Mini Sidebar Nav Mock */}
                    <div className="hidden w-28 flex-col gap-1.5 border-r border-[#F1F5F9] pr-3 sm:flex">
                      <div className="flex items-center gap-2 rounded-lg bg-[#2563EB] px-2.5 py-1.5 text-xs font-semibold text-white">
                        <LayoutDashboard className="h-3.5 w-3.5" />
                        <span>Dashboard</span>
                      </div>
                      <div className="flex items-center gap-2 rounded-lg px-2.5 py-1 text-xs font-medium text-[#64748B] hover:text-[#1E293B]">
                        <Calendar className="h-3.5 w-3.5" />
                        <span>Timetable</span>
                      </div>
                      <div className="flex items-center gap-2 rounded-lg px-2.5 py-1 text-xs font-medium text-[#64748B] hover:text-[#1E293B]">
                        <FileText className="h-3.5 w-3.5" />
                        <span>Exams</span>
                      </div>
                      <div className="flex items-center gap-2 rounded-lg px-2.5 py-1 text-xs font-medium text-[#64748B] hover:text-[#1E293B]">
                        <Users className="h-3.5 w-3.5" />
                        <span>Faculty</span>
                      </div>
                      <div className="flex items-center gap-2 rounded-lg px-2.5 py-1 text-xs font-medium text-[#64748B] hover:text-[#1E293B]">
                        <DoorOpen className="h-3.5 w-3.5" />
                        <span>Rooms</span>
                      </div>
                      <div className="flex items-center gap-2 rounded-lg px-2.5 py-1 text-xs font-medium text-[#64748B] hover:text-[#1E293B]">
                        <GraduationCap className="h-3.5 w-3.5" />
                        <span>Students</span>
                      </div>
                      <div className="flex items-center gap-2 rounded-lg px-2.5 py-1 text-xs font-medium text-[#64748B] hover:text-[#1E293B]">
                        <BarChart3 className="h-3.5 w-3.5" />
                        <span>Reports</span>
                      </div>
                    </div>

                    {/* Schedule Grid */}
                    <div className="flex-1">
                      <div className="mb-3 flex items-center justify-between">
                        <h3 className="text-sm font-bold text-[#0F172A]">Today's Schedule</h3>
                        <div className="flex items-center gap-1.5 rounded-md bg-[#F1F5F9] px-2 py-0.5 text-[11px] font-medium text-[#64748B]">
                          <span>Tue, 24 Jun 2025</span>
                          <Calendar className="h-3 w-3" />
                        </div>
                      </div>

                      {/* Timetable Matrix */}
                      <div className="space-y-1.5 text-[10px]">
                        {/* Header days */}
                        <div className="grid grid-cols-6 gap-1 font-semibold text-[#94A3B8]">
                          <span>Time</span>
                          <span className="text-center">MON</span>
                          <span className="text-center">TUE</span>
                          <span className="text-center">WED</span>
                          <span className="text-center">THU</span>
                          <span className="text-center">FRI</span>
                        </div>

                        {/* Row 1 */}
                        <div className="grid grid-cols-6 items-center gap-1">
                          <span className="text-[9px] text-[#64748B]">8:00-10:00</span>
                          <span className="rounded bg-blue-100 py-1 text-center font-medium text-blue-700" />
                          <span className="rounded bg-indigo-100 py-1 text-center font-medium text-indigo-700" />
                          <span className="rounded bg-emerald-100 py-1 text-center font-medium text-emerald-700" />
                          <span className="rounded bg-slate-100 py-1" />
                          <span className="rounded bg-blue-100 py-1" />
                        </div>

                        {/* Row 2 */}
                        <div className="grid grid-cols-6 items-center gap-1">
                          <span className="text-[9px] text-[#64748B]">10:00-12:00</span>
                          <span className="rounded bg-amber-100 py-1" />
                          <span className="rounded bg-slate-100 py-1" />
                          <span className="rounded bg-purple-100 py-1" />
                          <span className="rounded bg-slate-100 py-1" />
                          <span className="rounded bg-indigo-100 py-1" />
                        </div>

                        {/* Row 3 */}
                        <div className="grid grid-cols-6 items-center gap-1">
                          <span className="text-[9px] text-[#64748B]">12:00-2:00</span>
                          <span className="rounded bg-emerald-100 py-1" />
                          <span className="rounded bg-emerald-100 py-1" />
                          <span className="rounded bg-blue-100 py-1" />
                          <span className="rounded bg-purple-100 py-1" />
                          <span className="rounded bg-amber-100 py-1" />
                        </div>

                        {/* Row 4 */}
                        <div className="grid grid-cols-6 items-center gap-1">
                          <span className="text-[9px] text-[#64748B]">2:00-4:00</span>
                          <span className="rounded bg-amber-100 py-1" />
                          <span className="rounded bg-blue-100 py-1" />
                          <span className="rounded bg-slate-100 py-1" />
                          <span className="rounded bg-slate-100 py-1" />
                          <span className="rounded bg-slate-100 py-1" />
                        </div>

                        {/* Row 5 */}
                        <div className="grid grid-cols-6 items-center gap-1">
                          <span className="text-[9px] text-[#64748B]">4:00-6:00</span>
                          <span className="rounded bg-slate-100 py-1" />
                          <span className="rounded bg-slate-100 py-1" />
                          <span className="rounded bg-slate-100 py-1" />
                          <span className="rounded bg-amber-100 py-1" />
                          <span className="rounded bg-slate-100 py-1" />
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* Quick Stats Bar */}
                  <div className="mt-4 border-t border-[#F1F5F9] pt-3">
                    <p className="mb-2 text-[11px] font-bold uppercase tracking-wider text-[#94A3B8]">
                      Quick Stats
                    </p>
                    <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
                      <div className="flex items-center gap-2 rounded-xl bg-[#F8FAFC] p-2">
                        <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-[#EFF6FF] text-[#2563EB]">
                          <Users className="h-3.5 w-3.5" />
                        </div>
                        <div>
                          <p className="text-[10px] text-[#64748B]">Total Students</p>
                          <p className="text-xs font-bold text-[#0F172A]">2,540</p>
                        </div>
                      </div>

                      <div className="flex items-center gap-2 rounded-xl bg-[#F8FAFC] p-2">
                        <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-[#F0FDF4] text-[#16A34A]">
                          <GraduationCap className="h-3.5 w-3.5" />
                        </div>
                        <div>
                          <p className="text-[10px] text-[#64748B]">Total Lecturers</p>
                          <p className="text-xs font-bold text-[#0F172A]">182</p>
                        </div>
                      </div>

                      <div className="flex items-center gap-2 rounded-xl bg-[#F8FAFC] p-2">
                        <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-[#FEF3C7] text-[#D97706]">
                          <Building2 className="h-3.5 w-3.5" />
                        </div>
                        <div>
                          <p className="text-[10px] text-[#64748B]">Classrooms</p>
                          <p className="text-xs font-bold text-[#0F172A]">48</p>
                        </div>
                      </div>

                      <div className="flex items-center gap-2 rounded-xl bg-[#F8FAFC] p-2">
                        <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-[#F5F3FF] text-[#7C3AED]">
                          <Calendar className="h-3.5 w-3.5" />
                        </div>
                        <div>
                          <p className="text-[10px] text-[#64748B]">Upcoming Exams</p>
                          <p className="text-xs font-bold text-[#0F172A]">12</p>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

          </div>
        </div>
      </section>

      {/* Key Features Section */}
      <section id="features" className="border-t border-[#EDF2F7] bg-white py-20 lg:py-24">
        <div className="mx-auto max-w-7xl px-6 sm:px-8">
          <div className="max-w-2xl">
            <h2 className="text-3xl font-extrabold tracking-tight text-[#0F172A] sm:text-4xl">
              Key Features
            </h2>
            <p className="mt-3 text-lg text-[#64748B]">
              Everything you need to manage academic operations efficiently.
            </p>
          </div>

          <div className="mt-12 grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
            {/* Card 1: Timetable Management */}
            <FeatureCard
              icon={<Calendar className="h-6 w-6 text-[#2563EB]" />}
              iconBg="bg-[#EFF6FF]"
              title="Timetable Management"
              description="Create and manage class schedules with real-time conflict detection."
            />

            {/* Card 2: Examination Planning */}
            <FeatureCard
              icon={<FileText className="h-6 w-6 text-[#059669]" />}
              iconBg="bg-[#ECFDF5]"
              title="Examination Planning"
              description="Schedule exams, assign invigilators, and allocate venues efficiently."
            />

            {/* Card 3: Faculty Workload */}
            <FeatureCard
              icon={<Users className="h-6 w-6 text-[#7C3AED]" />}
              iconBg="bg-[#F5F3FF]"
              title="Faculty Workload"
              description="Track teaching assignments, contact hours and avoid overloads."
            />

            {/* Card 4: Room & Resource Management */}
            <FeatureCard
              icon={<Key className="h-6 w-6 text-[#D97706]" />}
              iconBg="bg-[#FFFBEB]"
              title="Room & Resource Management"
              description="Monitor classroom capacity, availability and utilization rates."
            />

            {/* Card 5: Student & Staff Access */}
            <FeatureCard
              icon={<UserCheck className="h-6 w-6 text-[#0D9488]" />}
              iconBg="bg-[#F0FDFA]"
              title="Student & Staff Access"
              description="Personalized views for students, invigilator rosters for faculty and master views for admins."
            />

            {/* Card 6: Insights & Analytics */}
            <FeatureCard
              icon={<TrendingUp className="h-6 w-6 text-[#E11D48]" />}
              iconBg="bg-[#FFF1F2]"
              title="Insights & Analytics"
              description="Get reports, trends and optimization recommendations for better decisions."
            />
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-[#EEF2F6] bg-white py-10 text-center text-[13px] leading-relaxed text-[#94A3B8]">
        <p className="font-semibold text-[#64748B]">
          Islington College &nbsp;|&nbsp; RTE Department
        </p>
        <p className="mt-1">
          Automated Academic Scheduling &amp; Resource Management Platform
        </p>
      </footer>
    </div>
  )
}

function FeatureCard({
  icon,
  iconBg,
  title,
  description,
}: {
  icon: React.ReactNode
  iconBg: string
  title: string
  description: string
}) {
  return (
    <div className="group relative flex flex-col justify-between rounded-[20px] border border-[#E2E8F0] bg-white p-6 shadow-xs transition-all duration-300 hover:-translate-y-1 hover:border-[#2563EB]/40 hover:shadow-lg hover:shadow-[#2563EB]/5">
      <div>
        <div className="flex items-center justify-between">
          <div className={`flex h-12 w-12 items-center justify-center rounded-2xl ${iconBg}`}>
            {icon}
          </div>
          <div className="flex h-8 w-8 items-center justify-center rounded-full bg-[#F8FAFC] text-[#94A3B8] transition-colors group-hover:bg-[#2563EB] group-hover:text-white">
            <ArrowRight className="h-4 w-4 transition-transform group-hover:translate-x-0.5" />
          </div>
        </div>

        <h3 className="mt-5 text-lg font-bold text-[#0F172A]">{title}</h3>
        <p className="mt-2 text-sm leading-relaxed text-[#64748B]">{description}</p>
      </div>
    </div>
  )
}
