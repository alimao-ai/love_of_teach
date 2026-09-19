<script setup lang="ts">
withDefaults(
  defineProps<{
    label?: string
  }>(),
  {
    label: '正在加载数据',
  },
)
</script>

<template>
  <div class="loading-block" role="status">
    <span class="spinner" aria-hidden="true" />
    <span>{{ label }}</span>
  </div>
</template>
