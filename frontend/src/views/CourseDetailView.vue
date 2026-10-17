<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  AlertCircle,
  ArrowLeft,
  BookOpen,
  CalendarDays,
  Download,
  FileText,
  FolderOpen,
  GraduationCap,
  Link2,
  Presentation,
  ShieldCheck,
  UploadCloud,
  Users,
  Video,
} from 'lucide-vue-next'
import EmptyState from '@/components/EmptyState.vue'
import LoadingBlock from '@/components/LoadingBlock.vue'
import MetricTile from '@/components/MetricTile.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { api, errorMessage } from '@/services/api'
import { authState } from '@/stores/auth'
import type { CourseDetail, ResourceType } from '@/types/api'
import { formatDateTime, formatNumber } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const course = ref<CourseDetail>()
const loading = ref(true)
const error = ref('')
const success = ref('')
const savingChapter = ref(false)
const uploading = ref(false)
const selectedFile = ref<File>()

const chapterForm = reactive({
  title: '',
  displayOrder: 1,
  description: '',
})

const resourceForm = reactive<{
  name: string
  resourceType: ResourceType
  chapterId?: number
  description: string
}>({
  name: '',
  resourceType: 'DOCUMENT',
  chapterId: undefined,
  description: '',
})

const resourceIcons: Record<ResourceType, typeof FileText> = {
  DOCUMENT: FileText,
  SLIDES: Presentation,
  VIDEO: Video,
  LINK: Link2,
}

const offeringId = computed(() => {
  const value = Number(route.params.offeringId)
  return Number.isInteger(value) && value > 0 ? value : undefined
})
const canManage = computed(
  () =>
    authState.user?.role === 'TEACHER' &&
    authState.user.teacherId === course.value?.teacherId,
)

async function loadDetail(id: number) {
  loading.value = true
  error.value = ''
  try {
    course.value = await api.getCourseDetail(id)
  } catch (requestError) {
    error.value = errorMessage(requestError)
  } finally {
    loading.value = false
  }
}

async function createChapter() {
  if (!offeringId.value) return
  savingChapter.value = true
  error.value = ''
  success.value = ''
  try {
    await api.createChapter(offeringId.value, {
      title: chapterForm.title,
      displayOrder: chapterForm.displayOrder,
      description: chapterForm.description,
    })
    success.value = '章节已创建'
    chapterForm.title = ''
    chapterForm.description = ''
    chapterForm.displayOrder += 1
    await loadDetail(offeringId.value)
  } catch (requestError) {
    error.value = errorMessage(requestError)
  } finally {
    savingChapter.value = false
  }
}

function onResourceFile(event: Event) {
  const target = event.target as HTMLInputElement
  selectedFile.value = target.files?.[0]
  if (selectedFile.value && !resourceForm.name) {
    resourceForm.name = selectedFile.value.name.replace(/\.[^.]+$/, '')
  }
}

async function uploadResource() {
  if (!offeringId.value || !selectedFile.value) {
    error.value = '请选择需要上传的文件'
    return
  }
  uploading.value = true
  error.value = ''
  success.value = ''
  try {
    await api.uploadResource(offeringId.value, {
      name: resourceForm.name,
      resourceType: resourceForm.resourceType,
      chapterId: resourceForm.chapterId,
      description: resourceForm.description,
      file: selectedFile.value,
    })
    success.value = '教学资源上传成功'
    resourceForm.name = ''
    resourceForm.description = ''
    resourceForm.chapterId = undefined
    selectedFile.value = undefined
    await loadDetail(offeringId.value)
  } catch (requestError) {
    error.value = errorMessage(requestError)
  } finally {
    uploading.value = false
  }
}

async function openResource(resource: CourseDetail['resources'][number]) {
  try {
    await api.downloadResource(resource.downloadUrl, resource.originalFilename || resource.name)
  } catch (requestError) {
    error.value = errorMessage(requestError)
  }
}

