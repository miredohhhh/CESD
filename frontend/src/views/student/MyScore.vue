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

        <el-card class="admin-table-card" shadow="never" data-testid="my-score-student-info">
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

          <el-descriptions :column="2" border class="my-score-descriptions">
            <el-descriptions-item label="姓名">
              {{ score.studentName || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="学号">
              {{ score.studentNo || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="专业">
              {{ score.majorName || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="班级">
              {{ score.className || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="成绩状态">
              <el-tag :type="scoreStatusTagType">{{ scoreStatusLabel }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="计算时间">
              {{ formatTime(score.calculateTime) }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card class="admin-table-card" shadow="never">
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
            border
            stripe
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
  gap: 18px;
}

.my-score-alert {
  margin-bottom: 0;
}

.score-overview-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.score-overview-card {
  border: 1px solid #d8e0ea;
}

.score-overview-card--total {
  background: linear-gradient(135deg, #f5f9ff 0%, #edf5ff 100%);
  border-color: #bfd7ff;
}

.score-overview-card--class {
  background: linear-gradient(135deg, #f3fbf7 0%, #edf8f1 100%);
  border-color: #bfdec9;
}

.score-overview-card--major {
  background: linear-gradient(135deg, #fff9ed 0%, #fff4dc 100%);
  border-color: #ead09a;
}

.score-overview-card__label {
  color: #5f6b7a;
  font-size: 14px;
}

.score-overview-card__value {
  margin-top: 10px;
  color: #1f2a37;
  font-size: 34px;
  font-weight: 700;
  line-height: 1.15;
  font-variant-numeric: tabular-nums;
}

.score-overview-card__value--rank {
  font-size: 28px;
}

.score-overview-card__meta {
  margin-top: 8px;
  color: #7b8794;
  font-size: 13px;
}

.my-score-descriptions {
  width: 100%;
}

.score-table-number {
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

.score-empty-card {
  min-height: 280px;
}

.score-empty-card__text {
  margin-bottom: 14px;
  color: #697586;
  font-size: 14px;
}

@media (max-width: 900px) {
  .score-overview-grid {
    grid-template-columns: 1fr;
  }
}
</style>
