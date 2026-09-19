import { describe, expect, it } from 'vitest'
import { formatDate, formatNumber, formatPercent, statusClass } from './format'

describe('format utilities', () => {
  it('formats numbers and percentages for dashboard metrics', () => {
    expect(formatNumber(12345)).toBe('12,345')
    expect(formatPercent(62.5)).toBe('62.5%')
    expect(formatPercent(undefined)).toBe('0.0%')
  })

  it('formats ISO date values for catalog tables', () => {
    expect(formatDate('2026-09-01')).toBe('2026/09/01')
  })

  it('maps offering status to semantic CSS classes', () => {
    expect(statusClass('OPEN')).toBe('status-open')
    expect(statusClass('PLANNED')).toBe('status-planned')
    expect(statusClass('CLOSED')).toBe('status-closed')
  })
})
