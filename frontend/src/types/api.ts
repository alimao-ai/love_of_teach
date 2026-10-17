export type OfferingStatus = 'PLANNED' | 'OPEN' | 'CLOSED'

export type ResourceType = 'DOCUMENT' | 'SLIDES' | 'VIDEO' | 'LINK'

export type UserRole = 'DIRECTOR' | 'SUPERVISOR' | 'TEACHER' | 'ADMIN'

export interface AuthUser {
  id: number
  username: string
  displayName: string
  role: UserRole
  roleLabel: string
  teacherId?: number
  teacherNo?: string
  teacherName?: string
}

export interface AuthResponse {
  token: string
  user: AuthUser
}

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
  chapterId?: number
  chapterTitle?: string
  description?: string
  originalFilename?: string
  fileSize?: number
  downloadUrl: string
}

export interface ResourceChapter {
  id: number
  title: string
  displayOrder: number
  description?: string
  resourceCount: number
}

export interface ResourceSearchItem {
  id: number
  name: string
  resourceType: ResourceType
  resourceTypeLabel: string
  description?: string
  chapterId?: number
  chapterTitle?: string
  offeringId: number
  courseCode: string
  courseName: string
  teacherName: string
  semesterName: string
  className: string
  uploadedAt: string
  downloadUrl: string
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
  chapters: ResourceChapter[]
  resourceEmptyMessage?: string
}

export interface EvaluationSummaryItem {
  offeringId: number
  courseCode: string
  courseName: string
  teacherName: string
  departmentName: string
  semesterName: string
  className: string
  studentCount: number
  averageScore: number
  evaluationCount: number
  latestComment?: string
  latestEvaluatedAt?: string
  weak: boolean
}

export interface EvaluationSummary {
  evaluatedOfferingCount: number
  evaluationCount: number
  overallAverageScore: number
  weakOfferingCount: number
  items: EvaluationSummaryItem[]
}

export interface Evaluation {
  id: number
  offeringId: number
  courseCode: string
  courseName: string
  teacherName: string
  className: string
  score: number
  comment: string
  reviewerName: string
  evaluatedAt: string
}

export interface TeacherFeedback {
  teacherId: number
  teacherName: string
  offeringCount: number
  evaluationCount: number
  overallAverageScore: number
  courses: EvaluationSummaryItem[]
  feedback: Evaluation[]
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
