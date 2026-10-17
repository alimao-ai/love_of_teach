import { computed, reactive } from 'vue'
import type { AuthResponse, AuthUser, UserRole } from '@/types/api'

export const TOKEN_KEY = 'love_of_teach_token'
export const USER_KEY = 'love_of_teach_user'

const memoryStorage = new Map<string, string>()

export function storageGet(key: string) {
  try {
    if (typeof window !== 'undefined' && typeof window.localStorage?.getItem === 'function') {
      return window.localStorage.getItem(key)
    }
  } catch {
    // Fall through to memory storage in constrained test runtimes.
  }
  return memoryStorage.get(key) ?? null
}

export function storageSet(key: string, value: string) {
  try {
    if (typeof window !== 'undefined' && typeof window.localStorage?.setItem === 'function') {
      window.localStorage.setItem(key, value)
      return
    }
  } catch {
    // Fall through to memory storage in constrained test runtimes.
  }
  memoryStorage.set(key, value)
}

export function storageRemove(key: string) {
  try {
    if (typeof window !== 'undefined' && typeof window.localStorage?.removeItem === 'function') {
      window.localStorage.removeItem(key)
      return
    }
  } catch {
    // Fall through to memory storage in constrained test runtimes.
  }
  memoryStorage.delete(key)
}

function loadUser(): AuthUser | undefined {
  const value = storageGet(USER_KEY)
  if (!value) return undefined
  try {
    return JSON.parse(value) as AuthUser
  } catch {
    storageRemove(USER_KEY)
    return undefined
  }
}

export const authState = reactive<{
  token: string
  user?: AuthUser
}>({
  token: storageGet(TOKEN_KEY) ?? '',
  user: loadUser(),
})

export const isAuthenticated = computed(() => Boolean(authState.token))

export function saveAuth(response: AuthResponse) {
  authState.token = response.token
  authState.user = response.user
  storageSet(TOKEN_KEY, response.token)
  storageSet(USER_KEY, JSON.stringify(response.user))
}

export function clearAuth() {
  authState.token = ''
  authState.user = undefined
  storageRemove(TOKEN_KEY)
  storageRemove(USER_KEY)
}

export function hasRole(...roles: UserRole[]) {
  return Boolean(authState.user && roles.includes(authState.user.role))
}
