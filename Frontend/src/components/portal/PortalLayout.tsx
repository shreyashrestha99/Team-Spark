import { useState, type ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { ChevronLeft, LayoutDashboard, LogOut, type LucideIcon } from 'lucide-react'
import { IslingtonLogo } from '../IslingtonLogo'
import { useAuth } from '../../context/Auth'
import { initialsOf } from '../admin/types'

export interface PortalTab<T extends string> {
  id: T
  label: string
  icon: LucideIcon
  // Small count shown beside the label, e.g. upcoming exams
  badge?: number
}

interface Props<T extends string> {
  // "Student Portal" or "Lecturer Portal"
  portalName: string
  tabs: PortalTab<T>[]
  activeTab: T
  onTabChange: (tab: T) => void
  title: string
  subtitle: string
  // Line under the user's name, e.g. a student number or a department
  userDetail?: string
  children: ReactNode
}

/** Sidebar and top bar shared by the student and lecturer portals, styled like the admin dashboard. */
export function PortalLayout<T extends string>({
  portalName,
  tabs,
  activeTab,
  onTabChange,
  title,
  subtitle,
  userDetail,
  children,
}: Props<T>) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [collapsed, setCollapsed] = useState(false)

  const handleLogout = async () => {
    await logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="flex h-screen overflow-hidden bg-[#F5F7FA]">
      <aside
        className={`flex flex-col border-r border-[#E2E8F0] bg-white transition-all duration-300 ${
          collapsed ? 'w-[72px]' : 'w-[260px]'
        }`}
      >
        <div className="flex h-[72px] shrink-0 items-center border-b border-[#E2E8F0] px-4">
          {collapsed ? (
            <button
              onClick={() => setCollapsed(false)}
              className="mx-auto flex h-10 w-10 items-center justify-center rounded-xl bg-[#2563EB] text-white cursor-pointer"
              title="Expand sidebar"
            >
              <LayoutDashboard className="h-5 w-5" />
            </button>
          ) : (
            <div className="flex w-full items-center justify-between">
              <div className="flex items-center gap-2.5">
                <IslingtonLogo size="sm" />
                <div className="leading-tight">
                  <p className="text-sm font-bold text-[#0F172A]">{portalName}</p>
                  <p className="text-[11px] text-[#94A3B8]">RTE Department</p>
                </div>
              </div>
              <button
                onClick={() => setCollapsed(true)}
                className="rounded-lg p-1.5 text-[#94A3B8] transition hover:bg-[#F1F5F9] hover:text-[#475569] cursor-pointer"
                title="Collapse sidebar"
              >
                <ChevronLeft className="h-4 w-4" />
              </button>
            </div>
          )}
        </div>

        <nav className="flex-1 overflow-y-auto px-3 py-4">
          {!collapsed && (
            <p className="mb-2 px-3 text-[10px] font-bold uppercase tracking-widest text-[#94A3B8]">
              My Space
            </p>
          )}

          {tabs.map((tab) => {
            const Icon = tab.icon
            const isActive = activeTab === tab.id

            return (
              <button
                key={tab.id}
                onClick={() => onTabChange(tab.id)}
                className={`mb-1 flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition cursor-pointer ${
                  isActive
                    ? 'bg-[#2563EB] text-white shadow-md shadow-[#2563EB]/20'
                    : 'text-[#64748B] hover:bg-[#F8FAFC] hover:text-[#1E293B]'
                }`}
                title={tab.label}
              >
                <Icon className="h-4.5 w-4.5 shrink-0" />
                {!collapsed && (
                  <div className="flex flex-1 items-center justify-between">
                    <span className="truncate">{tab.label}</span>
                    {tab.badge !== undefined && tab.badge > 0 && (
                      <span className={`rounded-md px-2 py-0.5 text-[11px] font-semibold ${
                        isActive ? 'bg-white/20 text-white' : 'bg-[#F1F5F9] text-[#94A3B8]'
                      }`}>
                        {tab.badge}
                      </span>
                    )}
                  </div>
                )}
              </button>
            )
          })}
        </nav>

        <div className="shrink-0 border-t border-[#E2E8F0] p-3">
          {collapsed ? (
            <button
              onClick={handleLogout}
              className="flex w-full items-center justify-center rounded-xl bg-[#FEF2F2] p-2.5 text-[#EF4444] transition hover:bg-[#FEE2E2] cursor-pointer"
              title="Log Out"
            >
              <LogOut className="h-5 w-5" />
            </button>
          ) : (
            <div className="space-y-2">
              <div className="flex items-center gap-3 rounded-xl bg-[#F8FAFC] p-3">
                <div className="flex h-9 w-9 items-center justify-center rounded-full bg-gradient-to-br from-[#2563EB] to-[#7C3AED] text-xs font-bold text-white">
                  {initialsOf(user?.fullName)}
                </div>
                <div className="flex-1 overflow-hidden">
                  <p className="truncate text-sm font-semibold text-[#1E293B]">{user?.fullName}</p>
                  <p className="truncate text-[11px] text-[#94A3B8]">{userDetail || user?.email}</p>
                </div>
              </div>

              <button
                onClick={handleLogout}
                className="flex w-full items-center justify-center gap-2 rounded-xl bg-[#FEF2F2] px-4 py-2.5 text-sm font-semibold text-[#EF4444] transition hover:bg-[#FEE2E2] cursor-pointer"
              >
                <LogOut className="h-4 w-4" />
                <span>Log Out</span>
              </button>
            </div>
          )}
        </div>
      </aside>

      <main className="flex-1 overflow-y-auto">
        <header className="sticky top-0 z-20 flex h-[72px] items-center border-b border-[#E2E8F0] bg-white/95 px-6 backdrop-blur-md">
          <div>
            <h1 className="text-xl font-bold text-[#0F172A]">{title}</h1>
            <p className="text-xs text-[#94A3B8]">{subtitle}</p>
          </div>
        </header>

        <div className="p-6">{children}</div>
      </main>
    </div>
  )
}
