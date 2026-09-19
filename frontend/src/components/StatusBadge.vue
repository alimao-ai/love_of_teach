<script setup lang="ts">
import { computed } from 'vue'
import { statusClass } from '@/utils/format'

const props = defineProps<{
  status: string
  label: string
}>()

const classes = computed(() => ['status-badge', statusClass(props.status)])
</script>

<template>
  <span :class="classes">{{ label }}</span>
</template>
