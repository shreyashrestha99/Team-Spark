import type { CrudRow, ResourceConfig } from './crud/types'

// ─── Shared cell renderers ────────────────────────────────────────────────────

// Green/red pill for boolean flags
function flagBadge(value: unknown, onText: string, offText: string) {
  const on = Boolean(value)
  return (
    <span className={`inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-medium ${
      on ? 'bg-[#ECFDF5] text-[#059669]' : 'bg-[#FEF2F2] text-[#EF4444]'
    }`}>
      <span className={`h-1.5 w-1.5 rounded-full ${on ? 'bg-[#059669]' : 'bg-[#EF4444]'}`} />
      {on ? onText : offText}
    </span>
  )
}

// Neutral pill for status strings
function statusBadge(status?: string) {
  if (!status) return '—'
  const upper = status.toUpperCase()
  const tone = upper === 'CANCELLED'
    ? 'bg-[#FEF2F2] text-[#EF4444]'
    : upper === 'COMPLETED'
      ? 'bg-[#F1F5F9] text-[#475569]'
      : 'bg-[#EFF6FF] text-[#2563EB]'
  return <span className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${tone}`}>{upper}</span>
}

// "10:00:00" -> "10:00"
function hhmm(time?: string): string {
  return time ? time.slice(0, 5) : '—'
}

// Renders a start–end slot
function slot(row: CrudRow) {
  return `${hhmm(row.startTime)} – ${hhmm(row.endTime)}`
}

const STATUS_OPTIONS = [
  { value: 'SCHEDULED', label: 'SCHEDULED' },
  { value: 'COMPLETED', label: 'COMPLETED' },
  { value: 'CANCELLED', label: 'CANCELLED' },
]

// ─── Resource definitions ─────────────────────────────────────────────────────

export const PROGRAMMES: ResourceConfig = {
  key: 'PROGRAMMES',
  title: 'Programmes',
  singular: 'Programme',
  description: 'Degree programmes offered by the college.',
  endpoint: '/admin/programmes',
  sortable: ['programmeCode', 'programmeName', 'durationYears'],
  defaultSortBy: 'programmeCode',
  defaultSortDir: 'asc',
  columns: [
    { key: 'programmeCode', label: 'Code', mono: true, className: 'text-[#2563EB]' },
    { key: 'programmeName', label: 'Programme Name', className: 'text-[#1E293B] font-medium' },
    { key: 'durationYears', label: 'Years' },
    { key: 'batchCount', label: 'Batches' },
    { key: 'isActive', label: 'Status', render: (r) => flagBadge(r.isActive, 'Active', 'Inactive') },
  ],
  fields: [
    { name: 'programmeName', label: 'Programme Name', type: 'text', required: true, maxLength: 150, placeholder: 'e.g. BSc (Hons) Computing' },
    { name: 'programmeCode', label: 'Programme Code', type: 'text', required: true, maxLength: 50, half: true, placeholder: 'e.g. BSC-COMP' },
    { name: 'durationYears', label: 'Duration (years)', type: 'number', required: true, min: 1, max: 10, half: true },
    { name: 'isActive', label: 'Active', type: 'checkbox', defaultChecked: true },
  ],
}

export const BATCHES: ResourceConfig = {
  key: 'BATCHES',
  title: 'Batches',
  singular: 'Batch',
  description: 'Student cohorts within a programme.',
  endpoint: '/admin/batches',
  sortable: ['batchName', 'yearOfStudy', 'semester'],
  defaultSortBy: 'batchName',
  defaultSortDir: 'asc',
  columns: [
    { key: 'batchName', label: 'Batch Name', className: 'text-[#1E293B] font-medium' },
    { key: 'programmeName', label: 'Programme' },
    { key: 'yearOfStudy', label: 'Year' },
    { key: 'semester', label: 'Sem' },
    { key: 'studentCount', label: 'Students' },
    { key: 'isActive', label: 'Status', render: (r) => flagBadge(r.isActive, 'Active', 'Inactive') },
  ],
  fields: [
    {
      name: 'programmeId', label: 'Programme', type: 'select', required: true,
      optionsEndpoint: '/admin/programmes/active', optionValue: 'programmeId', optionLabel: 'programmeName',
    },
    { name: 'batchName', label: 'Batch Name', type: 'text', required: true, maxLength: 100, placeholder: 'e.g. Computing 2022 (L5)' },
    { name: 'yearOfStudy', label: 'Year of Study', type: 'number', required: true, min: 1, max: 10, half: true },
    { name: 'semester', label: 'Semester', type: 'number', required: true, min: 1, max: 12, half: true },
    { name: 'startDate', label: 'Start Date', type: 'date', half: true },
    { name: 'endDate', label: 'End Date', type: 'date', half: true },
    { name: 'isActive', label: 'Active', type: 'checkbox', defaultChecked: true },
  ],
}

export const MODULES: ResourceConfig = {
  key: 'MODULES',
  title: 'Modules',
  singular: 'Module',
  description: 'Taught modules and their credit weighting.',
  endpoint: '/admin/modules',
  sortable: ['moduleCode', 'moduleName', 'credits'],
  defaultSortBy: 'moduleCode',
  defaultSortDir: 'asc',
  columns: [
    { key: 'moduleCode', label: 'Code', mono: true, className: 'text-[#2563EB]' },
    { key: 'moduleName', label: 'Module Name', className: 'text-[#1E293B] font-medium' },
    { key: 'credits', label: 'Credits' },
    { key: 'yearOfStudy', label: 'Year' },
    { key: 'semester', label: 'Sem' },
    { key: 'isActive', label: 'Status', render: (r) => flagBadge(r.isActive, 'Active', 'Inactive') },
  ],
  fields: [
    { name: 'moduleCode', label: 'Module Code', type: 'text', required: true, maxLength: 50, half: true, placeholder: 'e.g. CS5001' },
    { name: 'moduleName', label: 'Module Name', type: 'text', required: true, maxLength: 150, half: true, placeholder: 'e.g. Software Engineering' },
    { name: 'credits', label: 'Credits', type: 'number', required: true, min: 1, max: 120, half: true },
    { name: 'yearOfStudy', label: 'Year of Study', type: 'number', required: true, min: 1, max: 10, half: true },
    { name: 'semester', label: 'Semester', type: 'number', required: true, min: 1, max: 12, half: true },
    { name: 'isActive', label: 'Active', type: 'checkbox', defaultChecked: true },
  ],
}

export const ROOMS: ResourceConfig = {
  key: 'ROOMS',
  title: 'Rooms',
  singular: 'Room',
  description: 'Classrooms, labs and exam halls with capacity.',
  endpoint: '/admin/rooms',
  sortable: ['roomCode', 'roomName', 'capacity'],
  defaultSortBy: 'roomCode',
  defaultSortDir: 'asc',
  columns: [
    { key: 'roomCode', label: 'Code', mono: true, className: 'text-[#2563EB]' },
    { key: 'roomName', label: 'Room Name', className: 'text-[#1E293B] font-medium' },
    { key: 'roomType', label: 'Type' },
    { key: 'capacity', label: 'Capacity' },
    { key: 'building', label: 'Building' },
    { key: 'isAvailable', label: 'Status', render: (r) => flagBadge(r.isAvailable, 'Available', 'Unavailable') },
  ],
  fields: [
    { name: 'roomCode', label: 'Room Code', type: 'text', required: true, maxLength: 50, half: true, placeholder: 'e.g. LAB-201' },
    { name: 'roomName', label: 'Room Name', type: 'text', required: true, maxLength: 100, half: true, placeholder: 'e.g. Computer Lab' },
    {
      name: 'roomType', label: 'Room Type', type: 'select', half: true,
      options: [
        { value: 'LECTURE', label: 'LECTURE' },
        { value: 'LAB', label: 'LAB' },
        { value: 'EXAM_HALL', label: 'EXAM_HALL' },
        { value: 'SEMINAR', label: 'SEMINAR' },
      ],
    },
    { name: 'capacity', label: 'Capacity', type: 'number', required: true, min: 1, max: 1000, half: true },
    { name: 'floor', label: 'Floor', type: 'number', min: 0, max: 100, half: true },
    { name: 'building', label: 'Building', type: 'text', maxLength: 100, half: true, placeholder: 'e.g. Main Block' },
    { name: 'hasProjector', label: 'Has Projector', type: 'checkbox', defaultChecked: false, half: true },
    { name: 'hasComputers', label: 'Has Computers', type: 'checkbox', defaultChecked: false, half: true },
    { name: 'hasAc', label: 'Has AC', type: 'checkbox', defaultChecked: false, half: true },
    { name: 'isAvailable', label: 'Available', type: 'checkbox', defaultChecked: true, half: true },
  ],
}

export const BATCH_MODULES: ResourceConfig = {
  key: 'BATCH_MODULES',
  title: 'Batch Modules',
  singular: 'Assignment',
  description: 'Which modules each batch studies.',
  endpoint: '/admin/batch-modules',
  searchable: false,
  defaultSortBy: 'createdAt',
  defaultSortDir: 'desc',
  pageSize: 20,
  columns: [
    { key: 'batchName', label: 'Batch', className: 'text-[#1E293B] font-medium' },
    { key: 'moduleCode', label: 'Module Code', mono: true, className: 'text-[#2563EB]' },
    { key: 'moduleName', label: 'Module Name' },
    { key: 'credits', label: 'Credits' },
  ],
  fields: [
    {
      name: 'batchId', label: 'Batch', type: 'select', required: true,
      optionsEndpoint: '/admin/batches/active', optionValue: 'batchId', optionLabel: 'label',
    },
    {
      name: 'moduleId', label: 'Module', type: 'select', required: true,
      optionsEndpoint: '/admin/modules/active', optionValue: 'moduleId', optionLabel: 'label',
    },
  ],
}

export const TIMETABLE_SESSIONS: ResourceConfig = {
  key: 'TIMETABLE_SESSIONS',
  title: 'Timetable Sessions',
  singular: 'Session',
  description: 'Scheduled classes. Clashes are blocked automatically.',
  endpoint: '/admin/timetable-sessions',
  searchable: false,
  sortable: ['sessionDate', 'startTime'],
  defaultSortBy: 'sessionDate',
  defaultSortDir: 'asc',
  pageSize: 20,
  columns: [
    { key: 'sessionDate', label: 'Date', className: 'text-[#1E293B] font-medium' },
    { key: 'slot', label: 'Time', render: slot, mono: true },
    { key: 'batchName', label: 'Batch' },
    { key: 'moduleCode', label: 'Module', mono: true, className: 'text-[#2563EB]' },
    { key: 'teacherName', label: 'Lecturer' },
    { key: 'roomCode', label: 'Room', mono: true },
    { key: 'status', label: 'Status', render: (r) => statusBadge(r.status) },
  ],
  fields: [
    {
      name: 'batchId', label: 'Batch', type: 'select', required: true, half: true,
      optionsEndpoint: '/admin/batches/active', optionValue: 'batchId', optionLabel: 'label',
    },
    {
      name: 'moduleId', label: 'Module', type: 'select', required: true, half: true,
      optionsEndpoint: '/admin/modules/active', optionValue: 'moduleId', optionLabel: 'label',
    },
    {
      name: 'teacherId', label: 'Lecturer', type: 'select', required: true, half: true,
      optionsEndpoint: '/admin/users', optionsParams: { role: 'ROLE_TEACHER' },
      optionValue: 'teacherId', optionLabel: 'fullName',
    },
    {
      name: 'roomId', label: 'Room', type: 'select', required: true, half: true,
      optionsEndpoint: '/admin/rooms/available', optionValue: 'roomId', optionLabel: 'label',
    },
    { name: 'sessionDate', label: 'Session Date', type: 'date', required: true },
    { name: 'startTime', label: 'Start Time', type: 'time', required: true, half: true },
    { name: 'endTime', label: 'End Time', type: 'time', required: true, half: true },
    {
      name: 'sessionType', label: 'Session Type', type: 'select', half: true,
      options: [
        { value: 'LECTURE', label: 'LECTURE' },
        { value: 'TUTORIAL', label: 'TUTORIAL' },
        { value: 'LAB', label: 'LAB' },
        { value: 'WORKSHOP', label: 'WORKSHOP' },
      ],
    },
    { name: 'status', label: 'Status', type: 'select', half: true, options: STATUS_OPTIONS },
  ],
}

export const EXAMS: ResourceConfig = {
  key: 'EXAMS',
  title: 'Exams',
  singular: 'Exam',
  description: 'Examination sessions per module and batch.',
  endpoint: '/admin/exams',
  searchable: false,
  sortable: ['examDate', 'startTime'],
  defaultSortBy: 'examDate',
  defaultSortDir: 'asc',
  pageSize: 20,
  columns: [
    { key: 'examDate', label: 'Date', className: 'text-[#1E293B] font-medium' },
    { key: 'slot', label: 'Time', render: slot, mono: true },
    { key: 'moduleCode', label: 'Module', mono: true, className: 'text-[#2563EB]' },
    { key: 'batchName', label: 'Batch' },
    { key: 'totalStudents', label: 'Students' },
    { key: 'roomCount', label: 'Rooms' },
    { key: 'invigilatorCount', label: 'Invigilators' },
    { key: 'status', label: 'Status', render: (r) => statusBadge(r.status) },
  ],
  fields: [
    {
      name: 'moduleId', label: 'Module', type: 'select', required: true, half: true,
      optionsEndpoint: '/admin/modules/active', optionValue: 'moduleId', optionLabel: 'label',
    },
    {
      name: 'batchId', label: 'Batch', type: 'select', required: true, half: true,
      optionsEndpoint: '/admin/batches/active', optionValue: 'batchId', optionLabel: 'label',
    },
    { name: 'examDate', label: 'Exam Date', type: 'date', required: true },
    { name: 'startTime', label: 'Start Time', type: 'time', required: true, half: true },
    { name: 'endTime', label: 'End Time', type: 'time', required: true, half: true },
    {
      name: 'examType', label: 'Exam Type', type: 'select', half: true,
      options: [
        { value: 'FINAL', label: 'FINAL' },
        { value: 'MIDTERM', label: 'MIDTERM' },
        { value: 'RESIT', label: 'RESIT' },
      ],
    },
    { name: 'status', label: 'Status', type: 'select', half: true, options: STATUS_OPTIONS },
  ],
}

export const EXAM_ROOMS: ResourceConfig = {
  key: 'EXAM_ROOMS',
  title: 'Exam Rooms',
  singular: 'Allocation',
  description: 'Venues allocated to each exam.',
  endpoint: '/admin/exam-rooms',
  searchable: false,
  defaultSortBy: 'createdAt',
  defaultSortDir: 'desc',
  pageSize: 20,
  columns: [
    { key: 'moduleCode', label: 'Module', mono: true, className: 'text-[#2563EB]' },
    { key: 'examDate', label: 'Exam Date' },
    { key: 'roomCode', label: 'Room', mono: true, className: 'text-[#1E293B] font-medium' },
    { key: 'allocatedCapacity', label: 'Allocated' },
    { key: 'seatsAllocated', label: 'Seats Used' },
    { key: 'status', label: 'Status', render: (r) => statusBadge(r.status) },
  ],
  fields: [
    {
      name: 'examId', label: 'Exam', type: 'select', required: true,
      optionsEndpoint: '/admin/exams', optionValue: 'examId',
      optionLabel: (r) => `${r.moduleCode ?? 'Exam'} — ${r.batchName ?? ''} (${r.examDate ?? ''})`,
    },
    {
      name: 'roomId', label: 'Room', type: 'select', required: true,
      optionsEndpoint: '/admin/rooms/available', optionValue: 'roomId', optionLabel: 'label',
    },
    { name: 'allocatedCapacity', label: 'Allocated Capacity', type: 'number', required: true, min: 1, half: true },
    { name: 'seatCount', label: 'Seat Count', type: 'number', min: 0, half: true },
    {
      name: 'status', label: 'Status', type: 'select',
      options: [
        { value: 'ALLOCATED', label: 'ALLOCATED' },
        { value: 'RELEASED', label: 'RELEASED' },
      ],
    },
  ],
}

export const INVIGILATORS: ResourceConfig = {
  key: 'INVIGILATORS',
  title: 'Invigilators',
  singular: 'Invigilator',
  description: 'Exam duty roster. Double-booking is blocked.',
  endpoint: '/admin/invigilators',
  searchable: false,
  defaultSortBy: 'createdAt',
  defaultSortDir: 'desc',
  pageSize: 20,
  columns: [
    { key: 'moduleCode', label: 'Module', mono: true, className: 'text-[#2563EB]' },
    { key: 'examDate', label: 'Exam Date' },
    { key: 'slot', label: 'Time', render: slot, mono: true },
    { key: 'fullName', label: 'Staff Member', className: 'text-[#1E293B] font-medium' },
    { key: 'role', label: 'Duty Role' },
  ],
  fields: [
    {
      name: 'examId', label: 'Exam', type: 'select', required: true,
      optionsEndpoint: '/admin/exams', optionValue: 'examId',
      optionLabel: (r) => `${r.moduleCode ?? 'Exam'} — ${r.batchName ?? ''} (${r.examDate ?? ''})`,
    },
    {
      name: 'userId', label: 'Staff Member', type: 'select', required: true,
      optionsEndpoint: '/admin/users', optionsParams: { role: 'ROLE_TEACHER' },
      optionValue: 'userId', optionLabel: 'fullName',
      helpText: 'Lecturers are listed here.',
    },
    {
      name: 'role', label: 'Duty Role', type: 'select',
      options: [
        { value: 'INVIGILATOR', label: 'INVIGILATOR' },
        { value: 'CHIEF_INVIGILATOR', label: 'CHIEF_INVIGILATOR' },
        { value: 'RELIEF', label: 'RELIEF' },
      ],
    },
  ],
}

export const SEAT_ALLOCATIONS: ResourceConfig = {
  key: 'SEAT_ALLOCATIONS',
  title: 'Seat Allocations',
  singular: 'Seat',
  description: 'Seating plan for each exam room.',
  endpoint: '/admin/seat-allocations',
  searchable: false,
  sortable: ['seatNumber'],
  defaultSortBy: 'seatNumber',
  defaultSortDir: 'asc',
  pageSize: 20,
  columns: [
    { key: 'moduleCode', label: 'Module', mono: true, className: 'text-[#2563EB]' },
    { key: 'examDate', label: 'Exam Date' },
    { key: 'roomCode', label: 'Room', mono: true },
    { key: 'studentNumber', label: 'Student No.', mono: true },
    { key: 'studentName', label: 'Student', className: 'text-[#1E293B] font-medium' },
    { key: 'seatNumber', label: 'Seat', mono: true, className: 'text-[#059669] font-semibold' },
  ],
  fields: [
    {
      name: 'examId', label: 'Exam', type: 'select', required: true,
      optionsEndpoint: '/admin/exams', optionValue: 'examId',
      optionLabel: (r) => `${r.moduleCode ?? 'Exam'} — ${r.batchName ?? ''} (${r.examDate ?? ''})`,
    },
    {
      name: 'examRoomId', label: 'Exam Room', type: 'select', required: true,
      optionsEndpoint: '/admin/exam-rooms', optionValue: 'examRoomId',
      optionLabel: (r) => `${r.roomCode ?? ''} — ${r.moduleCode ?? ''}`,
      helpText: 'Must be a room already allocated to the exam.',
    },
    {
      name: 'studentId', label: 'Student', type: 'select', required: true,
      optionsEndpoint: '/admin/users', optionsParams: { role: 'ROLE_STUDENT' },
      optionValue: 'studentId',
      optionLabel: (r) => `${r.fullName ?? ''} (${r.studentNumber ?? '—'})`,
      helpText: 'Only students in the exam batch can be seated.',
    },
    { name: 'seatNumber', label: 'Seat Number', type: 'text', required: true, maxLength: 30, half: true, placeholder: 'e.g. A-12' },
    { name: 'rowNumber', label: 'Row', type: 'text', maxLength: 20, half: true },
    { name: 'columnNumber', label: 'Column', type: 'text', maxLength: 20, half: true },
  ],
}

// ─── Registry ─────────────────────────────────────────────────────────────────

export type ResourceKey =
  | 'PROGRAMMES' | 'BATCHES' | 'MODULES' | 'ROOMS' | 'BATCH_MODULES'
  | 'TIMETABLE_SESSIONS' | 'EXAMS' | 'EXAM_ROOMS' | 'INVIGILATORS' | 'SEAT_ALLOCATIONS'

// Config plus the primary key used for update and delete
export const RESOURCES: Record<ResourceKey, { config: ResourceConfig; idField: string }> = {
  PROGRAMMES: { config: PROGRAMMES, idField: 'programmeId' },
  BATCHES: { config: BATCHES, idField: 'batchId' },
  MODULES: { config: MODULES, idField: 'moduleId' },
  ROOMS: { config: ROOMS, idField: 'roomId' },
  BATCH_MODULES: { config: BATCH_MODULES, idField: 'batchModuleId' },
  TIMETABLE_SESSIONS: { config: TIMETABLE_SESSIONS, idField: 'sessionId' },
  EXAMS: { config: EXAMS, idField: 'examId' },
  EXAM_ROOMS: { config: EXAM_ROOMS, idField: 'examRoomId' },
  INVIGILATORS: { config: INVIGILATORS, idField: 'invigilatorId' },
  SEAT_ALLOCATIONS: { config: SEAT_ALLOCATIONS, idField: 'allocationId' },
}
