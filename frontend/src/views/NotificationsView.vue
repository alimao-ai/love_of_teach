<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  AlertCircle,
  Bell,
  BellOff,
  CheckCircle2,
  CheckCheck,
  MailOpen,
} from 'lucide-vue-next'
import EmptyState from '@/components/EmptyState.vue'
import LoadingBlock from '@/components/LoadingBlock.vue'
import MetricTile from '@/components/MetricTile.vue'
import { api, errorMessage } from '@/services/api'
import type { CourseSubscriptionItem, NotificationOverview } from '@/types/api'
import { formatDateTime } from '@/utils/format'

const overview = ref<NotificationOverview>()
const loading = ref(false)
const busyCourseId = ref<number>()
const error = ref('')
const success = ref('')

const unreadCount = computed(() => overview.value?.unreadCount ?? 0)
const subscribedCount = computed(
  () => overview.value?.courses.filter((course) => course.subscribed).length ?? 0,
)

async function loadOverview() {
  loading.value = true
  error.value = ''
  try {
    overview.value = await api.notificationOverview()
  } catch (requestError) {
    error.value = errorMessage(requestError)
  } finally {
    loading.value = false
  }
}

async function toggleSubscription(course: CourseSubscriptionItem) {
  busyCourseId.value = course.courseId
  error.value = ''
  success.value = ''
  try {
    if (course.subscribed) {
      await api.unsubscribeCourse(course.courseId)
      success.value = `已退订 ${course.courseName}`
    } else {
      await api.subscribeCourse(course.courseId)
      success.value = `已订阅 ${course.courseName}`
    }
    await loadOverview()
  } catch (requestError) {
    error.value = errorMessage(requestError)
  } finally {
    busyCourseId.value = undefined
  }
}

async function markAllRead() {
  error.value = ''
  success.value = ''
  try {
    await api.markNotificationsRead()
    success.value = '通知已全部标记为已读'
    await loadOverview()
  } catch (requestError) {
    error.value = errorMessage(requestError)
  }
}

onMounted(loadOverview)
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>课程变更通知</h1>
        <p>订阅本人所授课程，课程或开课信息发生变更后进入站内通知中心。</p>
      </div>
      <div class="page-actions">
        <button
          class="button"
          type="button"
          :disabled="unreadCount === 0"
          @click="markAllRead"
        >
          <CheckCheck :size="16" aria-hidden="true" />
          全部已读
        </button>
        <button class="button" type="button" :disabled="loading" @click="loadOverview">
          刷新
        </button>
      </div>
    </header>

    <div v-if="error" class="error-banner">
      <AlertCircle :size="17" aria-hidden="true" />
      <span>{{ error }}</span>
    </div>
    <div v-if="success" class="success-banner">
      <CheckCircle2 :size="17" aria-hidden="true" />
      <span>{{ success }}</span>
    </div>

    <div class="metric-grid">
      <MetricTile
        label="未读通知"
        :value="unreadCount"
        hint="需要关注的课程变更"
        :icon="Bell"
        :tone="unreadCount > 0 ? 'amber' : 'green'"
      />
      <MetricTile
        label="已订阅课程"
        :value="subscribedCount"
        hint="仅本人所授课程"
        :icon="MailOpen"
        tone="green"
      />
    </div>

    <div class="split-layout notifications-layout">
      <section class="panel">
        <div class="panel-header">
          <div>
            <h2>课程订阅</h2>
            <p>退订后不再接收该课程的新通知，历史通知仍保留。</p>
          </div>
          <Bell :size="20" color="#667c8e" aria-hidden="true" />
        </div>
        <LoadingBlock v-if="loading" />
        <EmptyState
          v-else-if="!overview || overview.courses.length === 0"
          compact
          title="暂无可订阅课程"
          description="教师档案关联课程后会显示在这里。"
        />
        <div v-else class="subscription-list">
          <article
            v-for="course in overview.courses"
            :key="course.courseId"
            class="subscription-item"
          >
            <div>
              <strong>{{ course.courseName }}</strong>
              <span>{{ course.courseCode }} · {{ course.teacherName }}</span>
            </div>
            <button
              class="button"
              :class="{ 'button-danger': course.subscribed }"
              type="button"
              :disabled="busyCourseId === course.courseId"
              @click="toggleSubscription(course)"
            >
              <BellOff v-if="course.subscribed" :size="15" aria-hidden="true" />
              <Bell v-else :size="15" aria-hidden="true" />
              {{ course.subscribed ? '退订' : '订阅' }}
            </button>
          </article>
        </div>
      </section>

      <section class="panel panel-flush">
        <div class="panel-header" style="padding: 18px 20px 0; margin-bottom: 12px">
          <div>
            <h2>通知记录</h2>
            <p>课程信息或开课数据变化会生成新的站内通知。</p>
          </div>
        </div>
        <LoadingBlock v-if="loading" />
        <EmptyState
          v-else-if="!overview || overview.notifications.length === 0"
          title="暂无课程通知"
          description="订阅课程后，变更消息会显示在这里。"
        />
        <div v-else class="notification-list">
          <article
            v-for="notification in overview.notifications"
            :key="notification.id"
            class="notification-item"
            :class="{ unread: !notification.read }"
          >
            <div class="notification-dot" aria-hidden="true" />
            <div>
              <header>
                <strong>{{ notification.title }}</strong>
                <time>{{ formatDateTime(notification.createdAt) }}</time>
              </header>
              <p>{{ notification.message }}</p>
              <span>{{ notification.courseCode }} · {{ notification.courseName }}</span>
            </div>
          </article>
        </div>
      </section>
    </div>
  </section>
</template>
