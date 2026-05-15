import { defineStore } from 'pinia'
import {
  getCurrentUserPermissions,
  type CurrentUserPermissionVO,
  type LoginUserVO,
  type LoginVO,
  type MenuPermissionVO,
} from '@/api/auth'

export type UserRole = 'STUDENT' | 'AUDITOR' | 'ADMIN'

const STORAGE_KEY = 'cesd_user_session'

interface PersistedUserState {
  token: string
  userId: number | null
  username: string
  realName: string
  roleId: number | null
  roleCode: UserRole | string
  roleName: string
  studentId: number | null
  reviewerId: number | null
  permissionCodes: string[]
  menus: MenuPermissionVO[]
  buttons: string[]
}

interface UserState extends PersistedUserState {
  initialized: boolean
  sessionChecked: boolean
}

function createEmptyState(): PersistedUserState {
  return {
    token: '',
    userId: null,
    username: '',
    realName: '',
    roleId: null,
    roleCode: '',
    roleName: '',
    studentId: null,
    reviewerId: null,
    permissionCodes: [],
    menus: [],
    buttons: [],
  }
}

function isAdminRole(roleCode?: string) {
  return String(roleCode || '').toUpperCase() === 'ADMIN'
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    ...createEmptyState(),
    initialized: false,
    sessionChecked: false,
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    role: (state) => state.roleCode,
    displayName: (state) => state.realName || state.username || '未登录',
    displayRole: (state) => state.roleName || state.roleCode || '未认证',
    hasPermission: (state) => (code: string) => {
      if (isAdminRole(state.roleCode)) {
        return true
      }
      return (
        state.permissionCodes.includes('*') ||
        state.permissionCodes.includes(code) ||
        state.buttons.includes(code)
      )
    },
    hasAnyPermission: (state) => (codes: string[]) => {
      if (isAdminRole(state.roleCode)) {
        return true
      }
      return codes.some(
        (code) =>
          state.permissionCodes.includes('*') ||
          state.permissionCodes.includes(code) ||
          state.buttons.includes(code),
      )
    },
  },
  actions: {
    setLoginResult(loginResult: LoginVO) {
      this.token = loginResult.token
      this.applyUser(loginResult.user)
      this.initialized = true
      this.sessionChecked = false
      this.persist()
    },
    setCurrentUser(user: LoginUserVO) {
      this.applyUser(user)
      this.initialized = true
      this.sessionChecked = true
      this.persist()
    },
    setPermissions(data: CurrentUserPermissionVO) {
      this.permissionCodes = data.permissionCodes || []
      this.menus = data.menus || []
      this.buttons = data.buttons || []
      this.persist()
    },
    async refreshPermissions() {
      if (!this.token) {
        return
      }
      const data = await getCurrentUserPermissions()
      this.setPermissions(data)
    },
    clearLogin() {
      Object.assign(this, createEmptyState())
      this.initialized = true
      this.sessionChecked = true
      localStorage.removeItem(STORAGE_KEY)
    },
    logout() {
      this.clearLogin()
    },
    initFromStorage() {
      if (this.initialized) {
        return
      }
      const raw = localStorage.getItem(STORAGE_KEY)
      if (!raw) {
        this.initialized = true
        return
      }
      try {
        const data = JSON.parse(raw) as PersistedUserState
        this.token = data.token || ''
        this.userId = data.userId ?? null
        this.username = data.username || ''
        this.realName = data.realName || ''
        this.roleId = data.roleId ?? null
        this.roleCode = data.roleCode || ''
        this.roleName = data.roleName || ''
        this.studentId = data.studentId ?? null
        this.reviewerId = data.reviewerId ?? null
        this.permissionCodes = data.permissionCodes || []
        this.menus = data.menus || []
        this.buttons = data.buttons || []
      } catch {
        localStorage.removeItem(STORAGE_KEY)
      } finally {
        this.initialized = true
        this.sessionChecked = false
      }
    },
    persist() {
      const data: PersistedUserState = {
        token: this.token,
        userId: this.userId,
        username: this.username,
        realName: this.realName,
        roleId: this.roleId,
        roleCode: this.roleCode,
        roleName: this.roleName,
        studentId: this.studentId,
        reviewerId: this.reviewerId,
        permissionCodes: this.permissionCodes,
        menus: this.menus,
        buttons: this.buttons,
      }
      localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
    },
    setRole(role: UserRole) {
      this.roleCode = role
      this.persist()
    },
    setStudentId(studentId: number) {
      this.studentId = studentId
      this.persist()
    },
    setReviewerId(reviewerId: number) {
      this.reviewerId = reviewerId
      this.persist()
    },
    applyUser(user: LoginUserVO) {
      this.userId = user.userId
      this.username = user.username
      this.realName = user.realName || ''
      this.roleId = user.roleId ?? null
      this.roleCode = user.roleCode || ''
      this.roleName = user.roleName || ''
      this.studentId = user.studentId ?? null
      const roleCode = String(user.roleCode || '').toUpperCase()
      this.reviewerId =
        roleCode === 'AUDITOR' || roleCode === 'REVIEWER' || roleCode === 'ADMIN'
          ? user.userId
          : null
    },
  },
})
