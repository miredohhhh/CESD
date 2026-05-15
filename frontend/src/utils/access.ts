import type { RouteLocationNormalized, RouteRecordNormalized, RouteRecordRaw } from 'vue-router'
import type { useUserStore } from '@/stores/user'

type UserStore = ReturnType<typeof useUserStore>

type RouteLike = RouteRecordNormalized | RouteRecordRaw

export function normalizeRole(roleCode?: string) {
  return String(roleCode || '').toUpperCase()
}

export function isAdminRole(roleCode?: string) {
  return normalizeRole(roleCode) === 'ADMIN'
}

export function isAuditRole(roleCode?: string) {
  const role = normalizeRole(roleCode)
  return role === 'AUDITOR' || role === 'REVIEWER'
}

export function getDefaultPath(roleCode?: string) {
  const role = normalizeRole(roleCode)
  if (role === 'ADMIN') {
    return '/admin/scores'
  }
  if (isAuditRole(role)) {
    return '/audit/pending'
  }
  return '/student/applications'
}

function roleMatches(expectedRole: string, actualRole?: string) {
  const expected = normalizeRole(expectedRole)
  const actual = normalizeRole(actualRole)
  return expected === actual || (expected === 'AUDITOR' && actual === 'REVIEWER')
}

function collectRoles(records: RouteLike[]) {
  return records.flatMap((record) => {
    const roles = record.meta?.roles
    return Array.isArray(roles) ? roles.map(String) : []
  })
}

function collectPermissionCodes(records: RouteLike[]) {
  const codes: string[] = []
  records.forEach((record) => {
    const singleCode = record.meta?.permissionCode
    if (typeof singleCode === 'string' && singleCode) {
      codes.push(singleCode)
    }
    const multiCodes = record.meta?.permissionCodes
    if (Array.isArray(multiCodes)) {
      multiCodes.forEach((code) => {
        if (typeof code === 'string' && code) {
          codes.push(code)
        }
      })
    }
  })
  return codes
}

export function canAccessRouteRecords(records: RouteLike[], userStore: UserStore) {
  const requiresAuth = records.some((record) => Boolean(record.meta?.requiresAuth))
  if (requiresAuth && !userStore.isLoggedIn) {
    return false
  }

  const roles = collectRoles(records)
  if (roles.length && !roles.some((role) => roleMatches(role, userStore.roleCode))) {
    return false
  }

  const permissionCodes = collectPermissionCodes(records)
  if (permissionCodes.length && !permissionCodes.every((code) => userStore.hasPermission(code))) {
    return false
  }

  return true
}

export function canAccessRouteLocation(to: RouteLocationNormalized, userStore: UserStore) {
  return canAccessRouteRecords(to.matched, userStore)
}

export function canShowMenuRoute(route: RouteRecordNormalized, userStore: UserStore) {
  if (route.meta?.hidden === true || route.meta?.showInMenu !== true) {
    return false
  }
  if (!userStore.isLoggedIn) {
    return false
  }
  return canAccessRouteRecords([route], userStore)
}
