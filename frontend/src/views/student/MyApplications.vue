<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getMyApplicationStatistics,
  getMyApplicationsPage,
  type MyApplicationStatisticsVO,
  type MyMaterialApplicationVO,
} from '@/api/frontend'
import { submitMaterialApplication, withdrawMaterialApplication } from '@/api/material'
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue'
import { useUserStore } from '@/stores/user'
import {
  getMaterialStatusTagType,
  getMaterialStatusText,
  materialStatusText,
  type MaterialStatus,
} from '@/utils/status'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const actionLoadingId = ref<number>()
const applications = ref<MyMaterialApplicationVO[]>([])
const total = ref(0)
const hasStudentBinding = computed(() => Boolean(userStore.studentId))

const filters = reactive({
  status: '' as MaterialStatus | '',
  keyword: '',
})

const pagination = reactive({
  current: 1,
  size: 10,
})

const statistics = reactive<MyApplicationStatisticsVO>({
  studentId: 0,
  totalCount: 0,
  draftCount: 0,
  submittedCount: 0,
  approvedCount: 0,
  rejectedCount: 0,
  cancelledCount: 0,
})

const statusOptions = computed(() =>
  Object.entries(materialStatusText).map(([value, label]) => ({
    value: value as MaterialStatus,
    label,
  })),
)

const statisticCards = computed(() => [
  { label: '全部申报', value: statistics.totalCount, tone: 'primary' },
  { label: '草稿', value: statistics.draftCount, tone: 'info' },
  { label: '待审核', value: statistics.submittedCount, tone: 'warning' },
  { label: '已通过', value: statistics.approvedCount, tone: 'success' },
  { label: '已驳回', value: statistics.rejectedCount, tone: 'danger' },
  { label: '已撤回', value: statistics.cancelledCount, tone: 'info' },
])

function resetStatistics() {
  Object.assign(statistics, {
    studentId: 0,
    totalCount: 0,
    draftCount: 0,
    submittedCount: 0,
    approvedCount: 0,
    rejectedCount: 0,
    cancelledCount: 0,
  })
}

async function loadStatistics() {
  if (!userStore.studentId) {
    resetStatistics()
    return
  }
  const data = await getMyApplicationStatistics()
  Object.assign(statistics, data)
}

async function loadApplications() {
  if (!userStore.studentId) {
    applications.value = []
    total.value = 0
    return
  }
  loading.value = true
  try {
    const page = await getMyApplicationsPage({
      pageNo: pagination.current,
      pageSize: pagination.size,
      status: filters.status || undefined,
      keyword: filters.keyword || undefined,
    })
    applications.value = page.records
    total.value = page.total
  } finally {
    loading.value = false
  }
}

async function refreshPage() {
  await Promise.all([loadStatistics(), loadApplications()])
}

function handleSearch() {
  pagination.current = 1
  void refreshPage()
}

function handleReset() {
  filters.status = ''
  filters.keyword = ''
  pagination.current = 1
  void refreshPage()
}

function handlePageChange(page: number) {
  pagination.current = page
  void loadApplications()
}

function handleSizeChange(size: number) {
  pagination.size = size
  pagination.current = 1
  void loadApplications()
}

function goCreate() {
  void router.push('/student/applications/create')
}

function goDetail(row: MyMaterialApplicationVO) {
  void router.push(`/student/applications/${row.id}`)
}

async function handleSubmit(row: MyMaterialApplicationVO) {
  await ElMessageBox.confirm(`确认提交「${row.title}」？`, '提交确认', { type: 'warning' })
  actionLoadingId.value = row.id
  try {
    await submitMaterialApplication(row.id)
    ElMessage.success('提交成功')
    await refreshPage()
  } finally {
    actionLoadingId.value = undefined
  }
}

async function handleWithdraw(row: MyMaterialApplicationVO) {
  await ElMessageBox.confirm(`确认撤回「${row.title}」？`, '撤回确认', { type: 'warning' })
  actionLoadingId.value = row.id
  try {
    await withdrawMaterialApplication(row.id, {})
    ElMessage.success('撤回成功')
    await refreshPage()
  } finally {
    actionLoadingId.value = undefined
  }
}

function canSubmit(status: string) {
  return (
    userStore.hasPermission('student:application:submit') &&
    (status === 'DRAFT' || status === 'REJECTED' || status === 'CANCELLED')
  )
}

function canWithdraw(status: string) {
  return userStore.hasPermission('student:application:withdraw') && status === 'SUBMITTED'
}

function formatDateTime(value?: string) {
  return value ? new Date(value).toLocaleString() : '-'
}

function formatScore(value?: number) {
  return value ?? '-'
}

watch(
  () => userStore.studentId,
  () => {
    pagination.current = 1
    void refreshPage()
  },
)

onMounted(() => {
  void refreshPage()
})
</script>

