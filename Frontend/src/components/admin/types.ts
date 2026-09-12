// Shared types and helpers for the admin dashboard

export interface UserProfile {
  userId: string
  fullName: string
  username: string
  email: string
  phoneNumber?: string
  role: string
  isActive?: boolean
  createdAt?: string

  // Student specific
  studentId?: string
  studentNumber?: string
  registrationNumber?: string
  studentStatus?: string
  batchId?: string
  batchName?: string
  programmeName?: string

  // Teacher specific
  teacherId?: string
  teacherDepartment?: string
  teacherDesignation?: string

  // Staff specific
  staffId?: string
  staffDepartment?: string
  staffDesignation?: string
}

// Spring Data page wrapper
export interface PageResponse {
  content: UserProfile[]
  pageNumber: number
  pageSize: number
  totalElements: number
  totalPages: number
  last: boolean
}

export interface DashboardStats {
  students: number
  teachers: number
  staff: number
  rooms: number
  conflicts: number
}

export interface Batch {
  batchId: string
  batchName: string
  yearOfStudy?: number
  semester?: number
  isActive?: boolean
  programmeId?: string
  programmeName?: string
  programmeCode?: string
  label?: string
}

// Fields collected by the create-user form
export interface AddUserForm {
  fullName: string
  username: string
  email: string
  password: string
  phoneNumber: string
  studentNumber: string
  registrationNumber: string
  batchId: string
  department: string
  designation: string
}

export type ActiveTab = 'STUDENTS' | 'TEACHERS' | 'STAFF'
export type SortField = 'fullName' | 'username' | 'email' | 'createdAt'
export type SortDir = 'asc' | 'desc'

// Blank form used on open and reset
export const EMPTY_ADD_FORM: AddUserForm = {
  fullName: '',
  username: '',
  email: '',
  password: '',
  phoneNumber: '',
  studentNumber: '',
  registrationNumber: '',
  batchId: '',
  department: '',
  designation: '',
}

// Singular label for headings
export function tabLabel(tab: ActiveTab): string {
  switch (tab) {
    case 'STUDENTS': return 'Student'
    case 'TEACHERS': return 'Teacher'
    case 'STAFF': return 'Staff'
  }
}

// Backend role name for a tab
export function tabToRole(tab: ActiveTab): string {
  switch (tab) {
    case 'STUDENTS': return 'ROLE_STUDENT'
    case 'TEACHERS': return 'ROLE_TEACHER'
    case 'STAFF': return 'ROLE_STAFF'
  }
}

// Create endpoint for a tab
export function tabToEndpoint(tab: ActiveTab): string {
  switch (tab) {
    case 'STUDENTS': return '/admin/users/student'
    case 'TEACHERS': return '/admin/users/teacher'
    case 'STAFF': return '/admin/users/staff'
  }
}

// Initials shown in avatars
export function initialsOf(fullName?: string): string {
  return fullName?.split(' ').map((n) => n[0]).join('').slice(0, 2).toUpperCase() || '?'
}
