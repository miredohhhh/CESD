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
  <div class="page">
    <el-alert
      v-if="!hasStudentBinding"
      title="当前账号未绑定学生信息，无法查看我的申报。"
      type="warning"
      :closable="false"
      show-icon
    />

    <div class="statistics-grid">
      <el-card shadow="never"
        ><el-statistic title="全部申报" :value="statistics.totalCount"
      /></el-card>
      <el-card shadow="never"><el-statistic title="草稿" :value="statistics.draftCount" /></el-card>
      <el-card shadow="never"
        ><el-statistic title="待审核" :value="statistics.submittedCount"
      /></el-card>
      <el-card shadow="never"
        ><el-statistic title="已通过" :value="statistics.approvedCount"
      /></el-card>
      <el-card shadow="never"
        ><el-statistic title="已驳回" :value="statistics.rejectedCount"
      /></el-card>
      <el-card shadow="never"
        ><el-statistic title="已撤回" :value="statistics.cancelledCount"
      /></el-card>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form inline>
        <el-form-item label="状态">
          <el-select
            v-model="filters.status"
            clearable
            placeholder="全部状态"
            class="filter-select"
          >
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="关键字">
          <el-input
            v-model="filters.keyword"
            clearable
            placeholder="标题或说明"
            class="keyword-input"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button
            v-if="userStore.hasPermission('student:application:create')"
            type="success"
            :disabled="!hasStudentBinding"
            @click="router.push('/student/applications/create')"
          >
            新增申报
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="applications" border>
        <el-table-column prop="title" label="申报标题" min-width="180" show-overflow-tooltip />
        <el-table-column
          prop="categoryName"
          label="综测分类"
          min-width="130"
          show-overflow-tooltip
        />
        <el-table-column prop="itemName" label="综测项目" min-width="150" show-overflow-tooltip />
        <el-table-column label="申请分" width="90">
          <template #default="{ row }">{{ formatScore(row.applyScore) }}</template>
        </el-table-column>
        <el-table-column label="认定分" width="90">
          <template #default="{ row }">{{ formatScore(row.finalScore) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getMaterialStatusTagType(row.status)">
              {{ getMaterialStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="attachmentCount" label="附件数" width="90" />
        <el-table-column label="提交时间" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.submitTime) }}</template>
        </el-table-column>
        <el-table-column label="审核时间" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.reviewTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="190">
          <template #default="{ row }">
            <el-button link type="primary" @click="goDetail(row)">详情</el-button>
            <el-button
              v-if="canSubmit(row.status)"
              link
              type="success"
              :loading="actionLoadingId === row.id"
              @click="handleSubmit(row)"
            >
              提交
            </el-button>
            <el-button
              v-if="canWithdraw(row.status)"
              link
              type="warning"
              :loading="actionLoadingId === row.id"
              @click="handleWithdraw(row)"
            >
              撤回
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-row">
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
  </div>
</template>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.statistics-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(120px, 1fr));
  gap: 12px;
}

.filter-card :deep(.el-form-item) {
  margin-bottom: 0;
}

.filter-select {
  width: 160px;
}

.keyword-input {
  width: 240px;
}

.pagination-row {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
}

@media (max-width: 1180px) {
  .statistics-grid {
    grid-template-columns: repeat(3, minmax(120px, 1fr));
  }
}
</style>
