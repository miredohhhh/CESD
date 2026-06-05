import { expect, test, type APIRequestContext, type Page } from '@playwright/test'

const API_BASE = process.env.CESD_API_BASE || 'http://localhost:8080/api'
const WEB_BASE = process.env.PLAYWRIGHT_BASE_URL || 'http://localhost:5173'
const PASSWORD = '123456'
const STORAGE_KEY = 'cesd_user_session'

const studentMenus = ['/student/applications', '/student/applications/create', '/student/score']

const auditMenus = ['/audit/pending']

const adminMenus = [
  '/audit/pending',
  '/admin/scores',
  '/admin/evaluation-categories',
  '/admin/evaluation-items',
  '/admin/majors',
  '/admin/classes',
  '/admin/students',
  '/admin/permissions',
  '/admin/role-permissions',
]

async function backendAvailable(request: APIRequestContext) {
  try {
    const response = await request.get(`${API_BASE}/health`, { timeout: 1_500 })
    return response.ok()
  } catch {
    return false
  }
}

async function frontendAvailable(request: APIRequestContext) {
  try {
    const response = await request.get(WEB_BASE, { timeout: 1_500 })
    return response.ok()
  } catch {
    return false
  }
}

async function login(page: Page, username: string) {
  await page.goto('/login')
  await page.evaluate(() => localStorage.clear())
  await page.goto('/login')
  await page.getByTestId('login-username-input').locator('input').fill(username)
  await page.getByTestId('login-password-input').locator('input').fill(PASSWORD)
  await page.getByTestId('login-submit-button').click()
}

async function logout(page: Page) {
  await page.getByTestId('logout-button').click()
  await expect(page).toHaveURL(/\/login$/)
}

function menuTestId(path: string) {
  return `menu-item-${path.replace(/^\/+/, '').replace(/[/:]+/g, '-')}`
}

async function expectMenusVisible(page: Page, paths: string[]) {
  await expect(page.getByTestId('side-menu')).toBeVisible()
  for (const path of paths) {
    await expect(page.getByTestId(menuTestId(path))).toBeVisible()
  }
}

async function expectMenusHidden(page: Page, paths: string[]) {
  for (const path of paths) {
    await expect(page.getByTestId(menuTestId(path))).toHaveCount(0)
  }
}

async function expectOnlyMenuScopes(page: Page, scopes: string[]) {
  const allScopes = ['student', 'audit', 'admin']
  for (const scope of allScopes) {
    const count = page.locator(`[data-menu-scope="${scope}"]`)
    if (scopes.includes(scope)) {
      await expect.poll(async () => count.count()).toBeGreaterThan(0)
    } else {
      await expect(count).toHaveCount(0)
    }
  }
}

async function expectStorageCleared(page: Page) {
  await expect
    .poll(async () =>
      page.evaluate((key) => {
        const session = localStorage.getItem(key)
        return {
          session,
          tokenLeaked: Boolean(session?.includes('Bearer ')),
        }
      }, STORAGE_KEY),
    )
    .toEqual({ session: null, tokenLeaked: false })
}

async function expectSessionRole(page: Page, roleCodes: string[]) {
  await expect
    .poll(async () =>
      page
        .evaluate((key) => {
          const raw = localStorage.getItem(key)
          return raw ? String(JSON.parse(raw).roleCode || '') : ''
        }, STORAGE_KEY)
        .then((roleCode) => roleCodes.includes(roleCode)),
    )
    .toBe(true)
}

test('STUDENT login only exposes student routes and menus', async ({ page, request }) => {
  test.skip(!(await backendAvailable(request)), 'backend is not available on localhost:8080')
  test.skip(
    !(await frontendAvailable(request)),
    'frontend dev server is not available on localhost:5173',
  )

  await login(page, 'student001')
  await expect(page).toHaveURL(/\/student\/applications$/)
  await expectMenusVisible(page, studentMenus)
  await expectMenusHidden(page, [...auditMenus, ...adminMenus])
  await expectOnlyMenuScopes(page, ['student'])
  await expect(page.getByTestId('student-create-application-button')).toBeVisible()

  await page.goto('/audit/pending')
  await expect(page).toHaveURL(/\/student\/applications$/)

  await page.goto('/admin/scores')
  await expect(page).toHaveURL(/\/student\/applications$/)
})

test('REVIEWER login only exposes audit route and menu', async ({ page, request }) => {
  test.skip(!(await backendAvailable(request)), 'backend is not available on localhost:8080')
  test.skip(
    !(await frontendAvailable(request)),
    'frontend dev server is not available on localhost:5173',
  )

  await login(page, 'reviewer001')
  await expect(page).toHaveURL(/\/audit\/pending$/)
  await expectMenusVisible(page, auditMenus)
  await expectMenusHidden(page, [
    ...studentMenus,
    ...adminMenus.filter((path) => path !== '/audit/pending'),
  ])
  await expectOnlyMenuScopes(page, ['audit'])
  await expect(page.getByTestId('pending-applications-table')).toBeVisible()

  await page.goto('/student/applications')
  await expect(page).toHaveURL(/\/audit\/pending$/)

  await page.goto('/admin/scores')
  await expect(page).toHaveURL(/\/audit\/pending$/)
})

