<template>
  <section v-loading="loading" class="admin-page" data-testid="application-detail-page">
    <AdminPageHeader
      eyebrow="申报详情"
      title="申报详情"
      description="查看申报材料详情、附件、审核状态和审核记录，并根据当前状态进行提交或撤回操作。"
    >
      <template #actions>
        <el-button data-testid="application-detail-back-button" @click="goBack">返回</el-button>
        <el-button
          v-if="canSubmit"
          type="primary"
          data-testid="application-detail-submit-button"
          :loading="operating"
          @click="handleSubmitApplication"
        >
          提交申报
        </el-button>
        <el-button
          v-if="canWithdraw"
          type="warning"
          data-testid="application-detail-withdraw-button"
          :loading="operating"
          @click="handleWithdrawApplication"
        >
          撤回申报
        </el-button>
      </template>
    </AdminPageHeader>

    <el-alert v-if="loadError" :title="loadError" type="error" show-icon :closable="false" />
    <el-empty v-if="!loading && !detail" description="暂无申报详情" />

    <template v-if="detail">
      <el-card class="application-status-card" shadow="never">
        <div class="application-status-card__main">
          <div>
            <div class="application-status-card__label">当前状态</div>
            <div class="application-status-card__title">
              <el-tag :type="getMaterialStatusTagType(detail.status)" size="large">
                {{ getMaterialStatusText(detail.status) }}
              </el-tag>
              <span>{{ statusSummary }}</span>
            </div>
          </div>
          <div class="application-score-box">
            <span>申请分</span>
            <strong>{{ formatScore(detail.applyScore) }}</strong>
          </div>
          <div class="application-score-box application-score-box--final">
            <span>认定分</span>
            <strong>{{ formatScore(detail.finalScore) }}</strong>
          </div>
        </div>
        <el-alert
          v-if="statusNotice"
          class="application-status-card__notice"
          :title="statusNotice"
          :type="statusNoticeType"
          show-icon
          :closable="false"
        />
      </el-card>

      <el-card v-if="canViewAuditPanel" class="admin-table-card audit-action-card" shadow="never">
        <div class="admin-card-header">
          <div>
            <div class="admin-card-header__title">审核处理</div>
            <div class="admin-card-header__meta">
              核对学生信息、申报说明和附件材料后，再进行审核处理
            </div>
          </div>
        </div>
        <el-alert
          class="detail-section-alert"
          :title="auditNotice"
          :type="auditNoticeType"
          show-icon
          :closable="false"
        />
        <div class="audit-action-summary">
          <div class="audit-metric">
            <span>申请分数</span>
            <strong>{{ formatScore(detail.applyScore) }}</strong>
          </div>
          <div class="audit-metric">
            <span>附件数量</span>
            <strong>{{ detail.attachments?.length ?? 0 }}</strong>
          </div>
          <div class="audit-metric">
            <span>提交时间</span>
            <strong>{{ formatDateTime(detail.submitTime) }}</strong>
          </div>
        </div>
        <div class="audit-action-buttons">
          <el-button data-testid="application-detail-audit-back-button" @click="goBack">
            返回待审核列表
          </el-button>
          <el-button
            v-if="canApprove"
            type="success"
            data-testid="application-detail-approve-open-button"
            :loading="operating"
            @click="openApproveDialog"
          >
            审核通过
          </el-button>
          <el-button
            v-if="canReject"
            type="danger"
            data-testid="application-detail-reject-open-button"
            :loading="operating"
            @click="openRejectDialog"
          >
            审核驳回
          </el-button>
        </div>
      </el-card>

      <el-card class="admin-table-card" shadow="never">
        <div class="admin-card-header">
          <div>
            <div class="admin-card-header__title">材料基本信息</div>
            <div class="admin-card-header__meta">申报标题、说明、提交和审核时间</div>
          </div>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="标题">{{ detail.title }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getMaterialStatusTagType(detail.status)">
              {{ getMaterialStatusText(detail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">{{
            formatDateTime(detail.submitTime)
          }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{
            formatDateTime(detail.reviewTime)
          }}</el-descriptions-item>
          <el-descriptions-item v-if="detail.rejectReason" label="驳回原因" :span="2">
            <span class="application-reject-reason">{{ detail.rejectReason }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="说明" :span="2">
            {{ detail.description || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card class="admin-table-card" shadow="never">
        <div class="admin-card-header">
          <div>
            <div class="admin-card-header__title">学生与项目信息</div>
            <div class="admin-card-header__meta">学生身份、专业班级和综测项目归属</div>
          </div>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="学生">{{ detail.studentName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ detail.studentNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="专业">{{ detail.majorName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ detail.className || '-' }}</el-descriptions-item>
          <el-descriptions-item label="综测分类">{{
            detail.categoryName || '-'
          }}</el-descriptions-item>
          <el-descriptions-item label="综测项目">{{ detail.itemName || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card class="admin-table-card" shadow="never">
        <div class="admin-card-header">
          <div>
            <div class="admin-card-header__title">附件信息</div>
            <div class="admin-card-header__meta">
              {{
                canViewAuditPanel
                  ? '审核时请重点核对附件名称、大小和上传时间'
                  : '展示已上传附件，草稿、驳回和撤回状态可继续维护'
              }}
            </div>
          </div>
          <el-upload
            v-if="canManageAttachments"
            data-testid="application-attachment-upload"
            :show-file-list="false"
            :http-request="handleUploadAttachment"
            :disabled="uploading"
          >
            <el-button
              type="primary"
              :loading="uploading"
              data-testid="application-attachment-upload-button"
            >
              上传附件
            </el-button>
          </el-upload>
        </div>
        <el-alert
          v-if="canManageAttachments"
          class="detail-section-alert"
          title="当前材料可继续上传或删除附件；提交后附件将进入只读状态。"
          type="info"
          show-icon
          :closable="false"
        />
        <el-table
          :data="detail.attachments || []"
          border
          stripe
          empty-text="暂无附件，请在提交审核前按要求上传证明材料。"
          data-testid="application-attachments-table"
        >
          <el-table-column prop="originalName" label="原始文件名" min-width="220" />
          <el-table-column prop="fileType" label="类型" width="110" />
          <el-table-column label="大小" width="120">
            <template #default="{ row }">{{ formatFileSize(row.fileSize) }}</template>
          </el-table-column>
          <el-table-column label="上传时间" min-width="170">
            <template #default="{ row }">{{ formatDateTime(row.uploadTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="170" fixed="right">
            <template #default="{ row }">
              <div class="admin-table-actions">
                <el-button
                  link
                  type="primary"
                  :data-testid="`application-attachment-download-${row.id}`"
                  @click="handleDownloadAttachment(row)"
                >
                  下载
                </el-button>
                <el-button
                  v-if="canManageAttachments"
                  link
                  type="danger"
                  :data-testid="`application-attachment-delete-${row.id}`"
                  @click="handleDeleteAttachment(row)"
                >
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card class="admin-table-card" shadow="never">
        <div class="admin-card-header">
          <div>
            <div class="admin-card-header__title">审核记录</div>
            <div class="admin-card-header__meta">展示审核状态流转、认定分、审核意见和处理时间</div>
          </div>
        </div>
        <el-table
          :data="detail.reviewRecords || []"
          border
          stripe
          empty-text="暂无审核记录"
          data-testid="review-records-table"
        >
          <el-table-column prop="reviewerId" label="审核人ID" width="110" />
          <el-table-column label="审核前" width="110">
            <template #default="{ row }">{{ getMaterialStatusText(row.beforeStatus) }}</template>
          </el-table-column>
          <el-table-column label="审核后" width="110">
            <template #default="{ row }">{{ getMaterialStatusText(row.afterStatus) }}</template>
          </el-table-column>
          <el-table-column label="结果" width="100">
            <template #default="{ row }">
              <el-tag :type="row.reviewResult === 'APPROVED' ? 'success' : 'danger'">
                {{ getReviewResultText(row.reviewResult) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="reviewScore" label="认定分" width="100" />
          <el-table-column
            prop="reviewComment"
            label="审核意见"
            min-width="200"
            show-overflow-tooltip
          />
          <el-table-column label="审核时间" min-width="170">
            <template #default="{ row }">{{ formatDateTime(row.reviewTime) }}</template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>

    <el-dialog v-model="approveDialogVisible" title="审核通过" width="560px">
      <el-alert
        class="audit-dialog-intro"
        title="请确认材料真实有效，并填写最终认定分数。审核通过后材料状态将变为已通过。"
        type="success"
        show-icon
        :closable="false"
      />
      <el-form ref="approveFormRef" :model="approveForm" :rules="approveRules" label-position="top">
        <el-form-item label="认定分数" prop="reviewScore">
          <el-input-number
            v-model="approveForm.reviewScore"
            class="audit-score-input"
            :min="0"
            :precision="2"
          />
        </el-form-item>
        <el-form-item label="审核意见" prop="reviewComment">
          <el-input
            v-model="approveForm.reviewComment"
            type="textarea"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="admin-dialog-footer">
          <el-button @click="approveDialogVisible = false">取消</el-button>
          <el-button
            type="success"
            data-testid="application-detail-approve-confirm-button"
            :loading="operating"
            @click="handleApprove"
          >
            确认通过
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectDialogVisible" title="审核驳回" width="560px">
      <el-alert
        class="audit-dialog-intro"
        title="请填写明确的驳回原因，学生可根据原因修改后重新提交。"
        type="warning"
        show-icon
        :closable="false"
      />
      <el-form ref="rejectFormRef" :model="rejectForm" :rules="rejectRules" label-position="top">
        <el-form-item label="驳回原因" prop="rejectReason">
          <el-input
            v-model="rejectForm.rejectReason"
            type="textarea"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="审核意见" prop="reviewComment">
          <el-input
            v-model="rejectForm.reviewComment"
            type="textarea"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="admin-dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button
            type="danger"
            data-testid="application-detail-reject-confirm-button"
            :loading="operating"
            @click="handleReject"
          >
            确认驳回
          </el-button>
        </div>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ElMessage,
  ElMessageBox,
  type FormInstance,
  type FormRules,
  type UploadRequestOptions,
} from 'element-plus'
import {
  getApplicationDetail,
  type MaterialApplicationDetailVO,
  type MaterialAttachmentVO,
} from '@/api/frontend'
import {
  approveMaterialApplication,
  deleteMaterialAttachment,
  downloadMaterialAttachment,
  rejectMaterialApplication,
  submitMaterialApplication,
  uploadMaterialAttachment,
  withdrawMaterialApplication,
} from '@/api/material'
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue'
import { useUserStore } from '@/stores/user'
import { getMaterialStatusTagType, getMaterialStatusText } from '@/utils/status'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const operating = ref(false)
const uploading = ref(false)
const loadError = ref('')
const detail = ref<MaterialApplicationDetailVO | null>(null)
const approveDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const approveFormRef = ref<FormInstance>()
const rejectFormRef = ref<FormInstance>()

const approveForm = reactive({
  reviewScore: undefined as number | undefined,
  reviewComment: '',
})

const rejectForm = reactive({
  rejectReason: '',
  reviewComment: '',
})

const approveRules: FormRules = {
  reviewScore: [{ required: true, message: '请输入认定分数', trigger: 'change' }],
}

const rejectRules: FormRules = {
  rejectReason: [{ required: true, message: '请输入驳回原因', trigger: 'blur' }],
}

const materialId = computed(() => Number(route.params.id))
const isStudent = computed(() => userStore.roleCode === 'STUDENT')
const canSubmit = computed(
  () =>
    isStudent.value &&
    detail.value &&
    ['DRAFT', 'REJECTED', 'CANCELLED'].includes(detail.value.status) &&
    userStore.hasPermission('student:application:submit'),
)
const canWithdraw = computed(
  () =>
    isStudent.value &&
    detail.value?.status === 'SUBMITTED' &&
    userStore.hasPermission('student:application:withdraw'),
)
const canApprove = computed(
  () =>
    detail.value?.status === 'SUBMITTED' && userStore.hasPermission('audit:application:approve'),
)
const canReject = computed(
  () => detail.value?.status === 'SUBMITTED' && userStore.hasPermission('audit:application:reject'),
)
const canViewAuditPanel = computed(
  () =>
    Boolean(detail.value) &&
    !isStudent.value &&
    (userStore.hasPermission('audit:pending:view') ||
      userStore.hasPermission('audit:application:approve') ||
      userStore.hasPermission('audit:application:reject')),
)
const canManageAttachments = computed(
  () =>
    isStudent.value &&
    detail.value &&
    detail.value.studentId === userStore.studentId &&
    ['DRAFT', 'REJECTED', 'CANCELLED'].includes(detail.value.status) &&
    userStore.hasPermission('student:application:create'),
)

const statusSummary = computed(() => {
  if (!detail.value) {
    return '-'
  }
  if (detail.value.status === 'APPROVED') {
    return '申报已通过审核'
  }
  if (detail.value.status === 'REJECTED') {
    return '申报被驳回，可按规则重新提交'
  }
  if (detail.value.status === 'SUBMITTED') {
    return '申报已提交，等待审核'
  }
  if (detail.value.status === 'CANCELLED') {
    return '申报已撤回，可重新提交'
  }
  return '草稿未提交'
})

const statusNotice = computed(() => {
  if (!detail.value) {
    return ''
  }
  if (detail.value.status === 'APPROVED') {
    return '该申报已通过审核，认定分数会进入成绩统计。'
  }
  if (detail.value.status === 'REJECTED') {
    return detail.value.rejectReason
      ? `驳回原因：${detail.value.rejectReason}`
      : '该申报已驳回，请补充或修正材料后重新提交。'
  }
  if (detail.value.status === 'SUBMITTED') {
    return '该申报正在等待审核，如需修改材料可先撤回。'
  }
  if (detail.value.status === 'DRAFT' || detail.value.status === 'CANCELLED') {
    return '确认材料和附件无误后，可提交审核。'
  }
  return ''
})

const statusNoticeType = computed(() => {
  if (!detail.value) {
    return 'info'
  }
  if (detail.value.status === 'APPROVED') {
    return 'success'
  }
  if (detail.value.status === 'REJECTED') {
    return 'error'
  }
  if (detail.value.status === 'SUBMITTED') {
    return 'warning'
  }
  return 'info'
})

const auditNotice = computed(() => {
  if (!detail.value) {
    return ''
  }
  if (detail.value.status === 'SUBMITTED') {
    return '当前材料待审核，请核对附件、申报说明和学生信息后处理。'
  }
  if (detail.value.status === 'APPROVED') {
    return '该材料已审核通过，当前无需再次处理。'
  }
  if (detail.value.status === 'REJECTED') {
    return '该材料已被驳回，等待学生根据驳回原因修改后重新提交。'
  }
  if (detail.value.status === 'CANCELLED') {
    return '该材料已撤回，当前不处于待审核状态。'
  }
  return '该材料尚未提交，当前不处于待审核状态。'
})

const auditNoticeType = computed(() => {
  if (!detail.value) {
    return 'info'
  }
  if (detail.value.status === 'SUBMITTED') {
    return 'warning'
  }
  if (detail.value.status === 'APPROVED') {
    return 'success'
  }
  if (detail.value.status === 'REJECTED') {
    return 'error'
  }
  return 'info'
})

async function loadDetail() {
  loading.value = true
  loadError.value = ''
  try {
    detail.value = await getApplicationDetail(materialId.value)
  } catch {
    detail.value = null
    loadError.value = '加载申报详情失败'
  } finally {
    loading.value = false
  }
}

function goBack() {
  if (['AUDITOR', 'REVIEWER', 'ADMIN'].includes(String(userStore.roleCode))) {
    void router.push('/audit/pending')
    return
  }
  void router.push('/student/applications')
}

async function handleSubmitApplication() {
  await ElMessageBox.confirm('确认提交该申报？', '提交确认', { type: 'warning' })
  operating.value = true
  try {
    await submitMaterialApplication(materialId.value)
    ElMessage.success('提交成功')
    await loadDetail()
  } finally {
    operating.value = false
  }
}

async function handleWithdrawApplication() {
  await ElMessageBox.confirm('确认撤回该申报？', '撤回确认', { type: 'warning' })
  operating.value = true
  try {
    await withdrawMaterialApplication(materialId.value, { reason: '学生主动撤回' })
    ElMessage.success('撤回成功')
    await loadDetail()
  } finally {
    operating.value = false
  }
}

function openApproveDialog() {
  approveForm.reviewScore = detail.value?.finalScore ?? detail.value?.applyScore
  approveForm.reviewComment = ''
  approveFormRef.value?.clearValidate()
  approveDialogVisible.value = true
}

function openRejectDialog() {
  rejectForm.rejectReason = ''
  rejectForm.reviewComment = ''
  rejectFormRef.value?.clearValidate()
  rejectDialogVisible.value = true
}

async function handleApprove() {
  await approveFormRef.value?.validate()
  await ElMessageBox.confirm('确认审核通过该申报？', '审核确认', { type: 'warning' })
  operating.value = true
  try {
    await approveMaterialApplication(materialId.value, {
      reviewScore: approveForm.reviewScore ?? 0,
      reviewComment: approveForm.reviewComment || undefined,
    })
    ElMessage.success('审核通过成功')
    approveDialogVisible.value = false
    await loadDetail()
  } finally {
    operating.value = false
  }
}

async function handleReject() {
  await rejectFormRef.value?.validate()
  await ElMessageBox.confirm('确认驳回该申报？', '审核确认', { type: 'warning' })
  operating.value = true
  try {
    await rejectMaterialApplication(materialId.value, {
      rejectReason: rejectForm.rejectReason,
      reviewComment: rejectForm.reviewComment || undefined,
    })
    ElMessage.success('审核驳回成功')
    rejectDialogVisible.value = false
    await loadDetail()
  } finally {
    operating.value = false
  }
}

async function handleUploadAttachment(options: UploadRequestOptions) {
  uploading.value = true
  try {
    await uploadMaterialAttachment(materialId.value, options.file)
    ElMessage.success('附件上传成功')
    await loadDetail()
  } finally {
    uploading.value = false
  }
}

async function handleDownloadAttachment(row: MaterialAttachmentVO) {
  const blob = await downloadMaterialAttachment(row.id)
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = row.originalName || 'attachment'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

async function handleDeleteAttachment(row: MaterialAttachmentVO) {
  await ElMessageBox.confirm(`确认删除附件「${row.originalName}」？`, '删除确认', {
    type: 'warning',
  })
  operating.value = true
  try {
    await deleteMaterialAttachment(row.id)
    ElMessage.success('附件删除成功')
    await loadDetail()
  } finally {
    operating.value = false
  }
}

function getReviewResultText(result?: string) {
  if (result === 'APPROVED') {
    return '通过'
  }
  if (result === 'REJECTED') {
    return '驳回'
  }
  return result || '-'
}

function formatDateTime(value?: string) {
  return value ? new Date(value).toLocaleString() : '-'
}

function formatScore(value?: number) {
  return value ?? '-'
}

function formatFileSize(value?: number) {
  if (value == null) {
    return '-'
  }
  if (value < 1024) {
    return `${value} B`
  }
  if (value < 1024 * 1024) {
    return `${(value / 1024).toFixed(1)} KB`
  }
  return `${(value / 1024 / 1024).toFixed(1)} MB`
}

onMounted(loadDetail)
</script>

<style scoped>
.application-status-card {
  border: 1px solid var(--app-border-color);
  border-radius: var(--app-radius);
  background: var(--app-card-bg);
}

.application-status-card__main {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  gap: 18px;
  align-items: center;
}

.application-status-card__label {
  color: #64748b;
  font-size: 13px;
}

.application-status-card__title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 8px;
  color: #111827;
  font-size: 18px;
  font-weight: 650;
}

.application-status-card__notice {
  margin-top: 16px;
}

.application-score-box {
  min-width: 104px;
  padding: 12px 14px;
  border: 1px solid var(--app-border-color);
  border-radius: var(--app-radius-small);
  background: #fafafa;
  text-align: right;
}

.application-score-box span {
  display: block;
  color: #64748b;
  font-size: 12px;
}

.application-score-box strong {
  display: block;
  margin-top: 4px;
  color: #111827;
  font-size: 22px;
  line-height: 1.15;
}

.application-score-box--final strong {
  color: #047857;
}

.application-reject-reason {
  color: #b91c1c;
  font-weight: 600;
}

.detail-section-alert {
  margin-bottom: 14px;
}

.audit-action-card {
  border-color: var(--app-border-color);
  background: var(--app-card-bg);
}

.audit-action-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.audit-metric {
  min-width: 0;
  padding: 12px 14px;
  border: 1px solid var(--app-border-color);
  border-radius: var(--app-radius-small);
  background: #fafafa;
}

.audit-metric span {
  display: block;
  color: #64748b;
  font-size: 12px;
}

.audit-metric strong {
  display: block;
  margin-top: 5px;
  color: #111827;
  font-size: 17px;
  line-height: 1.35;
  overflow-wrap: anywhere;
}

.audit-action-buttons {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.audit-dialog-intro {
  margin-bottom: 16px;
}

.audit-score-input {
  width: 100%;
}

@media (max-width: 860px) {
  .application-status-card__main {
    grid-template-columns: 1fr;
  }

  .application-score-box {
    text-align: left;
  }

  .audit-action-summary {
    grid-template-columns: 1fr;
  }

  .audit-action-buttons {
    justify-content: flex-start;
  }
}
</style>
