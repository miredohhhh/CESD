<template>
  <section class="admin-page" data-testid="application-form-page">
    <AdminPageHeader
      eyebrow="学生申报"
      title="新增申报"
      description="选择测评分类和项目，填写申报材料信息后保存为草稿，确认无误后可在详情页提交审核。"
    >
      <template #actions>
        <el-button data-testid="application-form-back-button" @click="goBack">返回列表</el-button>
      </template>
    </AdminPageHeader>

    <el-alert
      v-if="!userStore.studentId"
      title="当前账号未绑定学生信息，无法创建申报"
      type="warning"
      show-icon
      :closable="false"
    />

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-card class="admin-table-card" shadow="never">
        <div class="admin-card-header">
          <div>
            <div class="admin-card-header__title">选择综测分类与项目</div>
            <div class="admin-card-header__meta">请先选择分类，再选择该分类下的综测项目</div>
          </div>
        </div>

        <div class="application-form-grid">
          <el-form-item label="综测分类" prop="categoryId">
            <el-select
              v-model="form.categoryId"
              filterable
              placeholder="请选择综测分类"
              @change="handleCategoryChange"
            >
              <el-option
                v-for="category in enabledCategories"
                :key="category.id"
                :label="category.categoryName"
                :value="category.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="综测项目" prop="itemId">
            <el-select
              v-model="form.itemId"
              filterable
              :placeholder="form.categoryId ? '请选择综测项目' : '请先选择综测分类'"
              @change="handleItemChange"
            >
              <el-option
                v-for="item in filteredItems"
                :key="item.id"
                :label="item.itemName"
                :value="item.id"
              >
                <span>{{ item.itemName }}</span>
                <span v-if="item.score != null" class="option-extra">默认 {{ item.score }} 分</span>
              </el-option>
            </el-select>
          </el-form-item>
        </div>
      </el-card>

      <el-card class="admin-table-card" shadow="never">
        <div class="admin-card-header">
          <div>
            <div class="admin-card-header__title">填写申报信息</div>
            <div class="admin-card-header__meta">标题用于列表识别，说明用于补充材料背景和依据</div>
          </div>
        </div>

        <el-form-item label="申报标题" prop="title">
          <el-input
            v-model="form.title"
            maxlength="200"
            show-word-limit
            placeholder="请输入申报标题"
          />
        </el-form-item>

        <el-form-item label="申报说明" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="5"
            maxlength="1000"
            show-word-limit
            placeholder="请说明申报依据、获奖情况、活动信息或其他必要材料说明"
          />
        </el-form-item>
      </el-card>

      <el-card class="admin-table-card" shadow="never">
        <div class="admin-card-header">
          <div>
            <div class="admin-card-header__title">填写申请分数</div>
            <div class="admin-card-header__meta">
              选择项目后如存在默认分，系统会自动带入，可按实际情况调整
            </div>
          </div>
        </div>

        <el-form-item label="申请分数" prop="applyScore">
          <el-input-number v-model="form.applyScore" :min="0" :precision="2" />
        </el-form-item>
      </el-card>

      <el-card class="admin-table-card" shadow="never">
        <div class="admin-card-header">
          <div>
            <div class="admin-card-header__title">附件说明</div>
            <div class="admin-card-header__meta">保存草稿后进入详情页上传或维护附件</div>
          </div>
        </div>
        <el-alert
          title="本页先创建草稿，附件在申报详情页上传。提交审核前请确认附件完整。"
          type="info"
          show-icon
          :closable="false"
        />
      </el-card>

      <div class="application-form-actions">
        <el-button @click="goBack">返回列表</el-button>
        <el-button
          type="primary"
          data-testid="application-form-save-draft-button"
          :loading="saving"
          :disabled="!userStore.studentId"
          @click="handleSave"
        >
          保存草稿
        </el-button>
      </div>
    </el-form>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
  getEvaluationCategoriesPage,
  getEvaluationItemsPage,
  type EvaluationCategoryVO,
  type EvaluationItemVO,
} from '@/api/evaluation'
import { createMaterialApplication, type CreateMaterialApplicationPayload } from '@/api/material'
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const saving = ref(false)
const categories = ref<EvaluationCategoryVO[]>([])
const items = ref<EvaluationItemVO[]>([])

const form = reactive<CreateMaterialApplicationPayload & { categoryId?: number }>({
  categoryId: undefined,
  itemId: 0,
  title: '',
  description: '',
  applyScore: undefined,
  status: 'DRAFT',
})

const rules: FormRules = {
  categoryId: [{ required: true, message: '请选择综测分类', trigger: 'change' }],
  itemId: [{ required: true, message: '请选择综测项目', trigger: 'change' }],
  title: [{ required: true, message: '请输入申报标题', trigger: 'blur' }],
  applyScore: [{ required: true, message: '请输入申请分数', trigger: 'change' }],
}

const enabledCategories = computed(() =>
  categories.value.filter((item) => item.status === undefined || item.status === 1),
)

const filteredItems = computed(() =>
  items.value.filter(
    (item) =>
      (!form.categoryId || item.categoryId === form.categoryId) &&
      (item.status === undefined || item.status === 1),
  ),
)

async function fetchOptions() {
  const [categoryResult, itemResult] = await Promise.all([
    getEvaluationCategoriesPage({ pageNum: 1, pageSize: 100, status: 1 }),
    getEvaluationItemsPage({ pageNum: 1, pageSize: 100, status: 1 }),
  ])
  categories.value = categoryResult.records
  items.value = itemResult.records
}

function handleCategoryChange() {
  form.itemId = 0
}

function handleItemChange(itemId: number) {
  const item = items.value.find((candidate) => candidate.id === itemId)
  if (item?.score != null) {
    form.applyScore = item.score
  }
}

async function handleSave() {
  if (!userStore.studentId) {
    ElMessage.warning('当前账号未绑定学生信息，无法创建申报')
    return
  }
  await formRef.value?.validate()
  saving.value = true
  try {
    const created = await createMaterialApplication({
      itemId: form.itemId,
      title: form.title,
      description: form.description || undefined,
      applyScore: form.applyScore,
      status: 'DRAFT',
    })
    ElMessage.success('保存草稿成功')
    void router.push(`/student/applications/${created.id}`)
  } finally {
    saving.value = false
  }
}

function goBack() {
  void router.push('/student/applications')
}

onMounted(fetchOptions)
</script>

<style scoped>
.application-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 18px;
}

.application-form-grid :deep(.el-select),
.application-form-grid :deep(.el-input),
.application-form-grid :deep(.el-input-number) {
  width: 100%;
}

.application-form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 2px;
}

.option-extra {
  float: right;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

@media (max-width: 760px) {
  .application-form-grid {
    grid-template-columns: 1fr;
  }

  .application-form-actions {
    justify-content: flex-start;
  }
}
</style>
