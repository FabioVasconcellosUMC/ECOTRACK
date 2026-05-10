<template>
  <div class="p-7 flex flex-col justify-between min-h-[180px] relative">
    <div class="flex items-center justify-between">
      <p class="eyebrow">{{ label }}</p>
      <component :is="icon" :size="16" class="text-ink-3" />
    </div>

    <div>
      <p class="scoreboard text-[60px] text-ink mt-3 leading-none tabular-nums" style="text-shadow: 0 2px 12px rgba(0, 0, 0, 0.4);">
        {{ value }}
      </p>
      <p v-if="unit" class="mono-tag text-ink-2 mt-1.5 text-[11px]">{{ unit }}</p>
    </div>

    <div class="flex items-center gap-1.5">
      <span
        class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-semibold border backdrop-blur-md"
        :class="trend === 'up'
          ? 'bg-success-soft border-success/30 text-success'
          : 'bg-danger-soft border-danger/30 text-danger'"
      >
        <TrendingUp v-if="trend === 'up'" :size="11" />
        <TrendingDown v-else :size="11" />
        {{ delta }}%
      </span>
    </div>
  </div>
</template>

<script setup>
import { TrendingUp, TrendingDown } from 'lucide-vue-next'

defineProps({
  label: { type: String, required: true },
  value: { type: [String, Number], required: true },
  unit: { type: String, default: '' },
  delta: { type: String, default: '' },
  icon: { type: [Object, Function], required: true },
  trend: { type: String, default: 'up' },
})
</script>
