import request from '@/api/request'
import type { PageResult } from '@/types/common'

export interface ScoreSummaryVO {
  id: number
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
  createTime?: string
  updateTime?: string
}

export interface ScoreSummaryPageParams {
  pageNum?: number
  pageSize?: number
  studentId?: number
  studentNo?: string
  studentName?: string
  classId?: number
  majorId?: number
  status?: number
}

export function recalculateStudentScore(studentId: number) {
  return request.post<ScoreSummaryVO>(`/scores/students/${studentId}/recalculate`)
}

export function recalculateClassScores(classId: number) {
  return request.post<ScoreSummaryVO[]>(`/scores/classes/${classId}/recalculate`)
}

export function recalculateMajorScores(majorId: number) {
  return request.post<ScoreSummaryVO[]>(`/scores/majors/${majorId}/recalculate`)
}

export function recalculateAllScores() {
  return request.post<void>('/scores/recalculate-all')
}

export function getScoreSummariesPage(params: ScoreSummaryPageParams) {
  return request.get<PageResult<ScoreSummaryVO>>('/scores/page', { params })
}

export function exportScoreSummaries(params: ScoreSummaryPageParams = {}) {
  return request.downloadResponse('/scores/export', { params })
}

export function exportClassRanking(classId: number) {
  return request.downloadResponse(`/scores/classes/${classId}/ranking/export`)
}

export function exportMajorRanking(majorId: number) {
  return request.downloadResponse(`/scores/majors/${majorId}/ranking/export`)
}
