<template>

  <div
    :class="[
      'inline-flex items-center justify-center shrink-0',
      'rounded-[10px] bg-bg-elevated border border-bg-line-strong',
      className,
    ]"
    :style="{ width: `${box}px`, height: `${box}px` }"
  >
    <svg
      :width="leaf"
      :height="leaf"
      viewBox="0 0 24 24"
      fill="none"
      :stroke="`url(#${gid})`"
      stroke-width="2"
      stroke-linecap="round"
      stroke-linejoin="round"
      aria-hidden="true"
    >
      <defs>
        <linearGradient :id="gid" x1="0" y1="1" x2="1" y2="0">
          <stop offset="0%"   stop-color="#006D78" />
          <stop offset="100%" stop-color="#2DD4BF" />
        </linearGradient>
      </defs>

      <path d="M11 20A7 7 0 0 1 9.8 6.1C15.5 5 17 4.48 19.2 2.96c1.4 5.97.05 11.91-2.4 13.43c-2.8 1.71-9.7 1.61-12.5-.13" />
      <path d="M2 21c0-3 1.85-5.36 5.08-6C9.5 14.52 12 13 13 12" />
    </svg>
  </div>
</template>

<script setup>
const props = defineProps({

  size:      { type: [Number, String], default: 36 },
  className: { type: String,            default: '' },
})

const box  = Number(props.size)
const leaf = Math.round(box * 0.58)

const gid = `leaf-${Math.random().toString(36).slice(2, 8)}`
</script>
