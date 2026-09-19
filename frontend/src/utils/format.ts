import type { OfferingStatus } from '@/types/api'

export function formatNumber(value: number | undefined | null) {
  return new Intl.NumberFormat('zh-CN').format(value ?? 0)
}

export function formatPercent(value: number | undefined | null) {
  return `${Number(value ?? 0).toFixed(1)}%`
}

export function formatDateTime(value: string | undefined | null) {
  if (!value) return '-'
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(new Date(value))
}

export function formatDate(value: string | undefined | null) {
  if (!value) return '-'
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(new Date(`${value}T00:00:00`))
}

export function statusClass(status: OfferingStatus) {
  return {
    PLANNED: 'status-planned',
    OPEN: 'status-open',
    CLOSED: 'status-closed',
  }[status]
}