watch(
  offeringId,
  (id) => {
    success.value = ''
    if (id === undefined) {
      course.value = undefined
      loading.value = false
      error.value = '课程编号格式不正确'
      return
    }
    void loadDetail(id)
  },
  { immediate: true },
)
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <button class="button" type="button" style="margin-bottom: 14px" @click="router.back()">
          <ArrowLeft :size="16" aria-hidden="true" />
          返回列表
        </button>
        <h1>{{ course?.courseName || '课程详情' }}</h1>
        <p v-if="course">
          {{ course.courseCode }} · {{ course.departmentName }} · {{ course.semesterName }}
        </p>
      </div>
      <StatusBadge
        v-if="course"
        :status="course.status"
        :label="course.statusLabel"
      />
    </header>

    <div v-if="error" class="error-banner">
      <AlertCircle :size="17" aria-hidden="true" />
      <span>{{ error }}</span>
    </div>
    <div v-if="success" class="success-banner">
      <span>{{ success }}</span>
    </div>

    <LoadingBlock v-if="loading" />

    <template v-else-if="course">
      <div class="metric-grid">
        <MetricTile
          label="学生人次"
          :value="formatNumber(course.studentCount)"
          hint="当前开课班级"
          :icon="Users"
          tone="green"
        />
        <MetricTile
          label="教学资源"
          :value="formatNumber(course.resources.length)"
          hint="文档、课件、视频与链接"
          :icon="FolderOpen"
          tone="amber"
        />
        <MetricTile
          label="督导次数"
          :value="formatNumber(course.supervisionCount)"
          hint="已记录听评课次数"
          :icon="ShieldCheck"
          :tone="course.supervised ? 'green' : 'red'"
        />
        <MetricTile
          label="课程学分"
          :value="course.credits"
          hint="课程基础信息"
          :icon="BookOpen"
        />
      </div>

      <div class="detail-grid">
        <section class="panel">
          <div class="panel-header">
            <div>
              <h2>课程简介与开课信息</h2>
              <p>支撑教研室统筹排课和教师备课查看。</p>
            </div>
            <GraduationCap :size="21" color="#667c8e" aria-hidden="true" />
          </div>

          <p style="margin: 0 0 18px; color: var(--ink-700); font-size: 13px; line-height: 1.8">
            {{ course.description || '暂无课程简介' }}
          </p>

          <dl class="detail-list">
            <div class="detail-item">
              <dt>授课教师</dt>
              <dd>{{ course.teacherName }}</dd>
            </div>
            <div class="detail-item">
              <dt>开课班级</dt>
              <dd>{{ course.className }}</dd>
            </div>
            <div class="detail-item">
              <dt>开课学期</dt>
              <dd>{{ course.semesterName }}</dd>
            </div>
            <div class="detail-item">
              <dt>所属院系</dt>
              <dd>{{ course.departmentName }}</dd>
            </div>
            <div class="detail-item">
              <dt>督导状态</dt>
              <dd>{{ course.supervised ? '已督导' : '待安排' }}</dd>
            </div>
            <div class="detail-item">
              <dt>课程状态</dt>
              <dd>{{ course.statusLabel }}</dd>
            </div>
          </dl>
        </section>

        <aside class="panel">
          <div class="panel-header">
            <div>
              <h2>课程资源</h2>
              <p>按上传时间倒序展示。</p>
            </div>
            <CalendarDays :size="20" color="#667c8e" aria-hidden="true" />
          </div>

          <div v-if="course.chapters.length > 0" class="chapter-strip">
            <span v-for="chapter in course.chapters" :key="chapter.id">
              {{ chapter.title }} · {{ chapter.resourceCount }}
            </span>
          </div>

          <EmptyState
            v-if="course.resources.length === 0"
            compact
            title="暂无教学资源"
            :description="course.resourceEmptyMessage"
          />
          <div v-else class="resource-list">
            <article
              v-for="resource in course.resources"
              :key="resource.id"
              class="resource-item"
            >
              <div class="resource-icon">
                <component :is="resourceIcons[resource.resourceType]" :size="18" aria-hidden="true" />
              </div>
              <div>
                <strong>{{ resource.name }}</strong>
                <span>
                  {{ resource.resourceTypeLabel }} · {{ formatDateTime(resource.uploadedAt) }}
                </span>
              </div>
              <button
                class="icon-button"
                type="button"
                :title="`打开${resource.name}`"
                @click="openResource(resource)"
              >
                <Download :size="15" aria-hidden="true" />
              </button>
            </article>
          </div>
        </aside>
      </div>

      <section v-if="canManage" class="panel">
        <div class="panel-header">
          <div>
            <h2>教师资源管理</h2>
            <p>创建章节并上传课件、教案或视频，上传后学生可在课程详情和资源库检索。</p>
          </div>
          <UploadCloud :size="21" color="#667c8e" aria-hidden="true" />
        </div>

        <div class="management-grid">
          <form class="form-stack" @submit.prevent="createChapter">
            <h3>新增章节</h3>
            <label class="field">
              <span>章节名称</span>
              <input v-model="chapterForm.title" required maxlength="128" placeholder="例如 第 1 章 课程导论" />
            </label>
            <label class="field">
              <span>章节顺序</span>
              <input v-model.number="chapterForm.displayOrder" required type="number" min="1" />
            </label>
            <label class="field">
              <span>章节说明</span>
              <input v-model="chapterForm.description" maxlength="512" placeholder="简要说明章节内容" />
            </label>
            <button class="button" type="submit" :disabled="savingChapter">
              {{ savingChapter ? '正在创建' : '创建章节' }}
            </button>
          </form>

          <form class="form-stack" @submit.prevent="uploadResource">
            <h3>上传教学资源</h3>
            <div class="form-grid">
              <label class="field">
                <span>资源名称</span>
                <input v-model="resourceForm.name" required maxlength="128" placeholder="请输入资源名称" />
              </label>
              <label class="field">
                <span>资源类型</span>
                <select v-model="resourceForm.resourceType">
                  <option value="DOCUMENT">文档 / 教案</option>
                  <option value="SLIDES">课件</option>
                  <option value="VIDEO">视频</option>
                </select>
              </label>
            </div>
            <label class="field">
              <span>所属章节</span>
              <select v-model="resourceForm.chapterId">
                <option :value="undefined">暂不归类</option>
                <option v-for="chapter in course.chapters" :key="chapter.id" :value="chapter.id">
                  {{ chapter.title }}
                </option>
              </select>
            </label>
            <label class="field">
              <span>资源说明</span>
              <input v-model="resourceForm.description" maxlength="512" placeholder="简要说明资源用途" />
            </label>
            <label class="field">
              <span>选择文件</span>
              <input type="file" accept=".pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.txt,.md,.mp4,.webm,.zip" @change="onResourceFile" />
            </label>
            <button class="button button-primary" type="submit" :disabled="uploading">
              {{ uploading ? '正在上传' : '上传资源' }}
            </button>
          </form>
        </div>
      </section>
    </template>

    <EmptyState
      v-else
      title="未找到课程记录"
      description="该开课记录可能已删除，请返回课程列表重新选择。"
    />
  </section>
</template>
