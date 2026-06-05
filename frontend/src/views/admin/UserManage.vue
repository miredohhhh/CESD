<template>
  <section class="admin-page" data-testid="user-manage-page">
    <AdminPageHeader
      eyebrow="账号管理"
      title="用户管理"
      description="维护系统登录账号、角色、状态和密码重置等信息。"
    >
      <template #actions>
        <el-button
          v-if="canCreate"
          type="primary"
          data-testid="user-create-button"
          @click="openCreateDialog"
        >
          新增用户
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
              placeholder="用户名 / 真实姓名 / 手机号 / 邮箱"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="角色">
            <el-select v-model="filters.roleId" clearable filterable placeholder="全部角色">
              <el-option
                v-for="role in roles"
                :key="role.id"
                :label="`${role.roleName} (${role.roleCode})`"
                :value="role.id"
              />
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
          <div class="admin-card-header__title">用户列表</div>
          <div class="admin-card-header__meta">共 {{ page.total }} 个登录账号</div>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="records"
        border
        stripe
        empty-text="暂无用户数据"
        data-testid="user-table"
      >
        <el-table-column prop="username" label="用户名" min-width="140" />
        <el-table-column prop="realName" label="真实姓名" min-width="140" />
        <el-table-column label="角色" min-width="170">
          <template #default="{ row }">{{ getRoleLabel(row.roleId) }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column prop="email" label="邮箱" min-width="190" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" min-width="170" />
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column
          v-if="canUpdate || canDelete || canResetPassword"
          label="操作"
          width="230"
          fixed="right"
        >
          <template #default="{ row }">
            <div class="admin-table-actions">
              <el-button
                v-if="canUpdate"
                link
                type="primary"
                :data-testid="`user-edit-${row.id}`"
                @click="openEditDialog(row)"
              >
                编辑
              </el-button>
              <el-button
                v-if="canResetPassword"
                link
                type="warning"
                :data-testid="`user-reset-password-${row.id}`"
                @click="openResetDialog(row)"
              >
                重置密码
              </el-button>
              <el-button
                v-if="canDelete"
                link
                type="danger"
                :data-testid="`user-delete-${row.id}`"
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑用户' : '新增用户'" width="720px">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="admin-dialog-grid"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item v-if="!editingId" label="登录密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            maxlength="100"
            show-password
            autocomplete="new-password"
          />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="form.roleId" filterable placeholder="请选择角色">
            <el-option
              v-for="role in roles"
              :key="role.id"
              :label="`${role.roleName} (${role.roleCode})`"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" maxlength="20" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" maxlength="100" />
        </el-form-item>
        <el-form-item label="头像地址" class="admin-dialog-grid__full">
          <el-input v-model="form.avatar" maxlength="500" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="admin-dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="saving"
            data-testid="user-form-save-button"
            @click="handleSave"
          >
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="resetDialogVisible" title="重置用户密码" width="480px">
      <p class="admin-text-muted">管理员重置后，用户需要使用新密码重新登录。</p>
      <el-form ref="resetFormRef" :model="resetForm" :rules="resetRules" label-position="top">
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="resetForm.newPassword"
            type="password"
            maxlength="100"
            show-password
            autocomplete="new-password"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="admin-dialog-footer">
          <el-button @click="resetDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleResetPassword">
            确认重置
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
  createUser,
  deleteUser,
  getRolesPage,
  getUsersPage,
  resetUserPassword,
  updateUser,
  type CreateSysUserPayload,
  type SysRoleVO,
  type SysUserVO,
  type UpdateSysUserPayload,
} from '@/api/base'
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue'
import { useUserStore } from '@/stores/user'

interface UserForm {
  username: string
  password: string
  realName: string
  roleId?: number
  phone: string
  email: string
  avatar: string
  status: number
}

const userStore = useUserStore()
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const resetDialogVisible = ref(false)
const formRef = ref<FormInstance>()
const resetFormRef = ref<FormInstance>()
const records = ref<SysUserVO[]>([])
const roles = ref<SysRoleVO[]>([])
const editingId = ref<number | null>(null)
const resettingUserId = ref<number | null>(null)

