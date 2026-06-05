import type { AxiosResponse } from 'axios'

export function downloadResponseFile(response: AxiosResponse<Blob>, fallbackFilename: string) {
  downloadBlob(response.data, resolveFilename(response, fallbackFilename))
}

export function downloadBlob(blob: Blob, filename: string) {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

function resolveFilename(response: AxiosResponse<Blob>, fallbackFilename: string) {
  const disposition = String(response.headers['content-disposition'] || '')
  const utf8Match = disposition.match(/filename\*=UTF-8''([^;]+)/i)
  if (utf8Match?.[1]) {
    return decodeURIComponent(utf8Match[1])
  }
  const filenameMatch = disposition.match(/filename="?([^";]+)"?/i)
  if (filenameMatch?.[1]) {
    return filenameMatch[1]
  }
  return fallbackFilename
}
