<template>
  <section class="admin-page" data-testid="permission-manage-page">
    <AdminPageHeader
      eyebrow="权限模型"
      title="权限管理"
      description="维护系统菜单、按钮和接口权限定义，为角色授权提供基础数据。"
    >
      <template #actions>
        <el-button
          v-if="canCreate"
          type="primary"
          data-testid="permission-create-button"
          @click="openCreateDialog"
        >
          新增权限
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
              placeholder="权限名称 / 权限编码"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="权限类型">
            <el-select v-model="filters.permissionType" clearable placeholder="全部类型">
              <el-option label="菜单" value="MENU" />
              <el-option label="按钮" value="BUTTON" />
              <el-option label="接口" value="API" />
            </el-select>
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
          <div class="admin-card-header__title">权限定义列表</div>
          <div class="admin-card-header__meta">共 {{ total }} 条权限定义</div>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="permissions"
        border
        stripe
        empty-text="暂无权限定义"
        data-testid="permission-table"
      >
        <el-table-column prop="permissionName" label="权限名称" min-width="160" />
        <el-table-column label="权限编码" min-width="250" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="admin-code-text">{{ row.permissionCode }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getPermissionTypeTagType(row.permissionType)">
              {{ getPermissionTypeText(row.permissionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="routePath" label="路由路径" min-width="170" show-overflow-tooltip />
        <el-table-column prop="apiPath" label="接口路径" min-width="190" show-overflow-tooltip />
        <el-table-column prop="httpMethod" label="方法" width="90" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="canUpdate || canDelete" label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <div class="admin-table-actions">
              <el-button
                v-if="canUpdate"
                link
                type="primary"
                :data-testid="`permission-edit-${row.id}`"
                @click="openEditDialog(row)"
              >
                编辑
              </el-button>
              <el-button
                v-if="canDelete"
                link
                type="danger"
                :data-testid="`permission-delete-${row.id}`"
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
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadPermissions"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑权限' : '新增权限'" width="760px">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="admin-dialog-grid"
      >
        <el-form-item label="权限名称" prop="permissionName">
          <el-input v-model="form.permissionName" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="权限类型" prop="permissionType">
          <el-select v-model="form.permissionType">
            <el-option label="菜单" value="MENU" />
            <el-option label="按钮" value="BUTTON" />
            <el-option label="接口" value="API" />
          </el-select>
        </el-form-item>
        <el-form-item label="权限编码" prop="permissionCode" class="admin-dialog-grid__full">
          <el-input v-model="form.permissionCode" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="父级权限">
          <el-tree-select
            v-model="form.parentId"
            :data="treeOptions"
            check-strictly
            clearable
            node-key="id"
            :props="{ label: 'permissionName', children: 'children' }"
            placeholder="无父级权限"
          />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="路由路径">
          <el-input v-model="form.routePath" placeholder="/admin/xxx" />
        </el-form-item>
        <el-form-item label="组件路径">
          <el-input v-model="form.componentPath" placeholder="views/admin/Xxx.vue" />
        </el-form-item>
        <el-form-item label="接口路径">
          <el-input v-model="form.apiPath" placeholder="/api/xxx/**" />
        </el-form-item>
        <el-form-item label="请求方法">
          <el-input v-model="form.httpMethod" placeholder="GET / POST / PUT / DELETE" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" class="admin-dialog-grid__full">
          <el-input
            v-model="form.remark"
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
          <el-button
            type="primary"
            :loading="saving"
            data-testid="permission-form-save-button"
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
  createPermission,
  deletePermission,
  getPermissionsPage,
  getPermissionTree,
  updatePermission,
  type PermissionType,
  type SaveSysPermissionPayload,
  type SysPermissionVO,
} from '@/api/permission'
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue'
import { useUserStore } from '@/stores/user'

type TagType = 'success' | 'info' | 'warning' | 'danger' | 'primary'

const userStore = useUserStore()
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const permissions = ref<SysPermissionVO[]>([])
const treeOptions = ref<SysPermissionVO[]>([])
const total = ref(0)
const editingId = ref<number>()
const formRef = ref<FormInstance>()
const filters = reactive({
  keyword: '',
  permissionType: '' as PermissionType | '',
  status: undefined as number | undefined,
})
const pagination = reactive({ current: 1, size: 10 })
const form = reactive<SaveSysPermissionPayload>({
  permissionName: '',
  permissionCode: '',
  permissionType: 'BUTTON',
  parentId: undefined,
  routePath: '',
  componentPath: '',
  apiPath: '',
  httpMethod: '',
  icon: '',
  sortOrder: 0,
  status: 1,
  remark: '',
})
const rules: FormRules<SaveSysPermissionPayload> = {
  permissionName: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
  permissionCode: [{ required: true, message: '请输入权限编码', trigger: 'blur' }],
  permissionType: [{ required: true, message: '请选择权限类型', trigger: 'change' }],
}
const canCreate = computed(() => userStore.hasPermission('admin:permission:create'))
const canUpdate = computed(() => userStore.hasPermission('admin:permission:update'))
const canDelete = computed(() => userStore.hasPermission('admin:permission:delete'))
onMounted(() => {
  void loadPermissions()
  void loadPermissionTree()
})
async function loadPermissions() {
  loading.value = true
  try {
    const r = await getPermissionsPage({
      pageNum: pagination.current,
      pageSize: pagination.size,
      keyword: filters.keyword || undefined,
      permissionType: filters.permissionType || undefined,
      status: filters.status,
    })
    permissions.value = r.records
    total.value = r.total
  } finally {
    loading.value = false
  }
}
async function loadPermissionTree() {
  treeOptions.value = await getPermissionTree()
}
function getPermissionTypeTagType(type?: string): TagType {
  if (type === 'MENU') {
    return 'primary'
  }
  if (type === 'BUTTON') {
    return 'warning'
  }
  if (type === 'API') {
    return 'success'
  }
  return 'info'
}
function getPermissionTypeText(type?: string) {
  if (type === 'MENU') {
    return '菜单'
  }
  if (type === 'BUTTON') {
    return '按钮'
  }
  if (type === 'API') {
    return '接口'
  }
  return type || '-'
}
function handleSearch() {
  pagination.current = 1
  void loadPermissions()
}
function handleReset() {
  filters.keyword = ''
  filters.permissionType = ''
  filters.status = undefined
  handleSearch()
}
function handleSizeChange() {
  pagination.current = 1
  void loadPermissions()
}
function resetForm() {
  editingId.value = undefined
  Object.assign(form, {
    permissionName: '',
    permissionCode: '',
    permissionType: 'BUTTON',
    parentId: undefined,
    routePath: '',
    componentPath: '',
    apiPath: '',
    httpMethod: '',
    icon: '',
    sortOrder: 0,
    status: 1,
    remark: '',
  })
}
function openCreateDialog() {
  resetForm()
  dialogVisible.value = true
}
function openEditDialog(row: SysPermissionVO) {
  editingId.value = row.id
  Object.assign(form, {
    permissionName: row.permissionName,
    permissionCode: row.permissionCode,
    permissionType: row.permissionType,
    parentId: row.parentId ?? undefined,
    routePath: row.routePath || '',
    componentPath: row.componentPath || '',
    apiPath: row.apiPath || '',
    httpMethod: row.httpMethod || '',
    icon: row.icon || '',
    sortOrder: row.sortOrder ?? 0,
    status: row.status,
    remark: row.remark || '',
  })
  dialogVisible.value = true
}
async function handleSave() {
  const ok = await formRef.value?.validate().catch(() => false)
  if (!ok) return
  saving.value = true
  try {
    if (editingId.value) await updatePermission(editingId.value, form)
    else await createPermission(form)
    ElMessage.success('已保存')
    dialogVisible.value = false
    await loadPermissions()
    await loadPermissionTree()
  } finally {
    saving.value = false
  }
}
async function handleDelete(row: SysPermissionVO) {
  await ElMessageBox.confirm(`确认删除权限 ${row.permissionName}？`, '二次确认', {
    type: 'warning',
  })
  await deletePermission(row.id)
  ElMessage.success('已删除')
  await loadPermissions()
  await loadPermissionTree()
}
</script>
