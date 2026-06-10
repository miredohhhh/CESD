<template>
  <section class="admin-page admin-list-page" data-testid="evaluation-category-manage-page">
    <AdminPageHeader
      eyebrow="基础数据"
      title="综测分类管理"
      description="维护综合测评分类、分类编码、最高分和排序规则。"
    >
      <template #actions>
        <el-button
          v-if="canCreate"
          type="primary"
          data-testid="evaluation-category-create-button"
          @click="openCreateDialog"
        >
          新增分类
        </el-button>
      </template>
    </AdminPageHeader>

    <el-card class="admin-filter-card" shadow="never">
      <el-form :model="filters" label-position="top">
        <div class="admin-filter-grid">
          <el-form-item label="关键字" class="admin-filter-grid__wide">
            <el-input
              v-model="filters.keyword"
              clearable
              placeholder="分类名称 / 分类编码"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="filters.status" clearable placeholder="全部状态">
              <el-option label="启用" :value="1" />
              <el-option label="停用" :value="0" />
            </el-select>
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
          <div class="admin-card-header__title">分类列表</div>
          <div class="admin-card-header__meta">共 {{ page.total }} 个综测分类</div>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="records"
        empty-text="暂无综测分类"
        data-testid="evaluation-category-table"
      >
        <el-table-column
          prop="categoryName"
          label="分类名称"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column label="分类编码" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="admin-code-text">{{ row.categoryCode || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="maxScore" label="最高分" width="100" />
        <el-table-column prop="sortNo" label="排序号" width="100" />
        <el-table-column prop="description" label="说明" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column v-if="canUpdate || canDelete" label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <div class="admin-table-actions">
              <el-button
                v-if="canUpdate"
                link
                type="primary"
                :data-testid="`evaluation-category-edit-${row.id}`"
                @click="openEditDialog(row)"
              >
                编辑
              </el-button>
              <el-button
                v-if="canDelete"
                link
                type="danger"
                :data-testid="`evaluation-category-delete-${row.id}`"
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="admin-pagination">
        <el-pagination
          v-model:current-page="page.pageNum"
          v-model:page-size="page.pageSize"
          :total="page.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="fetchList"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑分类' : '新增分类'"
      width="680px"
      class="admin-list-dialog"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="admin-dialog-grid"
      >
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="form.categoryName" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="分类编码" prop="categoryCode">
          <el-input v-model="form.categoryCode" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="最高分" prop="maxScore">
          <el-input-number v-model="form.maxScore" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="排序号" prop="sortNo">
          <el-input-number v-model="form.sortNo" :min="0" :precision="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="说明" prop="description" class="admin-dialog-grid__full">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="admin-dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  createEvaluationCategory,
  deleteEvaluationCategory,
  getEvaluationCategoriesPage,
  updateEvaluationCategory,
  type CreateEvaluationCategoryPayload,
  type EvaluationCategoryVO,
} from '@/api/evaluation'
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const saving = ref(false)
const records = ref<EvaluationCategoryVO[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const filters = reactive({
  keyword: '',
  status: undefined as number | undefined,
})

const page = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

const form = reactive<CreateEvaluationCategoryPayload>({
  categoryName: '',
  categoryCode: '',
  maxScore: undefined,
  sortNo: 0,
  description: '',
  status: 1,
})

const rules: FormRules = {
  categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
}

const canCreate = computed(() => userStore.hasPermission('admin:evaluation-category:create'))
const canUpdate = computed(() => userStore.hasPermission('admin:evaluation-category:update'))
const canDelete = computed(() => userStore.hasPermission('admin:evaluation-category:delete'))

async function fetchList() {
  loading.value = true
  try {
    const result = await getEvaluationCategoriesPage({
      pageNum: page.pageNum,
      pageSize: page.pageSize,
      keyword: filters.keyword || undefined,
      status: filters.status,
    })
    records.value = result.records
    page.total = result.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.pageNum = 1
  void fetchList()
}

function handleReset() {
  filters.keyword = ''
  filters.status = undefined
  handleSearch()
}

function handleSizeChange() {
  page.pageNum = 1
  void fetchList()
}

function resetForm() {
  editingId.value = null
  Object.assign(form, {
    categoryName: '',
    categoryCode: '',
    maxScore: undefined,
    sortNo: 0,
    description: '',
    status: 1,
  })
}

function openCreateDialog() {
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(row: EvaluationCategoryVO) {
  editingId.value = row.id
  Object.assign(form, {
    categoryName: row.categoryName,
    categoryCode: row.categoryCode ?? '',
    maxScore: row.maxScore,
    sortNo: row.sortNo ?? 0,
    description: row.description ?? '',
    status: row.status ?? 1,
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (editingId.value) {
      await updateEvaluationCategory(editingId.value, form)
    } else {
      await createEvaluationCategory(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    void fetchList()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: EvaluationCategoryVO) {
  await ElMessageBox.confirm(`确认删除分类「${row.categoryName}」？`, '删除确认', {
    type: 'warning',
  })
  await deleteEvaluationCategory(row.id)
  ElMessage.success('删除成功')
  void fetchList()
}

onMounted(fetchList)
</script>
