<template>
  <el-card>
    <template #header>
      <div class="page-header">
        <span>综测分类管理</span>
        <el-button v-if="canCreate" type="primary" @click="openCreateDialog">新增分类</el-button>
      </div>
    </template>

    <el-form :model="filters" inline>
      <el-form-item label="关键字">
        <el-input v-model="filters.keyword" clearable placeholder="分类名称 / 编码" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="filters.status" clearable placeholder="全部状态" style="width: 120px">
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="records" border>
      <el-table-column prop="categoryName" label="分类名称" min-width="160" />
      <el-table-column prop="categoryCode" label="分类编码" min-width="140" />
      <el-table-column prop="maxScore" label="最高分" width="100" />
      <el-table-column prop="sortNo" label="排序号" width="90" />
      <el-table-column prop="description" label="说明" min-width="180" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="170" />
      <el-table-column v-if="canUpdate || canDelete" label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button v-if="canUpdate" link type="primary" @click="openEditDialog(row)"
            >编辑</el-button
          >
          <el-button v-if="canDelete" link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page.pageNum"
      v-model:page-size="page.pageSize"
      class="pager"
      :total="page.total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @current-change="fetchList"
      @size-change="handleSizeChange"
    />

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑分类' : '新增分类'" width="540px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="form.categoryName" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="分类编码" prop="categoryCode">
          <el-input v-model="form.categoryCode" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="最高分" prop="maxScore">
          <el-input-number v-model="form.maxScore" :min="0" :precision="2" style="width: 180px" />
        </el-form-item>
        <el-form-item label="排序号" prop="sortNo">
          <el-input-number v-model="form.sortNo" :min="0" :precision="0" style="width: 180px" />
        </el-form-item>
        <el-form-item label="说明" prop="description">
          <el-input v-model="form.description" type="textarea" maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
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
  fetchList()
}

function handleReset() {
  filters.keyword = ''
  filters.status = undefined
  handleSearch()
}

function handleSizeChange() {
  page.pageNum = 1
  fetchList()
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
    fetchList()
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
  fetchList()
}

onMounted(fetchList)
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
