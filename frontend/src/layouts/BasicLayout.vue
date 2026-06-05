<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter, type RouteRecordNormalized } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { flattenMenus } from '@/api/permission'
import { changePassword, logout as logoutApi, type ChangePasswordPayload } from '@/api/auth'
import { canShowMenuRoute } from '@/utils/access'

interface MenuItem {
  title: string
  path: string
  order: number
}

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const passwordDialogVisible = ref(false)
const passwordSaving = ref(false)
const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive<ChangePasswordPayload>({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const pageTitle = computed(() => String(route.meta.title || 'CESD'))

const passwordRules: FormRules<ChangePasswordPayload> = {
  oldPassword: [{ required: true, message: 'Please enter your current password', trigger: 'blur' }],
  newPassword: [
    { required: true, message: 'Please enter a new password', trigger: 'blur' },
    { min: 6, message: 'Password must be at least 6 characters', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: 'Please confirm the new password', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('Passwords do not match'))
          return
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
}

function getRouteOrder(routeRecord: RouteRecordNormalized) {
  const order = routeRecord.meta.menuOrder
  return typeof order === 'number' ? order : 999
}

function resolveRouteByPath(path?: string) {
  if (!path) {
    return undefined
  }
  return router.getRoutes().find((item) => item.path === path)
}

function buildRouteMenu(routeRecord: RouteRecordNormalized): MenuItem {
  return {
    title: String(routeRecord.meta.title || routeRecord.name || routeRecord.path),
    path: routeRecord.path,
    order: getRouteOrder(routeRecord),
  }
}

function getMenuScope(path: string) {
  if (path.startsWith('/student/')) {
    return 'student'
  }
  if (path.startsWith('/audit/')) {
    return 'audit'
  }
  if (path.startsWith('/admin/')) {
    return 'admin'
  }
  return 'common'
}

function getMenuTestId(path: string) {
  const normalized = path.replace(/^\/+/, '').replace(/[/:]+/g, '-')
  return `menu-item-${normalized}`
}

const routeMenuItems = computed(() =>
  router
    .getRoutes()
    .filter((item) => canShowMenuRoute(item, userStore))
    .sort((a, b) => getRouteOrder(a) - getRouteOrder(b))
    .map(buildRouteMenu),
)

const backendMenuItems = computed(() => {
  const seen = new Set<string>()
  const items: MenuItem[] = []

  flattenMenus(userStore.menus).forEach((menu) => {
    const matchedRoute = resolveRouteByPath(menu.routePath)
    if (
      !matchedRoute ||
      !canShowMenuRoute(matchedRoute, userStore) ||
      seen.has(matchedRoute.path)
    ) {
      return
    }

    seen.add(matchedRoute.path)
    items.push({
      title:
        menu.permissionName ||
        String(matchedRoute.meta.title || matchedRoute.name || matchedRoute.path),
      path: matchedRoute.path,
      order: typeof menu.sortOrder === 'number' ? menu.sortOrder : getRouteOrder(matchedRoute),
    })
  })

  return items.sort((a, b) => a.order - b.order)
})

const menuItems = computed(() => {
  const seen = new Set<string>()
  const items: MenuItem[] = []

  backendMenuItems.value.forEach((item) => {
    seen.add(item.path)
    items.push(item)
  })

  routeMenuItems.value.forEach((item) => {
    if (seen.has(item.path)) {
      return
    }
    seen.add(item.path)
    items.push(item)
  })

  return items.sort((a, b) => a.order - b.order)
})

function resetPasswordForm() {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.clearValidate()
}

function openPasswordDialog() {
  resetPasswordForm()
  passwordDialogVisible.value = true
}

async function handleChangePassword() {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  passwordSaving.value = true
  try {
    await changePassword(passwordForm)
    ElMessage.success('Password changed. Please sign in again.')
    passwordDialogVisible.value = false
    userStore.clearLogin()
    await router.replace('/login')
  } finally {
    passwordSaving.value = false
  }
}

async function handleLogout() {
  try {
    await logoutApi()
  } catch {
    // Logout must still clear local credentials even if the basic backend endpoint is unavailable.
  } finally {
    userStore.logout()
    await router.replace('/login')
  }
}
</script>

<template>
  <el-container class="layout">
    <el-aside class="layout-aside" width="224px">
      <div class="brand">
        <div class="brand-title">CESD</div>
        <div class="brand-subtitle">Evaluation System</div>
      </div>
      <el-menu :default-active="route.path" router class="side-menu" data-testid="side-menu">
        <el-menu-item
          v-for="item in menuItems"
          :key="item.path"
          :index="item.path"
          :data-testid="getMenuTestId(item.path)"
          :data-menu-scope="getMenuScope(item.path)"
        >
          {{ item.title }}
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="layout-header">
        <h1>{{ pageTitle }}</h1>
        <div class="identity-panel" data-testid="current-user-panel">
          <span class="user-name" data-testid="current-user-name">{{ userStore.displayName }}</span>
          <el-tag size="small" type="info" data-testid="current-user-role">{{
            userStore.displayRole
          }}</el-tag>
          <el-button size="small" data-testid="change-password-entry" @click="openPasswordDialog">
            修改密码
          </el-button>
          <el-button size="small" data-testid="logout-button" @click="handleLogout">
            退出登录
          </el-button>
        </div>
      </el-header>
      <el-main class="layout-main"><RouterView /></el-main>
    </el-container>
  </el-container>

  <el-dialog
    v-model="passwordDialogVisible"
    title="修改密码"
    width="460px"
    data-testid="change-password-dialog"
    @closed="resetPasswordForm"
  >
    <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="96px">
      <el-form-item label="旧密码" prop="oldPassword">
        <el-input
          v-model="passwordForm.oldPassword"
          type="password"
          show-password
          autocomplete="current-password"
          data-testid="old-password-input"
        />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="passwordForm.newPassword"
          type="password"
          show-password
          autocomplete="new-password"
          data-testid="new-password-input"
        />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
          v-model="passwordForm.confirmPassword"
          type="password"
          show-password
          autocomplete="new-password"
          data-testid="confirm-password-input"
          @keyup.enter="handleChangePassword"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="passwordDialogVisible = false">取消</el-button>
      <el-button
        type="primary"
        :loading="passwordSaving"
        data-testid="change-password-confirm-button"
        @click="handleChangePassword"
      >
        确认修改
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.layout {
  min-height: 100vh;
}
.layout-aside {
  border-right: 1px solid #e5e7eb;
  background: #fff;
}
.brand {
  height: 72px;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
}
.brand-title {
  font-size: 20px;
  font-weight: 700;
  color: #111827;
}
.brand-subtitle {
  margin-top: 4px;
  color: #6b7280;
  font-size: 12px;
}
.side-menu {
  border-right: 0;
}
.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 24px;
  border-bottom: 1px solid #e5e7eb;
  background: #fff;
}
.layout-header h1 {
  margin: 0;
  font-size: 18px;
  font-weight: 650;
  color: #111827;
}
.identity-panel {
  display: flex;
  align-items: center;
  gap: 10px;
}
.user-name {
  color: #111827;
  font-size: 14px;
  font-weight: 600;
}
.layout-main {
  padding: 20px;
  background: #f5f7fb;
}
</style>
