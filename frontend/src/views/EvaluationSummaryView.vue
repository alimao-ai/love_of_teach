<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { AlertCircle, AlertTriangle, BarChart3, ClipboardList, Star, Target } from 'lucide-vue-next'
import EmptyState from '@/components/EmptyState.vue'
import LoadingBlock from '@/components/LoadingBlock.vue'
import MetricTile from '@/components/MetricTile.vue'
import { api, errorMessage } from '@/services/api'
import type { Department, EvaluationSummary, Semester } from '@/types/api'
import { formatDateTime, formatNumber } from '@/utils/format'

const departments = ref<Department[]>([])
const semesters = ref<Semester[]>([])
const summary = ref<EvaluationSummary>()
const semesterId = ref<number>()
const departmentId = ref<number>()
const loading = ref(false)
const error = ref('')

const metrics = computed(() => summary.value ?? {
  evaluatedOfferingCount: 0,
  evaluationCount: 0,
  overallAverageScore: 0,
  weakOfferingCount: 0,
  items: [],
})

async function loadSummary() {
  loading.value = true
  error.value = ''
  try {
    summary.value = await api.evaluationSummary({
      semesterId: semesterId.value,
      departmentId: departmentId.value,
    })
  } catch (requestError) {
    error.value = errorMessage(requestError)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    const [departmentData, semesterData] = await Promise.all([
      api.listDepartments(true),
      api.listSemesters(true),
    ])
    departments.value = departmentData
    semesters.value = semesterData
    semesterId.value =
      semesterData.find((semester) => semester.code === '2025-2026-2')?.id ?? semesterData[0]?.id
    departmentId.value =
      departmentData.find((department) => department.code === 'SOFTWARE')?.id
    await loadSummary()
  } catch (requestError) {
    error.value = errorMessage(requestError)
  }
})
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>课程评价汇总</h1>
        <p>按学期和院系汇总课程评价，识别低分课程并查看最新反馈，辅助督导安排后续听评课。</p>
      </div>
      <button class="button" type="button" :disabled="loading" @click="loadSummary">刷新汇总</button>
    </header>

    <div v-if="error" class="error-banner">
      <AlertCircle :size="17" aria-hidden="true" />
      <span>{{ error }}</span>
    </div>

    <form class="filter-bar" style="grid-template-columns: repeat(2, minmax(180px, 280px))" @submit.prevent="loadSummary">
      <label class="field">
        <span>学期</span>
        <select v-model="semesterId">
          <option :value="undefined">全部学期</option>
          <option v-for="semester in semesters" :key="semester.id" :value="semester.id">
            {{ semester.name }}
          </option>
        </select>
      </label>
      <label class="field">
        <span>院系 / 教研室</span>
        <select v-model="departmentId">
          <option :value="undefined">全部院系</option>
          <option v-for="department in departments" :key="department.id" :value="department.id">
            {{ department.name }}
          </option>
        </select>
      </label>
    </form>

    <div class="metric-grid">
      <MetricTile
        label="已评课程"
        :value="formatNumber(metrics.evaluatedOfferingCount)"
        hint="至少有一条评价"
        :icon="ClipboardList"
      />
      <MetricTile
        label="评价总数"
        :value="formatNumber(metrics.evaluationCount)"
        hint="督导与主任评价累计"
        :icon="Star"
        tone="green"
      />
      <MetricTile
        label="平均评分"
        :value="metrics.overallAverageScore.toFixed(1)"
        hint="百分制平均分"
        :icon="BarChart3"
        tone="amber"
      />
      <MetricTile
        label="薄弱课程"
        :value="formatNumber(metrics.weakOfferingCount)"
        hint="平均分低于 70"
        :icon="AlertTriangle"
        tone="red"
      />
    </div>

    <section class="panel panel-flush">
      <div class="panel-header" style="padding: 18px 20px 0; margin-bottom: 12px">
        <div>
          <h2>课程评分明细</h2>
          <p>按平均分升序排列，优先定位需要改进的课程。</p>
        </div>
        <Target :size="21" color="#667c8e" aria-hidden="true" />
      </div>
      <LoadingBlock v-if="loading" />
      <EmptyState
        v-else-if="metrics.items.length === 0"
        title="当前范围暂无评价数据"
        description="督导完成课程评价后，汇总数据会显示在这里。"
      />
      <div v-else class="data-table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>课程</th>
              <th>教师</th>
              <th>班级</th>
              <th class="numeric">平均分</th>
              <th class="numeric">评价数</th>
              <th>最新评语</th>
              <th>评价时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in metrics.items" :key="item.offeringId" :class="{ 'weak-row': item.weak }">
              <td>
                <span class="cell-main">{{ item.courseName }}</span>
                <span class="cell-sub">{{ item.courseCode }} · {{ item.departmentName }}</span>
              </td>
              <td>{{ item.teacherName }}</td>
              <td>{{ item.className }}</td>
              <td class="numeric">
                <strong :class="{ 'score-weak': item.weak }">{{ item.averageScore.toFixed(1) }}</strong>
              </td>
              <td class="numeric">{{ item.evaluationCount }}</td>
              <td>{{ item.latestComment || '-' }}</td>
              <td>{{ formatDateTime(item.latestEvaluatedAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </section>
</template>
