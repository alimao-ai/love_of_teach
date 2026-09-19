import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
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
      path: '/coverage',
      name: 'coverage',
      component: () => import('@/views/SupervisionView.vue'),
    },
    {
      path: '/catalog',
      name: 'catalog',
      component: () => import('@/views/CatalogView.vue'),
    },
    {
      path: '/import',
      name: 'import',
      component: () => import('@/views/ImportView.vue'),
    },
  ],
})

export default router
