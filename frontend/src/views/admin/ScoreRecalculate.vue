<template>
  <section
    class="admin-page admin-list-page score-recalculate-page"
    data-testid="score-recalculate-page"
  >
    <AdminPageHeader
      eyebrow="成绩管理"
      title="成绩重算"
      description="按学生、班级、专业或全量范围重新计算综合测评成绩和排名。"
    >
      <template #actions>
        <div class="score-header-actions">
          <el-button
            v-if="canExportScore"
            :loading="exporting"
            data-testid="score-export-summary-button"
            @click="handleExportScoreSummaries"
          >
            导出成绩汇总
          </el-button>
          <el-button
            v-if="canExportScore"
            :loading="exporting"
            data-testid="score-export-class-ranking-button"
            @click="handleExportClassRanking"
          >
            导出班级排名
          </el-button>
          <el-button
            v-if="canExportScore"
            :loading="exporting"
            data-testid="score-export-major-ranking-button"
            @click="handleExportMajorRanking"
          >
            导出专业排名
          </el-button>
        </div>
      </template>
    </AdminPageHeader>

    <el-alert
      class="score-risk-alert"
      title="当前为管理端成绩重算入口，操作会更新成绩统计和排名。"
      description="建议在基础数据、材料审核结果确认后执行；单个学生、班级、专业重算适合日常修正，全量重算适合阶段性统一刷新。"
      type="warning"
      show-icon
      :closable="false"
    />

    <el-card class="admin-table-card score-workbench-card" shadow="never">
      <div class="admin-card-header">
        <div>
          <div class="admin-card-header__title">重算范围</div>
          <div class="admin-card-header__meta">选择一个范围后执行重算，系统会保留最近操作结果</div>
        </div>
      </div>

      <el-tabs v-model="activeTab" class="score-tabs">
        <el-tab-pane label="单个学生重算" name="student">
          <div class="score-action-panel">
            <div>
              <h3>单个学生</h3>
              <p>用于单独刷新某一名学生的总分、班级排名和专业排名。</p>
            </div>
            <div class="score-action-control">
              <el-select
                v-model="selectedStudentId"
                filterable
                placeholder="请选择学生"
                class="score-action-select"
              >
                <el-option
                  v-for="student in students"
                  :key="student.id"
                  :label="`${student.studentNo} - ${student.name}`"
                  :value="student.id"
                />
              </el-select>
              <el-button
                class="score-action-button"
                type="primary"
                data-testid="score-recalculate-student-button"
                :loading="running"
                :disabled="!canRecalculate"
                @click="handleStudentRecalculate"
              >
                重算该学生成绩
              </el-button>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="班级重算" name="class">
          <div class="score-action-panel">
            <div>
              <h3>班级范围</h3>
              <p>用于刷新某个班级内全部学生的成绩和班级排名。</p>
            </div>
            <div class="score-action-control">
              <el-select
                v-model="selectedClassId"
                filterable
                placeholder="请选择班级"
                class="score-action-select"
              >
                <el-option
                  v-for="classItem in classes"
                  :key="classItem.id"
                  :label="`${classItem.className} / ${classItem.grade}`"
                  :value="classItem.id"
                />
              </el-select>
              <el-button
                class="score-action-button"
                type="primary"
                data-testid="score-recalculate-class-button"
                :loading="running"
                :disabled="!canRecalculate"
                @click="handleClassRecalculate"
              >
                重算该班级成绩
              </el-button>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="专业重算" name="major">
          <div class="score-action-panel">
            <div>
              <h3>专业范围</h3>
              <p>用于刷新某个专业内全部学生的成绩和专业排名。</p>
            </div>
            <div class="score-action-control">
              <el-select
                v-model="selectedMajorId"
                filterable
                placeholder="请选择专业"
                class="score-action-select"
              >
                <el-option
                  v-for="major in majors"
                  :key="major.id"
                  :label="`${major.majorName} / ${major.majorCode || '-'}`"
                  :value="major.id"
                />
              </el-select>
              <el-button
                class="score-action-button"
                type="primary"
                data-testid="score-recalculate-major-button"
                :loading="running"
                :disabled="!canRecalculate"
                @click="handleMajorRecalculate"
              >
                重算该专业成绩
              </el-button>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="全量重算" name="all">
          <div class="score-action-panel score-action-panel--danger">
            <div>
              <h3>全量范围</h3>
              <p>全量重算会重新计算所有学生成绩和排名，数据量较大时可能需要更长时间。</p>
            </div>
            <el-button
              class="score-action-button"
              type="primary"
              data-testid="score-recalculate-all-button"
              :loading="running"
              :disabled="!canRecalculate"
              @click="handleAllRecalculate"
            >
              重算全部成绩
            </el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-card class="admin-table-card score-result-card" shadow="never">
      <div class="admin-card-header">
        <div>
          <div class="admin-card-header__title">最近操作结果</div>
          <div class="admin-card-header__meta">仅展示当前页面最近执行的重算操作</div>
        </div>
      </div>

      <el-table
        class="score-result-table"
        :data="operationLogs"
        empty-text="暂无重算操作记录"
        data-testid="score-recalculate-result-table"
      >
        <el-table-column prop="type" label="操作类型" width="150" show-overflow-tooltip />
        <el-table-column prop="target" label="操作对象" min-width="240" show-overflow-tooltip />
        <el-table-column prop="time" label="操作时间" width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag type="success">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { AxiosResponse } from 'axios'
