<template>
  <el-card>
    <template #header>
      <div class="page-header">
        <span>成绩重算</span>
      </div>
    </template>

    <el-alert
      class="mb-16"
      title="当前为管理端成绩重算入口，操作会更新成绩统计和排名。"
      type="warning"
      show-icon
      :closable="false"
    />

    <el-tabs v-model="activeTab">
      <el-tab-pane label="单个学生重算" name="student">
        <el-form label-width="110px" class="form">
          <el-form-item label="选择学生">
            <el-select
              v-model="selectedStudentId"
              filterable
              placeholder="请选择学生"
              style="width: 360px"
            >
              <el-option
                v-for="student in students"
                :key="student.id"
                :label="`${student.studentNo} - ${student.name}`"
                :value="student.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              :loading="running"
              :disabled="!canRecalculate"
              @click="handleStudentRecalculate"
            >
              重算该学生成绩
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="班级重算" name="class">
        <el-form label-width="110px" class="form">
          <el-form-item label="选择班级">
            <el-select
              v-model="selectedClassId"
              filterable
              placeholder="请选择班级"
              style="width: 360px"
            >
              <el-option
                v-for="classItem in classes"
                :key="classItem.id"
                :label="`${classItem.className} / ${classItem.grade}`"
                :value="classItem.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              :loading="running"
              :disabled="!canRecalculate"
              @click="handleClassRecalculate"
            >
              重算该班级成绩
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="专业重算" name="major">
        <el-form label-width="110px" class="form">
          <el-form-item label="选择专业">
            <el-select
              v-model="selectedMajorId"
              filterable
              placeholder="请选择专业"
              style="width: 360px"
            >
              <el-option
                v-for="major in majors"
                :key="major.id"
                :label="`${major.majorName} / ${major.majorCode || '-'}`"
                :value="major.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              :loading="running"
              :disabled="!canRecalculate"
              @click="handleMajorRecalculate"
            >
              重算该专业成绩
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="全量重算" name="all">
        <el-alert
          class="mb-16"
          title="全量重算会重新计算所有学生成绩和排名，可能耗时较长。"
          type="error"
          show-icon
          :closable="false"
        />
        <el-button
          type="danger"
          :loading="running"
          :disabled="!canRecalculate"
          @click="handleAllRecalculate"
        >
          重算全部成绩
        </el-button>
      </el-tab-pane>
    </el-tabs>

    <el-table v-if="operationLogs.length" class="result-table" :data="operationLogs" border>
      <el-table-column prop="type" label="操作类型" width="150" />
      <el-table-column prop="target" label="操作对象" min-width="220" />
      <el-table-column prop="time" label="操作时间" width="180" />
      <el-table-column prop="status" label="状态" width="100" />
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getClassesPage,
  getMajorsPage,
  getStudentsPage,
  type ClassInfoVO,
  type MajorInfoVO,
  type StudentVO,
} from '@/api/base'
import {
  recalculateAllScores,
  recalculateClassScores,
  recalculateMajorScores,
  recalculateStudentScore,
} from '@/api/score'
import { useUserStore } from '@/stores/user'

interface OperationLog {
  type: string
  target: string
  time: string
  status: string
}

const userStore = useUserStore()
const activeTab = ref('student')
const running = ref(false)
const students = ref<StudentVO[]>([])
const classes = ref<ClassInfoVO[]>([])
const majors = ref<MajorInfoVO[]>([])
const selectedStudentId = ref<number>()
const selectedClassId = ref<number>()
const selectedMajorId = ref<number>()
const operationLogs = ref<OperationLog[]>([])

const canRecalculate = computed(() => userStore.hasPermission('admin:score:recalculate'))

async function fetchOptions() {
  const [studentResult, classResult, majorResult] = await Promise.all([
    getStudentsPage({ pageNum: 1, pageSize: 100, status: 1 }),
    getClassesPage({ pageNum: 1, pageSize: 100, status: 1 }),
    getMajorsPage({ pageNum: 1, pageSize: 100, status: 1 }),
  ])
  students.value = studentResult.records
  classes.value = classResult.records
  majors.value = majorResult.records
}

function addLog(type: string, target: string) {
  operationLogs.value.unshift({
    type,
    target,
    time: new Date().toLocaleString(),
    status: '成功',
  })
}

async function runWithConfirm(
  message: string,
  action: () => Promise<unknown>,
  type: string,
  target: string,
) {
  await ElMessageBox.confirm(message, '操作确认', { type: 'warning' })
  running.value = true
  try {
    await action()
    addLog(type, target)
    ElMessage.success('重算成功')
  } finally {
    running.value = false
  }
}

function handleStudentRecalculate() {
  const student = students.value.find((item) => item.id === selectedStudentId.value)
  if (!selectedStudentId.value || !student) {
    ElMessage.warning('请选择学生')
    return
  }
  runWithConfirm(
    `确认重算学生「${student.name}」的成绩？`,
    () => recalculateStudentScore(selectedStudentId.value!),
    '学生重算',
    `${student.studentNo} - ${student.name}`,
  )
}

function handleClassRecalculate() {
  const classItem = classes.value.find((item) => item.id === selectedClassId.value)
  if (!selectedClassId.value || !classItem) {
    ElMessage.warning('请选择班级')
    return
  }
  runWithConfirm(
    `确认重算班级「${classItem.className}」的成绩？`,
    () => recalculateClassScores(selectedClassId.value!),
    '班级重算',
    classItem.className,
  )
}

function handleMajorRecalculate() {
  const major = majors.value.find((item) => item.id === selectedMajorId.value)
  if (!selectedMajorId.value || !major) {
    ElMessage.warning('请选择专业')
    return
  }
  runWithConfirm(
    `确认重算专业「${major.majorName}」的成绩？`,
    () => recalculateMajorScores(selectedMajorId.value!),
    '专业重算',
    major.majorName,
  )
}

function handleAllRecalculate() {
  runWithConfirm('确认重算全部学生成绩？', recalculateAllScores, '全量重算', '全部学生')
}

onMounted(fetchOptions)
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.mb-16 {
  margin-bottom: 16px;
}

.form {
  padding-top: 12px;
}

.result-table {
  margin-top: 20px;
}
</style>
