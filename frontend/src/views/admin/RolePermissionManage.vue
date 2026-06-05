<template>
  <section class="admin-page" data-testid="role-permission-page">
    <AdminPageHeader
      eyebrow="角色授权"
      title="角色授权"
      description="为不同角色配置菜单、按钮和接口权限，控制系统访问范围。"
    >
      <template #actions>
        <el-button
          type="primary"
          data-testid="role-permission-save-button"
          :disabled="!selectedRoleId"
          :loading="saving"
          @click="handleSave"
        >
          保存授权
        </el-button>
      </template>
    </AdminPageHeader>

    <div class="admin-split-layout">
      <el-card class="admin-filter-card" shadow="never">
        <div class="admin-card-header">
          <div>
            <div class="admin-card-header__title">选择角色</div>
            <div class="admin-card-header__meta">先选择角色，再维护其权限范围</div>
          </div>
        </div>
        <el-form label-position="top">
          <el-form-item label="角色">
            <el-select
              v-model="selectedRoleId"
              filterable
              placeholder="请选择角色"
              @change="handleRoleChange"
            >
              <el-option
                v-for="role in roles"
                :key="role.id"
                :label="`${role.roleName} (${role.roleCode})`"
                :value="role.id"
              />
            </el-select>
          </el-form-item>
        </el-form>
        <p class="admin-text-muted">
          权限树包含菜单、按钮和接口权限。保存时会提交已勾选和半选节点，后端仍会执行最终权限校验。
        </p>
      </el-card>

      <el-card class="admin-tree-card" shadow="never">
        <div class="admin-card-header">
          <div>
            <div class="admin-card-header__title">权限树</div>
            <div class="admin-card-header__meta">勾选该角色可访问的菜单、按钮和接口权限</div>
          </div>
        </div>
        <p class="admin-tree-card__intro">
          修改授权后请点击右上角“保存授权”。未选择角色时可浏览权限结构，但不会提交保存。
        </p>
        <el-tree
          ref="treeRef"
          v-loading="loading"
          :data="permissionTree"
          data-testid="role-permission-tree"
          show-checkbox
          node-key="id"
          default-expand-all
          :props="{ label: 'permissionName', children: 'children' }"
          empty-text="暂无权限数据"
        >
          <template #default="{ data }">
            <span class="admin-tree-node">
              <span>{{ data.permissionName }}</span>
              <el-tag size="small" :type="data.permissionType === 'MENU' ? 'primary' : 'info'">
                {{ data.permissionType }}
              </el-tag>
              <code class="admin-code-text">{{ data.permissionCode }}</code>
            </span>
          </template>
        </el-tree>
      </el-card>
    </div>
  </section>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import { ElMessage, type TreeInstance } from 'element-plus'
import { getRolesPage, type SysRoleVO } from '@/api/base'
import {
  assignRolePermissions,
  getPermissionTree,
  getRolePermissions,
  type SysPermissionVO,
} from '@/api/permission'
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue'
const roles = ref<SysRoleVO[]>([])
const permissionTree = ref<SysPermissionVO[]>([])
const selectedRoleId = ref<number>()
const loading = ref(false)
const saving = ref(false)
const treeRef = ref<TreeInstance>()
onMounted(async () => {
  await Promise.all([loadRoles(), loadPermissionTree()])
})
async function loadRoles() {
  roles.value = (await getRolesPage({ pageNum: 1, pageSize: 100 })).records
}
async function loadPermissionTree() {
  permissionTree.value = await getPermissionTree()
}
async function handleRoleChange() {
  if (!selectedRoleId.value) return
  loading.value = true
  try {
    const result = await getRolePermissions(selectedRoleId.value)
    await nextTick()
    treeRef.value?.setCheckedKeys(result.permissionIds)
  } finally {
    loading.value = false
  }
}
async function handleSave() {
  if (!selectedRoleId.value) return
  saving.value = true
  try {
    const checked = treeRef.value?.getCheckedKeys(false) ?? []
    const half = treeRef.value?.getHalfCheckedKeys() ?? []
    const ids = [...checked, ...half].map(Number).filter(Number.isFinite)
    await assignRolePermissions(selectedRoleId.value, ids)
    ElMessage.success('已保存')
  } finally {
    saving.value = false
  }
}
</script>
