<template>
  <section class="manage-page">
    <div class="page-heading">
      <div>
        <h1>Permission Management</h1>
        <p>Manage MENU, BUTTON and API permission codes.</p>
      </div>
      <el-button v-if="canCreate" type="primary" @click="openCreateDialog"
        >New Permission</el-button
      >
    </div>
    <el-card shadow="never">
      <el-form label-width="80px">
        <div class="filter-grid">
          <el-form-item label="Keyword"
            ><el-input v-model="filters.keyword" clearable @keyup.enter="handleSearch"
          /></el-form-item>
          <el-form-item label="Type"
            ><el-select v-model="filters.permissionType" clearable
              ><el-option label="MENU" value="MENU" /><el-option
                label="BUTTON"
                value="BUTTON" /><el-option label="API" value="API" /></el-select
          ></el-form-item>
          <el-form-item label="Status"
            ><el-select v-model="filters.status" clearable
              ><el-option label="Enabled" :value="1" /><el-option
                label="Disabled"
                :value="0" /></el-select
          ></el-form-item>
          <el-form-item
            ><el-button type="primary" @click="handleSearch">Search</el-button
            ><el-button @click="handleReset">Reset</el-button></el-form-item
          >
        </div>
      </el-form>
    </el-card>
    <el-card shadow="never">
      <el-table v-loading="loading" :data="permissions" border>
        <el-table-column prop="permissionName" label="Name" min-width="150" />
        <el-table-column prop="permissionCode" label="Code" min-width="220" />
        <el-table-column prop="permissionType" label="Type" width="90" />
        <el-table-column prop="routePath" label="Route" min-width="160" />
        <el-table-column prop="apiPath" label="API" min-width="180" />
        <el-table-column prop="sortOrder" label="Sort" width="80" />
        <el-table-column v-if="canUpdate || canDelete" label="Actions" width="150" fixed="right"
          ><template #default="{ row }"
            ><el-button v-if="canUpdate" link type="primary" @click="openEditDialog(row)"
              >Edit</el-button
            ><el-button v-if="canDelete" link type="danger" @click="handleDelete(row)"
              >Delete</el-button
            ></template
          ></el-table-column
        >
      </el-table>
      <div class="pagination-row">
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
    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? 'Edit Permission' : 'New Permission'"
      width="680px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="Name" prop="permissionName"
          ><el-input v-model="form.permissionName"
        /></el-form-item>
        <el-form-item label="Code" prop="permissionCode"
          ><el-input v-model="form.permissionCode"
        /></el-form-item>
        <el-form-item label="Type" prop="permissionType"
          ><el-select v-model="form.permissionType"
            ><el-option label="MENU" value="MENU" /><el-option
              label="BUTTON"
              value="BUTTON" /><el-option label="API" value="API" /></el-select
        ></el-form-item>
        <el-form-item label="Parent"
          ><el-tree-select
            v-model="form.parentId"
            :data="treeOptions"
            check-strictly
            clearable
            node-key="id"
            :props="{ label: 'permissionName', children: 'children' }"
        /></el-form-item>
        <el-form-item label="Route"><el-input v-model="form.routePath" /></el-form-item>
        <el-form-item label="Component"><el-input v-model="form.componentPath" /></el-form-item>
        <el-form-item label="API Path"><el-input v-model="form.apiPath" /></el-form-item>
        <el-form-item label="Method"><el-input v-model="form.httpMethod" /></el-form-item>
        <el-form-item label="Icon"><el-input v-model="form.icon" /></el-form-item>
        <el-form-item label="Sort"
          ><el-input-number v-model="form.sortOrder" :min="0"
        /></el-form-item>
        <el-form-item label="Status"
          ><el-select v-model="form.status"
            ><el-option label="Enabled" :value="1" /><el-option
              label="Disabled"
              :value="0" /></el-select
        ></el-form-item>
        <el-form-item label="Remark"
          ><el-input v-model="form.remark" type="textarea"
        /></el-form-item>
      </el-form>
      <template #footer
        ><el-button @click="dialogVisible = false">Cancel</el-button
        ><el-button type="primary" :loading="saving" @click="handleSave">Save</el-button></template
      >
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
import { useUserStore } from '@/stores/user'
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
  permissionName: [{ required: true, message: 'Required', trigger: 'blur' }],
  permissionCode: [{ required: true, message: 'Required', trigger: 'blur' }],
  permissionType: [{ required: true, message: 'Required', trigger: 'change' }],
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
    ElMessage.success('Saved')
    dialogVisible.value = false
    await loadPermissions()
    await loadPermissionTree()
  } finally {
    saving.value = false
  }
}
async function handleDelete(row: SysPermissionVO) {
  await ElMessageBox.confirm(`Delete ${row.permissionName}?`, 'Confirm', { type: 'warning' })
  await deletePermission(row.id)
  ElMessage.success('Deleted')
  await loadPermissions()
  await loadPermissionTree()
}
</script>
<style scoped>
.manage-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.page-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
}
.page-heading h1 {
  margin: 0;
  font-size: 22px;
}
.page-heading p {
  margin: 6px 0 0;
  color: #64748b;
}
.filter-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(180px, 1fr));
  gap: 12px;
}
.pagination-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
