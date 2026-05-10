<template>
  <article class="col-span-12 md:col-span-4 rounded-xl bg-bg-panel border border-bg-line-strong p-6 card-hover">
    <div class="flex items-start justify-between gap-4">
      <div class="w-11 h-11 rounded-xl bg-bg-elevated border border-bg-line flex items-center justify-center shrink-0">
        <component :is="icon" :size="20" class="text-cyan" />
      </div>
      <span class="px-2 py-0.5 rounded-full bg-info-soft border border-info/30 text-info text-[10px] font-bold tracking-wider">
        {{ status }}
      </span>
    </div>

    <h3 class="section-title text-[20px] mt-5">{{ title }}</h3>
    <p class="editorial text-[13px] mt-2 leading-relaxed">{{ description }}</p>
  </article>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  description: { type: String, required: true },
  icon: { type: [Object, Function], required: true },
  status: { type: String, default: 'ROADMAP' },
})
</script>
