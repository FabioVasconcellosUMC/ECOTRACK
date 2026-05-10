<template>
  <div class="flex flex-col gap-1.5">
    <label class="eyebrow">{{ label }}</label>
    <div
      class="flex items-center px-3 h-10 rounded-md bg-bg-base border border-bg-line
             focus-within:border-cyan/40 transition-colors"
    >
      <input
        :type="type"
        :value="modelValue"
        :placeholder="placeholder"
        class="flex-1 bg-transparent outline-none text-[13px] text-ink placeholder:text-ink-4"
        @input="$emit('update:modelValue', $event.target.value)"
      />
    </div>
  </div>
</template>

<script setup>
defineProps({
  modelValue: { type: [String, Number], default: '' },
  label: { type: String, required: true },
  placeholder: { type: String, default: '' },
  type: { type: String, default: 'text' },
})

defineEmits(['update:modelValue'])
</script>
