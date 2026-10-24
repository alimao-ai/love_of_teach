import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import NotificationsView from './NotificationsView.vue'

const mocks = vi.hoisted(() => ({
  notificationOverview: vi.fn(),
  markNotificationsRead: vi.fn(),
  subscribeCourse: vi.fn(),
  unsubscribeCourse: vi.fn(),
}))

vi.mock('@/services/api', () => ({
  api: {
    notificationOverview: mocks.notificationOverview,
    markNotificationsRead: mocks.markNotificationsRead,
    subscribeCourse: mocks.subscribeCourse,
    unsubscribeCourse: mocks.unsubscribeCourse,
  },
  errorMessage: () => '请求失败',
}))

describe('NotificationsView', () => {
  it('renders subscriptions and notifications, then marks all as read', async () => {
    mocks.notificationOverview.mockResolvedValue({
      unreadCount: 1,
      courses: [
        {
          courseId: 1,
          courseCode: 'CS101',
          courseName: '程序设计基础',
          teacherName: '张明',
          subscribed: true,
        },
      ],
      notifications: [
        {
          id: 1,
          courseId: 1,
          courseCode: 'CS101',
          courseName: '程序设计基础',
          title: '课程信息已更新',
          message: '课程 CS101 的开课信息已更新',
          createdAt: '2026-10-24T08:00:00',
          read: false,
        },
      ],
    })
    mocks.markNotificationsRead.mockResolvedValue(undefined)

    const wrapper = mount(NotificationsView)
    await flushPromises()

    expect(wrapper.text()).toContain('课程信息已更新')
    expect(wrapper.text()).toContain('已订阅课程')
    expect(wrapper.text()).toContain('退订')

    const markReadButton = wrapper
      .findAll('button')
      .find((button) => button.text().includes('全部已读'))
    await markReadButton?.trigger('click')
    await flushPromises()

    expect(mocks.markNotificationsRead).toHaveBeenCalled()
    expect(wrapper.text()).toContain('通知已全部标记为已读')
  })
})
