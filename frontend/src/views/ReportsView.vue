<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { AlertCircle, CheckCircle2, Download, FileSpreadsheet } from 'lucide-vue-next'
import { api, errorMessage } from '@/services/api'
import type { Department, Semester, Teacher } from '@/types/api'

const departments = ref<Department[]>([])
const semesters = ref<Semester[]>([])
const teachers = ref<Teacher[]>([])
const semesterId = ref<number>()
const departmentId = ref<number>()
const teacherId = ref<number>()
const exporting = ref(false)
const error = ref('')
const success = ref('')

async function loadTeachers() {
  teachers.value = await api.listTeachers(departmentId.value)
  if (teacherId.value && !teachers.value.some((teacher) => teacher.id === teacherId.value)) {
    teacherId.value = undefined
  }
}

async function exportReport() {
  exporting.value = true
  error.value = ''
  success.value = ''
  try {
    await api.exportOfferings({
      semesterId: semesterId.value,
      departmentId: departmentId.value,
      teacherId: teacherId.value,
    })
    success.value = '开课统计表已生成并开始下载'
  } catch (requestError) {
    error.value = errorMessage(requestError)
  } finally {
    exporting.value = false
  }
}

watch(departmentId, () => {
  if (departments.value.length > 0) void loadTeachers()
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
    await loadTeachers()
  } catch (requestError) {
    error.value = errorMessage(requestError)
  }
})
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>开课情况统计导出</h1>
        <p>按学期、院系和教师导出开课统计 CSV，包含开课数、学生人次、资源数、督导状态和平均评分。</p>
      </div>
      <button class="button button-primary" type="button" :disabled="exporting" @click="exportReport">
        <Download :size="16" aria-hidden="true" />
        {{ exporting ? '正在生成' : '导出统计表' }}
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

    <form class="filter-bar" @submit.prevent="exportReport">
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
      <div class="filter-actions">
        <button class="button button-primary" type="submit" :disabled="exporting">生成文件</button>
      </div>
    </form>

    <section class="panel">
      <div class="panel-header">
        <div>
          <h2>导出内容</h2>
          <p>CSV 文件使用 UTF-8 编码，可直接使用 Excel 打开并用于上报。</p>
        </div>
        <FileSpreadsheet :size="22" color="#667c8e" aria-hidden="true" />
      </div>
      <dl class="detail-list">
        <div class="detail-item">
          <dt>课程信息</dt>
          <dd>课程代码、课程名称、院系、教师、学期、班级</dd>
        </div>
        <div class="detail-item">
          <dt>开课数据</dt>
          <dd>学生人次、开课状态、资源数量</dd>
        </div>
        <div class="detail-item">
          <dt>质量数据</dt>
          <dd>督导状态、课程平均评分</dd>
        </div>
        <div class="detail-item">
          <dt>筛选范围</dt>
          <dd>按当前选择的学期、院系和教师导出</dd>
        </div>
      </dl>
    </section>
  </section>
</template>
