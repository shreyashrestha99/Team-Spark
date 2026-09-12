import {
  GraduationCap,
  Briefcase,
  UserCog,
  ChevronLeft,
  LayoutDashboard,
  LogOut,
  DoorOpen,
  BarChart3,
} from 'lucide-react'
import { IslingtonLogo } from '../IslingtonLogo'
import { initialsOf, type ActiveTab, type DashboardStats } from './types'

// Role tabs listed under User Management
const sidebarItems = [
  { id: 'STUDENTS' as ActiveTab, label: 'Students', statKey: 'students' as const, icon: GraduationCap, color: 'text-[#2563EB]', bg: 'bg-[#EFF6FF]' },
  { id: 'TEACHERS' as ActiveTab, label: 'Teachers', statKey: 'teachers' as const, icon: Briefcase, color: 'text-[#059669]', bg: 'bg-[#ECFDF5]' },
  { id: 'STAFF' as ActiveTab, label: 'Staff', statKey: 'staff' as const, icon: UserCog, color: 'text-[#7C3AED]', bg: 'bg-[#F5F3FF]' },
]

interface Props {
  activeTab: ActiveTab
  onTabChange: (tab: ActiveTab) => void
  collapsed: boolean
  onCollapsedChange: (collapsed: boolean) => void
  stats: DashboardStats | null
  userName?: string
  userEmail?: string
  onLogout: () => void
}

export function AdminSidebar({
  activeTab,
  onTabChange,
  collapsed,
  onCollapsedChange,
  stats,
  userName,
  userEmail,
  onLogout,
}: Props) {
  return (
    <aside
      className={`flex flex-col border-r border-[#E2E8F0] bg-white transition-all duration-300 ${
        collapsed ? 'w-[72px]' : 'w-[260px]'
      }`}
    >
      {/* Logo / brand */}
      <div className="flex h-[72px] items-center border-b border-[#E2E8F0] px-4">
        {collapsed ? (
          <button
            onClick={() => onCollapsedChange(false)}
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
                <p className="text-sm font-bold text-[#0F172A]">RTE Admin</p>
                <p className="text-[11px] text-[#94A3B8]">Dashboard</p>
              </div>
            </div>
            <button
              onClick={() => onCollapsedChange(true)}
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
            Overview
          </p>
        )}
        <button
          onClick={() => onTabChange('STUDENTS')}
          className={`mb-1 flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition cursor-pointer ${
            activeTab === 'STUDENTS'
              ? 'bg-[#2563EB] text-white shadow-md shadow-[#2563EB]/20'
              : 'text-[#64748B] hover:bg-[#F8FAFC] hover:text-[#1E293B]'
          }`}
          title="Dashboard"
        >
          <LayoutDashboard className="h-5 w-5 shrink-0" />
          {!collapsed && <span>Dashboard</span>}
        </button>

        {!collapsed && (
          <p className="mb-2 mt-6 px-3 text-[10px] font-bold uppercase tracking-widest text-[#94A3B8]">
            User Management
          </p>
        )}
        {collapsed && <div className="my-3 border-t border-[#F1F5F9]" />}

        {sidebarItems.map((item) => {
          const Icon = item.icon
          const isActive = activeTab === item.id
          return (
            <button
              key={item.id}
              onClick={() => onTabChange(item.id)}
              className={`mb-1 flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition cursor-pointer ${
                isActive
                  ? 'bg-[#F8FAFC] text-[#1E293B] shadow-sm ring-1 ring-[#E2E8F0]'
                  : 'text-[#64748B] hover:bg-[#F8FAFC] hover:text-[#1E293B]'
              }`}
              title={item.label}
            >
              <div className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-lg ${isActive ? item.bg : ''}`}>
                <Icon className={`h-4.5 w-4.5 ${isActive ? item.color : ''}`} />
              </div>
              {!collapsed && (
                <div className="flex flex-1 items-center justify-between">
                  <span>{item.label}</span>
                  <span className={`rounded-md px-2 py-0.5 text-[11px] font-semibold ${
                    isActive ? 'bg-white text-[#1E293B]' : 'bg-[#F1F5F9] text-[#94A3B8]'
                  }`}>
                    {stats?.[item.statKey] ?? '—'}
                  </span>
                </div>
              )}
            </button>
          )
        })}

        {collapsed && <div className="my-3 border-t border-[#F1F5F9]" />}
        {!collapsed && (
          <p className="mb-2 mt-6 px-3 text-[10px] font-bold uppercase tracking-widest text-[#94A3B8]">
            Resources
          </p>
        )}

        <button
          className="mb-1 flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium text-[#64748B] transition cursor-pointer hover:bg-[#F8FAFC] hover:text-[#1E293B]"
          title="Rooms"
        >
          <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg">
            <DoorOpen className="h-4.5 w-4.5" />
          </div>
          {!collapsed && (
            <div className="flex flex-1 items-center justify-between">
              <span>Rooms</span>
              <span className="rounded-md bg-[#F1F5F9] px-2 py-0.5 text-[11px] font-semibold text-[#94A3B8]">
                {stats?.rooms ?? '—'}
              </span>
            </div>
          )}
        </button>

        <button
          className="mb-1 flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium text-[#64748B] transition cursor-pointer hover:bg-[#F8FAFC] hover:text-[#1E293B]"
          title="Reports"
        >
          <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg">
            <BarChart3 className="h-4.5 w-4.5" />
          </div>
          {!collapsed && <span>Reports</span>}
        </button>
      </nav>

      {/* Admin profile + logout */}
      <div className="border-t border-[#E2E8F0] p-3">
        {collapsed ? (
          <button
            onClick={onLogout}
            className="flex w-full items-center justify-center rounded-xl bg-[#FEF2F2] p-2.5 text-[#EF4444] transition hover:bg-[#FEE2E2] cursor-pointer"
            title="Log Out"
          >
            <LogOut className="h-5 w-5" />
          </button>
        ) : (
          <div className="space-y-2">
            <div className="flex items-center gap-3 rounded-xl bg-[#F8FAFC] p-3">
              <div className="flex h-9 w-9 items-center justify-center rounded-full bg-gradient-to-br from-[#2563EB] to-[#7C3AED] text-xs font-bold text-white">
                {initialsOf(userName) === '?' ? 'A' : initialsOf(userName)}
              </div>
              <div className="flex-1 overflow-hidden">
                <p className="truncate text-sm font-semibold text-[#1E293B]">{userName || 'Admin'}</p>
                <p className="truncate text-[11px] text-[#94A3B8]">{userEmail || ''}</p>
              </div>
            </div>

            <button
              onClick={onLogout}
              className="flex w-full items-center justify-center gap-2 rounded-xl bg-[#FEF2F2] px-4 py-2.5 text-sm font-semibold text-[#EF4444] transition hover:bg-[#FEE2E2] cursor-pointer"
            >
              <LogOut className="h-4 w-4" />
              <span>Log Out</span>
            </button>
          </div>
        )}
      </div>
    </aside>
  )
}
