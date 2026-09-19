<script setup lang="ts">
import { computed } from 'vue'
import type { OfferingStatus } from '@/types/api'
import { statusClass } from '@/utils/format'

const props = defineProps<{
  status: OfferingStatus
  label: string
}>()

const classes = computed(() => ['status-badge', statusClass(props.status)])
</script>

<template>
  <span :class="classes">{{ label }}</span>
</template>
