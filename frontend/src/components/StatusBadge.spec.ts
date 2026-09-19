import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import StatusBadge from './StatusBadge.vue'

describe('StatusBadge', () => {
  it('renders the backend status label and semantic class', () => {
    const wrapper = mount(StatusBadge, {
      props: {
        status: 'OPEN',
        label: '开课中',
      },
    })

    expect(wrapper.text()).toBe('开课中')
    expect(wrapper.classes()).toContain('status-open')
  })
})
