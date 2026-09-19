<script setup lang="ts">
import type { Component } from 'vue'

withDefaults(
  defineProps<{
    label: string
    value: string | number
    hint?: string
    icon: Component
    tone?: 'blue' | 'green' | 'amber' | 'red'
  }>(),
  {
    hint: '',
    tone: 'blue',
  },
)
</script>

<template>
  <article :class="['metric-tile', `metric-${tone}`]">
    <div class="metric-icon">
      <component :is="icon" :size="20" aria-hidden="true" />
    </div>
    <div>
      <span class="metric-label">{{ label }}</span>
      <strong class="metric-value">{{ value }}</strong>
      <small v-if="hint">{{ hint }}</small>
    </div>
  </article>
</template>
