<template>
  <main class="login-page">
    <el-card class="login-card" shadow="never">
      <template #header>CESD Login</template>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @keyup.enter="handleLogin"
      >
        <el-form-item label="Username" prop="username"
          ><el-input v-model="form.username"
        /></el-form-item>
        <el-form-item label="Password" prop="password"
          ><el-input v-model="form.password" type="password" show-password
        /></el-form-item>
        <el-button type="primary" class="login-button" :loading="loading" @click="handleLogin"
          >Login</el-button
        >
      </el-form>
    </el-card>
  </main>
</template>
<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { getCurrentUser, login } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules: FormRules = {
  username: [{ required: true, message: 'Required', trigger: 'blur' }],
  password: [{ required: true, message: 'Required', trigger: 'blur' }],
}
async function handleLogin() {
  if (!formRef.value || loading.value) return
  await formRef.value.validate()
  loading.value = true
  try {
    const result = await login({ username: form.username.trim(), password: form.password })
    userStore.setLoginResult(result)
    userStore.setCurrentUser(await getCurrentUser())
    await userStore.refreshPermissions()
    ElMessage.success('Login success')
    await router.replace(getRedirectPath())
  } finally {
    loading.value = false
  }
}
function getRedirectPath() {
  const redirect = route.query.redirect
  if (typeof redirect === 'string' && redirect.startsWith('/')) return redirect
  const role = String(userStore.roleCode || '').toUpperCase()
  if (role === 'ADMIN') return '/admin/scores'
  if (role === 'AUDITOR' || role === 'REVIEWER') return '/audit/pending'
  return '/student/applications'
}
</script>
<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: #f5f7fb;
}
.login-card {
  width: 420px;
  max-width: calc(100vw - 32px);
}
.login-button {
  width: 100%;
}
</style>
