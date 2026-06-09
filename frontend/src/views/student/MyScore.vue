<template>
  <section class="admin-page my-score-page" data-testid="my-score-page" v-loading="loading">
    <AdminPageHeader
      eyebrow="学生成绩"
      title="我的成绩"
      description="查看我的综合测评总分、分类得分、班级排名和专业排名。"
    >
      <template #actions>
        <el-button data-testid="my-score-back-button" @click="goApplications">
          返回我的申报
        </el-button>
        <el-button
          type="primary"
          data-testid="my-score-refresh-button"
          :loading="loading"
          :disabled="!canViewScore || !hasStudentBinding"
          @click="loadData(true)"
        >
          刷新成绩
        </el-button>
      </template>
    </AdminPageHeader>

    <el-alert
      v-if="!hasStudentBinding"
      class="my-score-alert"
      title="当前账号未绑定学生信息"
      description="请联系管理员维护学生档案绑定关系后再查看综合测评成绩。"
      type="warning"
      show-icon
      :closable="false"
    />

    <el-alert
      v-else-if="!canViewScore"
      class="my-score-alert"
      title="当前账号暂无成绩查看权限"
      description="请确认当前角色已分配 student:score:view 权限。"
      type="warning"
      show-icon
      :closable="false"
    />

    <template v-else>
      <el-alert
        v-if="loadError"
        class="my-score-alert"
        :title="loadError"
        description="页面不会自动触发成绩重算，可等待材料审核通过后刷新，或联系管理员处理。"
        type="warning"
        show-icon
        :closable="false"
      />

      <template v-if="initialized && score">
        <div class="score-overview-grid" data-testid="my-score-overview">
          <el-card class="score-overview-card score-overview-card--total" shadow="never">
            <div class="score-overview-card__label">综合测评总分</div>
            <div class="score-overview-card__value">{{ formatScore(score.totalScore) }}</div>
            <div class="score-overview-card__meta">由后端成绩汇总结果返回</div>
          </el-card>

          <el-card class="score-overview-card score-overview-card--class" shadow="never">
            <div class="score-overview-card__label">班级排名</div>
            <div class="score-overview-card__value score-overview-card__value--rank">
              {{ formatRank(score.classRank) }}
            </div>
            <div class="score-overview-card__meta">同班级综合测评排名</div>
          </el-card>

          <el-card class="score-overview-card score-overview-card--major" shadow="never">
            <div class="score-overview-card__label">专业排名</div>
            <div class="score-overview-card__value score-overview-card__value--rank">
              {{ formatRank(score.majorRank) }}
            </div>
            <div class="score-overview-card__meta">同专业综合测评排名</div>
          </el-card>
        </div>

        <el-card
          class="admin-table-card my-score-info-card"
          shadow="never"
          data-testid="my-score-student-info"
        >
          <template #header>
            <div class="admin-card-header">
              <div>
                <div class="admin-card-header__title">学生与成绩状态</div>
                <div class="admin-card-header__meta">
                  展示当前登录学生的基础信息、成绩状态和最近计算时间。
                </div>
              </div>
              <el-tag :type="scoreStatusTagType">{{ scoreStatusLabel }}</el-tag>
            </div>
          </template>

          <div class="my-score-info-grid">
            <div class="my-score-info-item">
              <div class="my-score-info-item__label">姓名</div>
              <div class="my-score-info-item__value">{{ score.studentName || '-' }}</div>
            </div>
            <div class="my-score-info-item">
              <div class="my-score-info-item__label">学号</div>
              <div class="my-score-info-item__value">{{ score.studentNo || '-' }}</div>
            </div>
            <div class="my-score-info-item">
              <div class="my-score-info-item__label">专业</div>
              <div class="my-score-info-item__value">{{ score.majorName || '-' }}</div>
            </div>
            <div class="my-score-info-item">
              <div class="my-score-info-item__label">班级</div>
              <div class="my-score-info-item__value">{{ score.className || '-' }}</div>
            </div>
            <div class="my-score-info-item">
              <div class="my-score-info-item__label">成绩状态</div>
              <div class="my-score-info-item__value">
                <el-tag :type="scoreStatusTagType">{{ scoreStatusLabel }}</el-tag>
              </div>
            </div>
            <div class="my-score-info-item">
              <div class="my-score-info-item__label">计算时间</div>
              <div class="my-score-info-item__value">{{ formatTime(score.calculateTime) }}</div>
            </div>
          </div>
        </el-card>

        <el-card class="admin-table-card my-score-table-card" shadow="never">
          <template #header>
            <div class="admin-card-header">
              <div>
                <div class="admin-card-header__title">分类成绩明细</div>
                <div class="admin-card-header__meta">
                  按综合测评分类展示后端返回的分类得分和计算时间。
                </div>
              </div>
              <span class="admin-card-header__meta">共 {{ categoryScores.length }} 条</span>
            </div>
          </template>

          <el-table
            :data="categoryScores"
            class="my-score-category-table"
            data-testid="my-score-category-table"
            :empty-text="categoryEmptyText"
          >
            <el-table-column label="分类名称" min-width="180">
              <template #default="{ row }">
                {{ row.categoryName || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="分类编码" min-width="140">
              <template #default="{ row }">
                <span class="admin-code-text">{{ row.categoryCode || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="分类得分" width="140" align="right">
              <template #default="{ row }">
                <span class="score-table-number">{{ formatScore(row.categoryScore) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="计算时间" min-width="180">
              <template #default="{ row }">
                {{ formatTime(row.calculateTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </template>

      <el-card
        v-else-if="initialized"
        class="admin-table-card score-empty-card"
        shadow="never"
        data-testid="my-score-empty"
      >
        <el-empty description="暂无成绩数据">
          <div class="score-empty-card__text">
            请等待材料审核通过并完成成绩重算后再查看。页面不会在前端自行计算成绩。
          </div>
          <el-button type="primary" plain @click="goApplications">返回我的申报</el-button>
        </el-empty>
      </el-card>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue'
import {
  getMyCategoryScores,
  getMyScore,
  type FrontendScoreSummaryVO,
  type ScoreCategorySummaryVO,
} from '@/api/frontend'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const initialized = ref(false)
const loadError = ref('')
const score = ref<FrontendScoreSummaryVO | null>(null)
const categoryScores = ref<ScoreCategorySummaryVO[]>([])

const hasStudentBinding = computed(() => Boolean(userStore.studentId))
const canViewScore = computed(() => userStore.hasPermission('student:score:view'))

const scoreStatusLabel = computed(() => {
  if (!score.value) {
    return '暂无成绩'
  }
  if (score.value.status === 0) {
    return '未启用'
  }
  return '已计算'
})

const scoreStatusTagType = computed(() => {
  if (!score.value || score.value.status === 0) {
    return 'info'
  }
  return 'success'
})

const categoryEmptyText = computed(() =>
  loadError.value ? '分类成绩暂不可用' : '暂无分类成绩明细',
)

async function loadData(showSuccess = false) {
  if (!hasStudentBinding.value || !canViewScore.value) {
    score.value = null
    categoryScores.value = []
    loadError.value = ''
    initialized.value = true
    return
  }

  loading.value = true
  loadError.value = ''
  try {
    const [scoreResult, categories] = await Promise.all([getMyScore(), getMyCategoryScores()])
    score.value = scoreResult
    categoryScores.value = categories ?? []
    if (showSuccess) {
      ElMessage.success('成绩已刷新')
    }
  } catch {
    score.value = null
    categoryScores.value = []
    loadError.value = '成绩数据暂不可用'
  } finally {
    initialized.value = true
    loading.value = false
  }
}

function formatScore(value?: number) {
  return value === undefined || value === null ? '-' : value
}

function formatRank(value?: number) {
  return value ? `第 ${value} 名` : '暂无排名'
}

function formatTime(value?: string) {
  return value || '-'
}

function goApplications() {
  router.push('/student/applications')
}

onMounted(() => loadData(false))
</script>

<style scoped>
.my-score-page {
  gap: 16px;
  --my-score-card-border: rgba(15, 23, 42, 0.06);
  --my-score-card-shadow: 0 1px 2px rgba(15, 23, 42, 0.025), 0 8px 24px rgba(15, 23, 42, 0.035);
}

.my-score-page :deep(.admin-page-header) {
  align-items: flex-start;
  gap: 12px;
  padding: 0 0 2px;
}

.my-score-page :deep(.admin-page-header__eyebrow) {
  margin-bottom: 4px;
  color: var(--app-text-tertiary);
  font-size: 12px;
  font-weight: 500;
  letter-spacing: 0.02em;
}

.my-score-page :deep(.admin-page-header h1) {
  font-size: 23px;
  line-height: 1.2;
}

.my-score-page :deep(.admin-page-header__description) {
  margin-top: 6px;
  color: var(--app-text-secondary-color);
  line-height: 1.55;
}

.my-score-alert {
  margin-bottom: 0;
}

.score-overview-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.score-overview-card {
  position: relative;
  overflow: hidden;
  border: 1px solid var(--my-score-card-border);
  border-radius: var(--app-radius);
  background: var(--app-card-bg);
  box-shadow: var(--my-score-card-shadow);
  transition:
    box-shadow 0.18s ease,
    transform 0.18s ease;
}

.score-overview-card:hover {
  box-shadow:
    0 2px 5px rgba(15, 23, 42, 0.035),
    0 12px 28px rgba(15, 23, 42, 0.05);
  transform: translateY(-1px);
}

.score-overview-card::before {
  position: absolute;
  top: 0;
  right: 0;
  left: 0;
  height: 3px;
  background: var(--score-accent, var(--app-primary-color));
  content: '';
  opacity: 0.72;
}

.score-overview-card :deep(.el-card__body) {
  min-height: 132px;
  padding: 20px 22px 18px;
}

.score-overview-card--total {
  --score-accent: var(--app-primary-color);
}

.score-overview-card--class {
  --score-accent: #16a34a;
}

.score-overview-card--major {
  --score-accent: #d97706;
}

.score-overview-card__label {
  color: var(--app-text-secondary-color);
  font-size: 13px;
  font-weight: 500;
  line-height: 1.45;
}

.score-overview-card__value {
  margin-top: 10px;
  color: var(--app-text-color);
  font-size: 36px;
  font-weight: 700;
  line-height: 1.15;
  font-variant-numeric: tabular-nums;
}

.score-overview-card--total .score-overview-card__value {
  color: var(--app-primary-color);
}

.score-overview-card--class .score-overview-card__value {
  color: #15803d;
}

.score-overview-card--major .score-overview-card__value {
  color: #b45309;
}

.score-overview-card__value--rank {
  font-size: 28px;
}

.score-overview-card__meta {
  margin-top: 8px;
  color: var(--app-text-tertiary);
  font-size: 13px;
  line-height: 1.45;
}

.my-score-info-card,
.my-score-table-card,
.score-empty-card {
  border-color: var(--my-score-card-border);
  border-radius: var(--app-radius);
  background: var(--app-card-bg);
  box-shadow: var(--my-score-card-shadow);
  overflow: hidden;
}

.my-score-info-card :deep(.el-card__header),
.my-score-table-card :deep(.el-card__header) {
  padding: 18px 20px 14px;
  border-bottom: 1px solid var(--app-border-light);
}

.my-score-info-card :deep(.el-card__body),
.my-score-table-card :deep(.el-card__body) {
  padding: 18px 20px;
}

.my-score-info-card :deep(.admin-card-header),
.my-score-table-card :deep(.admin-card-header) {
  margin-bottom: 0;
}

.my-score-info-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.my-score-info-item {
  min-width: 0;
  padding: 14px 16px;
  border: 1px solid var(--app-border-light);
  border-radius: var(--app-radius-small);
  background: #fafafa;
}

.my-score-info-item__label {
  color: var(--app-text-secondary-color);
  font-size: 12px;
  font-weight: 500;
  line-height: 1.4;
}

.my-score-info-item__value {
  margin-top: 6px;
  color: var(--app-text-color);
  font-size: 14px;
  font-weight: 600;
  line-height: 1.45;
  overflow-wrap: anywhere;
}

.my-score-category-table {
  border-radius: var(--app-radius-small);
  background: var(--app-surface-solid);
}

.my-score-category-table :deep(.el-table__cell) {
  padding: 12px 0;
  border-right: 0;
}

.my-score-category-table :deep(th.el-table__cell) {
  background: #fafafa;
  color: #344054;
  font-weight: 600;
}

.my-score-category-table :deep(td.el-table__cell) {
  color: #344054;
}

.my-score-category-table :deep(.el-table__row:hover > td.el-table__cell) {
  background: rgba(22, 119, 255, 0.028);
}

.my-score-category-table :deep(.el-table__inner-wrapper::before) {
  background: var(--app-border-light);
}

.score-table-number {
  color: var(--app-primary-color);
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

.score-empty-card {
  min-height: 280px;
}

.score-empty-card :deep(.el-card__body) {
  display: flex;
  min-height: 280px;
  align-items: center;
  justify-content: center;
}

.score-empty-card__text {
  margin-bottom: 14px;
  color: var(--app-text-secondary-color);
  font-size: 14px;
}

@media (max-width: 900px) {
  .score-overview-grid {
    grid-template-columns: 1fr;
  }

  .my-score-info-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .my-score-info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
