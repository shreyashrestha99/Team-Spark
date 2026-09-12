import type React from 'react'
import {
  X,
  Users,
  Mail,
  Phone,
  Hash,
  FileText,
  Building,
  Award,
  CheckCircle,
  GraduationCap,
} from 'lucide-react'
import { initialsOf, type UserProfile } from './types'

interface Props {
  user: UserProfile
  onClose: () => void
}

export function ViewUserModal({ user, onClose }: Props) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4 backdrop-blur-sm">
      <div className="relative w-full max-w-lg rounded-2xl border border-[#E2E8F0] bg-white p-6 shadow-2xl sm:p-8">
        <button
          type="button"
          onClick={onClose}
          className="absolute right-4 top-4 rounded-lg p-1.5 text-[#94A3B8] transition hover:bg-[#F1F5F9] hover:text-[#1E293B] cursor-pointer"
        >
          <X className="h-5 w-5" />
        </button>

        <div className="mb-6 flex flex-col items-center text-center">
          <div className="flex h-16 w-16 items-center justify-center rounded-full bg-gradient-to-br from-[#2563EB] to-[#7C3AED] text-xl font-bold text-white shadow-lg shadow-[#2563EB]/25">
            {initialsOf(user.fullName)}
          </div>
          <h2 className="mt-3 text-xl font-bold text-[#0F172A]">{user.fullName}</h2>
          <span className="mt-1 inline-flex items-center gap-1 rounded-full bg-[#EFF6FF] px-2.5 py-0.5 text-xs font-semibold text-[#2563EB]">
            {user.role}
          </span>
        </div>

        <div className="space-y-3">
          <DetailRow icon={<Users className="h-4 w-4" />} label="Username" value={user.username} />
          <DetailRow icon={<Mail className="h-4 w-4" />} label="Email" value={user.email} />
          <DetailRow icon={<Phone className="h-4 w-4" />} label="Phone" value={user.phoneNumber || 'Not provided'} />

          {/* Student specific */}
          {user.studentNumber && (
            <DetailRow icon={<Hash className="h-4 w-4" />} label="Student Number" value={user.studentNumber} />
          )}
          {user.registrationNumber && (
            <DetailRow icon={<FileText className="h-4 w-4" />} label="Registration Number" value={user.registrationNumber} />
          )}
          {user.batchName && (
            <DetailRow icon={<GraduationCap className="h-4 w-4" />} label="Batch" value={user.batchName} />
          )}
          {user.programmeName && (
            <DetailRow icon={<Award className="h-4 w-4" />} label="Programme" value={user.programmeName} />
          )}
          {user.studentStatus && (
            <DetailRow
              icon={<CheckCircle className="h-4 w-4" />}
              label="Student Status"
              value={user.studentStatus}
              valueColor="text-[#059669]"
            />
          )}

          {/* Teacher specific */}
          {user.teacherDepartment && (
            <DetailRow icon={<Building className="h-4 w-4" />} label="Department" value={user.teacherDepartment} />
          )}
          {user.teacherDesignation && (
            <DetailRow icon={<Award className="h-4 w-4" />} label="Designation" value={user.teacherDesignation} />
          )}

          {/* Staff specific */}
          {user.staffDepartment && (
            <DetailRow icon={<Building className="h-4 w-4" />} label="Department" value={user.staffDepartment} />
          )}
          {user.staffDesignation && (
            <DetailRow icon={<Award className="h-4 w-4" />} label="Designation" value={user.staffDesignation} />
          )}

          <DetailRow
            icon={<CheckCircle className="h-4 w-4" />}
            label="Account Status"
            value={user.isActive ? 'Active' : 'Inactive'}
            valueColor={user.isActive ? 'text-[#059669]' : 'text-[#EF4444]'}
          />
        </div>

        <button
          type="button"
          onClick={onClose}
          className="mt-6 w-full rounded-xl border border-[#E2E8F0] py-2.5 text-sm font-medium text-[#475569] transition hover:bg-[#F8FAFC] cursor-pointer"
        >
          Close
        </button>
      </div>
    </div>
  )
}

function DetailRow({
  icon, label, value, valueColor,
}: {
  icon: React.ReactNode
  label: string
  value: string
  valueColor?: string
}) {
  return (
    <div className="flex items-center gap-3 rounded-xl bg-[#F8FAFC] px-4 py-3">
      <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-white text-[#64748B] shadow-sm">{icon}</div>
      <div className="flex-1">
        <p className="text-xs text-[#94A3B8]">{label}</p>
        <p className={`text-sm font-medium ${valueColor || 'text-[#1E293B]'}`}>{value}</p>
      </div>
    </div>
  )
}
