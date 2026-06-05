import request from '@/api/request'
import type { PageResult } from '@/types/common'
import type { MaterialStatus } from '@/utils/status'

export interface MyMaterialApplicationPageParams {
  pageNo?: number
  pageSize?: number
  status?: MaterialStatus | ''
  itemId?: number
  categoryId?: number
  keyword?: string
  startTime?: string
  endTime?: string
}

export interface PendingMaterialApplicationPageParams {
  pageNo?: number
  pageSize?: number
  status?: MaterialStatus
  studentId?: number
  studentNo?: string
  studentName?: string
  classId?: number
  majorId?: number
  itemId?: number
  categoryId?: number
  keyword?: string
  startTime?: string
  endTime?: string
}

export interface MyMaterialApplicationVO {
  id: number
  studentId: number
  itemId: number
  itemName?: string
  categoryId?: number
  categoryName?: string
  title: string
  description?: string
  applyScore?: number
  finalScore?: number
  status: MaterialStatus
  submitTime?: string
  reviewTime?: string
  rejectReason?: string
  submitCount?: number
  createTime?: string
  updateTime?: string
  attachmentCount?: number
}

export interface MyApplicationStatisticsVO {
  studentId: number
  totalCount: number
  draftCount: number
  submittedCount: number
  approvedCount: number
  rejectedCount: number
  cancelledCount: number
}

export interface MaterialAttachmentVO {
  id: number
  materialId: number
  originalName: string
  storedName: string
  filePath: string
  fileUrl?: string
  fileType?: string
  fileSize?: number
  uploadTime?: string
  createTime?: string
  updateTime?: string
}

export interface ReviewRecordVO {
  id: number
  materialId: number
  reviewerId: number
  beforeStatus: MaterialStatus
  afterStatus: MaterialStatus
  reviewResult: 'APPROVED' | 'REJECTED'
  reviewScore?: number
  reviewComment?: string
  reviewTime?: string
  createTime?: string
}

export interface MaterialApplicationDetailVO extends MyMaterialApplicationVO {
  studentNo?: string
  studentName?: string
  classId?: number
  className?: string
  majorId?: number
  majorName?: string
  attachments: MaterialAttachmentVO[]
  reviewRecords: ReviewRecordVO[]
}

export interface PendingMaterialApplicationVO {
  id: number
  studentId: number
  studentNo?: string
  studentName?: string
  classId?: number
  className?: string
  majorId?: number
  majorName?: string
  itemId: number
  itemName?: string
  categoryId?: number
  categoryName?: string
  title: string
  applyScore?: number
  status: MaterialStatus
  submitTime?: string
  attachmentCount?: number
  createTime?: string
  updateTime?: string
}

export interface ScoreCategorySummaryVO {
  id: number
  studentId: number
  categoryId: number
  categoryName?: string
  categoryCode?: string
  categoryScore: number
  calculateTime?: string
  createTime?: string
  updateTime?: string
}

export interface FrontendScoreSummaryVO {
  studentId: number
  studentNo?: string
  studentName?: string
  classId?: number
  className?: string
  majorId?: number
  majorName?: string
  totalScore?: number
  classRank?: number
  majorRank?: number
  calculateTime?: string
  status?: number
  categoryScores?: ScoreCategorySummaryVO[]
}

export function getMyApplicationsPage(params: MyMaterialApplicationPageParams) {
  return request.get<PageResult<MyMaterialApplicationVO>>('/frontend/my-applications/page', {
    params,
  })
}

export function getMyApplicationStatistics() {
  return request.get<MyApplicationStatisticsVO>('/frontend/my-applications/statistics')
}

export function getApplicationDetail(id: number) {
  return request.get<MaterialApplicationDetailVO>(`/frontend/applications/${id}/detail`)
}

export function getPendingApplicationsPage(params: PendingMaterialApplicationPageParams) {
  return request.get<PageResult<PendingMaterialApplicationVO>>('/frontend/audit/pending/page', {
    params,
  })
}

export function exportMaterialApplications(
  params: Omit<PendingMaterialApplicationPageParams, 'pageNo' | 'pageSize'>,
) {
  return request.downloadResponse('/material-applications/export', {
    params,
  })
}

export function getMyScore() {
  return request.get<FrontendScoreSummaryVO>('/frontend/my-score')
}

export function getMyScoreCategories() {
  return request.get<ScoreCategorySummaryVO[]>('/frontend/my-score/categories')
}

export function getMyCategoryScores() {
  return getMyScoreCategories()
}
