import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import RegisterView from './RegisterView.vue'

vi.mock('@/services/api', () => ({
  api: { register: vi.fn() },
  errorMessage: () => '注册失败',
}))

vi.mock('@/stores/auth', () => ({
  saveAuth: vi.fn(),
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() }),
}))

describe('RegisterView', () => {
  it('only offers teacher registration and requires a teacher number and invitation', async () => {
    const wrapper = mount(RegisterView, {
      global: {
        stubs: {
          RouterLink: true,
        },
      },
    })

    expect(wrapper.find('input[placeholder="例如 T001"]').exists()).toBe(true)
    expect(wrapper.find('input[placeholder="请输入管理员提供的邀请码"]').exists()).toBe(true)
    expect(wrapper.find('select').exists()).toBe(false)
    expect(wrapper.text()).toContain('教师注册需要邀请码')
  })
})
