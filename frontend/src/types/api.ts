export type OfferingStatus = 'PLANNED' | 'OPEN' | 'CLOSED'

export type ResourceType = 'DOCUMENT' | 'SLIDES' | 'VIDEO' | 'LINK'

export interface Department {
  id: number
  code: string
  name: string
  active: boolean
  teacherCount: number
}

export interface Semester {
  id: number
  code: string
  name: string
  startDate: string
  endDate: string
  active: boolean
}

export interface Teacher {
  id: number
  employeeNo: string
  name: string
  title: string
  departmentId: number
  departmentName: string
}

export interface CourseListItem {
  offeringId: number
  courseCode: string
  courseName: string
  departmentCode: string
  departmentName: string
  teacherId: number
  teacherName: string
  semesterId: number
  semesterName: string
  className: string
  studentCount: number
  status: OfferingStatus
  statusLabel: string
  supervised: boolean
  resourceCount: number
}

export interface TeacherCourseGroup {
  teacherId: number
  employeeNo: string
  teacherName: string
  title: string
  departmentName: string
  courses: CourseListItem[]
}

export interface TeacherOverview {
  teachers: TeacherCourseGroup[]
  teacherCount: number
  courseCount: number
  emptyMessage?: string
}

export interface CourseResource {
  id: number
  name: string
  resourceType: ResourceType
  resourceTypeLabel: string
  url: string
  uploadedAt: string
}

export interface CourseDetail {
  offeringId: number
  courseCode: string
  courseName: string
  credits: number
  description: string
  departmentCode: string
  departmentName: string
  teacherId: number
  teacherName: string
  semesterId: number
  semesterName: string
  className: string
  studentCount: number
  status: OfferingStatus
  statusLabel: string
  supervised: boolean
  supervisionCount: number
  resources: CourseResource[]
  resourceEmptyMessage?: string
}

export interface CoverageSummary {
  offeringCount: number
  supervisedCount: number
  unsupervisedCount: number
  coverageRate: number
  studentCount: number
}

export interface DepartmentCoverage {
  departmentId: number
  departmentCode: string
  departmentName: string
  offeringCount: number
  supervisedCount: number
  unsupervisedCount: number
  coverageRate: number
}

export interface SupervisionCoverage {
  summary: CoverageSummary
  departmentBreakdown: DepartmentCoverage[]
  courses: CourseListItem[]
}

export interface ImportFailure {
  rowNumber: number
  courseCode?: string
  reason: string
}

export interface ImportResult {
  totalRows: number
  successCount: number
  failureCount: number
  failures: ImportFailure[]
  failureCsv: string
}

export interface ApiError {
  message: string
  status: number
  fieldErrors?: Record<string, string>
}

export interface CourseFilters {
  semesterId?: number
  departmentId?: number
  teacherId?: number
  keyword?: string
}

export interface DepartmentPayload {
  code: string
  name: string
  active: boolean
}

export interface SemesterPayload {
  code: string
  name: string
  startDate: string
  endDate: string
  active: boolean
}
