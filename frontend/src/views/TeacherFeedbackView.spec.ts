import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import TeacherFeedbackView from './TeacherFeedbackView.vue'

const mocks = vi.hoisted(() => ({
  listSemesters: vi.fn(),
  myFeedback: vi.fn(),
}))

vi.mock('@/services/api', () => ({
  api: {
    listSemesters: mocks.listSemesters,
    myFeedback: mocks.myFeedback,
  },
  errorMessage: () => '请求失败',
}))

describe('TeacherFeedbackView', () => {
  it('shows only feedback returned for the current teacher', async () => {
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
    mocks.myFeedback.mockResolvedValue({
      teacherId: 1,
      teacherName: '张明',
      offeringCount: 1,
      evaluationCount: 1,
      overallAverageScore: 88,
      courses: [
        {
          offeringId: 1,
          courseCode: 'CS101',
          courseName: '程序设计基础',
          teacherName: '张明',
          departmentName: '软件工程教研室',
          semesterName: '2025-2026 学年第二学期',
          className: '软件2301',
          studentCount: 58,
          averageScore: 88,
          evaluationCount: 1,
          latestComment: '课堂组织清晰',
          latestEvaluatedAt: '2026-10-17T10:00:00',
          weak: false,
        },
      ],
      feedback: [
        {
          id: 1,
          offeringId: 1,
          courseCode: 'CS101',
          courseName: '程序设计基础',
          teacherName: '张明',
          className: '软件2301',
          score: 88,
          comment: '课堂组织清晰',
          reviewerName: '周督导',
          evaluatedAt: '2026-10-17T10:00:00',
        },
      ],
    })

    const wrapper = mount(TeacherFeedbackView)
    await flushPromises()

    expect(wrapper.text()).toContain('课堂组织清晰')
    expect(wrapper.text()).toContain('周督导')
    expect(wrapper.text()).toContain('88')
  })
})
