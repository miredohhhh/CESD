<template>
  <section
    class="admin-page admin-list-page role-permission-page"
    data-testid="role-permission-page"
  >
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

    <div class="admin-split-layout role-permission-layout">
      <el-card class="admin-filter-card role-permission-card role-selector-card" shadow="never">
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
              class="role-select"
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

      <el-card
        class="admin-tree-card role-permission-card role-permission-tree-card"
        shadow="never"
      >
        <div class="admin-card-header">
          <div>
            <div class="admin-card-header__title">权限树</div>
            <div class="admin-card-header__meta">勾选该角色可访问的菜单、按钮和接口权限</div>
          </div>
        </div>
        <p class="admin-tree-card__intro">
          修改授权后请点击右上角“保存授权”。未选择角色时可浏览权限结构，但不会提交保存。
        </p>
        <el-alert
          v-if="!selectedRoleId"
          class="role-permission-empty"
          type="info"
          title="请先选择左侧角色，再进行权限勾选和保存。"
          show-icon
          :closable="false"
        />
        <el-tree
          ref="treeRef"
          class="role-permission-tree"
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
            <span class="admin-tree-node role-permission-node">
              <span class="role-permission-node__name">{{ data.permissionName }}</span>
              <el-tag
                class="role-permission-node__tag"
                size="small"
                :type="getPermissionTagType(data.permissionType)"
              >
                {{ data.permissionType }}
              </el-tag>
              <code class="admin-code-text role-permission-node__code">
                {{ data.permissionCode }}
              </code>
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

function getPermissionTagType(type: SysPermissionVO['permissionType']) {
  if (type === 'MENU') return 'primary'
  if (type === 'BUTTON') return 'warning'
  return 'success'
}

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

<style scoped>
.role-permission-layout {
  grid-template-columns: minmax(280px, 30%) minmax(0, 1fr);
  gap: 18px;
}

.role-permission-card :deep(.el-card__body) {
  padding: 18px 20px 20px;
}

.role-select {
  width: 100%;
}

.role-permission-empty {
  margin-bottom: 14px;
  border-radius: var(--app-radius-small);
}

.role-permission-tree {
  max-height: min(64vh, 680px);
  overflow: auto;
  padding: 4px 2px 8px;
}

.role-permission-tree :deep(.el-tree) {
  background: transparent;
}

.role-permission-tree :deep(.el-tree-node__content) {
  min-height: 38px;
  border-radius: var(--app-radius-small);
  transition: background-color 0.16s ease;
}

.role-permission-tree :deep(.el-tree-node__content:hover) {
  background-color: #f5f8fc;
}

.role-permission-node {
  width: 100%;
  min-width: 0;
  gap: 8px;
}

.role-permission-node__name {
  min-width: 96px;
  color: var(--app-text-color);
  font-weight: 500;
}

.role-permission-node__tag {
  flex: 0 0 auto;
  border-radius: 999px;
}

.role-permission-node__code {
  min-width: 0;
  max-width: min(460px, 48vw);
  overflow: hidden;
  color: var(--app-text-secondary);
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 900px) {
  .role-permission-layout {
    grid-template-columns: 1fr;
  }

  .role-permission-tree {
    max-height: none;
  }

  .role-permission-node {
    align-items: flex-start;
    flex-wrap: wrap;
  }

  .role-permission-node__code {
    max-width: 100%;
  }
}
</style>
