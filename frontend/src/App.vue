<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute } from 'vue-router'
import {
  BookOpen,
  ChevronRight,
  Database,
  GraduationCap,
  ShieldCheck,
  UploadCloud,
} from 'lucide-vue-next'

const route = useRoute()

const navItems = [
  {
    to: '/courses',
    label: '课程查询',
    caption: '教研室主任',
    icon: BookOpen,
  },
  {
    to: '/coverage',
    label: '督导覆盖',
    caption: '教学督导',
    icon: ShieldCheck,
  },
  {
    to: '/catalog',
    label: '基础配置',
    caption: '管理员',
    icon: Database,
  },
  {
    to: '/import',
    label: '批量导入',
    caption: '管理员',
    icon: UploadCloud,
  },
]

const pageTitle = computed(() => {
  if (route.name === 'course-detail') return '课程详情'
  return navItems.find((item) => item.to === route.path)?.label ?? '教学质量平台'
})
</script>

<template>
  <div class="app-shell">
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
        <strong>Sprint 1</strong>
      </div>
    </aside>

    <div class="workspace">
      <header class="topbar">
        <div>
          <span class="topbar-kicker">教学质量全链路管理</span>
          <strong>{{ pageTitle }}</strong>
        </div>
        <div class="system-state">
          <span class="state-dot" aria-hidden="true" />
          数据服务正常
        </div>
      </header>

      <main class="main-content">
        <RouterView />
      </main>
    </div>
  </div>
</template>
