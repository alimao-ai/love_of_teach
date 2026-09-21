<script setup lang="ts">
import { computed, ref } from 'vue'
import {
  AlertCircle,
  CheckCircle2,
  Download,
  FileSpreadsheet,
  UploadCloud,
  X,
} from 'lucide-vue-next'
import EmptyState from '@/components/EmptyState.vue'
import LoadingBlock from '@/components/LoadingBlock.vue'
import MetricTile from '@/components/MetricTile.vue'
import { api, errorMessage } from '@/services/api'
import type { ImportResult } from '@/types/api'
import { formatNumber } from '@/utils/format'

const fileInput = ref<HTMLInputElement>()
const selectedFile = ref<File>()
const result = ref<ImportResult>()
const importing = ref(false)
const error = ref('')

const summary = computed(() => ({
  total: result.value?.totalRows ?? 0,
  success: result.value?.successCount ?? 0,
  failure: result.value?.failureCount ?? 0,
}))

function chooseFile() {
  fileInput.value?.click()
}

function onFileChange(event: Event) {
  const target = event.target as HTMLInputElement
  selectedFile.value = target.files?.[0]
  result.value = undefined
  error.value = ''
}

function clearFile() {
  selectedFile.value = undefined
  result.value = undefined
  error.value = ''
  if (fileInput.value) fileInput.value.value = ''
}

async function importFile() {
  if (!selectedFile.value) {
    error.value = '请先选择 CSV 文件'
    return
  }

  importing.value = true
  error.value = ''
  try {
    result.value = await api.importCourses(selectedFile.value)
  } catch (requestError) {
    error.value = errorMessage(requestError)
  } finally {
    importing.value = false
  }
}

function downloadFailures() {
  if (!result.value?.failureCsv) return
  const blob = new Blob([`\uFEFF${result.value.failureCsv}`], {
    type: 'text/csv;charset=utf-8',
  })
  const url = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = 'course-import-failures.csv'
  document.body.appendChild(anchor)
  anchor.click()
  anchor.remove()
  // 浏览器开始下载后再释放对象 URL，避免部分环境取消下载。
  window.setTimeout(() => URL.revokeObjectURL(url), 1_000)
}
</script>

<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>课程与教师数据批量导入</h1>
        <p>
          下载标准 CSV 模板后批量初始化课程和教师数据。系统逐行校验必填项、院系、学期、人数、学分与开课状态。
        </p>
      </div>
      <div class="page-actions">
        <a class="button" :href="api.templateUrl">
          <Download :size="16" aria-hidden="true" />
          下载 CSV 模板
        </a>
      </div>
    </header>

    <div v-if="error" class="error-banner">
      <AlertCircle :size="17" aria-hidden="true" />
      <span>{{ error }}</span>
    </div>

    <div class="split-layout">
      <section class="panel">
        <div class="panel-header">
          <div>
            <h2>选择导入文件</h2>
            <p>支持 UTF-8 编码的 .csv 文件，最大 5MB。</p>
          </div>
          <FileSpreadsheet :size="22" color="#667c8e" aria-hidden="true" />
        </div>

        <label class="drop-zone">
          <UploadCloud :size="34" aria-hidden="true" />
          <strong>点击选择 CSV 文件</strong>
          <span>导入过程中会保留成功记录，并返回每条失败原因</span>
          <input ref="fileInput" type="file" accept=".csv,text/csv" @change="onFileChange" />
        </label>

        <div v-if="selectedFile" class="file-pill">
          <span>{{ selectedFile.name }} · {{ (selectedFile.size / 1024).toFixed(1) }} KB</span>
          <button class="icon-button" type="button" title="移除文件" @click="clearFile">
            <X :size="15" aria-hidden="true" />
          </button>
        </div>

        <div class="button-row" style="margin-top: 14px">
          <button class="button" type="button" @click="chooseFile">选择文件</button>
          <button class="button button-primary" type="button" :disabled="!selectedFile || importing" @click="importFile">
            <UploadCloud :size="16" aria-hidden="true" />
            {{ importing ? '正在导入' : '开始导入' }}
          </button>
        </div>
      </section>

      <section class="panel">
        <div class="panel-header">
          <div>
            <h2>导入规则</h2>
            <p>必填字段与格式要求。</p>
          </div>
        </div>
        <dl class="detail-list">
          <div class="detail-item">
            <dt>课程字段</dt>
            <dd>courseCode、courseName、departmentCode、credits</dd>
          </div>
          <div class="detail-item">
            <dt>教师字段</dt>
            <dd>teacherNo、teacherName</dd>
          </div>
          <div class="detail-item">
            <dt>开课字段</dt>
            <dd>semesterCode、className、studentCount、status</dd>
          </div>
          <div class="detail-item">
            <dt>状态取值</dt>
            <dd>PLANNED / OPEN / CLOSED 或对应中文</dd>
          </div>
        </dl>
      </section>
    </div>

    <LoadingBlock v-if="importing" label="正在逐行校验并写入数据" />

    <template v-if="result && !importing">
      <div class="metric-grid">
        <MetricTile label="数据总行数" :value="formatNumber(summary.total)" :icon="FileSpreadsheet" />
        <MetricTile
          label="导入成功"
          :value="formatNumber(summary.success)"
          hint="已写入课程、教师与开课数据"
          :icon="CheckCircle2"
          tone="green"
        />
        <MetricTile
          label="导入失败"
          :value="formatNumber(summary.failure)"
          hint="失败行未写入数据库"
          :icon="AlertCircle"
          tone="red"
        />
      </div>

      <section class="panel panel-flush">
        <div class="panel-header" style="padding: 18px 20px 0; margin-bottom: 12px">
          <div>
            <h2>校验结果</h2>
            <p>失败原因可导出为 CSV，修正后可直接重新导入。</p>
          </div>
          <button
            v-if="result.failureCount > 0"
            class="button"
            type="button"
            @click="downloadFailures"
          >
            <Download :size="16" aria-hidden="true" />
            导出失败原因
          </button>
        </div>

        <div v-if="result.failureCount === 0" class="success-banner" style="margin: 0 20px 20px">
          <CheckCircle2 :size="17" aria-hidden="true" />
          <span>全部数据已成功导入。</span>
        </div>
        <EmptyState
          v-else-if="result.failures.length === 0"
          title="没有可显示的失败明细"
        />
        <div v-else class="data-table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>CSV 行号</th>
                <th>课程代码</th>
                <th>失败原因</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="failure in result.failures" :key="`${failure.rowNumber}-${failure.courseCode}`">
                <td>{{ failure.rowNumber }}</td>
                <td>{{ failure.courseCode || '-' }}</td>
                <td>{{ failure.reason }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </template>
  </section>
</template>
