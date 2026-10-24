import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import ResourceCoverageView from './ResourceCoverageView.vue'

const mocks = vi.hoisted(() => ({
  listDepartments: vi.fn(),
  listSemesters: vi.fn(),
  resourceCoverage: vi.fn(),
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() }),
}))

vi.mock('@/services/api', () => ({
  api: {
    listDepartments: mocks.listDepartments,
    listSemesters: mocks.listSemesters,
    resourceCoverage: mocks.resourceCoverage,
  },
  errorMessage: () => '请求失败',
}))

describe('ResourceCoverageView', () => {
  it('renders low coverage courses and department summary', async () => {
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
    mocks.resourceCoverage.mockResolvedValue({
      summary: {
        offeringCount: 2,
        coveredOfferingCount: 1,
        uncoveredOfferingCount: 1,
        coverageRate: 50,
        resourceCount: 3,
      },
      departmentBreakdown: [
        {
          departmentId: 1,
          departmentCode: 'SOFTWARE',
          departmentName: '软件工程教研室',
          offeringCount: 2,
          coveredOfferingCount: 1,
          uncoveredOfferingCount: 1,
          coverageRate: 50,
        },
      ],
      courses: [
        {
          offeringId: 20,
          courseCode: 'CS201',
          courseName: '数据结构',
          departmentName: '软件工程教研室',
          teacherName: '李华',
          className: '软件2302',
          resourceCount: 0,
          covered: false,
        },
      ],
    })

    const wrapper = mount(ResourceCoverageView)
    await flushPromises()

    expect(wrapper.text()).toContain('课程资源覆盖率')
    expect(wrapper.text()).toContain('数据结构')
    expect(wrapper.text()).toContain('待补充')
    expect(wrapper.text()).toContain('50.0%')
  })
})
