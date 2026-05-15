<template>
  <el-card>
    <template #header>
      <div class="page-header">
        <span>新增申报</span>
        <el-button @click="goBack">返回列表</el-button>
      </div>
    </template>

    <el-alert
      v-if="!userStore.studentId"
      class="mb-16"
      title="当前账号未绑定学生信息，无法创建申报"
      type="warning"
      show-icon
      :closable="false"
    />

    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="form">
      <el-form-item label="综测分类" prop="categoryId">
        <el-select
          v-model="form.categoryId"
          filterable
          placeholder="请选择综测分类"
          style="width: 100%"
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
          placeholder="请选择综测项目"
          style="width: 100%"
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

      <el-form-item label="申报标题" prop="title">
        <el-input v-model="form.title" maxlength="200" show-word-limit />
      </el-form-item>

      <el-form-item label="申报说明" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="5"
          maxlength="1000"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="申请分数" prop="applyScore">
        <el-input-number v-model="form.applyScore" :min="0" :precision="2" style="width: 220px" />
      </el-form-item>

      <el-form-item>
        <el-button
          type="primary"
          :loading="saving"
          :disabled="!userStore.studentId"
          @click="handleSave"
        >
          保存草稿
        </el-button>
        <el-button @click="goBack">返回列表</el-button>
      </el-form-item>
    </el-form>
  </el-card>
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
    await createMaterialApplication({
      itemId: form.itemId,
      title: form.title,
      description: form.description || undefined,
      applyScore: form.applyScore,
      status: 'DRAFT',
    })
    ElMessage.success('保存草稿成功')
    router.push('/student/applications')
  } finally {
    saving.value = false
  }
}

function goBack() {
  router.push('/student/applications')
}

onMounted(fetchOptions)
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.form {
  max-width: 760px;
}

.mb-16 {
  margin-bottom: 16px;
}

.option-extra {
  float: right;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
