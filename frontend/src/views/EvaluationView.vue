<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { AlertCircle, CheckCircle2, ClipboardCheck, Star } from 'lucide-vue-next'
import EmptyState from '@/components/EmptyState.vue'
import LoadingBlock from '@/components/LoadingBlock.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { api, errorMessage } from '@/services/api'
import type { CourseListItem, Department, Semester } from '@/types/api'

const departments = ref<Department[]>([])
const semesters = ref<Semester[]>([])
const courses = ref<CourseListItem[]>([])
const semesterId = ref<number>()
const departmentId = ref<number>()
const selectedCourse = ref<CourseListItem>()
const loading = ref(false)
const submitting = ref(false)
const error = ref('')
const success = ref('')
const form = reactive({
  score: 80,
  comment: '',
})

async function loadCourses() {
  loading.value = true
  error.value = ''
  try {
    courses.value = await api.listCourses({
      semesterId: semesterId.value,
      departmentId: departmentId.value,
    })
  } catch (requestError) {
    error.value = errorMessage(requestError)
  } finally {
    loading.value = false
  }
}

function selectCourse(course: CourseListItem) {
  selectedCourse.value = course
  form.score = 80
  form.comment = ''
  error.value = ''
  success.value = ''
}

async function submit() {
  if (!selectedCourse.value) return
  submitting.value = true
  error.value = ''
  success.value = ''
  try {
    await api.submitEvaluation({
      offeringId: selectedCourse.value.offeringId,
      score: form.score,
      comment: form.comment,
    })
    success.value = `已完成 ${selectedCourse.value.courseName} 的课程评价，督导状态已联动更新`
    form.comment = ''
  } catch (requestError) {
    error.value = errorMessage(requestError)
  } finally {
    submitting.value = false
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
    await loadCourses()
  } catch (requestError) {
    error.value = errorMessage(requestError)
  }
})
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>课程评价与打分</h1>
        <p>督导和教研室主任可对课程进行百分制评分并填写评语，评价会同步更新课程督导状态。</p>
      </div>
      <button class="button" type="button" :disabled="loading" @click="loadCourses">刷新课程</button>
    </header>

    <div v-if="error" class="error-banner">
      <AlertCircle :size="17" aria-hidden="true" />
      <span>{{ error }}</span>
    </div>
    <div v-if="success" class="success-banner">
      <CheckCircle2 :size="17" aria-hidden="true" />
      <span>{{ success }}</span>
    </div>

    <form class="filter-bar" style="grid-template-columns: repeat(2, minmax(180px, 280px))" @submit.prevent="loadCourses">
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

    <div class="split-layout">
      <form class="panel form-stack" @submit.prevent="submit">
        <div class="panel-header">
          <div>
            <h2>填写评价</h2>
            <p>{{ selectedCourse ? `${selectedCourse.courseName} · ${selectedCourse.className}` : '请先从右侧选择课程' }}</p>
          </div>
          <ClipboardCheck :size="21" color="#667c8e" aria-hidden="true" />
        </div>
        <label class="field">
          <span>评分（0-100）</span>
          <input v-model.number="form.score" required type="number" min="0" max="100" />
        </label>
        <label class="field">
          <span>评价评语</span>
          <textarea v-model="form.comment" required maxlength="2000" placeholder="请填写课堂观察、亮点与改进建议" />
        </label>
        <button class="button button-primary" type="submit" :disabled="!selectedCourse || submitting">
          <Star :size="16" aria-hidden="true" />
          {{ submitting ? '正在提交' : '提交评价' }}
        </button>
      </form>

      <section class="panel panel-flush">
        <div class="panel-header" style="padding: 18px 20px 0; margin-bottom: 12px">
          <div>
            <h2>待评价课程</h2>
            <p>点击课程后在左侧填写评分与评语。</p>
          </div>
        </div>
        <LoadingBlock v-if="loading" />
        <EmptyState v-else-if="courses.length === 0" title="当前范围暂无课程" />
        <div v-else class="data-table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>课程</th>
                <th>教师</th>
                <th>班级</th>
                <th>督导状态</th>
                <th>开课状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="course in courses"
                :key="course.offeringId"
                :class="{ 'selected-row': selectedCourse?.offeringId === course.offeringId }"
              >
                <td>
                  <span class="cell-main">{{ course.courseName }}</span>
                  <span class="cell-sub">{{ course.courseCode }}</span>
                </td>
                <td>{{ course.teacherName }}</td>
                <td>{{ course.className }}</td>
                <td>{{ course.supervised ? '已督导' : '待督导' }}</td>
                <td><StatusBadge :status="course.status" :label="course.statusLabel" /></td>
                <td>
                  <button class="button" type="button" @click="selectCourse(course)">选择</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  </section>
</template>
