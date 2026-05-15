<template>
  <el-card>
    <template #header>
      <div class="page-header">
        <span>专业管理</span>
        <el-button v-if="canCreate" type="primary" @click="openCreateDialog">新增专业</el-button>
      </div>
    </template>

    <el-form :model="filters" inline>
      <el-form-item label="关键字">
        <el-input v-model="filters.keyword" clearable placeholder="专业名称 / 编码 / 学院" />
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
      <el-table-column prop="majorName" label="专业名称" min-width="160" />
      <el-table-column prop="majorCode" label="专业编码" min-width="140" />
      <el-table-column prop="collegeName" label="所属学院" min-width="160" />
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑专业' : '新增专业'" width="540px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="专业名称" prop="majorName">
          <el-input v-model="form.majorName" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="专业编码" prop="majorCode">
          <el-input v-model="form.majorCode" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="所属学院" prop="collegeName">
          <el-input v-model="form.collegeName" maxlength="100" show-word-limit />
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
  createMajorInfo,
  deleteMajorInfo,
  getMajorsPage,
  updateMajorInfo,
  type CreateMajorInfoPayload,
  type MajorInfoVO,
} from '@/api/base'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const saving = ref(false)
const records = ref<MajorInfoVO[]>([])
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

const form = reactive<CreateMajorInfoPayload>({
  majorName: '',
  majorCode: '',
  collegeName: '',
  description: '',
  status: 1,
})

const rules: FormRules = {
  majorName: [{ required: true, message: '请输入专业名称', trigger: 'blur' }],
  collegeName: [{ required: true, message: '请输入所属学院', trigger: 'blur' }],
}

const canCreate = computed(() => userStore.hasPermission('admin:major:create'))
const canUpdate = computed(() => userStore.hasPermission('admin:major:update'))
const canDelete = computed(() => userStore.hasPermission('admin:major:delete'))

async function fetchList() {
  loading.value = true
  try {
    const result = await getMajorsPage({
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
    majorName: '',
    majorCode: '',
    collegeName: '',
    description: '',
    status: 1,
  })
}

function openCreateDialog() {
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(row: MajorInfoVO) {
  editingId.value = row.id
  Object.assign(form, {
    majorName: row.majorName,
    majorCode: row.majorCode ?? '',
    collegeName: row.collegeName,
    description: row.description ?? '',
    status: row.status,
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (editingId.value) {
      await updateMajorInfo(editingId.value, form)
    } else {
      await createMajorInfo(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: MajorInfoVO) {
  await ElMessageBox.confirm(`确认删除专业「${row.majorName}」？`, '删除确认', { type: 'warning' })
  await deleteMajorInfo(row.id)
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
