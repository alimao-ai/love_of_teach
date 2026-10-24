import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import SupervisionPlansView from './SupervisionPlansView.vue'

const mocks = vi.hoisted(() => ({
  listDepartments: vi.fn(),
  listSemesters: vi.fn(),
  listCourses: vi.fn(),
  supervisionPlans: vi.fn(),
  createSupervisionPlan: vi.fn(),
  updateSupervisionPlanStatus: vi.fn(),
}))

vi.mock('@/services/api', () => ({
  api: {
    listDepartments: mocks.listDepartments,
    listSemesters: mocks.listSemesters,
    listCourses: mocks.listCourses,
    supervisionPlans: mocks.supervisionPlans,
    createSupervisionPlan: mocks.createSupervisionPlan,
    updateSupervisionPlanStatus: mocks.updateSupervisionPlanStatus,
  },
  errorMessage: () => '请求失败',
}))

describe('SupervisionPlansView', () => {
  it('renders supervision plan progress and next action', async () => {
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
    mocks.listCourses.mockResolvedValue([
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
        supervised: false,
        resourceCount: 2,
      },
    ])
    mocks.supervisionPlans.mockResolvedValue({
      summary: {
        total: 1,
        planned: 0,
        inProgress: 1,
        completed: 0,
        overdue: 1,
        progressRate: 0,
      },
      plans: [
        {
          id: 3,
          offeringId: 10,
          courseCode: 'CS101',
          courseName: '程序设计基础',
          departmentName: '软件工程教研室',
          teacherName: '张明',
          className: '软件2301',
          semesterName: '2025-2026 学年第二学期',
          plannedDate: '2026-10-20',
          dueDate: '2026-10-23',
          status: 'IN_PROGRESS',
          statusLabel: '进行中',
          overdue: true,
          note: '检查课堂互动',
          supervisorName: '周督导',
        },
      ],
    })

    const wrapper = mount(SupervisionPlansView)
    await flushPromises()

    expect(wrapper.text()).toContain('程序设计基础')
    expect(wrapper.text()).toContain('已逾期')
    expect(wrapper.text()).toContain('完成计划')
    expect(wrapper.text()).toContain('周督导')
  })
})