<template>
  <section class="admin-page" data-testid="my-applications-page">
    <AdminPageHeader
      eyebrow="学生申报"
      title="我的申报"
      description="查看和管理我的综合测评申报材料，跟踪草稿、待审核、已通过和已驳回状态。"
    >
      <template #actions>
        <el-button
          v-if="userStore.hasPermission('student:application:create')"
          type="primary"
          data-testid="student-create-application-button"
          :disabled="!hasStudentBinding"
          @click="goCreate"
        >
          新增申报
        </el-button>
      </template>
    </AdminPageHeader>

    <el-alert
      v-if="!hasStudentBinding"
      title="当前账号未绑定学生信息，无法查看我的申报。"
      type="warning"
      :closable="false"
      show-icon
    />

    <div class="application-stat-grid">
      <el-card
        v-for="item in statisticCards"
        :key="item.label"
        class="application-stat-card"
        shadow="never"
      >
        <div class="application-stat-card__label">{{ item.label }}</div>
        <div
          :class="['application-stat-card__value', `application-stat-card__value--${item.tone}`]"
        >
          {{ item.value }}
        </div>
      </el-card>
    </div>

    <el-card class="admin-filter-card" shadow="never">
      <el-form :model="filters" label-position="top">
        <div class="admin-filter-grid">
          <el-form-item label="状态">
            <el-select v-model="filters.status" clearable placeholder="全部状态">
              <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="关键字" class="admin-filter-grid__wide">
            <el-input
              v-model="filters.keyword"
              clearable
              placeholder="申报标题或说明"
              @keyup.enter="handleSearch"
            />
          </el-form-item>

          <el-form-item label=" ">
            <div class="admin-filter-actions">
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </div>
          </el-form-item>
        </div>
      </el-form>
    </el-card>

    <el-card class="admin-table-card" shadow="never">
      <div class="admin-card-header">
        <div>
          <div class="admin-card-header__title">申报列表</div>
          <div class="admin-card-header__meta">共 {{ total }} 条申报记录</div>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="applications"
        border
        stripe
        empty-text="暂无申报记录，可点击右上角新增申报。"
        data-testid="my-applications-table"
      >
        <el-table-column prop="title" label="申报标题" min-width="190" show-overflow-tooltip />
        <el-table-column
          prop="categoryName"
          label="综测分类"
          min-width="140"
          show-overflow-tooltip
        />
        <el-table-column prop="itemName" label="综测项目" min-width="160" show-overflow-tooltip />
        <el-table-column label="申请分" width="90" align="right">
          <template #default="{ row }">{{ formatScore(row.applyScore) }}</template>
        </el-table-column>
        <el-table-column label="认定分" width="90" align="right">
          <template #default="{ row }">{{ formatScore(row.finalScore) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getMaterialStatusTagType(row.status)">
              {{ getMaterialStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="attachmentCount" label="附件数" width="90" align="right" />
        <el-table-column label="提交时间" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.submitTime) }}</template>
        </el-table-column>
        <el-table-column label="审核时间" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.reviewTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="190">
          <template #default="{ row }">
            <div class="admin-table-actions">
              <el-button
                link
                type="primary"
                :data-testid="`my-application-detail-${row.id}`"
                @click="goDetail(row)"
              >
                查看
              </el-button>
              <el-button
                v-if="canSubmit(row.status)"
                link
                type="success"
                :data-testid="`my-application-submit-${row.id}`"
                :loading="actionLoadingId === row.id"
                @click="handleSubmit(row)"
              >
                提交
              </el-button>
              <el-button
                v-if="canWithdraw(row.status)"
                link
                type="warning"
                :data-testid="`my-application-withdraw-${row.id}`"
                :loading="actionLoadingId === row.id"
                @click="handleWithdraw(row)"
              >
                撤回
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="admin-pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>
  </section>
</template>

<style scoped>
.application-stat-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(120px, 1fr));
  gap: 12px;
}

.application-stat-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.application-stat-card :deep(.el-card__body) {
  padding: 16px;
}

.application-stat-card__label {
  color: #64748b;
  font-size: 13px;
}

.application-stat-card__value {
  margin-top: 8px;
  color: #111827;
  font-size: 26px;
  font-weight: 700;
  line-height: 1.1;
}

.application-stat-card__value--success {
  color: #047857;
}

.application-stat-card__value--warning {
  color: #b45309;
}

.application-stat-card__value--danger {
  color: #b91c1c;
}

.application-stat-card__value--primary {
  color: #1d4ed8;
}

@media (max-width: 1180px) {
  .application-stat-grid {
    grid-template-columns: repeat(3, minmax(120px, 1fr));
  }
}

@media (max-width: 760px) {
  .application-stat-grid {
    grid-template-columns: repeat(2, minmax(120px, 1fr));
  }
}
</style>
