<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter, type RouteRecordNormalized } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { flattenMenus } from '@/api/permission'
import { canShowMenuRoute } from '@/utils/access'

interface MenuItem {
  title: string
  path: string
  order: number
}

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const pageTitle = computed(() => String(route.meta.title || 'CESD'))

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
  if (backendMenuItems.value.length) {
    return backendMenuItems.value
  }
  return routeMenuItems.value
})

function handleLogout() {
  userStore.logout()
  void router.replace('/login')
}
</script>

<template>
  <el-container class="layout">
    <el-aside class="layout-aside" width="224px">
      <div class="brand">
        <div class="brand-title">CESD</div>
        <div class="brand-subtitle">Evaluation System</div>
      </div>
      <el-menu :default-active="route.path" router class="side-menu">
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          {{ item.title }}
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="layout-header">
        <h1>{{ pageTitle }}</h1>
        <div class="identity-panel">
          <span class="user-name">{{ userStore.displayName }}</span>
          <el-tag size="small" type="info">{{ userStore.displayRole }}</el-tag>
          <el-button size="small" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="layout-main"><RouterView /></el-main>
    </el-container>
  </el-container>
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
