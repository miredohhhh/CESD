import request from '@/api/request'
import type { PageResult } from '@/types/common'
import type { MenuPermissionVO } from '@/api/auth'

export type PermissionType = 'MENU' | 'BUTTON' | 'API'

export interface SysPermissionPageParams {
  pageNum?: number
  pageNo?: number
  pageSize?: number
  keyword?: string
  permissionType?: PermissionType | ''
  status?: number
  parentId?: number
}

export interface SysPermissionVO {
  id: number
  permissionName: string
  permissionCode: string
  permissionType: PermissionType
  parentId?: number | null
  routePath?: string
  componentPath?: string
  apiPath?: string
  httpMethod?: string
  icon?: string
  sortOrder?: number
  status: number
  remark?: string
  createTime?: string
  updateTime?: string
  children?: SysPermissionVO[]
}

export interface SaveSysPermissionPayload {
  permissionName: string
  permissionCode: string
  permissionType: PermissionType
  parentId?: number | null
  routePath?: string
  componentPath?: string
  apiPath?: string
  httpMethod?: string
  icon?: string
  sortOrder?: number
  status?: number
  remark?: string
}

export interface RolePermissionVO {
  roleId: number
  permissionIds: number[]
  permissionCodes: string[]
}

export interface AssignRolePermissionsPayload {
  permissionIds: number[]
}

export function getPermissionsPage(params: SysPermissionPageParams) {
  return request.get<PageResult<SysPermissionVO>>('/permissions/page', { params })
}

export function getPermissionTree() {
  return request.get<SysPermissionVO[]>('/permissions/tree')
}

export function createPermission(data: SaveSysPermissionPayload) {
  return request.post<SysPermissionVO, SaveSysPermissionPayload>('/permissions', data)
}

export function updatePermission(id: number, data: SaveSysPermissionPayload) {
  return request.put<SysPermissionVO, SaveSysPermissionPayload>(`/permissions/${id}`, data)
}

export function deletePermission(id: number) {
  return request.delete<void>(`/permissions/${id}`)
}

export function getRolePermissions(roleId: number) {
  return request.get<RolePermissionVO>(`/roles/${roleId}/permissions`)
}

export function assignRolePermissions(roleId: number, permissionIds: number[]) {
  return request.put<RolePermissionVO, AssignRolePermissionsPayload>(
    `/roles/${roleId}/permissions`,
    {
      permissionIds,
    },
  )
}

export function flattenMenus(menus: MenuPermissionVO[]) {
  const result: MenuPermissionVO[] = []
  const visit = (items: MenuPermissionVO[]) => {
    items.forEach((item) => {
      if (item.routePath) {
        result.push(item)
      }
      if (item.children?.length) {
        visit(item.children)
      }
    })
  }
  visit(menus)
  return result
}
