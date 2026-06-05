import request from '@/api/request'
import type { PageResult } from '@/types/common'

export interface StudentPageParams {
  pageNum?: number
  pageSize?: number
  keyword?: string
  status?: number
  grade?: string
  majorId?: number
  classId?: number
}

export interface ClassInfoPageParams {
  pageNum?: number
  pageSize?: number
  keyword?: string
  status?: number
}

export interface MajorInfoPageParams {
  pageNum?: number
  pageSize?: number
  keyword?: string
  status?: number
}

export interface SysUserPageParams {
  pageNum?: number
  pageSize?: number
  keyword?: string
  status?: number
  roleId?: number
}

export interface SysRolePageParams {
  pageNum?: number
  pageSize?: number
  keyword?: string
  status?: number
}

export interface SystemConfigPageParams {
  pageNum?: number
  pageSize?: number
  keyword?: string
  status?: number
}

export interface OperationLogPageParams {
  pageNum?: number
  pageSize?: number
  username?: string
  operationModule?: string
  operationType?: string
  result?: string
  startTime?: string
  endTime?: string
}

export interface LoginLogPageParams {
  pageNum?: number
  pageSize?: number
  username?: string
  loginType?: string
  result?: string
  startTime?: string
  endTime?: string
}

export interface StudentVO {
  id: number
  userId: number
  studentNo: string
  name: string
  gender?: string
  grade: string
  majorId: number
  classId: number
  phone?: string
  email?: string
  status: number
  createTime?: string
  updateTime?: string
}

export interface ClassInfoVO {
  id: number
  className: string
  classCode?: string
  majorId: number
  grade: string
  counselorName?: string
  status: number
  createTime?: string
  updateTime?: string
}

export interface MajorInfoVO {
  id: number
  majorName: string
  majorCode?: string
  collegeName: string
  description?: string
  status: number
  createTime?: string
  updateTime?: string
}

export interface SysUserVO {
  id: number
  username: string
  realName: string
  roleId: number
  phone?: string
  email?: string
  avatar?: string
  status: number
  lastLoginTime?: string
  createTime?: string
  updateTime?: string
}

export interface SysRoleVO {
  id: number
  roleName: string
  roleCode: string
  description?: string
  status: number
  createTime?: string
  updateTime?: string
}

export interface SystemConfigVO {
  id: number
  configKey: string
  configValue: string
  description?: string
  status?: number
  createTime?: string
  updateTime?: string
}

export interface OperationLogVO {
  id: number
  userId?: number
  username?: string
  realName?: string
  roleCode?: string
  operationType: string
  operationModule: string
  operationDesc?: string
  requestMethod?: string
  requestUri?: string
  requestParams?: string
  ipAddress?: string
  userAgent?: string
  result: string
  errorMessage?: string
  operationTime?: string
  createTime?: string
}

export interface LoginLogVO {
  id: number
  userId?: number
  username?: string
  realName?: string
  roleCode?: string
  loginType: string
  result: string
  ipAddress?: string
  userAgent?: string
  errorMessage?: string
  loginTime?: string
  createTime?: string
}

export interface CreateMajorInfoPayload {
  majorName: string
  majorCode?: string
  collegeName: string
  description?: string
  status?: number
}

export interface UpdateMajorInfoPayload {
  majorName?: string
  majorCode?: string
  collegeName?: string
  description?: string
  status?: number
}

export interface CreateClassInfoPayload {
  className: string
  classCode?: string
  majorId: number
  grade: string
  counselorName?: string
  status?: number
}

export interface UpdateClassInfoPayload {
  className?: string
  classCode?: string
  majorId?: number
  grade?: string
  counselorName?: string
  status?: number
}

export interface CreateStudentPayload {
  userId: number
  studentNo: string
  name: string
  gender?: string
  grade: string
  majorId: number
  classId: number
  phone?: string
  email?: string
  status: number
}

export interface UpdateStudentPayload {
  userId?: number
  studentNo?: string
  name?: string
  gender?: string
  grade?: string
  majorId?: number
  classId?: number
  phone?: string
  email?: string
  status?: number
}

export interface CreateSysUserPayload {
  username: string
  passwordHash: string
  realName: string
  roleId: number
  phone?: string
  email?: string
  avatar?: string
  status: number
}

export interface UpdateSysUserPayload {
  username?: string
  passwordHash?: string
  realName?: string
  roleId?: number
  phone?: string
  email?: string
  avatar?: string
  status?: number
}

