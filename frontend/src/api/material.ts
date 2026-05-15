import request from '@/api/request'
import type { MyMaterialApplicationVO } from '@/api/frontend'

export type MaterialApplicationStatus =
  | 'DRAFT'
  | 'SUBMITTED'
  | 'APPROVED'
  | 'REJECTED'
  | 'CANCELLED'

export interface CreateMaterialApplicationPayload {
  itemId: number
  title: string
  description?: string
  applyScore?: number
  finalScore?: number
  status?: MaterialApplicationStatus
  rejectReason?: string
  submitCount?: number
}

export interface MaterialApplicationVO {
  id: number
  studentId: number
  itemId: number
  title: string
  description?: string
  applyScore?: number
  finalScore?: number
  status: MaterialApplicationStatus
  rejectReason?: string
  submitCount?: number
  submitTime?: string
  reviewTime?: string
  createTime?: string
  updateTime?: string
}

export interface WithdrawMaterialApplicationRequest {
  reason?: string
}

export interface ApproveMaterialApplicationPayload {
  reviewScore: number
  reviewComment?: string
}

export interface RejectMaterialApplicationPayload {
  rejectReason: string
  reviewComment?: string
}

export function createMaterialApplication(data: CreateMaterialApplicationPayload) {
  return request.post<MaterialApplicationVO, CreateMaterialApplicationPayload>(
    '/material-applications',
    data,
  )
}

export function approveMaterialApplication(id: number, data: ApproveMaterialApplicationPayload) {
  return request.post<MaterialApplicationVO, ApproveMaterialApplicationPayload>(
    `/material-applications/${id}/approve`,
    data,
  )
}

export function rejectMaterialApplication(id: number, data: RejectMaterialApplicationPayload) {
  return request.post<MaterialApplicationVO, RejectMaterialApplicationPayload>(
    `/material-applications/${id}/reject`,
    data,
  )
}

export function submitMaterialApplication(id: number) {
  return request.post<MyMaterialApplicationVO>(`/material-applications/${id}/submit`)
}

export function withdrawMaterialApplication(
  id: number,
  data: WithdrawMaterialApplicationRequest = {},
) {
  return request.post<MyMaterialApplicationVO, WithdrawMaterialApplicationRequest>(
    `/material-applications/${id}/withdraw`,
    data,
  )
}
