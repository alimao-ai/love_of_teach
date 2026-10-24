<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  AlertCircle,
  BarChart3,
  BookOpenCheck,
  FolderOpen,
  Percent,
  TrendingUp,
} from 'lucide-vue-next'
import EmptyState from '@/components/EmptyState.vue'
import LoadingBlock from '@/components/LoadingBlock.vue'
import MetricTile from '@/components/MetricTile.vue'
import { api, errorMessage } from '@/services/api'
import type {
  CourseResourceCoverage,
  Department,
  ResourceCoverage,
  Semester,
} from '@/types/api'
import { formatPercent } from '@/utils/format'

const router = useRouter()
const departments = ref<Department[]>([])
const semesters = ref<Semester[]>([])
const coverage = ref<ResourceCoverage>()
const semesterId = ref<number>()
const departmentId = ref<number>()
const loading = ref(false)
const error = ref('')

const summary = computed(() => coverage.value?.summary ?? {
  offeringCount: 0,
  coveredOfferingCount: 0,
  uncoveredOfferingCount: 0,
  coverageRate: 0,
  resourceCount: 0,
})

async function loadCoverage() {
  loading.value = true
  error.value = ''
  try {
    coverage.value = await api.resourceCoverage({
      semesterId: semesterId.value,
      departmentId: departmentId.value,
    })
  } catch (requestError) {
    error.value = errorMessage(requestError)
  } finally {
    loading.value = false
  }
}

function openCourse(course: CourseResourceCoverage) {
  void router.push({
    name: 'course-detail',
    params: { offeringId: course.offeringId },
  })
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
    await loadCoverage()
  } catch (requestError) {
    error.value = errorMessage(requestError)
  }
})
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>课程资源覆盖率</h1>
        <p>按课程统计已上架资源，定位资源建设薄弱课程并安排后续补充。</p>
      </div>
      <button class="button" type="button" :disabled="loading" @click="loadCoverage">
        刷新统计
      </button>
    </header>

    <div v-if="error" class="error-banner">
      <AlertCircle :size="17" aria-hidden="true" />
      <span>{{ error }}</span>
    </div>

    <form
      class="filter-bar"
      style="grid-template-columns: repeat(2, minmax(200px, 280px))"
      @submit.prevent="loadCoverage"
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
        <button class="button button-primary" type="submit" :disabled="loading">应用筛选</button>
      </div>
    </form>

    <div class="metric-grid">
      <MetricTile
        label="开课总数"
        :value="summary.offeringCount"
        hint="当前统计范围"
        :icon="BookOpenCheck"
      />
      <MetricTile
        label="已有资源"
        :value="summary.coveredOfferingCount"
        hint="至少有一个已上架资源"
        :icon="FolderOpen"
        tone="green"
      />
      <MetricTile
        label="资源数量"
        :value="summary.resourceCount"
        hint="仅统计已上架资源"
        :icon="BarChart3"
        tone="amber"
      />
      <MetricTile
        label="课程覆盖率"
        :value="formatPercent(summary.coverageRate)"
        hint="已有资源课程 ÷ 开课数"
        :icon="Percent"
        :tone="summary.coverageRate < 60 ? 'red' : 'green'"
      />
    </div>

    <section class="panel panel-flush">
      <div class="panel-header" style="padding: 18px 20px 0; margin-bottom: 12px">
        <div>
          <h2>学院覆盖情况</h2>
          <p>优先关注未覆盖课程数量较多的学院。</p>
        </div>
        <TrendingUp :size="20" color="#667c8e" aria-hidden="true" />
      </div>
      <LoadingBlock v-if="loading" />
      <EmptyState
        v-else-if="!coverage || coverage.departmentBreakdown.length === 0"
        title="当前范围暂无开课数据"
      />
      <div v-else class="data-table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>学院</th>
              <th class="numeric">开课数</th>
              <th class="numeric">已有资源</th>
              <th class="numeric">待补充</th>
              <th>覆盖率</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="department in coverage.departmentBreakdown" :key="department.departmentId">
              <td>
                <span class="cell-main">{{ department.departmentName }}</span>
                <span class="cell-sub">{{ department.departmentCode }}</span>
              </td>
              <td class="numeric">{{ department.offeringCount }}</td>
              <td class="numeric">{{ department.coveredOfferingCount }}</td>
              <td class="numeric">{{ department.uncoveredOfferingCount }}</td>
              <td>
                <div class="progress-line">
                  <div class="progress-track">
                    <div
                      class="progress-fill"
                      :class="{ low: department.coverageRate < 60 }"
                      :style="{ width: `${Math.min(department.coverageRate, 100)}%` }"
                    />
                  </div>
                  <strong>{{ formatPercent(department.coverageRate) }}</strong>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <section class="panel panel-flush">
      <div class="panel-header" style="padding: 18px 20px 0; margin-bottom: 12px">
        <div>
          <h2>低覆盖课程</h2>
          <p>未覆盖课程优先显示，可直接进入课程详情补充章节与资源。</p>
        </div>
      </div>
      <EmptyState
        v-if="!coverage || coverage.courses.length === 0"
        compact
        title="暂无课程资源统计"
      />
      <div v-else class="data-table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>课程</th>
              <th>教师</th>
              <th>班级</th>
              <th class="numeric">已上架资源</th>
              <th>覆盖状态</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="course in coverage.courses"
              :key="course.offeringId"
              class="clickable-row"
              :class="{ 'weak-row': !course.covered }"
              tabindex="0"
              @click="openCourse(course)"
              @keyup.enter="openCourse(course)"
            >
              <td>
                <span class="cell-main">{{ course.courseName }}</span>
                <span class="cell-sub">{{ course.courseCode }} · {{ course.departmentName }}</span>
              </td>
              <td>{{ course.teacherName }}</td>
              <td>{{ course.className }}</td>
              <td class="numeric">{{ course.resourceCount }}</td>
              <td>
                <span :class="['coverage-mark', { missing: !course.covered }]">
                  {{ course.covered ? '已覆盖' : '待补充' }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </section>
</template>
