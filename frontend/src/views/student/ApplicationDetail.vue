<template>
  <el-card>
    <template #header>
      <div class="page-header">
        <span>申报详情</span>
        <div>
          <el-button @click="goBack">返回</el-button>
          <el-button
            v-if="canSubmit"
            type="primary"
            :loading="operating"
            @click="handleSubmitApplication"
          >
            提交申报
          </el-button>
          <el-button
            v-if="canWithdraw"
            type="warning"
            :loading="operating"
            @click="handleWithdrawApplication"
          >
            撤回申报
          </el-button>
          <el-button v-if="canApprove" type="success" @click="approveDialogVisible = true"
            >审核通过</el-button
          >
          <el-button v-if="canReject" type="danger" @click="rejectDialogVisible = true"
            >审核驳回</el-button
          >
        </div>
      </div>
    </template>

    <el-alert
      v-if="loadError"
      class="mb-16"
      :title="loadError"
      type="error"
      show-icon
      :closable="false"
    />
    <el-empty v-if="!loading && !detail" description="暂无申报详情" />

    <template v-if="detail">
      <el-descriptions title="材料信息" :column="2" border class="mb-16">
        <el-descriptions-item label="标题">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getMaterialStatusTagType(detail.status)">
            {{ getMaterialStatusText(detail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请分">{{ detail.applyScore ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="认定分">{{ detail.finalScore ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detail.submitTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ detail.reviewTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="驳回原因" :span="2">{{
          detail.rejectReason || '-'
        }}</el-descriptions-item>
        <el-descriptions-item label="说明" :span="2">{{
          detail.description || '-'
        }}</el-descriptions-item>
      </el-descriptions>

      <el-descriptions title="学生与项目信息" :column="2" border class="mb-16">
        <el-descriptions-item label="学生">{{ detail.studentName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ detail.studentNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ detail.majorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ detail.className || '-' }}</el-descriptions-item>
        <el-descriptions-item label="综测分类">{{
          detail.categoryName || '-'
        }}</el-descriptions-item>
        <el-descriptions-item label="综测项目">{{ detail.itemName || '-' }}</el-descriptions-item>
      </el-descriptions>

      <h3>附件列表</h3>
      <el-table :data="detail.attachments || []" border class="mb-16">
        <el-table-column prop="originalName" label="原始文件名" min-width="180" />
        <el-table-column prop="fileType" label="类型" width="100" />
        <el-table-column prop="fileSize" label="大小" width="120" />
        <el-table-column prop="uploadTime" label="上传时间" min-width="170" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-link v-if="row.fileUrl" :href="row.fileUrl" target="_blank" type="primary"
              >查看</el-link
            >
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>

      <h3>审核记录</h3>
      <el-table :data="detail.reviewRecords || []" border>
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
              {{ row.reviewResult === 'APPROVED' ? '通过' : '驳回' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewScore" label="认定分" width="100" />
        <el-table-column prop="reviewComment" label="审核意见" min-width="180" />
        <el-table-column prop="reviewTime" label="审核时间" min-width="170" />
      </el-table>
    </template>

    <el-dialog v-model="approveDialogVisible" title="审核通过" width="480px">
      <el-form ref="approveFormRef" :model="approveForm" :rules="approveRules" label-width="100px">
        <el-form-item label="认定分数" prop="reviewScore">
          <el-input-number v-model="approveForm.reviewScore" :min="0" :precision="2" />
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
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="success" :loading="operating" @click="handleApprove">确认通过</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectDialogVisible" title="审核驳回" width="480px">
      <el-form ref="rejectFormRef" :model="rejectForm" :rules="rejectRules" label-width="100px">
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
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="operating" @click="handleReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { getApplicationDetail, type MaterialApplicationDetailVO } from '@/api/frontend'
import {
  approveMaterialApplication,
  rejectMaterialApplication,
  submitMaterialApplication,
  withdrawMaterialApplication,
} from '@/api/material'
import { useUserStore } from '@/stores/user'
import { getMaterialStatusTagType, getMaterialStatusText } from '@/utils/status'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const operating = ref(false)
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
  if (userStore.roleCode === 'AUDITOR' || userStore.roleCode === 'ADMIN') {
    router.push('/audit/pending')
    return
  }
  router.push('/student/applications')
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

onMounted(loadDetail)
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.mb-16 {
  margin-bottom: 16px;
}

h3 {
  margin: 18px 0 12px;
  font-size: 16px;
}
</style>
