<script setup lang="ts">
import { Inbox } from 'lucide-vue-next'

withDefaults(
  defineProps<{
    title: string
    description?: string
    compact?: boolean
  }>(),
  {
    description: '',
    compact: false,
  },
)
</script>

<template>
  <div :class="['empty-state', { 'empty-state-compact': compact }]">
    <Inbox :size="compact ? 24 : 34" aria-hidden="true" />
    <strong>{{ title }}</strong>
    <span v-if="description">{{ description }}</span>
  </div>
</template>
