<template>
  <section class="admin-page" data-testid="student-manage-page">
    <AdminPageHeader
      eyebrow="学生档案"
      title="学生管理"
      description="维护学生基础信息、账号绑定关系、专业班级归属和联系方式。"
    >
      <template #actions>
        <el-button
          v-if="canCreate"
          type="primary"
          data-testid="student-create-button"
          @click="openCreateDialog"
        >
          新增学生
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
              placeholder="学号 / 姓名 / 手机 / 邮箱"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="专业">
            <el-select v-model="filters.majorId" clearable filterable placeholder="全部专业">
              <el-option
                v-for="major in majors"
                :key="major.id"
                :label="major.majorName"
                :value="major.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="班级">
            <el-select v-model="filters.classId" clearable filterable placeholder="全部班级">
              <el-option
                v-for="classItem in filteredClasses"
                :key="classItem.id"
                :label="classItem.className"
                :value="classItem.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="年级">
            <el-input v-model="filters.grade" clearable placeholder="如 2023级" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="filters.status" clearable placeholder="全部状态">
              <el-option label="正常" :value="1" />
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
          <div class="admin-card-header__title">学生列表</div>
          <div class="admin-card-header__meta">共 {{ page.total }} 名学生</div>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="records"
        border
        stripe
        empty-text="暂无学生数据"
        data-testid="student-table"
      >
        <el-table-column prop="studentNo" label="学号" min-width="130" />
        <el-table-column prop="name" label="姓名" min-width="110" />
        <el-table-column label="绑定用户" min-width="170" show-overflow-tooltip>
          <template #default="{ row }">{{ getUserName(row.userId) }}</template>
        </el-table-column>
        <el-table-column label="专业" min-width="160">
          <template #default="{ row }">{{ getMajorName(row.majorId) }}</template>
        </el-table-column>
        <el-table-column label="班级" min-width="160">
          <template #default="{ row }">{{ getClassName(row.classId) }}</template>
        </el-table-column>
        <el-table-column prop="grade" label="年级" width="110" />
        <el-table-column prop="gender" label="性别" width="80" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '正常' : '停用' }}
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
                :data-testid="`student-edit-${row.id}`"
                @click="openEditDialog(row)"
              >
                编辑
              </el-button>
              <el-button
                v-if="canDelete"
                link
                type="danger"
                :data-testid="`student-delete-${row.id}`"
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑学生' : '新增学生'" width="820px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <section class="student-form-section">
          <h3>账号绑定</h3>
          <div class="admin-dialog-grid">
            <el-form-item label="绑定用户" prop="userId" class="admin-dialog-grid__full">
              <el-select v-model="form.userId" filterable placeholder="请选择用户">
                <el-option
                  v-for="user in users"
                  :key="user.id"
                  :label="`${user.username} - ${user.realName}`"
                  :value="user.id"
                />
              </el-select>
            </el-form-item>
          </div>
        </section>

        <section class="student-form-section">
          <h3>学籍归属</h3>
          <div class="admin-dialog-grid">
            <el-form-item label="学号" prop="studentNo">
              <el-input v-model="form.studentNo" maxlength="30" show-word-limit />
            </el-form-item>
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" maxlength="50" show-word-limit />
            </el-form-item>
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" clearable placeholder="请选择">
                <el-option label="男" value="M" />
                <el-option label="女" value="F" />
              </el-select>
            </el-form-item>
            <el-form-item label="年级" prop="grade">
              <el-input
                v-model="form.grade"
                maxlength="20"
                placeholder="如 2023级"
                show-word-limit
              />
            </el-form-item>
            <el-form-item label="专业" prop="majorId">
              <el-select v-model="form.majorId" filterable @change="form.classId = 0">
                <el-option
                  v-for="major in majors"
                  :key="major.id"
                  :label="major.majorName"
                  :value="major.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="班级" prop="classId">
              <el-select v-model="form.classId" filterable>
                <el-option
                  v-for="classItem in formClasses"
                  :key="classItem.id"
                  :label="classItem.className"
                  :value="classItem.id"
                />
              </el-select>
            </el-form-item>
          </div>
        </section>

        <section class="student-form-section">
          <h3>联系与状态</h3>
          <div class="admin-dialog-grid">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" maxlength="20" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" maxlength="100" />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status">
                <el-option label="正常" :value="1" />
                <el-option label="停用" :value="0" />
              </el-select>
            </el-form-item>
          </div>
        </section>
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
  createStudent,
  deleteStudent,
  getClassesPage,
  getMajorsPage,
  getStudentsPage,
  getUsersPage,
  updateStudent,
  type ClassInfoVO,
  type CreateStudentPayload,
  type MajorInfoVO,
  type StudentVO,
  type SysUserVO,
} from '@/api/base'
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const saving = ref(false)
const records = ref<StudentVO[]>([])
const majors = ref<MajorInfoVO[]>([])
const classes = ref<ClassInfoVO[]>([])
const users = ref<SysUserVO[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const filters = reactive({
  keyword: '',
  majorId: undefined as number | undefined,
  classId: undefined as number | undefined,
  grade: '',
  status: undefined as number | undefined,
})

const page = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

const form = reactive<CreateStudentPayload>({
  userId: 0,
  studentNo: '',
  name: '',
  gender: '',
  grade: '',
  majorId: 0,
  classId: 0,
  phone: '',
  email: '',
  status: 1,
})

const rules: FormRules = {
  userId: [{ required: true, message: '请选择绑定用户', trigger: 'change' }],
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  grade: [{ required: true, message: '请输入年级', trigger: 'blur' }],
  majorId: [{ required: true, message: '请选择专业', trigger: 'change' }],
  classId: [{ required: true, message: '请选择班级', trigger: 'change' }],
}

const canCreate = computed(() => userStore.hasPermission('admin:student:create'))
const canUpdate = computed(() => userStore.hasPermission('admin:student:update'))
const canDelete = computed(() => userStore.hasPermission('admin:student:delete'))

const filteredClasses = computed(() =>
  filters.majorId
    ? classes.value.filter((item) => item.majorId === filters.majorId)
    : classes.value,
)

const formClasses = computed(() =>
  form.majorId ? classes.value.filter((item) => item.majorId === form.majorId) : classes.value,
)

function getMajorName(id: number) {
  return majors.value.find((item) => item.id === id)?.majorName ?? '-'
}

function getClassName(id: number) {
  return classes.value.find((item) => item.id === id)?.className ?? '-'
}

function getUserName(id: number) {
  const user = users.value.find((item) => item.id === id)
  return user ? `${user.username} - ${user.realName}` : '-'
}

async function fetchOptions() {
  const [majorResult, classResult, userResult] = await Promise.all([
    getMajorsPage({ pageNum: 1, pageSize: 100, status: 1 }),
    getClassesPage({ pageNum: 1, pageSize: 100, status: 1 }),
    getUsersPage({ pageNum: 1, pageSize: 100, status: 1 }),
  ])
  majors.value = majorResult.records
  classes.value = classResult.records
  users.value = userResult.records
}

async function fetchList() {
  loading.value = true
  try {
    const result = await getStudentsPage({
      pageNum: page.pageNum,
      pageSize: page.pageSize,
      keyword: filters.keyword || undefined,
      status: filters.status,
      grade: filters.grade || undefined,
      majorId: filters.majorId,
      classId: filters.classId,
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
  filters.majorId = undefined
  filters.classId = undefined
  filters.grade = ''
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
    userId: users.value[0]?.id ?? 0,
    studentNo: '',
    name: '',
    gender: '',
    grade: '',
    majorId: majors.value[0]?.id ?? 0,
    classId: classes.value[0]?.id ?? 0,
    phone: '',
    email: '',
    status: 1,
  })
}

function openCreateDialog() {
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(row: StudentVO) {
  editingId.value = row.id
  Object.assign(form, {
    userId: row.userId,
    studentNo: row.studentNo,
    name: row.name,
    gender: row.gender ?? '',
    grade: row.grade,
    majorId: row.majorId,
    classId: row.classId,
    phone: row.phone ?? '',
    email: row.email ?? '',
    status: row.status,
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value?.validate()
  const selectedClass = classes.value.find((item) => item.id === form.classId)
  if (selectedClass && selectedClass.majorId !== form.majorId) {
    ElMessage.warning('学生专业必须与班级所属专业一致')
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await updateStudent(editingId.value, form)
    } else {
      await createStudent(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    void fetchList()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: StudentVO) {
  await ElMessageBox.confirm(`确认删除学生「${row.name}」？`, '删除确认', { type: 'warning' })
  await deleteStudent(row.id)
  ElMessage.success('删除成功')
  void fetchList()
}

onMounted(async () => {
  await fetchOptions()
  await fetchList()
})
</script>

<style scoped>
.student-form-section + .student-form-section {
  margin-top: 6px;
}

.student-form-section h3 {
  margin: 0 0 14px;
  color: #111827;
  font-size: 14px;
  font-weight: 650;
}
</style>
