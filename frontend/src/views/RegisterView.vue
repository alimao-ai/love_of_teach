<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  AlertCircle,
  ArrowRight,
  GraduationCap,
  IdCard,
  LockKeyhole,
  UserRound,
} from 'lucide-vue-next'
import { api, errorMessage } from '@/services/api'
import { saveAuth } from '@/stores/auth'
import type { UserRole } from '@/types/api'

const router = useRouter()
const username = ref('')
const displayName = ref('')
const password = ref('')
const role = ref<UserRole>('TEACHER')
const teacherNo = ref('')
const loading = ref(false)
const error = ref('')

async function submit() {
  loading.value = true
  error.value = ''
  try {
    const response = await api.register({
      username: username.value.trim(),
      displayName: displayName.value.trim(),
      password: password.value,
      role: role.value,
      teacherNo: role.value === 'TEACHER' ? teacherNo.value.trim() : undefined,
    })
    saveAuth(response)
    await router.push('/courses')
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
        <span class="auth-kicker">身份注册</span>
        <h1>用一个角色账号进入对应教学质量工作区</h1>
        <p>教师账号需要绑定教师工号，其他角色注册后可直接进入各自的业务页面。</p>
      </div>
    </section>

    <section class="auth-form-panel">
      <form class="auth-card" @submit.prevent="submit">
        <header>
          <span class="auth-kicker">创建账号</span>
          <h2>注册新账号</h2>
          <p>请选择身份并填写账号信息。</p>
        </header>

        <div v-if="error" class="error-banner">
          <AlertCircle :size="17" aria-hidden="true" />
          <span>{{ error }}</span>
        </div>

        <div class="form-grid">
          <label class="field">
            <span>用户名</span>
            <div class="input-with-icon">
              <UserRound :size="16" aria-hidden="true" />
              <input
                v-model="username"
                required
                minlength="3"
                maxlength="32"
                autocomplete="username"
                placeholder="字母、数字或下划线"
              />
            </div>
          </label>
          <label class="field">
            <span>姓名</span>
            <input v-model="displayName" required maxlength="64" placeholder="请输入真实姓名" />
          </label>
        </div>

        <label class="field">
          <span>身份角色</span>
          <select v-model="role">
            <option value="TEACHER">教师</option>
            <option value="SUPERVISOR">教学督导</option>
            <option value="DIRECTOR">教研室主任</option>
          </select>
        </label>

        <label v-if="role === 'TEACHER'" class="field">
          <span>教师工号</span>
          <div class="input-with-icon">
            <IdCard :size="16" aria-hidden="true" />
            <input v-model="teacherNo" required placeholder="例如 T001" />
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
              minlength="6"
              maxlength="64"
              autocomplete="new-password"
              placeholder="至少 6 位"
            />
          </div>
        </label>

        <button class="button button-primary auth-submit" type="submit" :disabled="loading">
          {{ loading ? '正在注册' : '注册并登录' }}
          <ArrowRight :size="16" aria-hidden="true" />
        </button>

        <p class="auth-switch">
          已有账号？
          <RouterLink to="/login">返回登录</RouterLink>
        </p>
      </form>
    </section>
  </div>
</template>
