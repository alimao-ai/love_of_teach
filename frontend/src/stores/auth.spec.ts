import { beforeEach, describe, expect, it } from 'vitest'
import {
  authState,
  clearAuth,
  hasRole,
  saveAuth,
  storageGet,
  TOKEN_KEY,
  USER_KEY,
} from './auth'

describe('auth store', () => {
  beforeEach(() => {
    clearAuth()
  })

  it('persists login token and identity', () => {
    saveAuth({
      token: 'token-123',
      user: {
        id: 1,
        username: 'teacher',
        displayName: '张明',
        role: 'TEACHER',
        roleLabel: '教师',
        teacherId: 10,
      },
    })

    expect(authState.token).toBe('token-123')
    expect(storageGet(TOKEN_KEY)).toBe('token-123')
    expect(JSON.parse(storageGet(USER_KEY) ?? '{}').displayName).toBe('张明')
  })

  it('checks role access from the current identity', () => {
    saveAuth({
      token: 'token-123',
      user: {
        id: 2,
        username: 'supervisor',
        displayName: '周督导',
        role: 'SUPERVISOR',
        roleLabel: '教学督导',
      },
    })

    expect(hasRole('SUPERVISOR', 'DIRECTOR')).toBe(true)
    expect(hasRole('TEACHER')).toBe(false)
  })

  it('clears token and user data on logout', () => {
    saveAuth({
      token: 'token-123',
      user: {
        id: 3,
        username: 'admin',
        displayName: '管理员',
        role: 'ADMIN',
        roleLabel: '管理员',
      },
    })
    clearAuth()

    expect(authState.token).toBe('')
    expect(authState.user).toBeUndefined()
    expect(storageGet(TOKEN_KEY)).toBeNull()
    expect(storageGet(USER_KEY)).toBeNull()
  })
})
