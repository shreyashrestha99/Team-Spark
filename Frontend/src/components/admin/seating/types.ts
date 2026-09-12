// One physical desk in a hall, occupied or deliberately left empty
export interface SeatCell {
  seatNumber: string
  rowLabel: string
  columnNumber: number
  occupied: boolean
  studentId: string | null
  studentName: string | null
  studentNumber: string | null
  moduleCode: string | null
  // True when another exam sharing this hall holds the desk
  otherExam: boolean
}

// One hall allocated to an exam, with its full desk grid
export interface SeatingRoom {
  examRoomId: string
  roomId: string
  roomCode: string
  roomName: string
  buildingName: string | null
  seatRows: number | null
  seatsPerRow: number | null
  capacity: number | null
  examCapacity: number | null
  allocatedCapacity: number | null
  seatedHere: number
  cells: SeatCell[]
}

// Everything one exam's seating plan contains
export interface SeatingPlan {
  examId: string
  moduleCode: string
  moduleName: string
  batchName: string
  examDate: string
  startTime: string
  endTime: string
  totalStudents: number
  seatedStudents: number
  unseatedStudents: number
  rooms: SeatingRoom[]
  warnings: string[]
  allocationMillis: number
}
