<template>
  <section class="admin-page admin-list-page" data-testid="evaluation-item-manage-page">
    <AdminPageHeader
      eyebrow="基础数据"
      title="综测项目管理"
      description="维护各分类下的综合测评项目、分值规则和附件要求。"
    >
      <template #actions>
        <el-button
          v-if="canCreate"
          type="primary"
          data-testid="evaluation-item-create-button"
          @click="openCreateDialog"
        >
          新增项目
        </el-button>
      </template>
    </AdminPageHeader>

    <el-card class="admin-filter-card" shadow="never">
      <el-form :model="filters" label-position="top">
        <div class="admin-filter-grid">
          <el-form-item label="关键字">
            <el-input
              v-model="filters.keyword"
              clearable
              placeholder="项目名称 / 编码 / 计分方式"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="所属分类">
            <el-select v-model="filters.categoryId" clearable filterable placeholder="全部分类">
              <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.categoryName"
                :value="category.id"
              />
            </el-select>
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
          <div class="admin-card-header__title">项目列表</div>
          <div class="admin-card-header__meta">共 {{ page.total }} 个综测项目</div>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="records"
        empty-text="暂无综测项目"
        data-testid="evaluation-item-table"
      >
        <el-table-column prop="itemName" label="项目名称" min-width="190" show-overflow-tooltip />
        <el-table-column label="项目编码" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="admin-code-text">{{ row.itemCode || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="所属分类" min-width="170" show-overflow-tooltip>
          <template #default="{ row }">{{ getCategoryName(row.categoryId) }}</template>
        </el-table-column>
        <el-table-column label="计分方式" width="110">
          <template #default="{ row }">
            <el-tag type="primary">{{ getScoreTypeText(row.scoreType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="默认分" width="90" />
        <el-table-column prop="maxScore" label="最高分" width="90" />
        <el-table-column label="附件" width="100">
          <template #default="{ row }">
            <el-tag :type="row.needAttachment === 1 ? 'warning' : 'info'">
              {{ row.needAttachment === 1 ? '需要' : '不需要' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortNo" label="排序号" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="canUpdate || canDelete" label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <div class="admin-table-actions">
              <el-button
                v-if="canUpdate"
                link
                type="primary"
                :data-testid="`evaluation-item-edit-${row.id}`"
                @click="openEditDialog(row)"
              >
                编辑
              </el-button>
              <el-button
                v-if="canDelete"
                link
                type="danger"
                :data-testid="`evaluation-item-delete-${row.id}`"
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
      :title="editingId ? '编辑项目' : '新增项目'"
      width="760px"
      class="admin-list-dialog"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="admin-dialog-grid"
      >
        <el-form-item label="所属分类" prop="categoryId">
          <el-select v-model="form.categoryId" filterable placeholder="请选择分类">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.categoryName"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="计分方式" prop="scoreType">
          <el-select v-model="form.scoreType">
            <el-option label="固定分" value="FIXED" />
            <el-option label="手动分" value="MANUAL" />
            <el-option label="范围分" value="RANGE" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目名称" prop="itemName">
          <el-input v-model="form.itemName" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="项目编码" prop="itemCode">
          <el-input v-model="form.itemCode" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="默认分" prop="score">
          <el-input-number v-model="form.score" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="最高分" prop="maxScore">
          <el-input-number v-model="form.maxScore" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="是否需要附件" prop="needAttachment">
          <el-select v-model="form.needAttachment">
            <el-option label="需要" :value="1" />
            <el-option label="不需要" :value="0" />
          </el-select>
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
            maxlength="500"
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
  createEvaluationItem,
  deleteEvaluationItem,
  getEvaluationCategoriesPage,
  getEvaluationItemsPage,
  updateEvaluationItem,
  type CreateEvaluationItemPayload,
  type EvaluationCategoryVO,
  type EvaluationItemVO,
} from '@/api/evaluation'
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const saving = ref(false)
const records = ref<EvaluationItemVO[]>([])
const categories = ref<EvaluationCategoryVO[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const filters = reactive({
  keyword: '',
  categoryId: undefined as number | undefined,
  status: undefined as number | undefined,
})

const page = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

const form = reactive<CreateEvaluationItemPayload>({
  categoryId: 0,
  itemName: '',
  itemCode: '',
  scoreType: 'FIXED',
  score: undefined,
  maxScore: undefined,
  needAttachment: 1,
  description: '',
  sortNo: 0,
  status: 1,
})

const rules: FormRules = {
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  itemName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  scoreType: [{ required: true, message: '请选择计分方式', trigger: 'change' }],
}

const canCreate = computed(() => userStore.hasPermission('admin:evaluation-item:create'))
const canUpdate = computed(() => userStore.hasPermission('admin:evaluation-item:update'))
const canDelete = computed(() => userStore.hasPermission('admin:evaluation-item:delete'))

function getCategoryName(id: number) {
  return categories.value.find((item) => item.id === id)?.categoryName ?? '-'
}

function getScoreTypeText(scoreType?: string) {
  if (scoreType === 'FIXED') {
    return '固定分'
  }
  if (scoreType === 'MANUAL') {
    return '手动分'
  }
  if (scoreType === 'RANGE') {
    return '范围分'
  }
  return scoreType || '-'
}

async function fetchCategories() {
  const result = await getEvaluationCategoriesPage({ pageNum: 1, pageSize: 100, status: 1 })
  categories.value = result.records
}

async function fetchList() {
  loading.value = true
  try {
    const result = await getEvaluationItemsPage({
      pageNum: page.pageNum,
      pageSize: page.pageSize,
      keyword: filters.keyword || undefined,
      status: filters.status,
    })
    let list = result.records
    if (filters.categoryId) {
      list = list.filter((item) => item.categoryId === filters.categoryId)
    }
    records.value = list
    page.total = filters.categoryId ? list.length : result.total
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
  filters.categoryId = undefined
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
    categoryId: categories.value[0]?.id ?? 0,
    itemName: '',
    itemCode: '',
    scoreType: 'FIXED',
    score: undefined,
    maxScore: undefined,
    needAttachment: 1,
    description: '',
    sortNo: 0,
    status: 1,
  })
}

function openCreateDialog() {
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(row: EvaluationItemVO) {
  editingId.value = row.id
  Object.assign(form, {
    categoryId: row.categoryId,
    itemName: row.itemName,
    itemCode: row.itemCode ?? '',
    scoreType: row.scoreType,
    score: row.score,
    maxScore: row.maxScore,
    needAttachment: row.needAttachment,
    description: row.description ?? '',
    sortNo: row.sortNo ?? 0,
    status: row.status ?? 1,
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (editingId.value) {
      await updateEvaluationItem(editingId.value, form)
    } else {
      await createEvaluationItem(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    void fetchList()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: EvaluationItemVO) {
  await ElMessageBox.confirm(`确认删除项目「${row.itemName}」？`, '删除确认', { type: 'warning' })
  await deleteEvaluationItem(row.id)
  ElMessage.success('删除成功')
  void fetchList()
}

onMounted(async () => {
  await fetchCategories()
  await fetchList()
})
</script>
