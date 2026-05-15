<template>
  <el-card>
    <template #header>
      <div class="page-header">
        <span>我的成绩</span>
        <div>
          <el-button @click="goApplications">返回我的申报</el-button>
          <el-button
            type="primary"
            :loading="loading"
            :disabled="!canViewScore"
            @click="loadData(true)"
          >
            刷新成绩
          </el-button>
        </div>
      </div>
    </template>

    <el-alert
      v-if="!userStore.studentId"
      class="mb-16"
      title="当前账号未绑定学生信息"
      type="warning"
      show-icon
      :closable="false"
    />

    <el-empty v-else-if="empty" description="暂无成绩数据，请先完成审核并重算成绩" />

    <template v-else>
      <el-row :gutter="16" class="mb-16">
        <el-col :span="8">
          <el-card shadow="never">
            <el-statistic title="总分" :value="score?.totalScore ?? 0" />
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never">
            <el-statistic title="班级排名" :value="score?.classRank ?? 0" />
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never">
            <el-statistic title="专业排名" :value="score?.majorRank ?? 0" />
          </el-card>
        </el-col>
      </el-row>

      <el-descriptions title="学生信息" :column="2" border class="mb-16">
        <el-descriptions-item label="姓名">{{ score?.studentName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ score?.studentNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ score?.majorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ score?.className || '-' }}</el-descriptions-item>
        <el-descriptions-item label="计算时间">{{
          score?.calculateTime || '-'
        }}</el-descriptions-item>
      </el-descriptions>

      <el-table v-loading="loading" :data="categoryScores" border>
        <el-table-column prop="categoryName" label="分类名称" min-width="160" />
        <el-table-column prop="categoryCode" label="分类编码" min-width="130" />
        <el-table-column prop="categoryScore" label="分类得分" width="120" />
        <el-table-column prop="calculateTime" label="计算时间" min-width="170" />
      </el-table>
    </template>
  </el-card>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
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
const score = ref<FrontendScoreSummaryVO | null>(null)
const categoryScores = ref<ScoreCategorySummaryVO[]>([])

const canViewScore = computed(() => userStore.hasPermission('student:score:view'))
const empty = computed(() => !score.value)

async function loadData(showSuccess = false) {
  if (!userStore.studentId) {
    score.value = null
    categoryScores.value = []
    return
  }
  loading.value = true
  try {
    const [scoreResult, categories] = await Promise.all([getMyScore(), getMyCategoryScores()])
    score.value = scoreResult
    categoryScores.value = categories
    if (showSuccess) {
      ElMessage.success('成绩已刷新')
    }
  } catch {
    score.value = null
    categoryScores.value = []
  } finally {
    loading.value = false
  }
}

function goApplications() {
  router.push('/student/applications')
}

onMounted(() => loadData(false))
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
</style>
