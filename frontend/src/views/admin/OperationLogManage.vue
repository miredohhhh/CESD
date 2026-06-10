<template>
  <section class="admin-page admin-list-page" data-testid="operation-log-page">
    <AdminPageHeader
      eyebrow="系统审计"
      title="操作日志管理"
      description="集中查看关键业务操作记录，辅助追踪账号行为、接口调用结果和异常信息。"
    >
      <template #actions>
        <el-button :loading="loading" @click="fetchList">刷新</el-button>
      </template>
    </AdminPageHeader>

    <el-card class="admin-filter-card" shadow="never">
      <el-form :model="filters" label-position="top">
        <div class="admin-filter-grid">
          <el-form-item label="用户名">
            <el-input
              v-model="filters.username"
              clearable
              placeholder="输入用户名"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="业务模块">
            <el-input
              v-model="filters.operationModule"
              clearable
              placeholder="例如 USER / EXPORT"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="操作类型">
            <el-input
              v-model="filters.operationType"
              clearable
              placeholder="例如 CREATE / UPDATE"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="执行结果">
            <el-select v-model="filters.result" clearable placeholder="全部">
              <el-option label="成功" value="SUCCESS" />
              <el-option label="失败" value="FAIL" />
            </el-select>
          </el-form-item>
          <el-form-item label="操作时间" class="admin-filter-grid__wide">
            <el-date-picker
              v-model="timeRange"
              type="datetimerange"
              value-format="YYYY-MM-DDTHH:mm:ss"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              range-separator="至"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label=" ">
            <div class="admin-filter-actions">
              <el-button
                type="primary"
                data-testid="operation-log-search-button"
                @click="handleSearch"
              >
                查询
              </el-button>
              <el-button data-testid="operation-log-reset-button" @click="handleReset">
                重置
              </el-button>
            </div>
          </el-form-item>
        </div>
      </el-form>
    </el-card>

    <el-card class="admin-table-card" shadow="never">
      <div class="admin-card-header">
        <div>
          <div class="admin-card-header__title">日志列表</div>
          <div class="admin-card-header__meta">共 {{ page.total }} 条操作记录</div>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="records"
        empty-text="暂无操作日志"
        data-testid="operation-log-table"
      >
        <el-table-column prop="username" label="用户名" min-width="140" show-overflow-tooltip />
        <el-table-column prop="realName" label="真实姓名" min-width="140" show-overflow-tooltip />
        <el-table-column prop="roleCode" label="角色" width="110" />
        <el-table-column
          prop="operationModule"
          label="模块"
          min-width="140"
          show-overflow-tooltip
        />
        <el-table-column prop="operationType" label="类型" min-width="140" show-overflow-tooltip />
        <el-table-column
          prop="operationDesc"
          label="操作说明"
          min-width="220"
          show-overflow-tooltip
        />
        <el-table-column label="方法" width="90">
          <template #default="{ row }">
            <el-tag :type="getMethodTagType(row.requestMethod)" size="small">
              {{ row.requestMethod || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestUri" label="请求 URI" min-width="220" show-overflow-tooltip />
        <el-table-column label="结果" width="110">
          <template #default="{ row }">
            <el-tag :type="getResultTagType(row.result)">{{ getResultText(row.result) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ipAddress" label="IP 地址" min-width="130" />
        <el-table-column prop="operationTime" label="操作时间" min-width="170" />
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <div class="admin-table-actions">
              <el-button
                link
                type="primary"
                :data-testid="`operation-log-detail-${row.id}`"
                @click="openDetail(row)"
              >
                查看
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

    <el-dialog v-model="detailVisible" title="操作日志详情" width="880px" class="admin-list-dialog">
      <div v-if="detail" class="log-detail">
        <section class="log-detail-section">
          <div class="log-detail-section__title">基础信息</div>
          <div class="log-detail-grid">
            <DetailItem label="日志 ID" :value="detail.id" />
            <DetailItem label="执行结果">
              <el-tag :type="getResultTagType(detail.result)">
                {{ getResultText(detail.result) }}
              </el-tag>
            </DetailItem>
            <DetailItem label="业务模块" :value="detail.operationModule" />
            <DetailItem label="操作类型" :value="detail.operationType" />
            <DetailItem label="操作说明" :value="detail.operationDesc" full />
          </div>
        </section>

        <section class="log-detail-section">
          <div class="log-detail-section__title">操作人信息</div>
          <div class="log-detail-grid">
            <DetailItem label="用户 ID" :value="detail.userId" />
            <DetailItem label="用户名" :value="detail.username" />
            <DetailItem label="真实姓名" :value="detail.realName" />
            <DetailItem label="角色编码" :value="detail.roleCode" />
          </div>
        </section>

        <section class="log-detail-section">
          <div class="log-detail-section__title">请求信息</div>
          <div class="log-detail-grid">
            <DetailItem label="请求方法">
              <el-tag :type="getMethodTagType(detail.requestMethod)" size="small">
                {{ detail.requestMethod || '-' }}
              </el-tag>
            </DetailItem>
            <DetailItem label="IP 地址" :value="detail.ipAddress" />
            <DetailItem label="请求 URI" :value="detail.requestUri" full />
            <DetailItem label="请求参数" full>
              <CopyableText
                :text="formatLogText(detail.requestParams)"
                copy-label="请求参数"
                @copy="copyText"
              />
            </DetailItem>
            <DetailItem label="User-Agent" full>
              <CopyableText
                :text="formatLogText(detail.userAgent)"
                copy-label="User-Agent"
                @copy="copyText"
              />
            </DetailItem>
          </div>
        </section>

        <section class="log-detail-section">
          <div class="log-detail-section__title">响应信息</div>
          <div class="log-detail-grid">
            <DetailItem label="处理结果">
              <el-tag :type="getResultTagType(detail.result)">
                {{ getResultText(detail.result) }}
              </el-tag>
            </DetailItem>
          </div>
        </section>

        <section class="log-detail-section">
          <div class="log-detail-section__title">异常信息</div>
          <div class="log-detail-grid">
            <DetailItem label="错误消息" full>
              <CopyableText
                :text="formatLogText(detail.errorMessage)"
                copy-label="错误消息"
                @copy="copyText"
              />
            </DetailItem>
          </div>
        </section>

        <section class="log-detail-section">
          <div class="log-detail-section__title">时间信息</div>
          <div class="log-detail-grid">
            <DetailItem label="操作时间" :value="detail.operationTime" />
            <DetailItem label="记录创建时间" :value="detail.createTime" />
          </div>
        </section>
      </div>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { defineComponent, h, onMounted, reactive, ref } from 'vue'
import { ElButton, ElMessage } from 'element-plus'
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue'
import { getOperationLogDetail, getOperationLogsPage, type OperationLogVO } from '@/api/base'

type TagType = 'success' | 'info' | 'warning' | 'danger' | 'primary'

const loading = ref(false)
const detailVisible = ref(false)
const records = ref<OperationLogVO[]>([])
const detail = ref<OperationLogVO | null>(null)
const timeRange = ref<string[]>([])

const filters = reactive({
  username: '',
  operationModule: '',
  operationType: '',
  result: '',
})

const page = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

const sensitiveKeys = [
  'password',
  'oldpassword',
  'newpassword',
  'confirmpassword',
  'token',
  'authorization',
]

const DetailItem = defineComponent({
  name: 'DetailItem',
  props: {
    label: { type: String, required: true },
    value: { type: [String, Number], default: undefined },
    full: { type: Boolean, default: false },
  },
  setup(props, { slots }) {
    return () =>
      h(
        'div',
        {
          class: ['log-detail-item', props.full ? 'log-detail-item--full' : ''],
        },
        [
          h('div', { class: 'log-detail-label' }, props.label),
          h('div', { class: 'log-detail-value' }, slots.default?.() || formatValue(props.value)),
        ],
      )
  },
})

const CopyableText = defineComponent({
  name: 'CopyableText',
  props: {
    text: { type: String, required: true },
    copyLabel: { type: String, required: true },
  },
  emits: ['copy'],
  setup(props, { emit }) {
    return () =>
      h('div', { class: 'copyable-block' }, [
        h('div', { class: 'copyable-block__header' }, [
          h(
            ElButton,
            {
              size: 'small',
              text: true,
              disabled: !props.text || props.text === '-',
              onClick: () => emit('copy', props.text, props.copyLabel),
            },
            () => '复制',
          ),
        ]),
        h('pre', { class: 'copyable-text' }, props.text || '-'),
      ])
  },
})

onMounted(() => {
  void fetchList()
})

function formatValue(value?: string | number) {
  return value === undefined || value === null || value === '' ? '-' : String(value)
}

function getResultTagType(result?: string): TagType {
  return result === 'SUCCESS' ? 'success' : 'danger'
}

function getResultText(result?: string) {
  if (result === 'SUCCESS') {
    return '成功'
  }
  if (result === 'FAIL') {
    return '失败'
  }
  return result || '-'
}

function getMethodTagType(method?: string): TagType {
  const normalized = String(method || '').toUpperCase()
  if (normalized === 'GET') {
    return 'success'
  }
  if (normalized === 'POST') {
    return 'primary'
  }
  if (normalized === 'PUT' || normalized === 'PATCH') {
    return 'warning'
  }
  if (normalized === 'DELETE') {
    return 'danger'
  }
  return 'info'
}

function isSensitiveKey(key: string) {
  const normalized = key.toLowerCase()
  return sensitiveKeys.some((item) => normalized.includes(item))
}

function sanitizeJsonValue(value: unknown): unknown {
  if (Array.isArray(value)) {
    return value.map((item) => sanitizeJsonValue(item))
  }
  if (value && typeof value === 'object') {
    return Object.fromEntries(
      Object.entries(value).map(([key, item]) => [
        key,
        isSensitiveKey(key) ? '******' : sanitizeJsonValue(item),
      ]),
    )
  }
  return value
}

function redactPlainText(text: string) {
  return text
    .replace(
      /("(?:password|oldPassword|newPassword|confirmPassword|token|Authorization)"\s*:\s*)("[^"]*"|[^,\n}\]]+)/gi,
      '$1"******"',
    )
    .replace(
      /((?:password|oldPassword|newPassword|confirmPassword|token|Authorization)\s*[:=]\s*)(Bearer\s+)?[^\s,;}]+/gi,
      '$1******',
    )
}

function formatLogText(text?: string) {
  if (!text || !text.trim()) {
    return '-'
  }

  try {
    return JSON.stringify(sanitizeJsonValue(JSON.parse(text)), null, 2)
  } catch {
    return redactPlainText(text)
  }
}

async function copyText(text: string, label: string) {
  if (!text || text === '-') {
    ElMessage.warning('没有可复制内容')
    return
  }

  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success(`${label}已复制`)
  } catch {
    ElMessage.error('复制失败，请手动选择文本')
  }
}

async function fetchList() {
  loading.value = true
  try {
    const result = await getOperationLogsPage({
      pageNum: page.pageNum,
      pageSize: page.pageSize,
      username: filters.username || undefined,
      operationModule: filters.operationModule || undefined,
      operationType: filters.operationType || undefined,
      result: filters.result || undefined,
      startTime: timeRange.value?.[0],
      endTime: timeRange.value?.[1],
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
  filters.username = ''
  filters.operationModule = ''
  filters.operationType = ''
  filters.result = ''
  timeRange.value = []
  handleSearch()
}

function handleSizeChange() {
  page.pageNum = 1
  void fetchList()
}

async function openDetail(row: OperationLogVO) {
  detail.value = await getOperationLogDetail(row.id)
  detailVisible.value = true
}
</script>
