import { defineConfig, devices } from '@playwright/test'

process.env.NO_PROXY = [process.env.NO_PROXY, process.env.no_proxy, 'localhost', '127.0.0.1', '::1']
  .filter(Boolean)
  .join(',')

const startWebServer = process.env.PLAYWRIGHT_START_WEB_SERVER === '1'
const webBaseUrl = process.env.PLAYWRIGHT_BASE_URL || 'http://localhost:5173'

export default defineConfig({
  testDir: './tests/e2e',
  timeout: 60_000,
  expect: {
    timeout: 10_000,
  },
  reporter: [['list']],
  use: {
    baseURL: webBaseUrl,
    trace: 'retain-on-failure',
  },
  webServer: startWebServer
    ? {
        command: 'npm run dev -- --host 127.0.0.1',
        url: webBaseUrl,
        reuseExistingServer: !process.env.CI,
        timeout: 120_000,
        gracefulShutdown: { signal: 'SIGTERM', timeout: 1_000 },
      }
    : undefined,
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
})
