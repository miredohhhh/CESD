<template>
  <el-card>
    <template #header>
      <div class="page-header">
        <span>待审核列表</span>
        <el-button @click="fetchList">刷新</el-button>
      </div>
    </template>

    <el-form :model="filters" inline>
      <el-form-item label="关键字">
        <el-input v-model="filters.keyword" clearable placeholder="标题 / 说明" />
      </el-form-item>
      <el-form-item label="学生姓名">
        <el-input v-model="filters.studentName" clearable placeholder="学生姓名" />
      </el-form-item>
      <el-form-item label="学号">
        <el-input v-model="filters.studentNo" clearable placeholder="学号" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="filters.status" clearable placeholder="状态" style="width: 140px">
          <el-option label="待审核" value="SUBMITTED" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="records" border>
      <el-table-column prop="title" label="材料标题" min-width="180" />
      <el-table-column prop="studentName" label="学生姓名" width="110" />
      <el-table-column prop="studentNo" label="学号" width="130" />
      <el-table-column prop="majorName" label="专业" min-width="140" />
      <el-table-column prop="className" label="班级" min-width="140" />
      <el-table-column prop="categoryName" label="综测分类" min-width="140" />
      <el-table-column prop="itemName" label="综测项目" min-width="160" />
      <el-table-column prop="applyScore" label="申请分" width="90" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="getMaterialStatusTagType(row.status)">
            {{ getMaterialStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="attachmentCount" label="附件数" width="90" />
      <el-table-column prop="submitTime" label="提交时间" min-width="170" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="goDetail(row.id)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page.pageNo"
      v-model:page-size="page.pageSize"
      class="pager"
      :total="page.total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @current-change="fetchList"
      @size-change="handleSizeChange"
    />
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getPendingApplicationsPage, type PendingMaterialApplicationVO } from '@/api/frontend'
import {
  getMaterialStatusTagType,
  getMaterialStatusText,
  type MaterialStatus,
} from '@/utils/status'

const router = useRouter()
const loading = ref(false)
const records = ref<PendingMaterialApplicationVO[]>([])

const filters = reactive({
  keyword: '',
  studentName: '',
  studentNo: '',
  status: 'SUBMITTED' as MaterialStatus,
})

const page = reactive({
  pageNo: 1,
  pageSize: 10,
  total: 0,
})

async function fetchList() {
  loading.value = true
  try {
    const result = await getPendingApplicationsPage({
      pageNo: page.pageNo,
      pageSize: page.pageSize,
      keyword: filters.keyword || undefined,
      studentName: filters.studentName || undefined,
      studentNo: filters.studentNo || undefined,
      status: filters.status,
    })
    records.value = result.records
    page.total = result.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.pageNo = 1
  fetchList()
}

function handleReset() {
  filters.keyword = ''
  filters.studentName = ''
  filters.studentNo = ''
  filters.status = 'SUBMITTED'
  handleSearch()
}

function handleSizeChange() {
  page.pageNo = 1
  fetchList()
}

function goDetail(id: number) {
  router.push(`/student/applications/${id}`)
}

onMounted(fetchList)
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
