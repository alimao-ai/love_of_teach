<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  AlertCircle,
  BookOpenCheck,
  FolderOpen,
  Search,
  ShieldCheck,
  UserRound,
  Users,
} from 'lucide-vue-next'
import EmptyState from '@/components/EmptyState.vue'
import LoadingBlock from '@/components/LoadingBlock.vue'
import MetricTile from '@/components/MetricTile.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { api, errorMessage } from '@/services/api'
import type {
  CourseListItem,
  Department,
  Semester,
  Teacher,
  TeacherOverview,
} from '@/types/api'
import { formatNumber } from '@/utils/format'

const router = useRouter()

const departments = ref<Department[]>([])
const semesters = ref<Semester[]>([])
const teachers = ref<Teacher[]>([])
const courses = ref<CourseListItem[]>([])
const overview = ref<TeacherOverview | null>(null)
const loading = ref(false)
const error = ref('')
const activeTab = ref<'courses' | 'teachers'>('courses')

const semesterId = ref<number>()
const departmentId = ref<number>()
const teacherId = ref<number>()
const keyword = ref('')
let initialized = false
let loadTimer: number | undefined
let requestSequence = 0

const metrics = computed(() => ({
  courseCount: courses.value.length,
  studentCount: courses.value.reduce((sum, item) => sum + item.studentCount, 0),
  resourceCount: courses.value.reduce((sum, item) => sum + item.resourceCount, 0),
  supervisedCount: courses.value.filter((item) => item.supervised).length,
}))

async function loadCatalogs() {
  const [departmentData, semesterData] = await Promise.all([
    api.listDepartments(true),
    api.listSemesters(true),
  ])
  departments.value = departmentData
  semesters.value = semesterData
  semesterId.value =
    semesterData.find((semester) => semester.code === '2025-2026-2')?.id ?? semesterData[0]?.id
  departmentId.value =
    departmentData.find((department) => department.code === 'SOFTWARE')?.id ?? departmentData[0]?.id
  await loadTeachers()
}

async function loadTeachers() {
  teachers.value = await api.listTeachers(departmentId.value)
  if (teacherId.value && !teachers.value.some((teacher) => teacher.id === teacherId.value)) {
    teacherId.value = undefined
  }
}

async function loadData() {
  const sequence = ++requestSequence
  loading.value = true
  error.value = ''
  try {
    const filters = {
      semesterId: semesterId.value,
      departmentId: departmentId.value,
      teacherId: teacherId.value,
      keyword: keyword.value.trim() || undefined,
    }
    const [courseData, overviewData] = await Promise.all([
      api.listCourses(filters),
      api.teacherOverview(filters),
    ])
    if (sequence !== requestSequence) return
    courses.value = courseData
    overview.value = overviewData
  } catch (requestError) {
    if (sequence === requestSequence) {
      error.value = errorMessage(requestError)
    }
  } finally {
    if (sequence === requestSequence) {
      loading.value = false
    }
  }
}

function scheduleLoadData() {
  if (!initialized) return
  if (loadTimer) window.clearTimeout(loadTimer)
  loadTimer = window.setTimeout(() => {
    loadTimer = undefined
    void loadData()
  }, 0)
}

function resetFilters() {
  semesterId.value = undefined
  departmentId.value = undefined
  teacherId.value = undefined
  keyword.value = ''
  teachers.value = []
  scheduleLoadData()
}

function openCourse(item: CourseListItem) {
  void router.push({ name: 'course-detail', params: { offeringId: item.offeringId } })
}

watch(departmentId, async () => {
  if (!initialized) return
  await loadTeachers()
  scheduleLoadData()
})

watch([semesterId, teacherId], () => {
  scheduleLoadData()
})

