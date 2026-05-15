import request from '@/api/request'
import type { PageResult } from '@/types/common'

export interface EvaluationCategoryPageParams {
  pageNum?: number
  pageSize?: number
  keyword?: string
  status?: number
}

export interface EvaluationCategoryVO {
  id: number
  categoryName: string
  categoryCode?: string
  maxScore?: number
  sortNo?: number
  description?: string
  status?: number
  createTime?: string
  updateTime?: string
}

export interface CreateEvaluationCategoryPayload {
  categoryName: string
  categoryCode?: string
  maxScore?: number
  sortNo?: number
  description?: string
  status?: number
}

export type UpdateEvaluationCategoryPayload = CreateEvaluationCategoryPayload

export interface EvaluationItemPageParams {
  pageNum?: number
  pageSize?: number
  keyword?: string
  status?: number
}

export interface EvaluationItemVO {
  id: number
  categoryId: number
  itemName: string
  itemCode?: string
  scoreType: string
  score?: number
  maxScore?: number
  needAttachment: number
  description?: string
  sortNo?: number
  status?: number
  createTime?: string
  updateTime?: string
}

export interface CreateEvaluationItemPayload {
  categoryId: number
  itemName: string
  itemCode?: string
  scoreType: string
  score?: number
  maxScore?: number
  needAttachment?: number
  description?: string
  sortNo?: number
  status?: number
}

export type UpdateEvaluationItemPayload = CreateEvaluationItemPayload

export function getEvaluationCategoriesPage(params: EvaluationCategoryPageParams) {
  return request.get<PageResult<EvaluationCategoryVO>>('/evaluation-categories/page', { params })
}

export function getEvaluationCategoryDetail(id: number) {
  return request.get<EvaluationCategoryVO>(`/evaluation-categories/${id}`)
}

export function createEvaluationCategory(data: CreateEvaluationCategoryPayload) {
  return request.post<EvaluationCategoryVO, CreateEvaluationCategoryPayload>(
    '/evaluation-categories',
    data,
  )
}

export function updateEvaluationCategory(id: number, data: UpdateEvaluationCategoryPayload) {
  return request.put<EvaluationCategoryVO, UpdateEvaluationCategoryPayload>(
    `/evaluation-categories/${id}`,
    data,
  )
}

export function deleteEvaluationCategory(id: number) {
  return request.delete<void>(`/evaluation-categories/${id}`)
}

export function getEvaluationItemsPage(params: EvaluationItemPageParams) {
  return request.get<PageResult<EvaluationItemVO>>('/evaluation-items/page', { params })
}

export function getEvaluationItemDetail(id: number) {
  return request.get<EvaluationItemVO>(`/evaluation-items/${id}`)
}

export function createEvaluationItem(data: CreateEvaluationItemPayload) {
  return request.post<EvaluationItemVO, CreateEvaluationItemPayload>('/evaluation-items', data)
}

export function updateEvaluationItem(id: number, data: UpdateEvaluationItemPayload) {
  return request.put<EvaluationItemVO, UpdateEvaluationItemPayload>(`/evaluation-items/${id}`, data)
}

export function deleteEvaluationItem(id: number) {
  return request.delete<void>(`/evaluation-items/${id}`)
}