test('ADMIN login exposes management menus and can access audit page', async ({
  page,
  request,
}) => {
  test.skip(!(await backendAvailable(request)), 'backend is not available on localhost:8080')
  test.skip(
    !(await frontendAvailable(request)),
    'frontend dev server is not available on localhost:5173',
  )

  await login(page, 'admin')
  await expect(page).toHaveURL(/\/admin\/scores$/)
  await expectMenusVisible(page, adminMenus)
  await expectMenusHidden(page, studentMenus)
  await expectOnlyMenuScopes(page, ['audit', 'admin'])
  await expect(page.getByTestId('score-recalculate-student-button')).toBeVisible()

  await page.goto('/audit/pending')
  await expect(page).toHaveURL(/\/audit\/pending$/)
})

test('logout clears local session and protected student route redirects to login', async ({
  page,
  request,
}) => {
  test.skip(!(await backendAvailable(request)), 'backend is not available on localhost:8080')
  test.skip(
    !(await frontendAvailable(request)),
    'frontend dev server is not available on localhost:5173',
  )

  await login(page, 'student001')
  await expect(page).toHaveURL(/\/student\/applications$/)
  await expectSessionRole(page, ['STUDENT'])
  await expectOnlyMenuScopes(page, ['student'])

  await logout(page)
  await expectStorageCleared(page)

  await page.goto('/student/applications')
  await expect(page).toHaveURL(/\/login\?redirect=.*student.*applications/)
})

test('page reload restores student session without leaking audit or admin menus', async ({
  page,
  request,
}) => {
  test.skip(!(await backendAvailable(request)), 'backend is not available on localhost:8080')
  test.skip(
    !(await frontendAvailable(request)),
    'frontend dev server is not available on localhost:5173',
  )

  await login(page, 'student001')
  await expect(page).toHaveURL(/\/student\/applications$/)
  await expectOnlyMenuScopes(page, ['student'])

  await page.reload()
  await expect(page).toHaveURL(/\/student\/applications$/)
  await expectSessionRole(page, ['STUDENT'])
  await expectMenusVisible(page, studentMenus)
  await expectMenusHidden(page, [...auditMenus, ...adminMenus])
  await expectOnlyMenuScopes(page, ['student'])
})

test('switching accounts does not retain previous role menus or permissions', async ({
  page,
  request,
}) => {
  test.skip(!(await backendAvailable(request)), 'backend is not available on localhost:8080')
  test.skip(
    !(await frontendAvailable(request)),
    'frontend dev server is not available on localhost:5173',
  )

  await login(page, 'student001')
  await expect(page).toHaveURL(/\/student\/applications$/)
  await expectSessionRole(page, ['STUDENT'])
  await expectMenusVisible(page, studentMenus)
  await expectMenusHidden(page, [...auditMenus, ...adminMenus])
  await expectOnlyMenuScopes(page, ['student'])

  await logout(page)
  await login(page, 'reviewer001')
  await expect(page).toHaveURL(/\/audit\/pending$/)
  await expectSessionRole(page, ['AUDITOR', 'REVIEWER'])
  await expectMenusVisible(page, auditMenus)
  await expectMenusHidden(page, [
    ...studentMenus,
    ...adminMenus.filter((path) => path !== '/audit/pending'),
  ])
  await expectOnlyMenuScopes(page, ['audit'])

  await logout(page)
  await login(page, 'admin')
  await expect(page).toHaveURL(/\/admin\/scores$/)
  await expectSessionRole(page, ['ADMIN'])
  await expectMenusVisible(page, adminMenus)
  await expectMenusHidden(page, studentMenus)
  await expectOnlyMenuScopes(page, ['audit', 'admin'])
})

test('restricted routes remain aligned with menu permissions across roles', async ({
  page,
  request,
}) => {
  test.skip(!(await backendAvailable(request)), 'backend is not available on localhost:8080')
  test.skip(
    !(await frontendAvailable(request)),
    'frontend dev server is not available on localhost:5173',
  )

  await login(page, 'student001')
  await expect(page).toHaveURL(/\/student\/applications$/)
  await page.goto('/admin/scores')
  await expect(page).toHaveURL(/\/student\/applications$/)
  await expectMenusHidden(page, adminMenus)

  await logout(page)
  await login(page, 'reviewer001')
  await expect(page).toHaveURL(/\/audit\/pending$/)
  await page.goto('/admin/permissions')
  await expect(page).toHaveURL(/\/audit\/pending$/)
  await expectMenusHidden(
    page,
    adminMenus.filter((path) => path !== '/audit/pending'),
  )

  await logout(page)
  await login(page, 'admin')
  await expect(page).toHaveURL(/\/admin\/scores$/)
  await expectSessionRole(page, ['ADMIN'])
  await page.goto('/admin/scores')
  await expect(page).toHaveURL(/\/admin\/scores$/)
  await expect(page.getByTestId('score-recalculate-student-button')).toBeVisible()
  await page.goto('/admin/permissions')
  await expect(page).toHaveURL(/\/admin\/permissions$/)
  await expect(page.getByTestId('permission-table')).toBeVisible()
})
