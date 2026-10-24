<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  AlertCircle,
  CheckCircle2,
  Download,
  FolderSearch,
  Search,
  Star,
} from 'lucide-vue-next'
import EmptyState from '@/components/EmptyState.vue'
import LoadingBlock from '@/components/LoadingBlock.vue'
import { api, errorMessage } from '@/services/api'
import { authState } from '@/stores/auth'
import type {
  Department,
  ResourceQualityLevel,
  ResourceSearchItem,
  ResourceType,
  Semester,
} from '@/types/api'
import { formatDateTime } from '@/utils/format'

const departments = ref<Department[]>([])
const semesters = ref<Semester[]>([])
const resources = ref<ResourceSearchItem[]>([])
const semesterId = ref<number>()
const departmentId = ref<number>()
const resourceType = ref<ResourceType | ''>('')
const keyword = ref('')
const loading = ref(false)
const error = ref('')
const success = ref('')
const selectedReviewResource = ref<ResourceSearchItem>()
const savingReview = ref(false)
const reviewForm = reactive<{
  level: ResourceQualityLevel
  comment: string
  recommended: boolean
}>({
  level: 'QUALIFIED',
  comment: '',
  recommended: false,
})
const canReview = computed(() =>
  Boolean(authState.user && ['SUPERVISOR', 'DIRECTOR', 'ADMIN'].includes(authState.user.role)),
)

async function loadData() {
  loading.value = true
  error.value = ''
  try {
    resources.value = await api.searchResources({
      semesterId: semesterId.value,
      departmentId: departmentId.value,
      resourceType: resourceType.value || undefined,
      keyword: keyword.value.trim() || undefined,
    })
  } catch (requestError) {
    error.value = errorMessage(requestError)
  } finally {
    loading.value = false
  }
}

async function openResource(resource: ResourceSearchItem) {
  try {
    await api.downloadResource(resource.downloadUrl, resource.name)
  } catch (requestError) {
    error.value = errorMessage(requestError)
  }
}

function startReview(resource: ResourceSearchItem) {
  selectedReviewResource.value = resource
  reviewForm.level = resource.qualityLevel ?? 'QUALIFIED'
  reviewForm.comment = ''
  reviewForm.recommended = resource.recommended
  error.value = ''
  success.value = ''
}

async function submitReview() {
  if (!selectedReviewResource.value) return
  savingReview.value = true
  error.value = ''
  success.value = ''
  try {
    await api.reviewResource(selectedReviewResource.value.id, {
      level: reviewForm.level,
      comment: reviewForm.comment.trim() || undefined,
      recommended: reviewForm.recommended,
    })
    success.value = `已完成 ${selectedReviewResource.value.name} 的质量评审`
    selectedReviewResource.value = undefined
    await loadData()
  } catch (requestError) {
    error.value = errorMessage(requestError)
  } finally {
    savingReview.value = false
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
        <h1>教学资源检索</h1>
        <p>按学期、院系、资源和关键字检索章节化课程资源，教师可从课程详情上传文件。</p>
      </div>
      <button class="button button-primary" type="button" :disabled="loading" @click="loadData">
        <Search :size="16" aria-hidden="true" />
        检索资源
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
        <span>资源类型</span>
        <select v-model="resourceType">
          <option value="">全部类型</option>
          <option value="DOCUMENT">文档 / 教案</option>
          <option value="SLIDES">课件</option>
          <option value="VIDEO">视频</option>
          <option value="LINK">链接</option>
        </select>
      </label>
      <label class="field">
        <span>关键字</span>
        <input v-model="keyword" placeholder="资源名称、说明或课程" @keyup.enter="loadData" />
      </label>
      <div class="filter-actions">
        <button class="button button-primary" type="submit" :disabled="loading">筛选</button>
      </div>
    </form>

    <section v-if="canReview && selectedReviewResource" class="panel">
      <div class="panel-header">
        <div>
          <h2>资源质量评审</h2>
          <p>{{ selectedReviewResource.courseName }} · {{ selectedReviewResource.name }}</p>
        </div>
        <Star :size="20" color="#667c8e" aria-hidden="true" />
      </div>
      <form class="review-form" @submit.prevent="submitReview">
        <label class="field">
          <span>质量等级</span>
          <select v-model="reviewForm.level">
            <option value="EXCELLENT">优质</option>
            <option value="QUALIFIED">合格</option>
            <option value="NEEDS_IMPROVEMENT">待改进</option>
          </select>
        </label>
        <label class="field">
          <span>评审意见</span>
          <input
            v-model="reviewForm.comment"
            maxlength="1000"
            placeholder="填写资源内容、结构或适用性评价"
          />
        </label>
        <label class="checkbox-row">
          <input v-model="reviewForm.recommended" type="checkbox" />
          推荐为优质资源
        </label>
        <div class="button-row">
          <button class="button button-primary" type="submit" :disabled="savingReview">
            {{ savingReview ? '正在保存' : '保存评审' }}
          </button>
          <button class="button" type="button" @click="selectedReviewResource = undefined">
            取消
          </button>
        </div>
      </form>
    </section>

    <section class="panel panel-flush">
      <div class="panel-header" style="padding: 18px 20px 0; margin-bottom: 12px">
        <div>
          <h2>资源列表</h2>
          <p>共 {{ resources.length }} 个资源，支持章节和课程维度检索。</p>
        </div>
        <FolderSearch :size="21" color="#667c8e" aria-hidden="true" />
      </div>
      <LoadingBlock v-if="loading" />
      <EmptyState
        v-else-if="resources.length === 0"
        title="没有找到教学资源"
        description="可调整学期、类型或关键字后重新检索。"
      />
      <div v-else class="data-table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>资源</th>
              <th>类型</th>
              <th>章节</th>
              <th>课程 / 教师</th>
              <th>质量</th>
              <th>上传时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="resource in resources" :key="resource.id">
              <td>
                <span class="cell-main">{{ resource.name }}</span>
                <span class="cell-sub">{{ resource.description || '暂无资源说明' }}</span>
              </td>
              <td>{{ resource.resourceTypeLabel }}</td>
              <td>{{ resource.chapterTitle || '未归类' }}</td>
              <td>
                <span class="cell-main">{{ resource.courseName }}</span>
                <span class="cell-sub">
                  {{ resource.courseCode }} · {{ resource.teacherName }} · {{ resource.className }}
                </span>
              </td>
              <td>
                <span v-if="resource.qualityLevelLabel" class="quality-badge">
                  {{ resource.qualityLevelLabel }}
                </span>
                <span v-if="resource.recommended" class="recommended-mark">
                  <Star :size="13" aria-hidden="true" />
                  推荐
                </span>
                <span v-if="!resource.qualityLevelLabel" class="cell-sub">未评审</span>
              </td>
              <td>{{ formatDateTime(resource.uploadedAt) }}</td>
              <td>
                <div class="button-row">
                  <button
                    class="icon-button"
                    type="button"
                    title="下载或打开资源"
                    @click="openResource(resource)"
                  >
                    <Download :size="15" aria-hidden="true" />
                  </button>
                  <button
                    v-if="canReview"
                    class="button"
                    type="button"
                    @click="startReview(resource)"
                  >
                    评审
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </section>
</template>