export interface ResetUserPasswordPayload {
  newPassword: string
}

export interface CreateSystemConfigPayload {
  configKey: string
  configValue: string
  description?: string
  status?: number
}

export interface UpdateSystemConfigPayload {
  configKey: string
  configValue: string
  description?: string
  status?: number
}

export function getStudentsPage(params: StudentPageParams) {
  return request.get<PageResult<StudentVO>>('/students/page', { params })
}

export function getStudentDetail(id: number) {
  return request.get<StudentVO>(`/students/${id}`)
}

export function createStudent(data: CreateStudentPayload) {
  return request.post<StudentVO>('/students', data)
}

export function updateStudent(id: number, data: UpdateStudentPayload) {
  return request.put<StudentVO>(`/students/${id}`, data)
}

export function deleteStudent(id: number) {
  return request.delete<void>(`/students/${id}`)
}

export function getClassesPage(params: ClassInfoPageParams) {
  return request.get<PageResult<ClassInfoVO>>('/classes/page', { params })
}

export function getClassDetail(id: number) {
  return request.get<ClassInfoVO>(`/classes/${id}`)
}

export function createClassInfo(data: CreateClassInfoPayload) {
  return request.post<ClassInfoVO>('/classes', data)
}

export function updateClassInfo(id: number, data: UpdateClassInfoPayload) {
  return request.put<ClassInfoVO>(`/classes/${id}`, data)
}

export function deleteClassInfo(id: number) {
  return request.delete<void>(`/classes/${id}`)
}

export function getMajorsPage(params: MajorInfoPageParams) {
  return request.get<PageResult<MajorInfoVO>>('/majors/page', { params })
}

export function getMajorDetail(id: number) {
  return request.get<MajorInfoVO>(`/majors/${id}`)
}

export function createMajorInfo(data: CreateMajorInfoPayload) {
  return request.post<MajorInfoVO>('/majors', data)
}

export function updateMajorInfo(id: number, data: UpdateMajorInfoPayload) {
  return request.put<MajorInfoVO>(`/majors/${id}`, data)
}

export function deleteMajorInfo(id: number) {
  return request.delete<void>(`/majors/${id}`)
}

export function getUsersPage(params: SysUserPageParams) {
  return request.get<PageResult<SysUserVO>>('/users/page', { params })
}

export function getUserDetail(id: number) {
  return request.get<SysUserVO>(`/users/${id}`)
}

export function createUser(data: CreateSysUserPayload) {
  return request.post<SysUserVO, CreateSysUserPayload>('/users', data)
}

export function updateUser(id: number, data: UpdateSysUserPayload) {
  return request.put<SysUserVO, UpdateSysUserPayload>(`/users/${id}`, data)
}

export function deleteUser(id: number) {
  return request.delete<void>(`/users/${id}`)
}

export function resetUserPassword(id: number, data: ResetUserPasswordPayload) {
  return request.put<SysUserVO, ResetUserPasswordPayload>(`/users/${id}/password/reset`, data)
}

export function getRolesPage(params: SysRolePageParams) {
  return request.get<PageResult<SysRoleVO>>('/roles/page', { params })
}

export function getSystemConfigsPage(params: SystemConfigPageParams) {
  return request.get<PageResult<SystemConfigVO>>('/system-configs/page', { params })
}

export function getSystemConfigDetail(id: number) {
  return request.get<SystemConfigVO>(`/system-configs/${id}`)
}

export function createSystemConfig(data: CreateSystemConfigPayload) {
  return request.post<SystemConfigVO, CreateSystemConfigPayload>('/system-configs', data)
}

export function updateSystemConfig(id: number, data: UpdateSystemConfigPayload) {
  return request.put<SystemConfigVO, UpdateSystemConfigPayload>(`/system-configs/${id}`, data)
}

export function deleteSystemConfig(id: number) {
  return request.delete<void>(`/system-configs/${id}`)
}

export function getOperationLogsPage(params: OperationLogPageParams) {
  return request.get<PageResult<OperationLogVO>>('/operation-logs/page', { params })
}

export function getOperationLogDetail(id: number) {
  return request.get<OperationLogVO>(`/operation-logs/${id}`)
}

export function getLoginLogsPage(params: LoginLogPageParams) {
  return request.get<PageResult<LoginLogVO>>('/login-logs/page', { params })
}

export function getLoginLogDetail(id: number) {
  return request.get<LoginLogVO>(`/login-logs/${id}`)
}
