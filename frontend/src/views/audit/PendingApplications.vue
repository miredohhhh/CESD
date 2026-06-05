<template>
  <section class="admin-page" data-testid="pending-applications-page">
    <AdminPageHeader
      eyebrow="审核工作台"
      title="待审核列表"
      description="查看待审核申报材料，按学生、专业、班级、分类、项目和提交时间筛选并进入详情审核。"
    >
      <template #actions>
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
      </template>
    </AdminPageHeader>

    <el-card class="admin-filter-card" shadow="never">
      <el-form :model="filters" label-position="top">
        <div class="admin-filter-grid pending-filter-grid">
          <el-form-item label="状态">
            <el-select v-model="filters.status" clearable placeholder="选择状态">
              <el-option label="待审核" value="SUBMITTED" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已驳回" value="REJECTED" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="filters.keyword" clearable placeholder="标题 / 说明" />
          </el-form-item>
          <el-form-item label="学生姓名">
            <el-input v-model="filters.studentName" clearable placeholder="学生姓名" />
          </el-form-item>
          <el-form-item label="学号">
            <el-input v-model="filters.studentNo" clearable placeholder="学号" />
          </el-form-item>
          <el-form-item label="专业 ID">
            <el-input-number
              v-model="filters.majorId"
              class="pending-filter-number"
              :min="1"
              controls-position="right"
              placeholder="专业 ID"
            />
          </el-form-item>
          <el-form-item label="班级 ID">
            <el-input-number
              v-model="filters.classId"
              class="pending-filter-number"
              :min="1"
              controls-position="right"
              placeholder="班级 ID"
            />
          </el-form-item>
          <el-form-item label="分类 ID">
            <el-input-number
              v-model="filters.categoryId"
              class="pending-filter-number"
              :min="1"
              controls-position="right"
              placeholder="分类 ID"
            />
          </el-form-item>
          <el-form-item label="项目 ID">
            <el-input-number
              v-model="filters.itemId"
              class="pending-filter-number"
              :min="1"
              controls-position="right"
              placeholder="项目 ID"
            />
          </el-form-item>
          <el-form-item class="admin-filter-grid__wide" label="提交时间">
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
          <el-form-item class="admin-filter-actions">
            <el-button
              type="primary"
              data-testid="pending-applications-search-button"
              @click="handleSearch"
            >
              查询
            </el-button>
            <el-button data-testid="pending-applications-reset-button" @click="handleReset">
              重置
            </el-button>
          </el-form-item>
        </div>
      </el-form>
    </el-card>

    <el-card class="admin-table-card" shadow="never">
      <div class="admin-card-header">
        <div>
          <div class="admin-card-header__title">待审核材料</div>
          <div class="admin-card-header__meta">
            共 {{ page.total }} 条记录，默认展示待审核状态材料
          </div>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="records"
        border
        stripe
        empty-text="暂无待审核材料；如审核范围为空或筛选条件过细，请调整条件后重试。"
        data-testid="pending-applications-table"
      >
        <el-table-column label="材料标题" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="pending-title-cell">
              <strong>{{ row.title }}</strong>
              <span>{{ row.categoryName || '-' }} / {{ row.itemName || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="studentName" label="学生姓名" width="110" />
        <el-table-column prop="studentNo" label="学号" width="130" />
        <el-table-column prop="majorName" label="专业" min-width="140" show-overflow-tooltip />
        <el-table-column prop="className" label="班级" min-width="140" show-overflow-tooltip />
        <el-table-column
          prop="categoryName"
          label="综测分类"
          min-width="140"
          show-overflow-tooltip
        />
        <el-table-column prop="itemName" label="综测项目" min-width="160" show-overflow-tooltip />
        <el-table-column label="申请分" width="100" align="right">
          <template #default="{ row }">{{ formatScore(row.applyScore) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getMaterialStatusTagType(row.status)">
              {{ getMaterialStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="附件数" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info">{{ row.attachmentCount ?? 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.submitTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <div class="admin-table-actions">
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
.pending-filter-grid {
  grid-template-columns: repeat(4, minmax(160px, 1fr));
}

.pending-filter-number {
  width: 100%;
}

.pending-filter-grid :deep(.el-date-editor) {
  width: 100%;
}

.pending-title-cell {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
}

.pending-title-cell strong {
  color: #111827;
  font-weight: 650;
  line-height: 1.35;
}

.pending-title-cell span {
  color: #64748b;
  font-size: 12px;
  line-height: 1.4;
}

@media (max-width: 1180px) {
  .pending-filter-grid {
    grid-template-columns: repeat(2, minmax(180px, 1fr));
  }
}

@media (max-width: 760px) {
  .pending-filter-grid {
    grid-template-columns: 1fr;
  }
}
</style>
