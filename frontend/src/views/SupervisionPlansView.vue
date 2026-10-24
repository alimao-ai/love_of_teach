<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  AlertCircle,
  CalendarDays,
  CheckCircle2,
  ClipboardCheck,
  Clock3,
  Play,
} from 'lucide-vue-next'
import EmptyState from '@/components/EmptyState.vue'
import LoadingBlock from '@/components/LoadingBlock.vue'
import MetricTile from '@/components/MetricTile.vue'
import { api, errorMessage } from '@/services/api'
import type {
  CourseListItem,
  Department,
  Semester,
  SupervisionPlanItem,
  SupervisionPlanOverview,
  SupervisionPlanStatus,
} from '@/types/api'
import { formatDateTime, formatPercent } from '@/utils/format'

const departments = ref<Department[]>([])
const semesters = ref<Semester[]>([])
const courses = ref<CourseListItem[]>([])
const overview = ref<SupervisionPlanOverview>()
const semesterId = ref<number>()
const departmentId = ref<number>()
const loading = ref(false)
const submitting = ref(false)
const busyPlanId = ref<number>()
const error = ref('')
const success = ref('')

const form = reactive({
  offeringId: undefined as number | undefined,
  plannedDate: dateInput(addDays(new Date(), 1)),
  dueDate: dateInput(addDays(new Date(), 5)),
  note: '',
})

const summary = computed(() => overview.value?.summary ?? {
  total: 0,
  planned: 0,
  inProgress: 0,
  completed: 0,
  overdue: 0,
  progressRate: 0,
})

async function loadData() {
  loading.value = true
  error.value = ''
  try {
    const [planData, courseData] = await Promise.all([
      api.supervisionPlans({
        semesterId: semesterId.value,
        departmentId: departmentId.value,
      }),
      api.listCourses({
        semesterId: semesterId.value,
        departmentId: departmentId.value,
      }),
    ])
    overview.value = planData
    courses.value = courseData
    if (!form.offeringId || !courseData.some((course) => course.offeringId === form.offeringId)) {
      form.offeringId = courseData[0]?.offeringId
    }
  } catch (requestError) {
    error.value = errorMessage(requestError)
  } finally {
    loading.value = false
  }
}

async function createPlan() {
  if (!form.offeringId) {
    error.value = '请选择开课记录'
    return
  }
  submitting.value = true
  error.value = ''
  success.value = ''
  try {
    await api.createSupervisionPlan({
      offeringId: form.offeringId,
      plannedDate: form.plannedDate,
      dueDate: form.dueDate,
      note: form.note.trim() || undefined,
    })
    success.value = '听评课计划已创建'
    form.note = ''
    await loadData()
  } catch (requestError) {
    error.value = errorMessage(requestError)
  } finally {
    submitting.value = false
  }
}

async function updateStatus(plan: SupervisionPlanItem, status: SupervisionPlanStatus) {
  busyPlanId.value = plan.id
  error.value = ''
  success.value = ''
  try {
    await api.updateSupervisionPlanStatus(plan.id, status)
    success.value = status === 'COMPLETED' ? '计划已完成并更新督导记录' : '计划状态已更新'
    await loadData()
  } catch (requestError) {
    error.value = errorMessage(requestError)
  } finally {
    busyPlanId.value = undefined
  }
}

function nextStatus(plan: SupervisionPlanItem) {
  if (plan.status === 'PLANNED') return 'IN_PROGRESS'
  if (plan.status === 'IN_PROGRESS') return 'COMPLETED'
  return undefined
}

function nextStatusLabel(plan: SupervisionPlanItem) {
  if (plan.status === 'PLANNED') return '开始执行'
  if (plan.status === 'IN_PROGRESS') return '完成计划'
  return ''
}

function planStatusClass(status: SupervisionPlanStatus) {
  return {
    planned: status === 'PLANNED',
    active: status === 'IN_PROGRESS',
    completed: status === 'COMPLETED',
    cancelled: status === 'CANCELLED',
  }
}

function addDays(date: Date, days: number) {
  const next = new Date(date)
  next.setDate(next.getDate() + days)
  return next
}

