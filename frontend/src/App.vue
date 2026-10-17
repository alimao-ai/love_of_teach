<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import {
  BookOpen,
  ClipboardCheck,
  ChevronRight,
  Database,
  FileDown,
  FolderOpen,
  GraduationCap,
  LogOut,
  MessageSquare,
  ShieldCheck,
  Star,
  UploadCloud,
  UserRound,
} from 'lucide-vue-next'
import { api } from '@/services/api'
import { authState, clearAuth, hasRole } from '@/stores/auth'
import type { UserRole } from '@/types/api'

const route = useRoute()
const router = useRouter()

const allNavItems: Array<{
  to: string
  label: string
  caption: string
  icon: typeof BookOpen
  roles?: UserRole[]
}> = [
  {
    to: '/courses',
    label: '课程查询',
    caption: '全部角色',
    icon: BookOpen,
  },
  {
    to: '/resources',
    label: '教学资源',
    caption: '章节与资源检索',
    icon: FolderOpen,
  },
  {
    to: '/coverage',
    label: '督导覆盖',
    caption: '教学督导',
    icon: ShieldCheck,
    roles: ['SUPERVISOR', 'DIRECTOR', 'ADMIN'],
  },
  {
    to: '/evaluations',
    label: '课程评价',
    caption: '督导 / 主任',
    icon: Star,
    roles: ['SUPERVISOR', 'DIRECTOR', 'ADMIN'],
  },
  {
    to: '/evaluation-summary',
    label: '评价汇总',
    caption: '督导 / 主任',
    icon: ClipboardCheck,
    roles: ['SUPERVISOR', 'DIRECTOR', 'ADMIN'],
  },
  {
    to: '/feedback',
    label: '我的反馈',
    caption: '教师',
    icon: MessageSquare,
    roles: ['TEACHER'],
  },
  {
    to: '/reports',
    label: '统计导出',
    caption: '教研室主任',
    icon: FileDown,
    roles: ['DIRECTOR', 'ADMIN'],
  },
  {
    to: '/catalog',
    label: '基础配置',
    caption: '管理员',
    icon: Database,
    roles: ['ADMIN'],
  },
  {
    to: '/import',
    label: '批量导入',
    caption: '管理员',
    icon: UploadCloud,
    roles: ['ADMIN'],
  },
]

const navItems = computed(() =>
  allNavItems.filter((item) => !item.roles || hasRole(...item.roles)),
)
const isPublic = computed(() => Boolean(route.meta.public))
const pageTitle = computed(() => {
  if (route.name === 'course-detail') return '课程详情'
  return navItems.value.find((item) => item.to === route.path)?.label ?? '教学质量平台'
})

async function logout() {
  try {
    await api.logout()
  } finally {
    clearAuth()
    await router.push({ name: 'login' })
  }
}
</script>

<template>
  <RouterView v-if="isPublic" />

  <div v-else class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">
          <GraduationCap :size="25" aria-hidden="true" />
        </div>
        <div>
          <strong>爱教学</strong>
          <span>教学质量数字化平台</span>
        </div>
      </div>

      <nav class="primary-nav" aria-label="主要功能">
        <RouterLink
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="nav-item"
        >
          <component :is="item.icon" :size="19" aria-hidden="true" />
          <span>
            <strong>{{ item.label }}</strong>
            <small>{{ item.caption }}</small>
          </span>
          <ChevronRight class="nav-arrow" :size="16" aria-hidden="true" />
        </RouterLink>
      </nav>

      <div class="sidebar-footer">
        <span>进阶 1 · 查课程</span>
        <strong>Sprint 2</strong>
      </div>
    </aside>

    <div class="workspace">
      <header class="topbar">
        <div>
          <span class="topbar-kicker">教学质量全链路管理</span>
          <strong>{{ pageTitle }}</strong>
        </div>
        <div class="topbar-actions">
          <div class="user-chip">
            <UserRound :size="16" aria-hidden="true" />
            <span>
              <strong>{{ authState.user?.displayName }}</strong>
              <small>{{ authState.user?.roleLabel }}</small>
            </span>
          </div>
          <button class="icon-button" type="button" title="退出登录" @click="logout">
            <LogOut :size="16" aria-hidden="true" />
          </button>
        </div>
      </header>

      <main class="main-content">
        <RouterView />
      </main>
    </div>
  </div>
</template>