import {
  getClassesPage,
  getMajorsPage,
  getStudentsPage,
  type ClassInfoVO,
  type MajorInfoVO,
  type StudentVO,
} from '@/api/base'
import {
  exportClassRanking,
  exportMajorRanking,
  exportScoreSummaries,
  recalculateAllScores,
  recalculateClassScores,
  recalculateMajorScores,
  recalculateStudentScore,
} from '@/api/score'
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue'
import { useUserStore } from '@/stores/user'
import { downloadResponseFile } from '@/utils/download'

interface OperationLog {
  type: string
  target: string
  time: string
  status: string
}

const userStore = useUserStore()
const activeTab = ref('student')
const running = ref(false)
const exporting = ref(false)
const students = ref<StudentVO[]>([])
const classes = ref<ClassInfoVO[]>([])
const majors = ref<MajorInfoVO[]>([])
const selectedStudentId = ref<number>()
const selectedClassId = ref<number>()
const selectedMajorId = ref<number>()
const operationLogs = ref<OperationLog[]>([])

const canRecalculate = computed(() => userStore.hasPermission('admin:score:recalculate'))
const canExportScore = computed(() => userStore.hasPermission('admin:score:export'))

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
  void runWithConfirm(
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
  void runWithConfirm(
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
  void runWithConfirm(
    `确认重算专业「${major.majorName}」的成绩？`,
    () => recalculateMajorScores(selectedMajorId.value!),
    '专业重算',
    major.majorName,
  )
}

function handleAllRecalculate() {
  void runWithConfirm('确认重算全部学生成绩？', recalculateAllScores, '全量重算', '全部学生')
}

async function runExport(action: () => Promise<AxiosResponse<Blob>>, filename: string) {
  exporting.value = true
  try {
    const response = await action()
    downloadResponseFile(response, filename)
    ElMessage.success('导出成功')
  } finally {
    exporting.value = false
  }
}

function handleExportScoreSummaries() {
  void runExport(() => exportScoreSummaries(), 'score-summary.xlsx')
}

function handleExportClassRanking() {
  if (!selectedClassId.value) {
    ElMessage.warning('请先选择班级')
    return
  }
  void runExport(() => exportClassRanking(selectedClassId.value!), 'class-ranking.xlsx')
}

function handleExportMajorRanking() {
  if (!selectedMajorId.value) {
    ElMessage.warning('请先选择专业')
    return
  }
  void runExport(() => exportMajorRanking(selectedMajorId.value!), 'major-ranking.xlsx')
}

onMounted(fetchOptions)
</script>

<style scoped>
.score-header-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.score-risk-alert {
  --el-alert-bg-color: #fff8eb;
  --el-alert-border-color: #ffe4b8;
  --el-alert-padding: 14px 16px;
  border: 1px solid #ffe4b8;
  border-radius: var(--app-radius-small);
}

.score-risk-alert :deep(.el-alert__title) {
  color: #ad6800;
  font-size: 14px;
  font-weight: 600;
}

.score-risk-alert :deep(.el-alert__description) {
  color: #8c5a12;
  line-height: 1.7;
}

.score-workbench-card :deep(.el-card__body),
.score-result-card :deep(.el-card__body) {
  padding: 18px 20px 20px;
}

.score-tabs {
  --el-tabs-header-height: 44px;
}

.score-tabs :deep(.el-tabs__header) {
  margin: 2px 0 16px;
}

.score-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: var(--app-border-color);
}

.score-tabs :deep(.el-tabs__item) {
  color: var(--app-text-secondary);
  font-weight: 500;
}

.score-tabs :deep(.el-tabs__item.is-active) {
  color: var(--app-primary-color);
  font-weight: 600;
}

.score-action-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  min-height: 118px;
  padding: 18px 20px;
  border: 1px solid var(--app-border-color);
  border-radius: var(--app-radius-small);
  background: #fbfcfe;
}

.score-action-panel--danger {
  border-color: #ffe4b8;
  background: #fffaf0;
}

.score-action-panel h3 {
  margin: 0;
  color: var(--app-text-color);
  font-size: 16px;
  font-weight: 600;
}

.score-action-panel p {
  max-width: 520px;
  margin: 8px 0 0;
  color: var(--app-text-secondary);
  font-size: 13px;
  line-height: 1.6;
}

.score-action-control {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  gap: 10px;
}

.score-action-select {
  width: 320px;
}

.score-action-button {
  min-width: 132px;
}

.score-result-table :deep(.el-tag) {
  border-radius: 999px;
}

@media (max-width: 900px) {
  .score-header-actions {
    justify-content: flex-start;
  }

  .score-action-panel,
  .score-action-control {
    align-items: stretch;
    flex-direction: column;
  }

  .score-action-control,
  .score-action-select {
    width: 100%;
  }
}
</style>
