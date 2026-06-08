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
const isSidebarCollapsed = ref(false)
const passwordDialogVisible = ref(false)
const passwordSaving = ref(false)
const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive<ChangePasswordPayload>({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const collapseTitle = computed(() => (isSidebarCollapsed.value ? '展开菜单' : '折叠菜单'))

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

function toggleSidebar() {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
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
  <div class="app-layout" :class="{ 'app-layout--collapsed': isSidebarCollapsed }">
    <header class="app-header">
      <div class="header-brand" aria-label="CESS 综合测评管理系统">
        <span class="brand-title">CESS</span>
        <span class="brand-name">综合测评管理系统</span>
      </div>

      <button
        class="header-collapse"
        type="button"
        :aria-label="collapseTitle"
        :title="collapseTitle"
        data-testid="sidebar-collapse-button"
        @click="toggleSidebar"
      >
        <span class="collapse-glyph" aria-hidden="true">{{ isSidebarCollapsed ? '›' : '‹' }}</span>
      </button>

      <div class="header-spacer"></div>

      <div class="identity-panel" data-testid="current-user-panel">
        <span class="user-name" data-testid="current-user-name" :title="userStore.displayName">
          {{ userStore.displayName }}
        </span>
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
    </header>

    <div class="app-body">
      <aside class="app-sidebar">
        <el-menu :default-active="route.path" router class="side-menu" data-testid="side-menu">
          <el-menu-item
            v-for="item in menuItems"
            :key="item.path"
            :index="item.path"
            :data-testid="getMenuTestId(item.path)"
            :data-menu-scope="getMenuScope(item.path)"
            :title="isSidebarCollapsed ? item.title : undefined"
          >
            <span class="menu-initial" aria-hidden="true">{{ item.title.slice(0, 1) }}</span>
            <span class="menu-label">{{ item.title }}</span>
          </el-menu-item>
        </el-menu>
      </aside>

      <main class="app-main"><RouterView /></main>
    </div>
  </div>

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
.app-layout {
  --current-sidebar-width: var(--app-sidebar-width);
  height: 100vh;
  min-height: 100vh;
  overflow: hidden;
  background: var(--app-page-bg);
}

.app-layout--collapsed {
  --current-sidebar-width: var(--app-sidebar-collapsed-width);
}

.app-header {
  display: flex;
  align-items: center;
  width: 100%;
  height: var(--app-header-height);
  border-bottom: 1px solid var(--app-border-light);
  background: var(--app-header-bg);
  box-shadow: var(--app-shadow-header);
  color: var(--app-text-color);
  z-index: 10;
}

.header-brand {
  display: flex;
  flex: 0 0 var(--current-sidebar-width);
  align-items: center;
  width: var(--current-sidebar-width);
  min-width: 0;
  height: 100%;
  gap: 10px;
  padding: 0 24px;
  overflow: hidden;
  white-space: nowrap;
  transition:
    width 0.2s ease,
    flex-basis 0.2s ease,
    padding 0.2s ease;
}

.brand-title {
  flex: 0 0 auto;
  color: var(--app-text-color);
  font-size: 22px;
  font-weight: 800;
  letter-spacing: 0.2px;
  line-height: 1;
}

.brand-name {
  min-width: 0;
  overflow: hidden;
  color: var(--app-text-secondary-color);
  font-size: 13px;
  font-weight: 600;
  text-overflow: ellipsis;
}

.app-layout--collapsed .header-brand {
  justify-content: center;
  padding: 0 10px;
}

.app-layout--collapsed .brand-name {
  display: none;
}

.header-collapse {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 1px solid var(--app-border-light);
  border-radius: 999px;
  background: var(--app-header-bg);
  color: #475569;
  cursor: pointer;
  transition:
    border-color 0.18s ease,
    color 0.18s ease,
    background 0.18s ease;
}

.header-collapse:hover {
  border-color: #d9d9d9;
  background: var(--app-menu-hover-bg);
  color: var(--app-primary-color);
}

.collapse-glyph {
  font-size: 24px;
  font-weight: 700;
  line-height: 1;
  transform: translateY(-1px);
}

.header-spacer {
  flex: 1 1 auto;
  min-width: 24px;
}

.identity-panel {
  display: flex;
  flex: 0 1 auto;
  align-items: center;
  min-width: 0;
  padding: 0 24px 0 16px;
  gap: 10px;
}

.user-name {
  max-width: 220px;
  overflow: hidden;
  color: var(--app-text-color);
  font-size: 14px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-body {
  display: flex;
  width: 100%;
  height: calc(100vh - var(--app-header-height));
  min-height: 0;
}

.app-sidebar {
  flex: 0 0 var(--current-sidebar-width);
  width: var(--current-sidebar-width);
  height: 100%;
  overflow: hidden;
  border-right: 1px solid var(--app-border-light);
  background: var(--app-sidebar-bg);
  transition:
    width 0.2s ease,
    flex-basis 0.2s ease;
}

.side-menu {
  height: 100%;
  padding: 12px 10px;
  overflow-y: auto;
  border-right: 0;
  background: transparent;
}

.side-menu :deep(.el-menu-item) {
  display: flex;
  align-items: center;
  height: 44px;
  margin: 4px 0;
  border-radius: var(--app-radius-small);
  color: #334155;
  font-weight: 500;
  line-height: 44px;
  transition:
    background 0.18s ease,
    color 0.18s ease;
}

.side-menu :deep(.el-menu-item:hover) {
  background: var(--app-menu-hover-bg);
  color: var(--app-primary-color);
}

.side-menu :deep(.el-menu-item.is-active) {
  background: var(--app-menu-active-bg);
  color: var(--app-primary-color);
  font-weight: 600;
}

.menu-label {
  min-width: 0;
  overflow: hidden;
  font-weight: 500;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.side-menu :deep(.el-menu-item.is-active) .menu-label {
  font-weight: 600;
}

.menu-initial {
  display: none;
}

.app-layout--collapsed .side-menu {
  padding: 12px 8px;
}

.app-layout--collapsed .side-menu :deep(.el-menu-item) {
  justify-content: center;
  padding: 0 !important;
}

.app-layout--collapsed .menu-label {
  display: none;
}

.app-layout--collapsed .menu-initial {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 700;
}

.app-main {
  flex: 1 1 auto;
  min-width: 0;
  height: 100%;
  padding: var(--app-content-padding);
  overflow: auto;
  background: var(--app-page-bg);
}

@media (max-width: 1366px) {
  .brand-name {
    display: none;
  }
}

@media (max-width: 900px) {
  .identity-panel {
    padding-right: 14px;
    gap: 8px;
  }

  .user-name {
    max-width: 120px;
  }
}
</style>
