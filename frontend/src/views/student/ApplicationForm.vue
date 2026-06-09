<template>
  <section class="admin-page application-form-page" data-testid="application-form-page">
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

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      class="application-form"
      label-position="top"
    >
      <el-card class="admin-table-card application-form-card" shadow="never">
        <div class="application-form-card__header">
          <div>
            <div class="admin-card-header__title">申报材料信息</div>
            <div class="admin-card-header__meta">
              按顺序完成项目选择、材料说明和申请分数，保存后可在详情页继续上传附件。
            </div>
          </div>
        </div>

        <div class="application-form-content">
          <section class="application-form-section">
            <div class="application-form-section__header">
              <span class="application-form-section__step">01</span>
              <div>
                <div class="application-form-section__title">选择综测分类与项目</div>
                <div class="application-form-section__description">
                  请先选择分类，再选择该分类下的综测项目。
                </div>
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
                    <span v-if="item.score != null" class="option-extra">
                      默认 {{ item.score }} 分
                    </span>
                  </el-option>
                </el-select>
              </el-form-item>
            </div>
          </section>

          <section class="application-form-section">
            <div class="application-form-section__header">
              <span class="application-form-section__step">02</span>
              <div>
                <div class="application-form-section__title">填写申报信息</div>
                <div class="application-form-section__description">
                  标题用于列表识别，说明用于补充材料背景和依据。
                </div>
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
          </section>

          <section class="application-form-section">
            <div class="application-form-section__header">
              <span class="application-form-section__step">03</span>
              <div>
                <div class="application-form-section__title">填写申请分数</div>
                <div class="application-form-section__description">
                  选择项目后如存在默认分，系统会自动带入，可按实际情况调整。
                </div>
              </div>
            </div>

            <el-form-item label="申请分数" prop="applyScore" class="application-score-item">
              <el-input-number v-model="form.applyScore" :min="0" :precision="2" />
            </el-form-item>
          </section>

          <section class="application-form-section">
            <div class="application-form-section__header">
              <span class="application-form-section__step">04</span>
              <div>
                <div class="application-form-section__title">附件说明</div>
                <div class="application-form-section__description">
                  保存草稿后进入详情页上传或维护附件。
                </div>
              </div>
            </div>
            <el-alert
              class="application-attachment-alert"
              title="本页先创建草稿，附件在申报详情页上传。提交审核前请确认附件完整。"
              type="info"
              show-icon
              :closable="false"
            />
          </section>
        </div>

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
      </el-card>
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
.application-form-page {
  gap: 16px;
  --application-form-border: rgba(15, 23, 42, 0.06);
  --application-form-shadow: 0 1px 2px rgba(15, 23, 42, 0.025), 0 8px 24px rgba(15, 23, 42, 0.035);
}

.application-form-page :deep(.admin-page-header) {
  align-items: flex-start;
  gap: 12px;
  padding: 0 0 2px;
}

.application-form-page :deep(.admin-page-header__eyebrow) {
  margin-bottom: 4px;
  color: var(--app-text-tertiary);
  font-size: 12px;
  font-weight: 500;
  letter-spacing: 0.02em;
}

.application-form-page :deep(.admin-page-header h1) {
  font-size: 23px;
  line-height: 1.2;
}

.application-form-page :deep(.admin-page-header__description) {
  margin-top: 6px;
  color: var(--app-text-secondary-color);
  line-height: 1.55;
}

.application-form {
  display: flex;
  flex-direction: column;
}

.application-form-card {
  border-color: var(--application-form-border);
  border-radius: var(--app-radius);
  background: var(--app-card-bg);
  box-shadow: var(--application-form-shadow);
  overflow: hidden;
}

.application-form-card :deep(.el-card__body) {
  padding: 0;
}

.application-form-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 22px 28px 18px;
  border-bottom: 1px solid var(--app-border-light);
}

.application-form-content {
  max-width: 760px;
  margin: 0 auto;
  padding: 28px 32px 8px;
}

.application-form-section {
  padding-bottom: 24px;
}

.application-form-section + .application-form-section {
  padding-top: 24px;
  border-top: 1px solid var(--app-border-light);
}

.application-form-section__header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 18px;
}

.application-form-section__step {
  display: inline-flex;
  width: 28px;
  height: 28px;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(22, 119, 255, 0.16);
  border-radius: 50%;
  color: var(--app-primary-color);
  background: #f0f7ff;
  font-size: 12px;
  font-weight: 700;
  line-height: 1;
}

.application-form-section__title {
  color: var(--app-text-color);
  font-size: 15px;
  font-weight: 650;
  line-height: 1.4;
}

.application-form-section__description {
  margin-top: 3px;
  color: var(--app-text-secondary-color);
  font-size: 13px;
  line-height: 1.55;
}

.application-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 18px;
}

.application-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.application-form :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.application-form :deep(.el-form-item__label) {
  margin-bottom: 7px;
  color: #344054;
  font-size: 14px;
  font-weight: 600;
  line-height: 1.4;
}

.application-form-grid :deep(.el-select),
.application-form-grid :deep(.el-input),
.application-form-grid :deep(.el-input-number),
.application-form :deep(.el-textarea),
.application-score-item :deep(.el-input-number) {
  width: 100%;
}

.application-form :deep(.el-input__wrapper),
.application-form :deep(.el-select__wrapper),
.application-form :deep(.el-textarea__inner) {
  border-radius: var(--app-radius-small);
  background: #fbfbfc;
  box-shadow: 0 0 0 1px var(--app-border-color) inset;
}

.application-form :deep(.el-input__wrapper:hover),
.application-form :deep(.el-select__wrapper:hover),
.application-form :deep(.el-textarea__inner:hover) {
  background: #fff;
  box-shadow: 0 0 0 1px rgba(22, 119, 255, 0.26) inset;
}

.application-form :deep(.el-input__wrapper.is-focus),
.application-form :deep(.el-select__wrapper.is-focused),
.application-form :deep(.el-textarea__inner:focus) {
  background: #fff;
  box-shadow:
    0 0 0 1px rgba(22, 119, 255, 0.55) inset,
    var(--app-focus-ring);
}

.application-score-item {
  max-width: 260px;
}

.application-attachment-alert {
  border: 1px solid rgba(22, 119, 255, 0.12);
  border-radius: var(--app-radius-small);
  background: #f6fbff;
}

.application-form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 18px 28px;
  border-top: 1px solid var(--app-border-light);
  background: #fff;
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

  .application-form-card__header,
  .application-form-content,
  .application-form-actions {
    padding-right: 18px;
    padding-left: 18px;
  }

  .application-score-item {
    max-width: none;
  }

  .application-form-actions {
    justify-content: flex-start;
  }
}
</style>
