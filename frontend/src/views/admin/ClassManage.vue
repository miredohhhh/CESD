<template>
  <el-card>
    <template #header>
      <div class="page-header">
        <span>班级管理</span>
        <el-button v-if="canCreate" type="primary" @click="openCreateDialog"> 新增班级 </el-button>
      </div>
    </template>

    <el-form :model="filters" inline>
      <el-form-item label="关键字">
        <el-input v-model="filters.keyword" clearable placeholder="班级名称 / 编码 / 年级" />
      </el-form-item>
      <el-form-item label="专业">
        <el-select
          v-model="filters.majorId"
          clearable
          filterable
          placeholder="全部专业"
          style="width: 180px"
        >
          <el-option
            v-for="major in majors"
            :key="major.id"
            :label="major.majorName"
            :value="major.id"
          />
        </el-select>
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
      <el-table-column prop="className" label="班级名称" min-width="160" />
      <el-table-column prop="classCode" label="班级编码" min-width="140" />
      <el-table-column label="所属专业" min-width="160">
        <template #default="{ row }">{{ getMajorName(row.majorId) }}</template>
      </el-table-column>
      <el-table-column prop="grade" label="年级" width="120" />
      <el-table-column prop="counselorName" label="辅导员" width="120" />
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑班级' : '新增班级'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="所属专业" prop="majorId">
          <el-select v-model="form.majorId" filterable placeholder="请选择专业" style="width: 100%">
            <el-option
              v-for="major in majors"
              :key="major.id"
              :label="major.majorName"
              :value="major.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="班级名称" prop="className">
          <el-input v-model="form.className" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="班级编码" prop="classCode">
          <el-input v-model="form.classCode" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-input v-model="form.grade" maxlength="20" placeholder="如 2023级" show-word-limit />
        </el-form-item>
        <el-form-item label="辅导员" prop="counselorName">
          <el-input v-model="form.counselorName" maxlength="50" show-word-limit />
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
  createClassInfo,
  deleteClassInfo,
  getClassesPage,
  getMajorsPage,
  updateClassInfo,
  type ClassInfoVO,
  type CreateClassInfoPayload,
  type MajorInfoVO,
} from '@/api/base'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const saving = ref(false)
const records = ref<ClassInfoVO[]>([])
const majors = ref<MajorInfoVO[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const filters = reactive({
  keyword: '',
  majorId: undefined as number | undefined,
  status: undefined as number | undefined,
})

const page = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

const form = reactive<CreateClassInfoPayload>({
  className: '',
  classCode: '',
  majorId: 0,
  grade: '',
  counselorName: '',
  status: 1,
})

const rules: FormRules = {
  majorId: [{ required: true, message: '请选择专业', trigger: 'change' }],
  className: [{ required: true, message: '请输入班级名称', trigger: 'blur' }],
  grade: [{ required: true, message: '请输入年级', trigger: 'blur' }],
}

const canCreate = computed(() => userStore.hasPermission('admin:class:create'))
const canUpdate = computed(() => userStore.hasPermission('admin:class:update'))
const canDelete = computed(() => userStore.hasPermission('admin:class:delete'))

function getMajorName(majorId: number) {
  return majors.value.find((item) => item.id === majorId)?.majorName ?? '-'
}

async function fetchMajors() {
  const result = await getMajorsPage({ pageNum: 1, pageSize: 100, status: 1 })
  majors.value = result.records
}

async function fetchList() {
  loading.value = true
  try {
    const result = await getClassesPage({
      pageNum: page.pageNum,
      pageSize: page.pageSize,
      keyword: filters.keyword || undefined,
      status: filters.status,
    })
    let list = result.records
    if (filters.majorId) {
      list = list.filter((item) => item.majorId === filters.majorId)
    }
    records.value = list
    page.total = filters.majorId ? list.length : result.total
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
  filters.majorId = undefined
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
    className: '',
    classCode: '',
    majorId: majors.value[0]?.id ?? 0,
    grade: '',
    counselorName: '',
    status: 1,
  })
}

function openCreateDialog() {
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(row: ClassInfoVO) {
  editingId.value = row.id
  Object.assign(form, {
    className: row.className,
    classCode: row.classCode ?? '',
    majorId: row.majorId,
    grade: row.grade,
    counselorName: row.counselorName ?? '',
    status: row.status,
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (editingId.value) {
      await updateClassInfo(editingId.value, form)
    } else {
      await createClassInfo(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: ClassInfoVO) {
  await ElMessageBox.confirm(`确认删除班级「${row.className}」？`, '删除确认', { type: 'warning' })
  await deleteClassInfo(row.id)
  ElMessage.success('删除成功')
  fetchList()
}

onMounted(async () => {
  await fetchMajors()
  await fetchList()
})
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
