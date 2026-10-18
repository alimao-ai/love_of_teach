import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import LoginView from './LoginView.vue'

const mocks = vi.hoisted(() => ({
  login: vi.fn(),
  saveAuth: vi.fn(),
  push: vi.fn(),
}))

vi.mock('@/services/api', () => ({
  api: { login: mocks.login },
  errorMessage: () => '登录失败',
}))

vi.mock('@/stores/auth', () => ({
  saveAuth: mocks.saveAuth,
}))

vi.mock('vue-router', () => ({
  useRoute: () => ({ query: {} }),
  useRouter: () => ({ push: mocks.push }),
}))

describe('LoginView', () => {
  it('submits credentials and stores the returned identity', async () => {
    mocks.login.mockResolvedValue({
      token: 'token-123',
      user: {
        id: 1,
        username: 'teacher',
        displayName: '张明',
        role: 'TEACHER',
        roleLabel: '教师',
      },
    })
    const wrapper = mount(LoginView, {
      global: {
        stubs: {
          RouterLink: true,
        },
      },
    })

    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('teacher')
    await inputs[1].setValue('teacher123')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(mocks.login).toHaveBeenCalledWith('teacher', 'teacher123')
    expect(mocks.saveAuth).toHaveBeenCalled()
    expect(mocks.push).toHaveBeenCalledWith('/courses')
    expect(wrapper.text()).not.toContain('director123')
    expect(wrapper.text()).not.toContain('admin123')
  })
})
