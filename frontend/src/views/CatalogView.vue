<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import {
  AlertCircle,
  BookOpenText,
  CalendarDays,
  CheckCircle2,
  Pencil,
  Plus,
  Power,
  RotateCcw,
} from 'lucide-vue-next'
import EmptyState from '@/components/EmptyState.vue'
import LoadingBlock from '@/components/LoadingBlock.vue'
import { api, errorMessage } from '@/services/api'
import type {
  Department,
  DepartmentPayload,
  Semester,
  SemesterPayload,
} from '@/types/api'
import { formatDate } from '@/utils/format'

const activeTab = ref<'departments' | 'semesters'>('departments')
const departments = ref<Department[]>([])
const semesters = ref<Semester[]>([])
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const success = ref('')
const editingDepartmentId = ref<number>()
const editingSemesterId = ref<number>()

const departmentForm = reactive<DepartmentPayload>({
  code: '',
  name: '',
  active: true,
})

const semesterForm = reactive<SemesterPayload>({
  code: '',
  name: '',
  startDate: '',
  endDate: '',
  active: true,
})

async function loadData() {
  loading.value = true
  error.value = ''
  try {
    const [departmentData, semesterData] = await Promise.all([
      api.listDepartments(false),
      api.listSemesters(false),
    ])
    departments.value = departmentData
    semesters.value = semesterData
  } catch (requestError) {
    error.value = errorMessage(requestError)
  } finally {
    loading.value = false
  }
}

function resetDepartmentForm() {
  editingDepartmentId.value = undefined
  departmentForm.code = ''
  departmentForm.name = ''
  departmentForm.active = true
}

function resetSemesterForm() {
  editingSemesterId.value = undefined
  semesterForm.code = ''
  semesterForm.name = ''
  semesterForm.startDate = ''
  semesterForm.endDate = ''
  semesterForm.active = true
}

function editDepartment(department: Department) {
  activeTab.value = 'departments'
  editingDepartmentId.value = department.id
  departmentForm.code = department.code
  departmentForm.name = department.name
  departmentForm.active = department.active
}

function editSemester(semester: Semester) {
  activeTab.value = 'semesters'
  editingSemesterId.value = semester.id
  semesterForm.code = semester.code
  semesterForm.name = semester.name
  semesterForm.startDate = semester.startDate
  semesterForm.endDate = semester.endDate
  semesterForm.active = semester.active
}

async function saveDepartment() {
  saving.value = true
  error.value = ''
  success.value = ''
  try {
    const payload = { ...departmentForm, code: departmentForm.code.trim(), name: departmentForm.name.trim() }
    if (editingDepartmentId.value) {
      await api.updateDepartment(editingDepartmentId.value, payload)
      success.value = '院系信息已更新'
    } else {
      await api.createDepartment(payload)
      success.value = '院系已新增'
    }
    resetDepartmentForm()
    await loadData()
  } catch (requestError) {
    error.value = errorMessage(requestError)
  } finally {
    saving.value = false
  }
}

async function saveSemester() {
  saving.value = true
  error.value = ''
  success.value = ''
  try {
    const payload = { ...semesterForm, code: semesterForm.code.trim(), name: semesterForm.name.trim() }
    if (editingSemesterId.value) {
      await api.updateSemester(editingSemesterId.value, payload)
      success.value = '学期信息已更新'
    } else {
      await api.createSemester(payload)
      success.value = '学期已新增'
    }
    resetSemesterForm()
    await loadData()
  } catch (requestError) {
    error.value = errorMessage(requestError)
  } finally {
    saving.value = false
  }
}

async function toggleDepartment(department: Department) {
  error.value = ''
  try {
    await api.updateDepartmentStatus(department.id, !department.active)
    success.value = department.active ? `${department.name} 已停用` : `${department.name} 已启用`
    await loadData()
  } catch (requestError) {
    error.value = errorMessage(requestError)
  }
}

async function toggleSemester(semester: Semester) {
  error.value = ''
  try {
    await api.updateSemesterStatus(semester.id, !semester.active)
    success.value = semester.active ? `${semester.name} 已停用` : `${semester.name} 已启用`
    await loadData()
  } catch (requestError) {
    error.value = errorMessage(requestError)
  }
}

