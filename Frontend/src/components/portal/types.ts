// One dated class on a personal timetable
export interface PortalSession {
  sessionId: string
  batchId: string
  batchName: string
  moduleId: string
  moduleCode: string
  moduleName: string
  teacherId: string
  teacherName: string | null
  roomId: string
  roomCode: string
  roomName: string
  groupId: string | null
  groupName: string | null
  periodNumber: number | null
  dayOfWeek: string
  sessionDate: string
  startTime: string
  endTime: string
  sessionType: string | null
  status: string
  durationMinutes: number
}

// Who is signed in, in the terms their own portal cares about
export interface MyProfile {
  userId: string
  fullName: string
  email: string

  studentId?: string
  studentNumber?: string
  registrationNumber?: string
  batchName?: string
  groupName?: string
  programmeName?: string
  yearOfStudy?: number
  semester?: number

  teacherId?: string
  department?: string
  designation?: string
  maxWeeklyHours?: number
}

// One exam a student sits, and the desk they sit it at
export interface MyExamSeat {
  examId: string
  moduleCode: string
  moduleName: string
  examType: string | null
  examDate: string
  startTime: string
  endTime: string
  durationMinutes: number | null
  buildingName: string | null
  roomCode: string | null
  roomName: string | null
  seatNumber: string
  rowLabel: string | null
  columnNumber: string | null
  status: string
  upcoming: boolean
  daysAway: number
}

// One exam a lecturer is rostered to invigilate
export interface InvigilationDuty {
  invigilatorId: string
  examId: string
  moduleCode: string
  moduleName: string
  batchName: string
  examDate: string
  startTime: string
  endTime: string
  durationMinutes: number | null
  role: string
  rooms: string[]
  candidateCount: number
  upcoming: boolean
  daysAway: number
}

export interface StudentDashboard {
  profile: MyProfile
  todaysClasses: PortalSession[]
  weekAhead: PortalSession[]
  upcomingExams: MyExamSeat[]
  nextExam: MyExamSeat | null
  classesThisWeek: number
  contactHoursThisWeek: number
  upcomingExamCount: number
}

export interface TeacherDashboard {
  profile: MyProfile
  todaysClasses: PortalSession[]
  weekAhead: PortalSession[]
  upcomingDuties: InvigilationDuty[]
  nextDuty: InvigilationDuty | null
  classesThisWeek: number
  contactHoursThisWeek: number
  overloaded: boolean
  modulesTaught: number
  upcomingDutyCount: number
}
