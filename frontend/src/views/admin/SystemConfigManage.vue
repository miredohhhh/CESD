<template>
  <section class="admin-page admin-list-page" data-testid="system-config-manage-page">
    <AdminPageHeader
      eyebrow="系统维护"
      title="系统配置管理"
      description="维护系统运行参数、上传限制、业务开关等配置项。"
    >
      <template #actions>
        <el-button
          v-if="canCreate"
          type="primary"
          data-testid="system-config-create-button"
          @click="openCreateDialog"
        >
          新增配置
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
              placeholder="配置键 / 配置说明"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="filters.status" clearable placeholder="全部状态">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
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
          <div class="admin-card-header__title">配置项列表</div>
          <div class="admin-card-header__meta">共 {{ page.total }} 条系统配置</div>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="records"
        empty-text="暂无系统配置"
        data-testid="system-config-table"
      >
        <el-table-column prop="configKey" label="配置键" min-width="210">
          <template #default="{ row }">
            <span class="admin-code-text">{{ row.configKey }}</span>
          </template>
        </el-table-column>
        <el-table-column label="配置值" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="admin-value-preview">{{ row.configValue || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="配置说明" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="admin-value-preview">{{ row.description || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column prop="updateTime" label="更新时间" min-width="170" />
        <el-table-column v-if="canUpdate || canDelete" label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <div class="admin-table-actions">
              <el-button
                v-if="canUpdate"
                link
                type="primary"
                :data-testid="`system-config-edit-${row.id}`"
                @click="openEditDialog(row)"
              >
                编辑
              </el-button>
              <el-button
                v-if="canDelete"
                link
                type="danger"
                :data-testid="`system-config-delete-${row.id}`"
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
      :title="editingId ? '编辑系统配置' : '新增系统配置'"
      width="680px"
      class="admin-list-dialog"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="admin-dialog-grid">
          <el-form-item label="配置键" prop="configKey" class="admin-dialog-grid__full">
            <el-input v-model="form.configKey" maxlength="100" show-word-limit />
          </el-form-item>
          <el-form-item label="配置值" prop="configValue" class="admin-dialog-grid__full">
            <el-input
              v-model="form.configValue"
              type="textarea"
              :rows="4"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          <el-form-item label="配置说明" class="admin-dialog-grid__full">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="3"
              maxlength="255"
              show-word-limit
            />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="form.status">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <div class="admin-dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="saving"
            data-testid="system-config-form-save-button"
            @click="handleSave"
          >
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  createSystemConfig,
  deleteSystemConfig,
  getSystemConfigsPage,
  updateSystemConfig,
  type CreateSystemConfigPayload,
  type SystemConfigVO,
} from '@/api/base'
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const records = ref<SystemConfigVO[]>([])
const editingId = ref<number | null>(null)

const filters = reactive({
  keyword: '',
  status: undefined as number | undefined,
})

const page = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

const form = reactive<CreateSystemConfigPayload>({
  configKey: '',
  configValue: '',
  description: '',
  status: 1,
})

const rules: FormRules<CreateSystemConfigPayload> = {
  configKey: [{ required: true, message: '请输入配置键', trigger: 'blur' }],
  configValue: [{ required: true, message: '请输入配置值', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const canCreate = computed(() => userStore.hasPermission('admin:system-config:create'))
const canUpdate = computed(() => userStore.hasPermission('admin:system-config:update'))
const canDelete = computed(() => userStore.hasPermission('admin:system-config:delete'))

onMounted(() => {
  void fetchList()
})

async function fetchList() {
  loading.value = true
  try {
    const result = await getSystemConfigsPage({
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
    configKey: '',
    configValue: '',
    description: '',
    status: 1,
  })
}

function openCreateDialog() {
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(row: SystemConfigVO) {
  editingId.value = row.id
  Object.assign(form, {
    configKey: row.configKey,
    configValue: row.configValue,
    description: row.description || '',
    status: row.status ?? 1,
  })
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await updateSystemConfig(editingId.value, form)
    } else {
      await createSystemConfig(form)
    }
    ElMessage.success('已保存')
    dialogVisible.value = false
    await fetchList()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: SystemConfigVO) {
  await ElMessageBox.confirm(`确认删除配置 ${row.configKey}？`, '二次确认', { type: 'warning' })
  await deleteSystemConfig(row.id)
  ElMessage.success('已删除')
  await fetchList()
}
</script>