function dateInput(date: Date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
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
      semesterData.find((semester) => semester.code === '2025-2026-2')?.id
      ?? semesterData[0]?.id
    departmentId.value =
      departmentData.find((department) => department.code === 'SOFTWARE')?.id
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
        <h1>听评课计划与执行进度</h1>
        <p>安排听评课任务，跟踪待执行、进行中、已完成与逾期计划，并联动课程督导记录。</p>
      </div>
      <button class="button" type="button" :disabled="loading" @click="loadData">
        刷新进度
      </button>
    </header>

    <div v-if="error" class="error-banner">
      <AlertCircle :size="17" aria-hidden="true" />
      <span>{{ error }}</span>
    </div>
    <div v-if="success" class="success-banner">
      <CheckCircle2 :size="17" aria-hidden="true" />
      <span>{{ success }}</span>
    </div>

    <form
      class="filter-bar"
      style="grid-template-columns: repeat(2, minmax(200px, 280px))"
      @submit.prevent="loadData"
    >
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
        <span>学院 / 教研室</span>
        <select v-model="departmentId">
          <option :value="undefined">全部学院</option>
          <option v-for="department in departments" :key="department.id" :value="department.id">
            {{ department.name }}
          </option>
        </select>
      </label>
      <div class="filter-actions">
        <button class="button" type="submit" :disabled="loading">应用筛选</button>
      </div>
    </form>

    <div class="metric-grid">
      <MetricTile
        label="计划总数"
        :value="summary.total"
        hint="当前筛选范围"
        :icon="ClipboardCheck"
      />
      <MetricTile
        label="进行中"
        :value="summary.inProgress"
        hint="正在执行"
        :icon="Play"
        tone="amber"
      />
      <MetricTile
        label="已完成"
        :value="summary.completed"
        hint="完成率已同步"
        :icon="CheckCircle2"
        tone="green"
      />
      <MetricTile
        label="逾期计划"
        :value="summary.overdue"
        hint="需优先处理"
        :icon="Clock3"
        tone="red"
      />
    </div>

    <section class="panel">
      <div class="panel-header">
        <div>
          <h2>新建听评课计划</h2>
          <p>计划完成后会自动生成或复用课程督导记录，避免重复计数。</p>
        </div>
        <CalendarDays :size="20" color="#667c8e" aria-hidden="true" />
      </div>
      <form class="plan-form" @submit.prevent="createPlan">
        <label class="field">
          <span>开课记录</span>
          <select v-model="form.offeringId" required>
            <option :value="undefined" disabled>请选择课程与班级</option>
            <option v-for="course in courses" :key="course.offeringId" :value="course.offeringId">
              {{ course.courseCode }} · {{ course.courseName }} · {{ course.className }}
            </option>
          </select>
        </label>
        <label class="field">
          <span>计划日期</span>
          <input v-model="form.plannedDate" required type="date" />
        </label>
        <label class="field">
          <span>截止日期</span>
          <input v-model="form.dueDate" required type="date" />
        </label>
        <label class="field">
          <span>计划说明</span>
          <input v-model="form.note" maxlength="512" placeholder="本次听评课关注点" />
        </label>
        <button class="button button-primary" type="submit" :disabled="submitting">
          {{ submitting ? '正在创建' : '创建计划' }}
        </button>
      </form>
    </section>

    <section class="panel panel-flush">
      <div class="panel-header" style="padding: 18px 20px 0; margin-bottom: 12px">
        <div>
          <h2>计划执行清单</h2>
          <p>整体进度 {{ formatPercent(summary.progressRate) }}，逾期计划不计入完成。</p>
        </div>
      </div>
      <LoadingBlock v-if="loading" />
      <EmptyState
        v-else-if="!overview || overview.plans.length === 0"
        title="暂无听评课计划"
        description="选择课程并创建计划后，执行进度会显示在这里。"
      />
      <div v-else class="data-table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>课程</th>
              <th>教师 / 班级</th>
              <th>计划日期</th>
              <th>截止日期</th>
              <th>状态</th>
              <th>执行人</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="plan in overview.plans" :key="plan.id" :class="{ 'weak-row': plan.overdue }">
              <td>
                <span class="cell-main">{{ plan.courseName }}</span>
                <span class="cell-sub">{{ plan.courseCode }} · {{ plan.departmentName }}</span>
              </td>
              <td>
                <span class="cell-main">{{ plan.teacherName }}</span>
                <span class="cell-sub">{{ plan.className }}</span>
              </td>
              <td>{{ plan.plannedDate }}</td>
              <td>
                {{ plan.dueDate }}
                <span v-if="plan.overdue" class="cell-sub score-weak">已逾期</span>
              </td>
              <td>
                <span class="plan-status" :class="planStatusClass(plan.status)">
                  {{ plan.statusLabel }}
                </span>
              </td>
              <td>
                <span class="cell-main">{{ plan.supervisorName }}</span>
                <span v-if="plan.completedAt" class="cell-sub">
                  {{ formatDateTime(plan.completedAt) }}
                </span>
              </td>
              <td>
                <button
                  v-if="nextStatus(plan)"
                  class="button"
                  type="button"
                  :disabled="busyPlanId === plan.id"
                  @click="updateStatus(plan, nextStatus(plan)!)"
                >
                  {{ nextStatusLabel(plan) }}
                </button>
                <span v-else class="cell-sub">无待办操作</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </section>
</template>