onMounted(loadData)
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>学期与院系基础配置</h1>
        <p>
          维护课程查询和督导统计所依赖的学期、院系数据，停用后不会出现在业务筛选的启用项中。
        </p>
      </div>
      <div class="page-actions">
        <button class="button" type="button" :disabled="loading" @click="loadData">
          <RotateCcw :size="16" aria-hidden="true" />
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

    <div class="tabs">
      <button
        type="button"
        class="tab"
        :class="{ active: activeTab === 'departments' }"
        @click="activeTab = 'departments'"
      >
        <BookOpenText :size="14" aria-hidden="true" />
        院系配置
      </button>
      <button
        type="button"
        class="tab"
        :class="{ active: activeTab === 'semesters' }"
        @click="activeTab = 'semesters'"
      >
        <CalendarDays :size="14" aria-hidden="true" />
        学期配置
      </button>
    </div>

    <LoadingBlock v-if="loading" />

    <div v-else-if="activeTab === 'departments'" class="split-layout">
      <form class="panel form-stack" @submit.prevent="saveDepartment">
        <div class="panel-header">
          <div>
            <h2>{{ editingDepartmentId ? '编辑院系' : '新增院系' }}</h2>
            <p>院系编码用于 CSV 导入和查询筛选。</p>
          </div>
          <button v-if="editingDepartmentId" class="icon-button" type="button" title="取消编辑" @click="resetDepartmentForm">
            <RotateCcw :size="16" aria-hidden="true" />
          </button>
        </div>
        <label class="field">
          <span>院系编码</span>
          <input v-model="departmentForm.code" required maxlength="32" placeholder="例如 SOFTWARE" />
        </label>
        <label class="field">
          <span>院系名称</span>
          <input v-model="departmentForm.name" required maxlength="128" placeholder="例如 软件工程教研室" />
        </label>
        <label class="checkbox-row">
          <input v-model="departmentForm.active" type="checkbox" />
          启用该院系
        </label>
        <button class="button button-primary" type="submit" :disabled="saving">
          <Plus v-if="!editingDepartmentId" :size="16" aria-hidden="true" />
          <Pencil v-else :size="16" aria-hidden="true" />
          {{ editingDepartmentId ? '保存修改' : '新增院系' }}
        </button>
      </form>

      <section class="panel panel-flush">
        <div class="panel-header" style="padding: 18px 20px 0; margin-bottom: 12px">
          <div>
            <h2>院系列表</h2>
            <p>共 {{ departments.length }} 个院系。</p>
          </div>
        </div>
        <EmptyState v-if="departments.length === 0" title="暂无院系数据" />
        <div v-else class="data-table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>院系</th>
                <th class="numeric">教师数</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="department in departments" :key="department.id">
                <td>
                  <span class="cell-main">{{ department.name }}</span>
                  <span class="cell-sub">{{ department.code }}</span>
                </td>
                <td class="numeric">{{ department.teacherCount }}</td>
                <td>
                  <span :class="['status-toggle', { inactive: !department.active }]">
                    <Power :size="13" aria-hidden="true" />
                    {{ department.active ? '启用' : '停用' }}
                  </span>
                </td>
                <td>
                  <div class="button-row">
                    <button class="icon-button" type="button" title="编辑院系" @click="editDepartment(department)">
                      <Pencil :size="15" aria-hidden="true" />
                    </button>
                    <button
                      class="icon-button"
                      type="button"
                      :title="department.active ? '停用院系' : '启用院系'"
                      @click="toggleDepartment(department)"
                    >
                      <Power :size="15" aria-hidden="true" />
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>

    <div v-else class="split-layout">
      <form class="panel form-stack" @submit.prevent="saveSemester">
        <div class="panel-header">
          <div>
            <h2>{{ editingSemesterId ? '编辑学期' : '新增学期' }}</h2>
            <p>结束日期不得早于开始日期。</p>
          </div>
          <button v-if="editingSemesterId" class="icon-button" type="button" title="取消编辑" @click="resetSemesterForm">
            <RotateCcw :size="16" aria-hidden="true" />
          </button>
        </div>
        <label class="field">
          <span>学期编码</span>
          <input v-model="semesterForm.code" required maxlength="32" placeholder="例如 2026-2027-1" />
        </label>
        <label class="field">
          <span>学期名称</span>
          <input v-model="semesterForm.name" required maxlength="64" placeholder="例如 2026-2027 学年第一学期" />
        </label>
        <div class="form-grid">
          <label class="field">
            <span>开始日期</span>
            <input v-model="semesterForm.startDate" required type="date" />
          </label>
          <label class="field">
            <span>结束日期</span>
            <input v-model="semesterForm.endDate" required type="date" />
          </label>
        </div>
        <label class="checkbox-row">
          <input v-model="semesterForm.active" type="checkbox" />
          启用该学期
        </label>
        <button class="button button-primary" type="submit" :disabled="saving">
          <Plus v-if="!editingSemesterId" :size="16" aria-hidden="true" />
          <Pencil v-else :size="16" aria-hidden="true" />
          {{ editingSemesterId ? '保存修改' : '新增学期' }}
        </button>
      </form>

      <section class="panel panel-flush">
        <div class="panel-header" style="padding: 18px 20px 0; margin-bottom: 12px">
          <div>
            <h2>学期列表</h2>
            <p>共 {{ semesters.length }} 个学期。</p>
          </div>
        </div>
        <EmptyState v-if="semesters.length === 0" title="暂无学期数据" />
        <div v-else class="data-table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>学期</th>
                <th>起止日期</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="semester in semesters" :key="semester.id">
                <td>
                  <span class="cell-main">{{ semester.name }}</span>
                  <span class="cell-sub">{{ semester.code }}</span>
                </td>
                <td>{{ formatDate(semester.startDate) }} 至 {{ formatDate(semester.endDate) }}</td>
                <td>
                  <span :class="['status-toggle', { inactive: !semester.active }]">
                    <Power :size="13" aria-hidden="true" />
                    {{ semester.active ? '启用' : '停用' }}
                  </span>
                </td>
                <td>
                  <div class="button-row">
                    <button class="icon-button" type="button" title="编辑学期" @click="editSemester(semester)">
                      <Pencil :size="15" aria-hidden="true" />
                    </button>
                    <button
                      class="icon-button"
                      type="button"
                      :title="semester.active ? '停用学期' : '启用学期'"
                      @click="toggleSemester(semester)"
                    >
                      <Power :size="15" aria-hidden="true" />
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  </section>
</template>
