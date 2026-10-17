import { createRouter, createWebHistory } from 'vue-router'
import type { UserRole } from '@/types/api'
import { authState, isAuthenticated } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { public: true },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { public: true },
    },
    {
      path: '/',
      redirect: '/courses',
    },
    {
      path: '/courses',
      name: 'courses',
      component: () => import('@/views/CourseExplorerView.vue'),
    },
    {
      path: '/courses/:offeringId',
      name: 'course-detail',
      component: () => import('@/views/CourseDetailView.vue'),
    },
    {
      path: '/resources',
      name: 'resources',
      component: () => import('@/views/ResourceLibraryView.vue'),
    },
    {
      path: '/evaluations',
      name: 'evaluations',
      component: () => import('@/views/EvaluationView.vue'),
      meta: { roles: ['SUPERVISOR', 'DIRECTOR', 'ADMIN'] },
    },
    {
      path: '/evaluation-summary',
      name: 'evaluation-summary',
      component: () => import('@/views/EvaluationSummaryView.vue'),
      meta: { roles: ['SUPERVISOR', 'DIRECTOR', 'ADMIN'] },
    },
    {
      path: '/feedback',
      name: 'feedback',
      component: () => import('@/views/TeacherFeedbackView.vue'),
      meta: { roles: ['TEACHER'] },
    },
    {
      path: '/reports',
      name: 'reports',
      component: () => import('@/views/ReportsView.vue'),
      meta: { roles: ['DIRECTOR', 'ADMIN'] },
    },
    {
      path: '/coverage',
      name: 'coverage',
      component: () => import('@/views/SupervisionView.vue'),
      meta: { roles: ['SUPERVISOR', 'DIRECTOR', 'ADMIN'] },
    },
    {
      path: '/catalog',
      name: 'catalog',
      component: () => import('@/views/CatalogView.vue'),
      meta: { roles: ['ADMIN'] },
    },
    {
      path: '/import',
      name: 'import',
      component: () => import('@/views/ImportView.vue'),
      meta: { roles: ['ADMIN'] },
    },
  ],
})

// Public auth routes are open; protected routes require a token and matching role.
router.beforeEach((to) => {
  if (to.meta.public) {
    return isAuthenticated.value ? { name: 'courses' } : true
  }
  if (!isAuthenticated.value) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  const roles = to.meta.roles as UserRole[] | undefined
  if (roles && authState.user && !roles.includes(authState.user.role)) {
    return { name: 'courses' }
  }
  return true
})

export default router
