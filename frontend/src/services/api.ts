import axios from 'axios'
import type {
  AuthResponse,
  AuthUser,
  CourseDetail,
  CourseFilters,
  CourseListItem,
  Department,
  DepartmentPayload,
  ImportResult,
  Semester,
  SemesterPayload,
  SupervisionCoverage,
  Teacher,
  TeacherOverview,
  Evaluation,
  EvaluationSummary,
  ResourceChapter,
  ResourceSearchItem,
  TeacherFeedback,
  UserRole,
} from '@/types/api'
import { storageGet, storageRemove, TOKEN_KEY } from '@/stores/auth'

const http = axios.create({
  baseURL: '/api',
  timeout: 15_000,
})

http.interceptors.request.use((config) => {
  const token = storageGet(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (
      axios.isAxiosError(error) &&
      error.response?.status === 401 &&
      !window.location.pathname.startsWith('/login') &&
      !window.location.pathname.startsWith('/register')
    ) {
      storageRemove(TOKEN_KEY)
      storageRemove('love_of_teach_user')
      window.location.assign('/login')
    }
    return Promise.reject(error)
  },
)

function compactParams<T extends object>(params: T) {
  return Object.fromEntries(
    Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== ''),
  )
}

export const api = {
  async login(username: string, password: string) {
    const { data } = await http.post<AuthResponse>('/auth/login', { username, password })
    return data
  },

  async register(payload: {
    username: string
    displayName: string
    password: string
    role: UserRole
    teacherNo?: string
  }) {
    const { data } = await http.post<AuthResponse>('/auth/register', payload)
    return data
  },

  async logout() {
    await http.post('/auth/logout')
  },

  async me() {
    const { data } = await http.get<AuthUser>('/auth/me')
    return data
  },

  async listDepartments(activeOnly = false) {
    const { data } = await http.get<Department[]>('/departments', {
      params: { activeOnly },
    })
    return data
  },

  async createDepartment(payload: DepartmentPayload) {
    const { data } = await http.post<Department>('/departments', payload)
    return data
  },

  async updateDepartment(id: number, payload: DepartmentPayload) {
    const { data } = await http.put<Department>(`/departments/${id}`, payload)
    return data
  },

  async updateDepartmentStatus(id: number, active: boolean) {
    const { data } = await http.patch<Department>(`/departments/${id}/status`, { active })
    return data
  },

  async listSemesters(activeOnly = false) {
    const { data } = await http.get<Semester[]>('/semesters', {
      params: { activeOnly },
    })
    return data
  },

  async createSemester(payload: SemesterPayload) {
    const { data } = await http.post<Semester>('/semesters', payload)
    return data
  },

  async updateSemester(id: number, payload: SemesterPayload) {
    const { data } = await http.put<Semester>(`/semesters/${id}`, payload)
    return data
  },

  async updateSemesterStatus(id: number, active: boolean) {
    const { data } = await http.patch<Semester>(`/semesters/${id}/status`, { active })
    return data
  },

  async listTeachers(departmentId?: number) {
    const { data } = await http.get<Teacher[]>('/teachers', {
      params: compactParams({ departmentId }),
    })
    return data
  },

  async listCourses(filters: CourseFilters) {
    const { data } = await http.get<CourseListItem[]>('/courses', {
      params: compactParams(filters),
    })
    return data
  },

  async teacherOverview(filters: CourseFilters) {
    const { data } = await http.get<TeacherOverview>('/courses/teacher-overview', {
      params: compactParams(filters),
    })
    return data
  },

  async getCourseDetail(offeringId: number) {
    const { data } = await http.get<CourseDetail>(`/courses/${offeringId}`)
    return data
  },

  async listChapters(offeringId: number) {
    const { data } = await http.get<ResourceChapter[]>(`/offerings/${offeringId}/chapters`)
    return data
  },

  async createChapter(
    offeringId: number,
    payload: { title: string; displayOrder: number; description?: string },
  ) {
    const { data } = await http.post<ResourceChapter>(
      `/offerings/${offeringId}/chapters`,
      payload,
    )
    return data
  },

  async uploadResource(
    offeringId: number,
    payload: {
      name: string
      resourceType: string
      chapterId?: number
      description?: string
      file: File
    },
  ) {
    const formData = new FormData()
    formData.append('name', payload.name)
    formData.append('resourceType', payload.resourceType)
    if (payload.chapterId) formData.append('chapterId', String(payload.chapterId))
    if (payload.description) formData.append('description', payload.description)
    formData.append('file', payload.file)
    const { data } = await http.post(`/offerings/${offeringId}/resources`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    return data
  },

  async searchResources(params: {
    semesterId?: number
    departmentId?: number
    offeringId?: number
    chapterId?: number
    resourceType?: string
    keyword?: string
  }) {
    const { data } = await http.get<ResourceSearchItem[]>('/resources/search', {
      params: compactParams(params),
    })
    return data
  },

  async submitEvaluation(payload: { offeringId: number; score: number; comment: string }) {
    const { data } = await http.post<Evaluation>('/evaluations', payload)
    return data
  },

  async evaluationSummary(filters: { semesterId?: number; departmentId?: number }) {
    const { data } = await http.get<EvaluationSummary>('/evaluations/summary', {
      params: compactParams(filters),
    })
    return data
  },

  async myFeedback(semesterId?: number) {
    const { data } = await http.get<TeacherFeedback>('/evaluations/mine', {
      params: compactParams({ semesterId }),
    })
    return data
  },

  async getCoverage(filters: Pick<CourseFilters, 'semesterId' | 'departmentId'>) {
    const { data } = await http.get<SupervisionCoverage>('/supervision/coverage', {
      params: compactParams(filters),
    })
    return data
  },

  async importCourses(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    const { data } = await http.post<ImportResult>('/import/courses', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    return data
  },

  async downloadTemplate() {
    const { data } = await http.get('/import/courses/template', { responseType: 'blob' })
    downloadBlob(data, 'course-import-template.csv')
  },

  async exportOfferings(filters: {
    semesterId?: number
    departmentId?: number
    teacherId?: number
  }) {
    const { data, headers } = await http.get('/reports/offerings/export', {
      params: compactParams(filters),
      responseType: 'blob',
    })
    const disposition = String(headers['content-disposition'] ?? '')
    const match = disposition.match(/filename="?([^";]+)"?/i)
    downloadBlob(data, match?.[1] ?? 'course-offering-report.csv')
  },

  async downloadResource(url: string, fallbackName = 'resource') {
    if (/^https?:\/\//i.test(url)) {
      window.open(url, '_blank', 'noopener,noreferrer')
      return
    }
    const { data } = await http.get(url.replace(/^\/api/, ''), { responseType: 'blob' })
    downloadBlob(data, fallbackName)
  },
}

function downloadBlob(data: Blob, filename: string) {
  const url = URL.createObjectURL(data)
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = filename
  document.body.appendChild(anchor)
  anchor.click()
  anchor.remove()
  window.setTimeout(() => URL.revokeObjectURL(url), 1_000)
}

export function errorMessage(error: unknown) {
  if (axios.isAxiosError(error)) {
    const message = (error.response?.data as { message?: string } | undefined)?.message
    return message || error.message || '请求失败'
  }
  return error instanceof Error ? error.message : '请求失败'
}
