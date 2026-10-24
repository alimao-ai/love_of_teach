<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  AlertCircle,
  BookOpenCheck,
  Building2,
  CheckCircle2,
  Percent,
  Users,
} from 'lucide-vue-next'
import EmptyState from '@/components/EmptyState.vue'
import LoadingBlock from '@/components/LoadingBlock.vue'
import MetricTile from '@/components/MetricTile.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { api, errorMessage } from '@/services/api'
import type { CourseDrilldown, CourseListItem, Department, Semester } from '@/types/api'
import { formatNumber, formatPercent } from '@/utils/format'

const router = useRouter()
const departments = ref<Department[]>([])
const semesters = ref<Semester[]>([])
const drilldown = ref<CourseDrilldown>()
const semesterId = ref<number>()
const departmentId = ref<number>()
const loading = ref(false)
const error = ref('')

const summary = computed(() => drilldown.value ?? {
  departmentCount: 0,
  offeringCount: 0,
  studentCount: 0,
  supervisedOfferingCount: 0,
  unsupervisedOfferingCount: 0,
  supervisionCoverageRate: 0,
  departments: [],
})

const courses = computed(() =>
  drilldown.value?.departments.flatMap((department) => department.courses) ?? [],
)

async function loadDrilldown() {
  loading.value = true
  error.value = ''
  try {
    drilldown.value = await api.courseDrilldown({
      semesterId: semesterId.value,
      departmentId: departmentId.value,
    })
  } catch (requestError) {
    error.value = errorMessage(requestError)
  } finally {
    loading.value = false
  }
}

function openCourse(course: CourseListItem) {
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
    await loadDrilldown()
  } catch (requestError) {
    error.value = errorMessage(requestError)
  }
})
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>学院课程钻取</h1>
        <p>从学院汇总进入课程明细，统一查看学生人次、督导覆盖与资源建设状态。</p>
      </div>
      <button class="button" type="button" :disabled="loading" @click="loadDrilldown">
        刷新钻取结果
      </button>
    </header>

    <div v-if="error" class="error-banner">
      <AlertCircle :size="17" aria-hidden="true" />
      <span>{{ error }}</span>
    </div>

    <form
      class="filter-bar"
      style="grid-template-columns: repeat(2, minmax(200px, 280px))"
      @submit.prevent="loadDrilldown"
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
        label="学院数量"
        :value="formatNumber(summary.departmentCount)"
        hint="当前筛选范围"
        :icon="Building2"
      />
      <MetricTile
        label="开课记录"
        :value="formatNumber(summary.offeringCount)"
        hint="课程与班级组合"
        :icon="BookOpenCheck"
      />
      <MetricTile
        label="学生人次"
        :value="formatNumber(summary.studentCount)"
        hint="全部开课班级合计"
        :icon="Users"
        tone="green"
      />
      <MetricTile
        label="督导覆盖率"
        :value="formatPercent(summary.supervisionCoverageRate)"
        :hint="`${summary.supervisedOfferingCount} 门已督导`"
        :icon="Percent"
        :tone="summary.supervisionCoverageRate < 40 ? 'red' : 'green'"
      />
    </div>

    <section class="panel panel-flush">
      <div class="panel-header" style="padding: 18px 20px 0; margin-bottom: 12px">
        <div>
          <h2>学院汇总</h2>
          <p>课程归属按课程基础数据计算，不随授课教师所属学院变化。</p>
        </div>
        <CheckCircle2 :size="20" color="#667c8e" aria-hidden="true" />
      </div>
      <LoadingBlock v-if="loading" />
      <EmptyState
        v-else-if="summary.departments.length === 0"
        title="当前范围暂无开课数据"
      />
      <div v-else class="data-table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>学院</th>
              <th class="numeric">开课数</th>
              <th class="numeric">学生人次</th>
              <th class="numeric">已督导</th>
              <th class="numeric">待督导</th>
              <th>覆盖率</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="department in summary.departments" :key="department.departmentId">
              <td>
                <span class="cell-main">{{ department.departmentName }}</span>
                <span class="cell-sub">{{ department.departmentCode }}</span>
              </td>
              <td class="numeric">{{ department.offeringCount }}</td>
              <td class="numeric">{{ department.studentCount }}</td>
              <td class="numeric">{{ department.supervisedOfferingCount }}</td>
              <td class="numeric">{{ department.unsupervisedOfferingCount }}</td>
              <td>
                <div class="progress-line">
                  <div class="progress-track">
                    <div
                      class="progress-fill"
                      :class="{ low: department.supervisionCoverageRate < 40 }"
                      :style="{ width: `${Math.min(department.supervisionCoverageRate, 100)}%` }"
                    />
                  </div>
                  <strong>{{ formatPercent(department.supervisionCoverageRate) }}</strong>
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
          <h2>钻取课程明细</h2>
          <p>与课程查询结果使用同一数据源，可直接进入课程详情。</p>
        </div>
      </div>
      <EmptyState v-if="courses.length === 0" compact title="暂无课程明细" />
      <div v-else class="data-table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>课程</th>
              <th>教师</th>
              <th>班级</th>
              <th class="numeric">学生人次</th>
              <th class="numeric">资源</th>
              <th>开课状态</th>
              <th>督导状态</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="course in courses"
              :key="course.offeringId"
              class="clickable-row"
              tabindex="0"
              @click="openCourse(course)"
              @keyup.enter="openCourse(course)"
            >
              <td>
                <span class="cell-main">{{ course.courseName }}</span>
                <span class="cell-sub">
                  {{ course.courseCode }} · {{ course.departmentName }}
                </span>
              </td>
              <td>{{ course.teacherName }}</td>
              <td>{{ course.className }}</td>
              <td class="numeric">{{ course.studentCount }}</td>
              <td class="numeric">{{ course.resourceCount }}</td>
              <td><StatusBadge :status="course.status" :label="course.statusLabel" /></td>
              <td>
                <span :class="['coverage-mark', { missing: !course.supervised }]">
                  {{ course.supervised ? '已督导' : '待安排' }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </section>
</template>
