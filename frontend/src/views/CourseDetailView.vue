<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  AlertCircle,
  ArrowLeft,
  BookOpen,
  CalendarDays,
  ExternalLink,
  FileText,
  FolderOpen,
  GraduationCap,
  Link2,
  Presentation,
  ShieldCheck,
  Users,
  Video,
} from 'lucide-vue-next'
import EmptyState from '@/components/EmptyState.vue'
import LoadingBlock from '@/components/LoadingBlock.vue'
import MetricTile from '@/components/MetricTile.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { api, errorMessage } from '@/services/api'
import type { CourseDetail, ResourceType } from '@/types/api'
import { formatDateTime, formatNumber } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const course = ref<CourseDetail>()
const loading = ref(true)
const error = ref('')

const resourceIcons: Record<ResourceType, typeof FileText> = {
  DOCUMENT: FileText,
  SLIDES: Presentation,
  VIDEO: Video,
  LINK: Link2,
}

const offeringId = computed(() => Number(route.params.offeringId))

async function loadDetail() {
  loading.value = true
  error.value = ''
  try {
    course.value = await api.getCourseDetail(offeringId.value)
  } catch (requestError) {
    error.value = errorMessage(requestError)
  } finally {
    loading.value = false
  }
}

watch(offeringId, loadDetail, { immediate: true })
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
              <a
                class="icon-button"
                :href="resource.url"
                target="_blank"
                rel="noreferrer"
                :title="`打开${resource.name}`"
              >
                <ExternalLink :size="15" aria-hidden="true" />
              </a>
            </article>
          </div>
        </aside>
      </div>
    </template>

    <EmptyState
      v-else
      title="未找到课程记录"
      description="该开课记录可能已删除，请返回课程列表重新选择。"
    />
  </section>
</template>
