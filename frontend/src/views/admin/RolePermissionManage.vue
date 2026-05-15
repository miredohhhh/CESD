<template>
  <section class="manage-page">
    <div class="page-heading">
      <div>
        <h1>Role Permissions</h1>
        <p>Assign menu, button and API permissions to roles.</p>
      </div>
      <el-button type="primary" :disabled="!selectedRoleId" :loading="saving" @click="handleSave"
        >Save</el-button
      >
    </div>
    <el-card shadow="never">
      <el-form label-width="80px">
        <el-form-item label="Role">
          <el-select
            v-model="selectedRoleId"
            filterable
            placeholder="Select role"
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
    </el-card>
    <el-card shadow="never">
      <el-tree
        ref="treeRef"
        v-loading="loading"
        :data="permissionTree"
        show-checkbox
        node-key="id"
        default-expand-all
        :props="{ label: 'permissionName', children: 'children' }"
      >
        <template #default="{ data }">
          <span class="tree-node"
            ><span>{{ data.permissionName }}</span
            ><el-tag size="small">{{ data.permissionType }}</el-tag
            ><code>{{ data.permissionCode }}</code></span
          >
        </template>
      </el-tree>
    </el-card>
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
    ElMessage.success('Saved')
  } finally {
    saving.value = false
  }
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
.tree-node {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
code {
  color: #64748b;
}
</style>
