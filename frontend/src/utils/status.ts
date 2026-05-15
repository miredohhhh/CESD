import type { TagProps } from 'element-plus'

export type MaterialStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED' | 'CANCELLED'

export const materialStatusText: Record<MaterialStatus, string> = {
  DRAFT: '草稿',
  SUBMITTED: '待审核',
  APPROVED: '已通过',
  REJECTED: '已驳回',
  CANCELLED: '已撤回',
}

const materialStatusTagType: Record<MaterialStatus, TagProps['type']> = {
  DRAFT: 'info',
  SUBMITTED: 'warning',
  APPROVED: 'success',
  REJECTED: 'danger',
  CANCELLED: 'info',
}

export function getMaterialStatusText(status: string) {
  return materialStatusText[status as MaterialStatus] || status || '-'
}

export function getMaterialStatusTagType(status: string): TagProps['type'] {
  return materialStatusTagType[status as MaterialStatus] || 'info'
}
