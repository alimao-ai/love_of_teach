<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { AlertCircle, BookOpenCheck, MessageSquare, Star } from 'lucide-vue-next'
import EmptyState from '@/components/EmptyState.vue'
import LoadingBlock from '@/components/LoadingBlock.vue'
import MetricTile from '@/components/MetricTile.vue'
import { api, errorMessage } from '@/services/api'
import type { Semester, TeacherFeedback } from '@/types/api'
import { formatDateTime, formatNumber } from '@/utils/format'

const semesters = ref<Semester[]>([])
const feedback = ref<TeacherFeedback>()
const semesterId = ref<number>()
const loading = ref(false)
const error = ref('')

const metrics = computed(() => feedback.value ?? {
  teacherId: 0,
  teacherName: '',
  offeringCount: 0,
  evaluationCount: 0,
  overallAverageScore: 0,
  courses: [],
  feedback: [],
})

async function loadData() {
  loading.value = true
  error.value = ''
  try {
    feedback.value = await api.myFeedback(semesterId.value)
  } catch (requestError) {
    error.value = errorMessage(requestError)
  } finally {
    loading.value = false
  }
}

watch(semesterId, () => {
  if (semesters.value.length > 0) void loadData()
})

onMounted(async () => {
  try {
    semesters.value = await api.listSemesters(true)
    semesterId.value =
      semesters.value.find((semester) => semester.code === '2025-2026-2')?.id
        ?? semesters.value[0]?.id
    await loadData()
  } catch (requestError) {
    error.value = errorMessage(requestError)
  }
})
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>我的课程评价与反馈</h1>
        <p>教师只能查看本人所授课程的评价内容，用于了解课堂亮点和后续改进方向。</p>
      </div>
      <button class="button" type="button" :disabled="loading" @click="loadData">刷新反馈</button>
    </header>

    <div v-if="error" class="error-banner">
      <AlertCircle :size="17" aria-hidden="true" />
      <span>{{ error }}</span>
    </div>

    <form class="filter-bar" style="grid-template-columns: minmax(180px, 300px)" @submit.prevent="loadData">
      <label class="field">
        <span>学期</span>
        <select v-model="semesterId">
          <option :value="undefined">全部学期</option>
          <option v-for="semester in semesters" :key="semester.id" :value="semester.id">
            {{ semester.name }}
          </option>
        </select>
      </label>
    </form>

    <div class="metric-grid">
      <MetricTile
        label="授课课程"
        :value="formatNumber(metrics.offeringCount)"
        hint="当前学期开课记录"
        :icon="BookOpenCheck"
      />
      <MetricTile
        label="收到评价"
        :value="formatNumber(metrics.evaluationCount)"
        hint="督导与主任评价总数"
        :icon="MessageSquare"
        tone="green"
      />
      <MetricTile
        label="平均评分"
        :value="metrics.overallAverageScore.toFixed(1)"
        hint="本人课程平均分"
        :icon="Star"
        tone="amber"
      />
    </div>

    <LoadingBlock v-if="loading" />

    <template v-else>
      <section class="panel">
        <div class="panel-header">
          <div>
            <h2>课程评分概览</h2>
            <p>未收到评价的课程也会显示，便于确认反馈覆盖情况。</p>
          </div>
        </div>
        <EmptyState v-if="metrics.courses.length === 0" title="当前学期暂无授课记录" />
        <div v-else class="teacher-courses">
          <article v-for="course in metrics.courses" :key="course.offeringId" class="course-mini">
            <header>
              <strong>{{ course.courseName }}</strong>
              <span class="score-pill" :class="{ weak: course.weak }">
                {{ course.evaluationCount ? course.averageScore.toFixed(1) : '暂无评分' }}
              </span>
            </header>
            <p>
              {{ course.courseCode }} · {{ course.className }}<br />
              {{ course.evaluationCount }} 条评价 · {{ course.studentCount }} 人次
            </p>
          </article>
        </div>
      </section>

      <section class="panel panel-flush">
        <div class="panel-header" style="padding: 18px 20px 0; margin-bottom: 12px">
          <div>
            <h2>评价反馈明细</h2>
            <p>仅展示本人所授课程的评价。</p>
          </div>
        </div>
        <EmptyState v-if="metrics.feedback.length === 0" title="暂无评价反馈" />
        <div v-else class="data-table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>课程</th>
                <th>班级</th>
                <th class="numeric">评分</th>
                <th>评语</th>
                <th>评价人</th>
                <th>评价时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in metrics.feedback" :key="item.id">
                <td>
                  <span class="cell-main">{{ item.courseName }}</span>
                  <span class="cell-sub">{{ item.courseCode }}</span>
                </td>
                <td>{{ item.className }}</td>
                <td class="numeric"><strong>{{ item.score }}</strong></td>
                <td>{{ item.comment }}</td>
                <td>{{ item.reviewerName }}</td>
                <td>{{ formatDateTime(item.evaluatedAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </template>
  </section>
</template>
