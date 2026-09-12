import type React from 'react'
import { GraduationCap, Briefcase, UserCog, DoorOpen } from 'lucide-react'
import type { ActiveTab, DashboardStats } from './types'

interface Props {
  stats: DashboardStats | null
  loading: boolean
  activeTab: ActiveTab
  onTabChange: (tab: ActiveTab) => void
}

export function StatsCards({ stats, loading, activeTab, onTabChange }: Props) {
  return (
    <div className="mb-6 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <StatCard
        icon={<GraduationCap className="h-6 w-6 text-[#2563EB]" />}
        iconBg="bg-[#EFF6FF]"
        label="Total Students"
        value={stats?.students}
        loading={loading}
        active={activeTab === 'STUDENTS'}
        onClick={() => onTabChange('STUDENTS')}
      />
      <StatCard
        icon={<Briefcase className="h-6 w-6 text-[#059669]" />}
        iconBg="bg-[#ECFDF5]"
        label="Total Teachers"
        value={stats?.teachers}
        loading={loading}
        active={activeTab === 'TEACHERS'}
        onClick={() => onTabChange('TEACHERS')}
      />
      <StatCard
        icon={<UserCog className="h-6 w-6 text-[#7C3AED]" />}
        iconBg="bg-[#F5F3FF]"
        label="Total Staff"
        value={stats?.staff}
        loading={loading}
        active={activeTab === 'STAFF'}
        onClick={() => onTabChange('STAFF')}
      />
      <StatCard
        icon={<DoorOpen className="h-6 w-6 text-[#D97706]" />}
        iconBg="bg-[#FEF3C7]"
        label="Total Rooms"
        value={stats?.rooms}
        loading={loading}
      />
    </div>
  )
}

function StatCard({
  icon, iconBg, label, value, loading, active, onClick,
}: {
  icon: React.ReactNode
  iconBg: string
  label: string
  value?: number
  loading: boolean
  active?: boolean
  onClick?: () => void
}) {
  return (
    <div
      role={onClick ? 'button' : undefined}
      tabIndex={onClick ? 0 : undefined}
      onClick={onClick}
      onKeyDown={(e) => { if (onClick && (e.key === 'Enter' || e.key === ' ')) { e.preventDefault(); onClick() } }}
      className={`flex items-center gap-4 rounded-2xl border p-5 transition-all ${
        active ? 'border-[#2563EB]/40 bg-[#EFF6FF] shadow-md shadow-[#2563EB]/10' : 'border-[#E2E8F0] bg-white hover:shadow-sm'
      } ${onClick ? 'cursor-pointer' : ''}`}
    >
      <div className={`flex h-12 w-12 items-center justify-center rounded-xl ${iconBg}`}>{icon}</div>
      <div>
        <p className="text-xs font-medium text-[#64748B]">{label}</p>
        {loading ? (
          <div className="mt-1 h-6 w-12 animate-pulse rounded bg-[#E2E8F0]" />
        ) : (
          <p className="text-2xl font-bold text-[#0F172A]">{value?.toLocaleString() ?? '—'}</p>
        )}
      </div>
    </div>
  )
}
