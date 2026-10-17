<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { AlertCircle, ArrowRight, GraduationCap, LockKeyhole, UserRound } from 'lucide-vue-next'
import { api, errorMessage } from '@/services/api'
import { saveAuth } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

async function submit() {
  loading.value = true
  error.value = ''
  try {
    const response = await api.login(username.value.trim(), password.value)
    saveAuth(response)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/courses'
    await router.push(redirect)
  } catch (requestError) {
    error.value = errorMessage(requestError)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-screen">
    <section class="auth-brand-panel">
      <div class="auth-brand">
        <div class="brand-mark auth-logo">
          <GraduationCap :size="30" aria-hidden="true" />
        </div>
        <strong>爱教学</strong>
        <span>教学质量全链路数字化管理平台</span>
      </div>
      <div class="auth-message">
        <span class="auth-kicker">进阶 1 · 查课程</span>
        <h1>让每一次教学评价都形成可追踪的改进闭环</h1>
        <p>课程资源、课堂评价、督导覆盖和统计报表统一汇聚在同一个教学质量工作台。</p>
      </div>
      <div class="auth-feature-list">
        <span>课程与教师数据底座</span>
        <span>章节化教学资源管理</span>
        <span>课程评价与反馈闭环</span>
        <span>开课统计与报表导出</span>
      </div>
    </section>

    <section class="auth-form-panel">
      <form class="auth-card" @submit.prevent="submit">
        <header>
          <span class="auth-kicker">欢迎回来</span>
          <h2>登录教学质量平台</h2>
          <p>登录后将根据账号角色展示对应工作区。</p>
        </header>

        <div v-if="error" class="error-banner">
          <AlertCircle :size="17" aria-hidden="true" />
          <span>{{ error }}</span>
        </div>

        <label class="field">
          <span>用户名</span>
          <div class="input-with-icon">
            <UserRound :size="16" aria-hidden="true" />
            <input v-model="username" required autocomplete="username" placeholder="请输入用户名" />
          </div>
        </label>
        <label class="field">
          <span>密码</span>
          <div class="input-with-icon">
            <LockKeyhole :size="16" aria-hidden="true" />
            <input
              v-model="password"
              required
              type="password"
              autocomplete="current-password"
              placeholder="请输入密码"
            />
          </div>
        </label>

        <button class="button button-primary auth-submit" type="submit" :disabled="loading">
          {{ loading ? '正在登录' : '登录' }}
          <ArrowRight :size="16" aria-hidden="true" />
        </button>

        <p class="auth-switch">
          还没有账号？
          <RouterLink to="/register">注册新账号</RouterLink>
        </p>

        <div class="demo-accounts">
          <strong>演示账号</strong>
          <span>主任：director / director123</span>
          <span>督导：supervisor / supervisor123</span>
          <span>教师：teacher / teacher123</span>
          <span>管理员：admin / admin123</span>
        </div>
      </form>
    </section>
  </div>
</template>
