import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import DepartmentDrilldownView from './DepartmentDrilldownView.vue'

const mocks = vi.hoisted(() => ({
  listDepartments: vi.fn(),
  listSemesters: vi.fn(),
  courseDrilldown: vi.fn(),
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() }),
}))

vi.mock('@/services/api', () => ({
  api: {
    listDepartments: mocks.listDepartments,
    listSemesters: mocks.listSemesters,
    courseDrilldown: mocks.courseDrilldown,
  },
  errorMessage: () => '请求失败',
}))

describe('DepartmentDrilldownView', () => {
  it('renders department summary and drilldown courses', async () => {
    mocks.listDepartments.mockResolvedValue([
      { id: 1, code: 'SOFTWARE', name: '软件工程教研室', active: true, teacherCount: 5 },
    ])
    mocks.listSemesters.mockResolvedValue([
      {
        id: 1,
        code: '2025-2026-2',
        name: '2025-2026 学年第二学期',
        startDate: '2026-02-23',
        endDate: '2026-07-10',
        active: true,
      },
    ])
    mocks.courseDrilldown.mockResolvedValue({
      departmentCount: 1,
      offeringCount: 1,
      studentCount: 58,
      supervisedOfferingCount: 1,
      unsupervisedOfferingCount: 0,
      supervisionCoverageRate: 100,
      departments: [
        {
          departmentId: 1,
          departmentCode: 'SOFTWARE',
          departmentName: '软件工程教研室',
          offeringCount: 1,
          studentCount: 58,
          supervisedOfferingCount: 1,
          unsupervisedOfferingCount: 0,
          supervisionCoverageRate: 100,
          courses: [
            {
              offeringId: 10,
              courseCode: 'CS101',
              courseName: '程序设计基础',
              departmentId: 1,
              departmentCode: 'SOFTWARE',
              departmentName: '软件工程教研室',
              teacherId: 2,
              teacherName: '张明',
              semesterId: 1,
              semesterName: '2025-2026 学年第二学期',
              className: '软件2301',
              studentCount: 58,
              status: 'OPEN',
              statusLabel: '开课',
              supervised: true,
              resourceCount: 3,
            },
          ],
        },
      ],
    })

    const wrapper = mount(DepartmentDrilldownView)
    await flushPromises()

    expect(wrapper.text()).toContain('学院课程钻取')
    expect(wrapper.text()).toContain('程序设计基础')
    expect(wrapper.text()).toContain('100.0%')
  })
})
