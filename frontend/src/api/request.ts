import axios, { type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types/common'
import { useUserStore } from '@/stores/user'

const SUCCESS_CODE = 200
const UNAUTHORIZED_CODE = 401

const axiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

axiosInstance.interceptors.request.use((config) => {
  const userStore = useUserStore()
  userStore.initFromStorage()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

axiosInstance.interceptors.response.use(
  (response) => {
    if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
      return response
    }
    const result = response.data as ApiResponse<unknown>
    if (result.code !== SUCCESS_CODE) {
      const message = result.message || 'Request failed'
      if (result.code === UNAUTHORIZED_CODE && !isLoginRequest(response.config.url)) {
        handleUnauthorized(message)
        return Promise.reject(new Error(message))
      }
      ElMessage.error(message)
      return Promise.reject(new Error(message))
    }
    return response
  },
  (error) => {
    const message = error?.response?.data?.message || error?.message || 'Network request failed'
    if (error?.response?.status === UNAUTHORIZED_CODE && !isLoginRequest(error?.config?.url)) {
      handleUnauthorized('Login expired, please sign in again')
      return Promise.reject(error)
    }
    ElMessage.error(message)
    return Promise.reject(error)
  },
)

function isLoginRequest(url?: string) {
  return Boolean(url?.includes('/auth/login'))
}

function handleUnauthorized(message: string) {
  const userStore = useUserStore()
  userStore.clearLogin()
  if (window.location.pathname !== '/login') {
    ElMessage.warning(message || 'Login expired, please sign in again')
    window.location.href = `/login?redirect=${encodeURIComponent(
      window.location.pathname + window.location.search,
    )}`
  }
}

function unwrap<T>(response: { data: ApiResponse<T> }) {
  return response.data.data
}

async function ensureBlobSuccess(response: AxiosResponse<Blob>) {
  const contentType = String(response.headers['content-type'] || '')
  if (contentType.includes('application/json')) {
    const result = JSON.parse(await response.data.text()) as ApiResponse<unknown>
    if (result.code !== SUCCESS_CODE) {
      const message = result.message || 'Request failed'
      ElMessage.error(message)
      return Promise.reject(new Error(message))
    }
  }
}

const request = {
  async get<T>(url: string, config?: AxiosRequestConfig) {
    const response = await axiosInstance.get<ApiResponse<T>>(url, config)
    return unwrap(response)
  },
  async post<T, D = unknown>(url: string, data?: D, config?: AxiosRequestConfig) {
    const response = await axiosInstance.post<ApiResponse<T>>(url, data, config)
    return unwrap(response)
  },
  async put<T, D = unknown>(url: string, data?: D, config?: AxiosRequestConfig) {
    const response = await axiosInstance.put<ApiResponse<T>>(url, data, config)
    return unwrap(response)
  },
  async delete<T>(url: string, config?: AxiosRequestConfig) {
    const response = await axiosInstance.delete<ApiResponse<T>>(url, config)
    return unwrap(response)
  },
  async download(url: string, config?: AxiosRequestConfig) {
    const response = await axiosInstance.get<Blob>(url, {
      ...config,
      responseType: 'blob',
    })
    await ensureBlobSuccess(response)
    return response.data
  },
  async downloadResponse(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<Blob>> {
    const response = await axiosInstance.get<Blob>(url, {
      ...config,
      responseType: 'blob',
    })
    await ensureBlobSuccess(response)
    return response
  },
}

export default request
