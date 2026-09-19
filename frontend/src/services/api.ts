import axios from 'axios'
import type {
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
} from '@/types/api'

const http = axios.create({
  baseURL: '/api',
  timeout: 15_000,
})

function compactParams<T extends object>(params: T) {
  return Object.fromEntries(
    Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== ''),
  )
}

export const api = {
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

  templateUrl: '/api/import/courses/template',
}

export function errorMessage(error: unknown) {
  if (axios.isAxiosError(error)) {
    const message = (error.response?.data as { message?: string } | undefined)?.message
    return message || error.message || '请求失败'
  }
  return error instanceof Error ? error.message : '请求失败'
}
