import request from '@/api/request'

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginUserVO {
  userId: number
  username: string
  realName?: string
  roleId?: number
  roleCode?: string
  roleName?: string
  studentId?: number | null
}

export interface LoginVO {
  token: string
  tokenType: string
  expiresIn: number
  user: LoginUserVO
}

export interface MenuPermissionVO {
  id: number
  permissionName: string
  permissionCode: string
  routePath?: string
  componentPath?: string
  icon?: string
  sortOrder?: number
  children?: MenuPermissionVO[]
}

export interface CurrentUserPermissionVO {
  userId: number
  roleCode: string
  permissionCodes: string[]
  menus: MenuPermissionVO[]
  buttons: string[]
}

export interface ChangePasswordPayload {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

export function login(data: LoginRequest) {
  return request.post<LoginVO, LoginRequest>('/auth/login', data)
}

export function getCurrentUser() {
  return request.get<LoginUserVO>('/auth/me')
}

export function getCurrentUserPermissions() {
  return request.get<CurrentUserPermissionVO>('/auth/permissions')
}

export function changePassword(data: ChangePasswordPayload) {
  return request.put<void, ChangePasswordPayload>('/auth/password', data)
}

export function logout() {
  return request.post<void>('/auth/logout')
}
