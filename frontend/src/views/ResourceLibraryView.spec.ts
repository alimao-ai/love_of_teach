import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import ResourceLibraryView from './ResourceLibraryView.vue'

const mocks = vi.hoisted(() => ({
  listDepartments: vi.fn(),
  listSemesters: vi.fn(),
  searchResources: vi.fn(),
}))

vi.mock('@/services/api', () => ({
  api: {
    listDepartments: mocks.listDepartments,
    listSemesters: mocks.listSemesters,
    searchResources: mocks.searchResources,
  },
  errorMessage: () => '请求失败',
}))

describe('ResourceLibraryView', () => {
  it('renders searchable chapter resources returned by the API', async () => {
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
    mocks.searchResources.mockResolvedValue([
      {
        id: 1,
        name: '第一章教案',
        resourceType: 'DOCUMENT',
        resourceTypeLabel: '文档',
        description: '课堂教案',
        chapterId: 10,
        chapterTitle: '第 1 章 基础语法',
        offeringId: 20,
        courseCode: 'CS101',
        courseName: '程序设计基础',
        teacherName: '张明',
        semesterName: '2025-2026 学年第二学期',
        className: '软件2301',
        uploadedAt: '2026-10-17T10:00:00',
        downloadUrl: '/api/resources/1/download',
      },
    ])

    const wrapper = mount(ResourceLibraryView)
    await flushPromises()

    expect(wrapper.text()).toContain('第一章教案')
    expect(wrapper.text()).toContain('第 1 章 基础语法')
    expect(wrapper.text()).toContain('程序设计基础')
  })
})
