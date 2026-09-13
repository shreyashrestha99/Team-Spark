import {
  GraduationCap,
  Briefcase,
  UserCog,
  LayoutDashboard,
  DoorOpen,
  BookOpen,
  Layers,
  Library,
  CalendarDays,
  FileSpreadsheet,
  Building2,
  ShieldCheck,
  Armchair,
  Building,
  Users,
  Clock,
  CalendarOff,
  UserX,
  Wand2,
  type LucideIcon,
} from 'lucide-react'
import type { ActiveTab } from './types'
import type { ResourceKey } from './resources'

// Bespoke screens rather than CRUD tables
export type SpecialView = 'DASHBOARD' | 'ROUTINE_GENERATOR' | 'EXAM_SEATING'

// Either a user-role tab, a generic CRUD resource, or a bespoke screen
export type AdminView = ActiveTab | ResourceKey | SpecialView

export interface NavItem {
  id: AdminView
  label: string
  icon: LucideIcon
  // Key into the dashboard stats for a count badge
  statKey?: 'students' | 'teachers' | 'staff' | 'rooms'
}

export interface NavGroup {
  heading: string
  items: NavItem[]
}

export const NAV_GROUPS: NavGroup[] = [
  {
    heading: 'Overview',
    items: [
      { id: 'DASHBOARD', label: 'Dashboard', icon: LayoutDashboard },
    ],
  },
  {
    heading: 'User Management',
    items: [
      { id: 'STUDENTS', label: 'Students', icon: GraduationCap, statKey: 'students' },
      { id: 'TEACHERS', label: 'Teachers', icon: Briefcase, statKey: 'teachers' },
      { id: 'STAFF', label: 'Staff', icon: UserCog, statKey: 'staff' },
    ],
  },
  {
    heading: 'Academic Setup',
    items: [
      { id: 'PROGRAMMES', label: 'Programmes', icon: Library },
      { id: 'BATCHES', label: 'Batches', icon: Layers },
      { id: 'MODULES', label: 'Modules', icon: BookOpen },
      { id: 'BATCH_MODULES', label: 'Batch Modules', icon: LayoutDashboard },
      { id: 'STUDENT_GROUPS', label: 'Student Groups', icon: Users },
      { id: 'BUILDINGS', label: 'Buildings', icon: Building },
      { id: 'ROOMS', label: 'Rooms', icon: DoorOpen, statKey: 'rooms' },
    ],
  },
  {
    heading: 'Scheduling',
    items: [
      { id: 'ROUTINE_GENERATOR', label: 'Generate Routine', icon: Wand2 },
      { id: 'TIMETABLE_SESSIONS', label: 'Timetable', icon: CalendarDays },
      { id: 'TIME_SLOTS', label: 'Time Slots', icon: Clock },
      { id: 'TEACHER_AVAILABILITY', label: 'Availability', icon: UserX },
      { id: 'HOLIDAYS', label: 'Holidays', icon: CalendarOff },
    ],
  },
  {
    heading: 'Examinations',
    items: [
      { id: 'EXAMS', label: 'Exams', icon: FileSpreadsheet },
      { id: 'EXAM_ROOMS', label: 'Exam Rooms', icon: Building2 },
      { id: 'INVIGILATORS', label: 'Invigilators', icon: ShieldCheck },
      { id: 'EXAM_SEATING', label: 'Auto Seating', icon: Armchair },
      { id: 'SEAT_ALLOCATIONS', label: 'Seat Plan', icon: Armchair },
    ],
  },
]

// True when the view is one of the user-role tabs
export function isUserTab(view: AdminView): view is ActiveTab {
  return view === 'STUDENTS' || view === 'TEACHERS' || view === 'STAFF'
}

// Label shown in the top bar
export function viewLabel(view: AdminView): string {
  for (const group of NAV_GROUPS) {
    const found = group.items.find((item) => item.id === view)
    if (found) return found.label
  }
  return 'Dashboard'
}
