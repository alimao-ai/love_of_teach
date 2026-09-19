<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  AlertCircle,
  BookOpenCheck,
  CheckCircle2,
  Clock3,
  Percent,
  ShieldCheck,
  Users,
} from 'lucide-vue-next'
import EmptyState from '@/components/EmptyState.vue'
import LoadingBlock from '@/components/LoadingBlock.vue'
import MetricTile from '@/components/MetricTile.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { api, errorMessage } from '@/services/api'
import type { CourseListItem, Department, Semester, SupervisionCoverage } from '@/types/api'
import { formatNumber, formatPercent } from '@/utils/format'

const router = useRouter()

const departments = ref<Department[]>([])
const semesters = ref<Semester[]>([])
const coverage = ref<SupervisionCoverage | null>(null)
const semesterId = ref<number>()
const departmentId = ref<number>()
const loading = ref(false)
const error = ref('')
let initialized = false
let loadTimer: number | undefined

const summary = computed(
  () =>
    coverage.value?.summary ?? {
      offeringCount: 0,
      supervisedCount: 0,
      unsupervisedCount: 0,
      coverageRate: 0,
      studentCount: 0,
    },
)

async function loadData() {
  loading.value = true
  error.value = ''
  try {
    coverage.value = await api.getCoverage({
      semesterId: semesterId.value,
      departmentId: departmentId.value,
    })
  } catch (requestError) {
    error.value = errorMessage(requestError)
  } finally {
    loading.value = false
  }
}

function progressClass(rate: number) {
  if (rate >= 80) return 'good'
  if (rate < 40) return 'low'
  return ''
}

function openCourse(item: CourseListItem) {
  void router.push({ name: 'course-detail', params: { offeringId: item.offeringId } })
}

watch([semesterId, departmentId], () => {
  if (!initialized) return
  if (loadTimer) window.clearTimeout(loadTimer)
  loadTimer = window.setTimeout(() => {
    loadTimer = undefined
    void loadData()
  }, 0)
})

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
    initialized = true
    await loadData()
  } catch (requestError) {
    error.value = errorMessage(requestError)
  }
})

onBeforeUnmount(() => {
  if (loadTimer) window.clearTimeout(loadTimer)
})
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>课程开设与督导覆盖率</h1>
        <p>
          按学期和院系统计开课数、已督导数、未督导数与覆盖率，并查看对应课程清单安排听评课。
        </p>
      </div>
      <div class="page-actions">
        <button class="button" type="button" :disabled="loading" @click="loadData">
          刷新统计
        </button>
      </div>
    </header>

    <div v-if="error" class="error-banner">
      <AlertCircle :size="17" aria-hidden="true" />
      <span>{{ error }}</span>
    </div>

    <form class="filter-bar" style="grid-template-columns: repeat(2, minmax(180px, 260px))" @submit.prevent="loadData">
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
        label="课程开设"
        :value="formatNumber(summary.offeringCount)"
        hint="当前统计范围"
        :icon="BookOpenCheck"
      />
      <MetricTile
        label="已督导"
        :value="formatNumber(summary.supervisedCount)"
        hint="至少有一条督导记录"
        :icon="CheckCircle2"
        tone="green"
      />
      <MetricTile
        label="待督导"
        :value="formatNumber(summary.unsupervisedCount)"
        hint="尚未安排听评课"
        :icon="Clock3"
        tone="amber"
      />
      <MetricTile
        label="督导覆盖率"
        :value="formatPercent(summary.coverageRate)"
        hint="已督导数 ÷ 开课数"
        :icon="Percent"
        :tone="summary.coverageRate < 40 ? 'red' : 'green'"
      />
    </div>

    <div class="panel">
      <div class="panel-header">
        <div>
          <h2>院系覆盖统计</h2>
          <p>共覆盖 {{ formatNumber(summary.studentCount) }} 学生人次。</p>
        </div>
        <Users :size="20" color="#667c8e" aria-hidden="true" />
      </div>

      <LoadingBlock v-if="loading" />
      <EmptyState
        v-else-if="!coverage || coverage.departmentBreakdown.length === 0"
        title="当前范围暂无开课记录"
        description="请调整学期或院系筛选条件。"
      />
      <div v-else class="data-table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>院系 / 教研室</th>
              <th class="numeric">开课数</th>
              <th class="numeric">已督导</th>
              <th class="numeric">待督导</th>
              <th>覆盖率</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in coverage.departmentBreakdown" :key="row.departmentId">
              <td>
                <span class="cell-main">{{ row.departmentName }}</span>
                <span class="cell-sub">{{ row.departmentCode }}</span>
              </td>
              <td class="numeric">{{ row.offeringCount }}</td>
              <td class="numeric">{{ row.supervisedCount }}</td>
              <td class="numeric">{{ row.unsupervisedCount }}</td>
              <td>
                <div class="progress-line">
                  <div class="progress-track">
                    <div
                      class="progress-fill"
                      :class="progressClass(row.coverageRate)"
                      :style="{ width: `${Math.min(row.coverageRate, 100)}%` }"
                    />
                  </div>
                  <strong>{{ formatPercent(row.coverageRate) }}</strong>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <section class="panel panel-flush">
      <div class="panel-header" style="padding: 18px 20px 0; margin-bottom: 12px">
        <div>
          <h2>课程督导清单</h2>
          <p>可据此安排尚未覆盖课程的听评课计划。</p>
        </div>
        <ShieldCheck :size="20" color="#667c8e" aria-hidden="true" />
      </div>

      <LoadingBlock v-if="loading" />
      <EmptyState
        v-else-if="!coverage || coverage.courses.length === 0"
        title="暂无课程清单"
        description="请调整筛选条件后再试。"
      />
      <div v-else class="data-table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>课程</th>
              <th>教师</th>
              <th>班级</th>
              <th>开课状态</th>
              <th>督导状态</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="item in coverage.courses"
              :key="item.offeringId"
              class="clickable-row"
              tabindex="0"
              @click="openCourse(item)"
              @keyup.enter="openCourse(item)"
            >
              <td>
                <span class="cell-main">{{ item.courseName }}</span>
                <span class="cell-sub">{{ item.courseCode }} · {{ item.departmentName }}</span>
              </td>
              <td>{{ item.teacherName }}</td>
              <td>{{ item.className }}</td>
              <td>
                <StatusBadge :status="item.status" :label="item.statusLabel" />
              </td>
              <td>
                <span :class="['coverage-mark', { missing: !item.supervised }]">
                  <ShieldCheck :size="14" aria-hidden="true" />
                  {{ item.supervised ? '已督导' : '待安排' }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </section>
</template>
