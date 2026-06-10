<template>
  <section class="admin-page pending-page" data-testid="pending-applications-page">
    <AdminPageHeader
      eyebrow="审核工作台"
      title="待审核列表"
      description="查看待审核申报材料，按学生、专业、班级、分类、项目和提交时间筛选并进入详情审核。"
    />

    <el-card class="admin-filter-card pending-filter-card" shadow="never">
      <el-form
        :model="filters"
        class="pending-filter-form"
        label-position="right"
        label-width="72px"
      >
        <div class="pending-filter-row pending-filter-row--primary">
          <el-form-item label="状态" class="pending-filter-item pending-filter-item--status">
            <el-select v-model="filters.status" clearable placeholder="选择状态">
              <el-option label="待审核" value="SUBMITTED" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已驳回" value="REJECTED" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键词" class="pending-filter-item pending-filter-item--keyword">
            <el-input v-model="filters.keyword" clearable placeholder="标题 / 说明" />
          </el-form-item>
          <el-form-item label="学生姓名" class="pending-filter-item pending-filter-item--student">
            <el-input v-model="filters.studentName" clearable placeholder="学生姓名" />
          </el-form-item>
          <el-form-item label="学号" class="pending-filter-item pending-filter-item--student-no">
            <el-input v-model="filters.studentNo" clearable placeholder="学号" />
          </el-form-item>

          <el-form-item label="提交时间" class="pending-filter-item pending-filter-item--time">
            <el-date-picker
              v-model="filters.submitTimeRange"
              type="datetimerange"
              value-format="YYYY-MM-DD HH:mm:ss"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              clearable
            />
          </el-form-item>

          <el-form-item class="pending-filter-actions-item">
            <div class="pending-filter-actions">
              <el-button data-testid="pending-applications-reset-button" @click="handleReset">
                重置
              </el-button>
              <el-button
                type="primary"
                data-testid="pending-applications-search-button"
                @click="handleSearch"
              >
                查询
              </el-button>
              <el-button link type="primary" class="pending-filter-toggle" @click="toggleAdvanced">
                {{ showAdvancedFilters ? '收起' : '展开' }}
                <span class="pending-filter-toggle__arrow" aria-hidden="true">
                  {{ showAdvancedFilters ? '⌃' : '⌄' }}
                </span>
              </el-button>
            </div>
          </el-form-item>
        </div>

        <div v-show="showAdvancedFilters" class="pending-filter-row pending-filter-row--advanced">
          <el-form-item label="专业 ID" class="pending-filter-item pending-filter-item--id">
            <el-input-number
              v-model="filters.majorId"
              class="pending-filter-number"
              :min="1"
              controls-position="right"
              placeholder="专业 ID"
            />
          </el-form-item>
          <el-form-item label="班级 ID" class="pending-filter-item pending-filter-item--id">
            <el-input-number
              v-model="filters.classId"
              class="pending-filter-number"
              :min="1"
              controls-position="right"
              placeholder="班级 ID"
            />
          </el-form-item>
          <el-form-item label="分类 ID" class="pending-filter-item pending-filter-item--id">
            <el-input-number
              v-model="filters.categoryId"
              class="pending-filter-number"
              :min="1"
              controls-position="right"
              placeholder="分类 ID"
            />
          </el-form-item>
          <el-form-item label="项目 ID" class="pending-filter-item pending-filter-item--id">
            <el-input-number
              v-model="filters.itemId"
              class="pending-filter-number"
              :min="1"
              controls-position="right"
              placeholder="项目 ID"
            />
          </el-form-item>
        </div>
      </el-form>
    </el-card>

    <el-card class="admin-table-card pending-table-card" shadow="never">
      <div class="admin-card-header pending-table-header">
        <div>
          <div class="admin-card-header__title">待审核材料</div>
          <div class="admin-card-header__meta">
            共 {{ page.total }} 条记录，默认展示待审核状态材料
          </div>
        </div>
        <div class="pending-table-toolbar">
          <el-button
            v-if="canExportApplications"
            :loading="exporting"
            data-testid="material-export-button"
            @click="handleExport"
          >
            导出申报明细
          </el-button>
          <el-button data-testid="pending-applications-refresh-button" @click="fetchList">
            刷新列表
          </el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="records"
        class="pending-table"
        empty-text="暂无待审核材料；如审核范围为空或筛选条件过细，请调整条件后重试。"
        data-testid="pending-applications-table"
      >
        <el-table-column label="材料标题" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="pending-title-cell">
              <strong>{{ row.title }}</strong>
              <span>{{ row.categoryName || '-' }} / {{ row.itemName || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="studentName" label="学生姓名" width="112" show-overflow-tooltip />
        <el-table-column prop="studentNo" label="学号" width="132" show-overflow-tooltip />
        <el-table-column prop="majorName" label="专业" min-width="150" show-overflow-tooltip />
        <el-table-column prop="className" label="班级" min-width="150" show-overflow-tooltip />
        <el-table-column
          prop="categoryName"
          label="综测分类"
          min-width="150"
          show-overflow-tooltip
        />
        <el-table-column prop="itemName" label="综测项目" min-width="180" show-overflow-tooltip />
        <el-table-column label="申请分" width="96" align="right" class-name="pending-number-cell">
          <template #default="{ row }">{{ formatScore(row.applyScore) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="108" align="center">
          <template #default="{ row }">
            <el-tag :type="getMaterialStatusTagType(row.status)">
              {{ getMaterialStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="附件数" width="92" align="right" class-name="pending-number-cell">
          <template #default="{ row }">
            <el-tag type="info">{{ row.attachmentCount ?? 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" min-width="172">
          <template #default="{ row }">{{ formatDateTime(row.submitTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="128" fixed="right" align="center">
          <template #default="{ row }">
            <div class="admin-table-actions pending-table-actions">
              <el-button
                link
                type="primary"
                :data-testid="`pending-application-detail-${row.id}`"
                @click="goDetail(row.id)"
              >
                查看详情
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="admin-pagination">
        <el-pagination
          v-model:current-page="page.pageNo"
          v-model:page-size="page.pageSize"
          :total="page.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="fetchList"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  exportMaterialApplications,
  getPendingApplicationsPage,
  type PendingMaterialApplicationVO,
} from '@/api/frontend'
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue'
import { useUserStore } from '@/stores/user'
import {
  getMaterialStatusTagType,
  getMaterialStatusText,
  type MaterialStatus,
} from '@/utils/status'
import { downloadResponseFile } from '@/utils/download'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const exporting = ref(false)
const showAdvancedFilters = ref(false)
const records = ref<PendingMaterialApplicationVO[]>([])

const filters = reactive({
  keyword: '',
  studentName: '',
  studentNo: '',
  status: 'SUBMITTED' as MaterialStatus | '',
  majorId: undefined as number | undefined,
  classId: undefined as number | undefined,
  categoryId: undefined as number | undefined,
  itemId: undefined as number | undefined,
  submitTimeRange: [] as string[] | null,
})

const page = reactive({
  pageNo: 1,
  pageSize: 10,
  total: 0,
})

const canExportApplications = computed(() => userStore.hasPermission('admin:material:export'))

function buildQueryParams() {
  const [startTime, endTime] = filters.submitTimeRange || []
  return {
    keyword: filters.keyword || undefined,
    studentName: filters.studentName || undefined,
    studentNo: filters.studentNo || undefined,
    status: filters.status || undefined,
    majorId: filters.majorId || undefined,
    classId: filters.classId || undefined,
    categoryId: filters.categoryId || undefined,
    itemId: filters.itemId || undefined,
    startTime: startTime || undefined,
    endTime: endTime || undefined,
  }
}

async function fetchList() {
  loading.value = true
  try {
    const result = await getPendingApplicationsPage({
      pageNo: page.pageNo,
      pageSize: page.pageSize,
      ...buildQueryParams(),
    })
    records.value = result.records
    page.total = result.total
  } finally {
    loading.value = false
  }
}

async function handleExport() {
  exporting.value = true
  try {
    const response = await exportMaterialApplications(buildQueryParams())
    downloadResponseFile(response, 'material-applications.xlsx')
    ElMessage.success('导出成功')
  } finally {
    exporting.value = false
  }
}

function handleSearch() {
  page.pageNo = 1
  fetchList()
}

function handleReset() {
  filters.keyword = ''
  filters.studentName = ''
  filters.studentNo = ''
  filters.status = 'SUBMITTED'
  filters.majorId = undefined
  filters.classId = undefined
  filters.categoryId = undefined
  filters.itemId = undefined
  filters.submitTimeRange = []
  handleSearch()
}

function handleSizeChange() {
  page.pageNo = 1
  fetchList()
}

function toggleAdvanced() {
  showAdvancedFilters.value = !showAdvancedFilters.value
}

function goDetail(id: number) {
  router.push(`/student/applications/${id}`)
}

function formatDateTime(value?: string) {
  return value ? new Date(value).toLocaleString() : '-'
}

function formatScore(value?: number) {
  return value ?? '-'
}

onMounted(fetchList)
</script>

<style scoped>
.pending-page {
  gap: 16px;
  --pending-card-border: rgba(15, 23, 42, 0.06);
  --pending-card-shadow: 0 1px 2px rgba(15, 23, 42, 0.025), 0 8px 24px rgba(15, 23, 42, 0.035);
}

.pending-page :deep(.admin-page-header) {
  align-items: flex-start;
  gap: 12px;
  padding: 0 0 2px;
}

.pending-page :deep(.admin-page-header__eyebrow) {
  margin-bottom: 4px;
  color: var(--app-text-tertiary);
  font-size: 12px;
  font-weight: 500;
  letter-spacing: 0.02em;
}

.pending-page :deep(.admin-page-header h1) {
  font-size: 23px;
  line-height: 1.2;
}

.pending-page :deep(.admin-page-header__description) {
  margin-top: 6px;
  color: var(--app-text-secondary-color);
  line-height: 1.55;
}

.pending-filter-card,
.pending-table-card {
  border-color: var(--pending-card-border);
  border-radius: var(--app-radius);
  background: var(--app-card-bg);
  box-shadow: var(--pending-card-shadow);
  overflow: hidden;
}

.pending-filter-card :deep(.el-card__body) {
  padding: 16px 20px;
}

.pending-filter-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.pending-filter-form :deep(.el-form-item__label) {
  height: 32px;
  padding-right: 8px;
  color: #344054;
  font-weight: 600;
  line-height: 32px;
}

.pending-filter-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px 16px;
}

.pending-filter-row--advanced {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid var(--app-border-light);
}

.pending-filter-item {
  flex: 0 1 auto;
}

.pending-filter-item--status {
  width: 190px;
}

.pending-filter-item--keyword {
  width: min(340px, 24vw);
  min-width: 220px;
}

.pending-filter-item--student {
  width: 210px;
}

.pending-filter-item--student-no {
  width: 190px;
}

.pending-filter-item--time {
  width: min(440px, 34vw);
  min-width: 360px;
}

.pending-filter-item--id {
  width: 190px;
}

.pending-filter-number {
  width: 100%;
}

.pending-filter-item :deep(.el-select),
.pending-filter-item :deep(.el-input),
.pending-filter-item :deep(.el-input-number),
.pending-filter-item :deep(.el-date-editor) {
  width: 100%;
}

.pending-filter-actions-item {
  margin-left: auto;
}

.pending-filter-actions {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.pending-filter-toggle {
  padding-right: 0;
  padding-left: 4px;
  font-weight: 500;
}

.pending-filter-toggle__arrow {
  margin-left: 2px;
  font-size: 13px;
}

.pending-table-card :deep(.el-card__body) {
  padding: 18px 20px 16px;
}

.pending-table-header {
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--app-border-light);
}

.pending-table-toolbar {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  gap: 10px;
}

.pending-table {
  width: 100%;
}

.pending-table :deep(.el-table__cell) {
  padding: 12px 0;
  border-right: 0;
}

.pending-table :deep(th.el-table__cell) {
  background: #fafafa;
  color: #344054;
  font-weight: 600;
}

.pending-table :deep(td.el-table__cell) {
  color: #344054;
}

.pending-table :deep(.el-table__header-wrapper .cell) {
  line-height: 1.45;
}

.pending-table :deep(.el-table__row:hover > td.el-table__cell) {
  background: #f5f8fc;
}

.pending-table :deep(.el-table__inner-wrapper::before) {
  background: var(--app-border-light);
}

.pending-table :deep(.el-table__fixed-right::before) {
  box-shadow: -6px 0 12px rgba(15, 23, 42, 0.03);
}

.pending-table :deep(.pending-number-cell .cell) {
  font-variant-numeric: tabular-nums;
}

.pending-title-cell {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 5px;
}

.pending-title-cell strong {
  overflow: hidden;
  color: #111827;
  font-weight: 650;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pending-title-cell span {
  overflow: hidden;
  color: #64748b;
  font-size: 12px;
  line-height: 1.4;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pending-table-actions {
  justify-content: center;
  white-space: nowrap;
}

.pending-table-actions :deep(.el-button.is-link) {
  padding: 0;
  font-weight: 500;
}

.pending-table-card .admin-pagination {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid var(--app-border-light);
}

@media (max-width: 1180px) {
  .pending-filter-actions-item {
    margin-left: 0;
  }

  .pending-filter-item--time {
    width: min(520px, 100%);
  }
}

@media (max-width: 760px) {
  .pending-filter-item--status,
  .pending-filter-item--keyword,
  .pending-filter-item--student,
  .pending-filter-item--student-no,
  .pending-filter-item--time,
  .pending-filter-item--id {
    width: 100%;
    min-width: 0;
  }

  .pending-table-header {
    flex-direction: column;
    align-items: stretch;
  }

  .pending-table-toolbar {
    justify-content: flex-end;
  }
}
</style>
