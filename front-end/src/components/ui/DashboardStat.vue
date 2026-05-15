<template>
  <div class="p-7 flex flex-col justify-between min-h-[180px] relative">
    <div class="flex items-center justify-between">
      <p class="eyebrow">{{ label }}</p>
      <span class="flex items-center justify-center w-8 h-8 rounded-md bg-cyan/10 border border-cyan/20 text-cyan">
        <component :is="icon" :size="14" />
      </span>
    </div>

    <div>
      <div class="flex items-baseline gap-2 mt-3">
        <p class="scoreboard text-[56px] text-ink leading-none tabular-nums kpi-numero">
          {{ value }}
        </p>
        <span class="font-display text-cyan text-[14px] kpi-unidade">{{ unit }}</span>
      </div>
    </div>

    <div class="flex items-center gap-1.5">
      <span
        class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-semibold border"
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
  unit:  { type: String, default: '' },
  delta: { type: String, default: '' },
  icon:  { type: [Object, Function], required: true },
  trend: { type: String, default: 'up' },
})
</script>

<style scoped>
.kpi-numero {
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.4);
}

.kpi-unidade {
  font-weight: 500;
  font-style: italic;
  letter-spacing: 0.04em;
}
</style>
