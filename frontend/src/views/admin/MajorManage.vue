<template>
  <section class="admin-page" data-testid="major-manage-page">
    <AdminPageHeader
      eyebrow="组织基础数据"
      title="专业管理"
      description="维护专业基础信息，为班级、学生和成绩统计提供基础数据。"
    >
      <template #actions>
        <el-button
          v-if="canCreate"
          type="primary"
          data-testid="major-create-button"
          @click="openCreateDialog"
        >
          新增专业
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
              placeholder="专业名称 / 专业编码 / 所属学院"
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
          <div class="admin-card-header__title">专业列表</div>
          <div class="admin-card-header__meta">共 {{ page.total }} 个专业</div>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="records"
        border
        stripe
        empty-text="暂无专业数据"
        data-testid="major-table"
      >
        <el-table-column prop="majorName" label="专业名称" min-width="170" />
        <el-table-column label="专业编码" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="admin-code-text">{{ row.majorCode || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="collegeName" label="所属学院" min-width="180" />
        <el-table-column prop="description" label="说明" min-width="220" show-overflow-tooltip />
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
            <div class="admin-table-actions">
              <el-button
                v-if="canUpdate"
                link
                type="primary"
                :data-testid="`major-edit-${row.id}`"
                @click="openEditDialog(row)"
              >
                编辑
              </el-button>
              <el-button
                v-if="canDelete"
                link
                type="danger"
                :data-testid="`major-delete-${row.id}`"
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑专业' : '新增专业'" width="680px">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="admin-dialog-grid"
      >
        <el-form-item label="专业名称" prop="majorName">
          <el-input v-model="form.majorName" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="专业编码" prop="majorCode">
          <el-input v-model="form.majorCode" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="所属学院" prop="collegeName" class="admin-dialog-grid__full">
          <el-input v-model="form.collegeName" maxlength="100" show-word-limit />
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
  createMajorInfo,
  deleteMajorInfo,
  getMajorsPage,
  updateMajorInfo,
  type CreateMajorInfoPayload,
  type MajorInfoVO,
} from '@/api/base'
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue'
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
    void fetchList()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: MajorInfoVO) {
  await ElMessageBox.confirm(`确认删除专业「${row.majorName}」？`, '删除确认', { type: 'warning' })
  await deleteMajorInfo(row.id)
  ElMessage.success('删除成功')
  void fetchList()
}

onMounted(fetchList)
</script>
