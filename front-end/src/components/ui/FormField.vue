<template>
  <div class="flex flex-col gap-1.5">
    <label class="eyebrow">{{ label }}</label>
    <div class="flex items-stretch gap-2">
      <div
        class="flex items-center gap-2 px-3 h-10 rounded-md bg-bg-base border flex-1 min-w-0
               transition-colors"
        :class="erro
          ? 'border-danger/60 focus-within:border-danger'
          : 'border-bg-line focus-within:border-cyan/40'"
      >
        <slot name="prefix" />
        <input
          :type="type"
          :value="modelValue"
          :placeholder="placeholder"
          :inputmode="modoEntrada"
          class="flex-1 min-w-0 bg-transparent outline-none text-[13px] text-ink placeholder:text-ink-4"
          :class="{ 'mono-tag': mask }"
          @input="aoDigitar"
          @blur="$emit('blur')"
        />
      </div>
      <slot name="suffix" />
    </div>
    <p
      v-if="erro"
      class="flex items-center gap-1.5 text-[11px] text-danger mt-0.5"
    >
      <AlertCircle :size="11" /> {{ erro }}
    </p>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { AlertCircle } from 'lucide-vue-next'
import { formatarComMascara } from '../../composables/useMascara'

const props = defineProps({
  modelValue:  { type: [String, Number], default: '' },
  label:       { type: String, required: true },
  placeholder: { type: String, default: '' },
  type:        { type: String, default: 'text' },
  mask:        { type: String, default: '' },
  erro:        { type: String, default: '' },
})

const emit = defineEmits(['update:modelValue', 'blur'])

const modoEntrada = computed(() => {
  if (props.mask) return 'numeric'
  if (props.type === 'number') return 'decimal'
  return 'text'
})

const aoDigitar = (evento) => {
  const valorDigitado = evento.target.value
  const valorFinal = props.mask
    ? formatarComMascara(valorDigitado, props.mask)
    : valorDigitado

  if (props.mask && valorFinal !== valorDigitado) {
    evento.target.value = valorFinal
  }

  emit('update:modelValue', valorFinal)
}
</script>
