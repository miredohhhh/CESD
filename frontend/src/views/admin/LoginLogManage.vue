<template>
  <section class="admin-page admin-list-page" data-testid="login-log-page">
    <AdminPageHeader
      eyebrow="账号安全"
      title="登录日志管理"
      description="查看登录成功、登录失败和退出登录记录，用于排查账号访问异常和登录态问题。"
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
          <el-form-item label="登录类型">
            <el-select v-model="filters.loginType" clearable placeholder="全部">
              <el-option label="登录" value="LOGIN" />
              <el-option label="退出登录" value="LOGOUT" />
            </el-select>
          </el-form-item>
          <el-form-item label="登录结果">
            <el-select v-model="filters.result" clearable placeholder="全部">
              <el-option label="成功" value="SUCCESS" />
              <el-option label="失败" value="FAIL" />
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围" class="admin-filter-grid__wide">
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
              <el-button type="primary" data-testid="login-log-search-button" @click="handleSearch">
                查询
              </el-button>
              <el-button data-testid="login-log-reset-button" @click="handleReset">重置</el-button>
            </div>
          </el-form-item>
        </div>
      </el-form>
    </el-card>

    <el-card class="admin-table-card" shadow="never">
      <div class="admin-card-header">
        <div>
          <div class="admin-card-header__title">日志列表</div>
          <div class="admin-card-header__meta">共 {{ page.total }} 条登录记录</div>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="records"
        empty-text="暂无登录日志"
        data-testid="login-log-table"
      >
        <el-table-column prop="username" label="用户名" min-width="140" show-overflow-tooltip />
        <el-table-column prop="realName" label="真实姓名" min-width="140" show-overflow-tooltip />
        <el-table-column prop="roleCode" label="角色" width="110" />
        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getLoginTypeTagType(row.loginType)">
              {{ getLoginTypeText(row.loginType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="结果" width="110">
          <template #default="{ row }">
            <el-tag :type="getResultTagType(row.result)">{{ getResultText(row.result) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ipAddress" label="IP 地址" min-width="130" />
        <el-table-column
          prop="userAgent"
          label="User-Agent"
          min-width="260"
          show-overflow-tooltip
        />
        <el-table-column prop="loginTime" label="登录时间" min-width="170" />
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <div class="admin-table-actions">
              <el-button
                link
                type="primary"
                :data-testid="`login-log-detail-${row.id}`"
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

    <el-dialog v-model="detailVisible" title="登录日志详情" width="820px" class="admin-list-dialog">
      <div v-if="detail" class="log-detail">
        <section class="log-detail-section">
          <div class="log-detail-section__title">基础信息</div>
          <div class="log-detail-grid">
            <DetailItem label="日志 ID" :value="detail.id" />
            <DetailItem label="登录类型">
              <el-tag :type="getLoginTypeTagType(detail.loginType)">
                {{ getLoginTypeText(detail.loginType) }}
              </el-tag>
            </DetailItem>
          </div>
        </section>

        <section class="log-detail-section">
          <div class="log-detail-section__title">登录用户信息</div>
          <div class="log-detail-grid">
            <DetailItem label="用户 ID" :value="detail.userId" />
            <DetailItem label="用户名" :value="detail.username" />
            <DetailItem label="真实姓名" :value="detail.realName" />
            <DetailItem label="角色编码" :value="detail.roleCode" />
          </div>
        </section>

        <section class="log-detail-section">
          <div class="log-detail-section__title">登录结果</div>
          <div class="log-detail-grid">
            <DetailItem label="处理结果">
              <el-tag :type="getResultTagType(detail.result)">
                {{ getResultText(detail.result) }}
              </el-tag>
            </DetailItem>
          </div>
        </section>

        <section class="log-detail-section">
          <div class="log-detail-section__title">IP / User-Agent 信息</div>
          <div class="log-detail-grid">
            <DetailItem label="IP 地址" :value="detail.ipAddress" />
            <DetailItem label="User-Agent" full>
              <CopyableText
                :text="formatPlainText(detail.userAgent)"
                copy-label="User-Agent"
                @copy="copyText"
              />
            </DetailItem>
          </div>
        </section>

        <section class="log-detail-section">
          <div class="log-detail-section__title">失败原因</div>
          <div class="log-detail-grid">
            <DetailItem label="错误消息" full>
              <CopyableText
                :text="formatPlainText(detail.errorMessage)"
                copy-label="错误消息"
                @copy="copyText"
              />
            </DetailItem>
          </div>
        </section>

        <section class="log-detail-section">
          <div class="log-detail-section__title">时间信息</div>
          <div class="log-detail-grid">
            <DetailItem label="登录时间" :value="detail.loginTime" />
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
import { getLoginLogDetail, getLoginLogsPage, type LoginLogVO } from '@/api/base'

type TagType = 'success' | 'info' | 'warning' | 'danger' | 'primary'

const loading = ref(false)
const detailVisible = ref(false)
const records = ref<LoginLogVO[]>([])
const detail = ref<LoginLogVO | null>(null)
const timeRange = ref<string[]>([])

const filters = reactive({
  username: '',
  loginType: '',
  result: '',
})

const page = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

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

function getLoginTypeTagType(loginType?: string): TagType {
  return loginType === 'LOGIN' ? 'primary' : 'info'
}

function getLoginTypeText(loginType?: string) {
  if (loginType === 'LOGIN') {
    return '登录'
  }
  if (loginType === 'LOGOUT') {
    return '退出登录'
  }
  return loginType || '-'
}

function formatPlainText(text?: string) {
  return text?.trim() || '-'
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
    const result = await getLoginLogsPage({
      pageNum: page.pageNum,
      pageSize: page.pageSize,
      username: filters.username || undefined,
      loginType: filters.loginType || undefined,
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
  filters.loginType = ''
  filters.result = ''
  timeRange.value = []
  handleSearch()
}

function handleSizeChange() {
  page.pageNum = 1
  void fetchList()
}

async function openDetail(row: LoginLogVO) {
  detail.value = await getLoginLogDetail(row.id)
  detailVisible.value = true
}
</script>
