// One placed session in the weekly pattern the generator returned
export interface RoutineSession {
  sessionId?: string
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
  roomCapacity: number | null
  groupId: string | null
  groupName: string | null
  slotId: string | null
  periodNumber: number | null
  dayOfWeek: string
  startTime: string
  endTime: string
  sessionType: string
  status: string
  durationMinutes: number
}

// Something the solver could not fit, with the resource that ran out
export interface UnplacedRequirement {
  moduleCode: string
  moduleName: string
  sessionType: string
  groupName: string | null
  durationMinutes: number
  reason: string
}

// Summary row for one generator run
export interface GenerationRun {
  runId: string
  batchId: string
  batchName: string
  fromDate: string
  toDate: string
  status: string
  requirementsTotal: number
  requirementsPlaced: number
  penaltyScore: number
  sessionsCreated: number
  notes: string | null
  createdAt: string
}

// Everything one generate call produced
export interface GenerationResult {
  run: GenerationRun
  weeklyPattern: RoutineSession[]
  unplaced: UnplacedRequirement[]
  penaltyBeforeOptimise: number
  penaltyAfterOptimise: number
  idleGapsBefore: number
  idleGapsAfter: number
  generationMillis: number
}