const filters = reactive({
  keyword: '',
  roleId: undefined as number | undefined,
  status: undefined as number | undefined,
})

const page = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

const form = reactive<UserForm>({
  username: '',
  password: '',
  realName: '',
  roleId: undefined,
  phone: '',
  email: '',
  avatar: '',
  status: 1,
})

const resetForm = reactive({
  newPassword: '',
})

const rules: FormRules<UserForm> = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' },
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const resetRules: FormRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码至少 6 位', trigger: 'blur' },
  ],
}

const canCreate = computed(() => userStore.hasPermission('admin:user:create'))
const canUpdate = computed(() => userStore.hasPermission('admin:user:update'))
const canDelete = computed(() => userStore.hasPermission('admin:user:delete'))
const canResetPassword = computed(() => userStore.hasPermission('admin:user:reset-password'))

onMounted(() => {
  void loadRoles()
  void fetchList()
})

async function loadRoles() {
  const result = await getRolesPage({ pageNum: 1, pageSize: 100, status: 1 })
  roles.value = result.records
}

async function fetchList() {
  loading.value = true
  try {
    const result = await getUsersPage({
      pageNum: page.pageNum,
      pageSize: page.pageSize,
      keyword: filters.keyword || undefined,
      roleId: filters.roleId,
      status: filters.status,
    })
    records.value = result.records
    page.total = result.total
  } finally {
    loading.value = false
  }
}

function getRoleLabel(roleId?: number) {
  const role = roles.value.find((item) => item.id === roleId)
  return role ? `${role.roleName} (${role.roleCode})` : roleId || '-'
}

function handleSearch() {
  page.pageNum = 1
  void fetchList()
}

function handleReset() {
  filters.keyword = ''
  filters.roleId = undefined
  filters.status = undefined
  handleSearch()
}

function handleSizeChange() {
  page.pageNum = 1
  void fetchList()
}

function resetUserForm() {
  editingId.value = null
  Object.assign(form, {
    username: '',
    password: '',
    realName: '',
    roleId: undefined,
    phone: '',
    email: '',
    avatar: '',
    status: 1,
  })
}

function openCreateDialog() {
  resetUserForm()
  dialogVisible.value = true
}

function openEditDialog(row: SysUserVO) {
  editingId.value = row.id
  Object.assign(form, {
    username: row.username,
    password: '',
    realName: row.realName,
    roleId: row.roleId,
    phone: row.phone || '',
    email: row.email || '',
    avatar: row.avatar || '',
    status: row.status,
  })
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid || !form.roleId) {
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      const payload: UpdateSysUserPayload = {
        username: form.username,
        realName: form.realName,
        roleId: form.roleId,
        phone: form.phone || undefined,
        email: form.email || undefined,
        avatar: form.avatar || undefined,
        status: form.status,
      }
      await updateUser(editingId.value, payload)
    } else {
      const payload: CreateSysUserPayload = {
        username: form.username,
        passwordHash: form.password,
        realName: form.realName,
        roleId: form.roleId,
        phone: form.phone || undefined,
        email: form.email || undefined,
        avatar: form.avatar || undefined,
        status: form.status,
      }
      await createUser(payload)
    }
    ElMessage.success('已保存')
    dialogVisible.value = false
    await fetchList()
  } finally {
    saving.value = false
  }
}

function openResetDialog(row: SysUserVO) {
  resettingUserId.value = row.id
  resetForm.newPassword = ''
  resetDialogVisible.value = true
}

async function handleResetPassword() {
  const valid = await resetFormRef.value?.validate().catch(() => false)
  if (!valid || !resettingUserId.value) {
    return
  }
  await ElMessageBox.confirm('确认重置该用户密码？', '二次确认', { type: 'warning' })
  saving.value = true
  try {
    await resetUserPassword(resettingUserId.value, { newPassword: resetForm.newPassword })
    ElMessage.success('密码已重置')
    resetDialogVisible.value = false
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: SysUserVO) {
  await ElMessageBox.confirm(`确认删除用户 ${row.username}？`, '二次确认', { type: 'warning' })
  await deleteUser(row.id)
  ElMessage.success('已删除')
  await fetchList()
}
</script>