onMounted(async () => {
  try {
    await loadCatalogs()
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
        <h1>课程信息查询与浏览</h1>
        <p>
          按学期、院系、教师与关键字定位开课记录，查看课程班级、学生人次、资源数量与督导状态。
        </p>
      </div>
      <div class="page-actions">
        <button class="button" type="button" @click="resetFilters">
          清空条件
        </button>
        <button class="button button-primary" type="button" :disabled="loading" @click="loadData">
          <Search :size="16" aria-hidden="true" />
          查询课程
        </button>
      </div>
    </header>

    <div v-if="error" class="error-banner">
      <AlertCircle :size="17" aria-hidden="true" />
      <span>{{ error }}</span>
    </div>

    <form class="filter-bar" @submit.prevent="loadData">
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
      <label class="field">
        <span>教师</span>
        <select v-model="teacherId">
          <option :value="undefined">全部教师</option>
          <option v-for="teacher in teachers" :key="teacher.id" :value="teacher.id">
            {{ teacher.name }}（{{ teacher.employeeNo }}）
          </option>
        </select>
      </label>
      <label class="field">
        <span>课程关键字</span>
        <input v-model="keyword" placeholder="课程代码、名称或教师姓名" @keyup.enter="loadData" />
      </label>
      <div class="filter-actions">
        <button class="button button-primary" type="submit" :disabled="loading">筛选</button>
      </div>
    </form>

    <div class="metric-grid">
      <MetricTile
        label="开课记录"
        :value="formatNumber(metrics.courseCount)"
        hint="当前筛选结果"
        :icon="BookOpenCheck"
      />
      <MetricTile
        label="学生人次"
        :value="formatNumber(metrics.studentCount)"
        hint="按开课班级累计"
        :icon="Users"
        tone="green"
      />
      <MetricTile
        label="教学资源"
        :value="formatNumber(metrics.resourceCount)"
        hint="资源文件总数"
        :icon="FolderOpen"
        tone="amber"
      />
      <MetricTile
        label="已督导课程"
        :value="formatNumber(metrics.supervisedCount)"
        hint="当前结果中的覆盖数"
        :icon="ShieldCheck"
        tone="red"
      />
    </div>

    <section class="panel panel-flush">
      <div class="panel-header" style="padding: 18px 20px 0; margin-bottom: 12px">
        <div>
          <h2>课程与教师视图</h2>
          <p>课程视图用于对比筛选结果，教师视图用于查看教师课程简介与开课情况。</p>
        </div>
        <div class="tabs" role="tablist">
          <button
            type="button"
            class="tab"
            :class="{ active: activeTab === 'courses' }"
            @click="activeTab = 'courses'"
          >
            课程列表
          </button>
          <button
            type="button"
            class="tab"
            :class="{ active: activeTab === 'teachers' }"
            @click="activeTab = 'teachers'"
          >
            教师总览
          </button>
        </div>
      </div>

      <LoadingBlock v-if="loading" />

      <template v-else-if="activeTab === 'courses'">
        <EmptyState
          v-if="courses.length === 0"
          title="当前条件下没有课程"
          description="可调整学期、院系、教师或课程关键字后重新筛选。"
        />
        <div v-else class="data-table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>课程</th>
                <th>教师</th>
                <th>开课学期</th>
                <th>班级</th>
                <th class="numeric">学生人数</th>
                <th class="numeric">资源</th>
                <th>督导</th>
                <th>开课状态</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="item in courses"
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
                <td>{{ item.semesterName }}</td>
                <td>{{ item.className }}</td>
                <td class="numeric">{{ formatNumber(item.studentCount) }}</td>
                <td class="numeric">{{ item.resourceCount }}</td>
                <td>
                  <span :class="['coverage-mark', { missing: !item.supervised }]">
                    <ShieldCheck :size="14" aria-hidden="true" />
                    {{ item.supervised ? '已督导' : '待安排' }}
                  </span>
                </td>
                <td>
                  <StatusBadge :status="item.status" :label="item.statusLabel" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </template>

      <template v-else>
        <EmptyState
          v-if="!overview || overview.teachers.length === 0"
          title="暂无教师数据"
          :description="overview?.emptyMessage || '请调整院系或教师筛选条件。'"
        />
        <div v-else style="padding: 0 20px 20px">
          <div
            v-for="teacher in overview.teachers"
            :key="teacher.teacherId"
            class="teacher-group"
          >
            <div class="teacher-profile">
              <UserRound :size="18" aria-hidden="true" />
              <strong>{{ teacher.teacherName }}</strong>
              <span>{{ teacher.employeeNo }} · {{ teacher.title }}</span>
              <span>{{ teacher.departmentName }}</span>
              <span class="profile-count">{{ teacher.courses.length }} 条开课记录</span>
            </div>

            <EmptyState
              v-if="teacher.courses.length === 0"
              compact
              title="该教师暂无开课数据"
              description="可调整学期或院系后再次查询。"
            />
            <div v-else class="teacher-courses">
              <article
                v-for="course in teacher.courses"
                :key="course.offeringId"
                class="course-mini"
                tabindex="0"
                @click="openCourse(course)"
                @keyup.enter="openCourse(course)"
              >
                <header>
                  <strong>{{ course.courseName }}</strong>
                  <StatusBadge :status="course.status" :label="course.statusLabel" />
                </header>
                <p>
                  {{ course.courseCode }} · {{ course.semesterName }}<br />
                  {{ course.className }} · {{ course.studentCount }} 人次 ·
                  {{ course.resourceCount }} 个资源
                </p>
              </article>
            </div>
          </div>
        </div>
      </template>
    </section>
  </section>
</template>
