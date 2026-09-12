import React from 'react'
import {
  X,
  Plus,
  Loader2,
  AlertCircle,
  CheckCircle,
  UserPlus,
  User,
  Mail,
  Phone,
  Shield,
  Hash,
  FileText,
  GraduationCap,
} from 'lucide-react'
import { tabLabel, type ActiveTab, type AddUserForm, type Batch } from './types'

// Shared styling for the main account fields
const FIELD_CLASS =
  'h-11 w-full rounded-xl border border-[#E2E8F0] pl-10 pr-4 text-sm text-[#1E293B] outline-none transition placeholder:text-[#94A3B8] focus:border-[#2563EB] focus:ring-2 focus:ring-[#2563EB]/10'

// Styling for the smaller role-specific fields
const SUB_FIELD_CLASS =
  'h-9 w-full rounded-lg border border-[#E2E8F0] bg-white px-3 text-xs text-[#1E293B] outline-none focus:border-[#2563EB]'

interface Props {
  activeTab: ActiveTab
  form: AddUserForm
  onFormChange: (patch: Partial<AddUserForm>) => void
  batches: Batch[]
  loading: boolean
  error: string | null
  success: string | null
  onSubmit: (e: React.FormEvent) => void
  onClose: () => void
}

export function AddUserModal({
  activeTab,
  form,
  onFormChange,
  batches,
  loading,
  error,
  success,
  onSubmit,
  onClose,
}: Props) {
  const label = tabLabel(activeTab)

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto bg-black/40 px-4 py-6 backdrop-blur-sm">
      <div className="relative max-h-[90vh] w-full max-w-xl overflow-y-auto rounded-2xl border border-[#E2E8F0] bg-white p-6 shadow-2xl sm:p-8">
        <button
          type="button"
          onClick={onClose}
          className="absolute right-4 top-4 rounded-lg p-1.5 text-[#94A3B8] transition hover:bg-[#F1F5F9] hover:text-[#1E293B] cursor-pointer"
        >
          <X className="h-5 w-5" />
        </button>

        <div className="mb-6 flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#EFF6FF] text-[#2563EB]">
            <UserPlus className="h-5 w-5" />
          </div>
          <div>
            <h2 className="text-xl font-bold text-[#0F172A]">Add New {label}</h2>
            <p className="text-sm text-[#64748B]">Enter user account and academic information.</p>
          </div>
        </div>

        <form onSubmit={onSubmit} className="space-y-4" noValidate>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div>
              <label className="mb-1.5 block text-sm font-semibold text-[#1E293B]">
                Full Name <span className="text-[#EF4444]">*</span>
              </label>
              <div className="relative">
                <User className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-[#94A3B8]" />
                <input
                  type="text"
                  value={form.fullName}
                  onChange={(e) => onFormChange({ fullName: e.target.value })}
                  placeholder="e.g. John Doe"
                  className={FIELD_CLASS}
                  maxLength={100}
                />
              </div>
            </div>

            <div>
              <label className="mb-1.5 block text-sm font-semibold text-[#1E293B]">
                Username <span className="text-[#EF4444]">*</span>
              </label>
              <div className="relative">
                <Shield className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-[#94A3B8]" />
                <input
                  type="text"
                  value={form.username}
                  onChange={(e) => onFormChange({ username: e.target.value })}
                  placeholder="e.g. johndoe"
                  className={FIELD_CLASS}
                  minLength={3}
                  maxLength={50}
                />
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div>
              <label className="mb-1.5 block text-sm font-semibold text-[#1E293B]">
                Email Address <span className="text-[#EF4444]">*</span>
              </label>
              <div className="relative">
                <Mail className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-[#94A3B8]" />
                <input
                  type="email"
                  value={form.email}
                  onChange={(e) => onFormChange({ email: e.target.value })}
                  placeholder="e.g. john@islington.edu.np"
                  className={FIELD_CLASS}
                  maxLength={100}
                />
              </div>
            </div>

            <div>
              <label className="mb-1.5 block text-sm font-semibold text-[#1E293B]">
                Password <span className="text-[#EF4444]">*</span>
              </label>
              <div className="relative">
                <Shield className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-[#94A3B8]" />
                <input
                  type="password"
                  value={form.password}
                  onChange={(e) => onFormChange({ password: e.target.value })}
                  placeholder="Minimum 6 characters"
                  className={FIELD_CLASS}
                  minLength={6}
                />
              </div>
            </div>
          </div>

          <div>
            <label className="mb-1.5 block text-sm font-semibold text-[#1E293B]">
              Phone Number <span className="font-normal text-[#94A3B8]">(optional)</span>
            </label>
            <div className="relative">
              <Phone className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-[#94A3B8]" />
              <input
                type="tel"
                value={form.phoneNumber}
                onChange={(e) => onFormChange({ phoneNumber: e.target.value })}
                placeholder="e.g. +977-9841234567"
                className={FIELD_CLASS}
                maxLength={20}
              />
            </div>
          </div>

          {/* Student fields */}
          {activeTab === 'STUDENTS' && (
            <div className="space-y-3 rounded-xl border border-[#DBEAFE] bg-[#EFF6FF]/50 p-4">
              <p className="text-xs font-bold uppercase tracking-wider text-[#2563EB]">Student Details</p>

              <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#1E293B]">
                    Student Number <span className="text-[#94A3B8]">(optional)</span>
                  </label>
                  <div className="relative">
                    <Hash className="pointer-events-none absolute left-3 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-[#94A3B8]" />
                    <input
                      type="text"
                      value={form.studentNumber}
                      onChange={(e) => onFormChange({ studentNumber: e.target.value })}
                      placeholder="e.g. NP03CS4S210001"
                      className={`${SUB_FIELD_CLASS} pl-9`}
                    />
                  </div>
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#1E293B]">
                    Registration Number <span className="text-[#94A3B8]">(optional)</span>
                  </label>
                  <div className="relative">
                    <FileText className="pointer-events-none absolute left-3 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-[#94A3B8]" />
                    <input
                      type="text"
                      value={form.registrationNumber}
                      onChange={(e) => onFormChange({ registrationNumber: e.target.value })}
                      placeholder="e.g. REG-2024-001"
                      className={`${SUB_FIELD_CLASS} pl-9`}
                    />
                  </div>
                </div>
              </div>

              {/* Batch is required so the student joins a cohort */}
              <div>
                <label className="mb-1 block text-xs font-semibold text-[#1E293B]">
                  Batch <span className="text-[#EF4444]">*</span>
                </label>
                <div className="relative">
                  <GraduationCap className="pointer-events-none absolute left-3 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-[#94A3B8]" />
                  <select
                    value={form.batchId}
                    onChange={(e) => onFormChange({ batchId: e.target.value })}
                    disabled={batches.length === 0}
                    className={`${SUB_FIELD_CLASS} pl-9 disabled:bg-[#F1F5F9] disabled:text-[#94A3B8]`}
                  >
                    <option value="">
                      {batches.length === 0 ? 'No batches available yet' : 'Select a batch…'}
                    </option>
                    {batches.map((b) => (
                      <option key={b.batchId} value={b.batchId}>
                        {b.label || b.batchName}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
            </div>
          )}

          {/* Teacher and staff share department + designation */}
          {activeTab !== 'STUDENTS' && (
            <div
              className={`space-y-3 rounded-xl border p-4 ${
                activeTab === 'TEACHERS'
                  ? 'border-[#D1FAE5] bg-[#ECFDF5]/50'
                  : 'border-[#EDE9FE] bg-[#F5F3FF]/50'
              }`}
            >
              <p className={`text-xs font-bold uppercase tracking-wider ${
                activeTab === 'TEACHERS' ? 'text-[#059669]' : 'text-[#7C3AED]'
              }`}>
                {label} Details
              </p>
              <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#1E293B]">Department</label>
                  <input
                    type="text"
                    value={form.department}
                    onChange={(e) => onFormChange({ department: e.target.value })}
                    placeholder={activeTab === 'TEACHERS' ? 'e.g. Computing & IT' : 'e.g. RTE Department'}
                    className={SUB_FIELD_CLASS}
                  />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-semibold text-[#1E293B]">Designation</label>
                  <input
                    type="text"
                    value={form.designation}
                    onChange={(e) => onFormChange({ designation: e.target.value })}
                    placeholder={activeTab === 'TEACHERS' ? 'e.g. Senior Lecturer' : 'e.g. Exam Coordinator'}
                    className={SUB_FIELD_CLASS}
                  />
                </div>
              </div>
            </div>
          )}

          {error && (
            <div className="flex items-center gap-2 rounded-xl bg-[#FEF2F2] px-3 py-2.5 text-sm text-[#DC2626]">
              <AlertCircle className="h-4 w-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}
          {success && (
            <div className="flex items-center gap-2 rounded-xl bg-[#ECFDF5] px-3 py-2.5 text-sm text-[#059669]">
              <CheckCircle className="h-4 w-4 shrink-0" />
              <span>{success}</span>
            </div>
          )}

          <div className="flex items-center justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="rounded-xl border border-[#E2E8F0] px-5 py-2.5 text-sm font-medium text-[#475569] transition hover:bg-[#F8FAFC] cursor-pointer"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="inline-flex items-center gap-2 rounded-xl bg-[#2563EB] px-5 py-2.5 text-sm font-semibold text-white shadow-md shadow-[#2563EB]/20 transition hover:bg-[#1D4ED8] disabled:opacity-60 cursor-pointer"
            >
              {loading ? (
                <><Loader2 className="h-4 w-4 animate-spin" /><span>Creating...</span></>
              ) : (
                <><Plus className="h-4 w-4" /><span>Create {label}</span></>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
