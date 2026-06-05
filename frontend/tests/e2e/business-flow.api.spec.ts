import { expect, test, type APIRequestContext } from '@playwright/test'

const API_BASE = process.env.CESD_API_BASE || 'http://localhost:8080/api'
const PASSWORD = '123456'

interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

interface LoginResult {
  token: string
  user: {
    userId: number
    username: string
    roleCode?: string
    studentId?: number | null
  }
}

interface PageResult<T> {
  records: T[]
  total: number
}

interface EvaluationItem {
  id: number
  itemName?: string
}

interface MaterialApplication {
  id: number
  title: string
  status: string
}

interface PendingApplication {
  id: number
  title: string
  status: string
}

interface ScoreSummary {
  totalScore?: number
  classRank?: number
  majorRank?: number
}

async function backendAvailable(request: APIRequestContext) {
  try {
    const response = await request.get(`${API_BASE}/health`, { timeout: 1_500 })
    return response.ok()
  } catch {
    return false
  }
}

async function unwrap<T>(response: Awaited<ReturnType<APIRequestContext['get']>>) {
  expect(response.ok()).toBeTruthy()
  const body = (await response.json()) as ApiResponse<T>
  expect(body.code, body.message).toBe(200)
  return body.data
}

async function tryLogin(request: APIRequestContext, username: string) {
  const response = await request.post(`${API_BASE}/auth/login`, {
    data: { username, password: PASSWORD },
  })
  if (!response.ok()) {
    return null
  }
  const body = (await response.json()) as ApiResponse<LoginResult>
  return body.code === 200 ? body.data : null
}

function auth(token: string) {
  return { Authorization: `Bearer ${token}` }
}

async function getFirstEvaluationItem(request: APIRequestContext, token: string) {
  const result = await unwrap<PageResult<EvaluationItem>>(
    await request.get(`${API_BASE}/evaluation-items/page`, {
      headers: auth(token),
      params: { pageNo: 1, pageNum: 1, pageSize: 20 },
    }),
  )
  return result.records[0]
}

async function createDraft(
  request: APIRequestContext,
  token: string,
  itemId: number,
  title: string,
) {
  return unwrap<MaterialApplication>(
    await request.post(`${API_BASE}/material-applications`, {
      headers: auth(token),
      data: {
        itemId,
        title,
        description: 'Playwright regression test material',
        applyScore: 9.25,
        status: 'DRAFT',
      },
    }),
  )
}

test('student submit, reviewer approve, admin recalculate, student score query', async ({
  request,
}) => {
  test.skip(!(await backendAvailable(request)), 'backend is not available on localhost:8080')

  const student = await tryLogin(request, 'student001')
  const reviewer = await tryLogin(request, 'reviewer001')
  const admin = await tryLogin(request, 'admin')
  test.skip(!student || !reviewer || !admin, 'development test accounts are not initialized')
  test.skip(!student.user.studentId, 'student001 is not bound to a student record')

  const item = await getFirstEvaluationItem(request, admin.token)
  test.skip(!item, 'no evaluation item exists for E2E material creation')

  const title = `PW approve ${Date.now()}`
  const created = await createDraft(request, student.token, item.id, title)
  expect(created.status).toBe('DRAFT')

  const submitted = await unwrap<MaterialApplication>(
    await request.post(`${API_BASE}/material-applications/${created.id}/submit`, {
      headers: auth(student.token),
      data: {},
    }),
  )
  expect(submitted.status).toBe('SUBMITTED')

  const pending = await unwrap<PageResult<PendingApplication>>(
    await request.get(`${API_BASE}/frontend/audit/pending/page`, {
      headers: auth(reviewer.token),
      params: { pageNo: 1, pageSize: 10, keyword: title },
    }),
  )
  test.skip(
    pending.total === 0,
    'reviewer001 reviewer_scope does not include the student001 test material',
  )

  const approved = await unwrap<MaterialApplication>(
    await request.post(`${API_BASE}/material-applications/${created.id}/approve`, {
      headers: auth(reviewer.token),
      data: { reviewScore: 9.25, reviewComment: 'Playwright approval' },
    }),
  )
  expect(approved.status).toBe('APPROVED')

  await unwrap<unknown>(
    await request.post(`${API_BASE}/scores/students/${student.user.studentId}/recalculate`, {
      headers: auth(admin.token),
      data: {},
    }),
  )

  const score = await unwrap<ScoreSummary>(
    await request.get(`${API_BASE}/frontend/my-score`, {
      headers: auth(student.token),
    }),
  )
  expect(Number(score.totalScore ?? 0)).toBeGreaterThan(0)
  expect(Number(score.classRank ?? 0)).toBeGreaterThan(0)
  expect(Number(score.majorRank ?? 0)).toBeGreaterThan(0)
})

test('student submit, reviewer reject, student can resubmit rejected material', async ({
  request,
}) => {
  test.skip(!(await backendAvailable(request)), 'backend is not available on localhost:8080')

  const student = await tryLogin(request, 'student001')
  const reviewer = await tryLogin(request, 'reviewer001')
  const admin = await tryLogin(request, 'admin')
  test.skip(!student || !reviewer || !admin, 'development test accounts are not initialized')

  const item = await getFirstEvaluationItem(request, admin.token)
  test.skip(!item, 'no evaluation item exists for E2E material creation')

  const title = `PW reject ${Date.now()}`
  const created = await createDraft(request, student.token, item.id, title)

  await unwrap<MaterialApplication>(
    await request.post(`${API_BASE}/material-applications/${created.id}/submit`, {
      headers: auth(student.token),
      data: {},
    }),
  )

  const pending = await unwrap<PageResult<PendingApplication>>(
    await request.get(`${API_BASE}/frontend/audit/pending/page`, {
      headers: auth(reviewer.token),
      params: { pageNo: 1, pageSize: 10, keyword: title },
    }),
  )
  test.skip(
    pending.total === 0,
    'reviewer001 reviewer_scope does not include the student001 test material',
  )

  const rejected = await unwrap<MaterialApplication>(
    await request.post(`${API_BASE}/material-applications/${created.id}/reject`, {
      headers: auth(reviewer.token),
      data: {
        rejectReason: 'Playwright rejection',
        reviewComment: 'Needs more evidence',
      },
    }),
  )
  expect(rejected.status).toBe('REJECTED')

  const resubmitted = await unwrap<MaterialApplication>(
    await request.post(`${API_BASE}/material-applications/${created.id}/submit`, {
      headers: auth(student.token),
      data: {},
    }),
  )
  expect(resubmitted.status).toBe('SUBMITTED')
})
