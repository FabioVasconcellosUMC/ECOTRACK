<template>
  <nav
    ref="navEl"
    class="relative flex items-stretch h-full"
    @mouseleave="onLeaveNav"
  >

    <span
      v-if="activeBox"
      class="absolute pointer-events-none rounded-md"
      :style="{
        left:    `${activeBox.left + 6}px`,
        width:   `${activeBox.width - 12}px`,
        top:     '8px',
        bottom:  '8px',
        background: 'rgba(45, 212, 191, 0.06)',
        border: '1px solid rgba(45, 212, 191, 0.12)',
        transition:
          'left 320ms cubic-bezier(0.65,0,0.35,1), ' +
          'width 320ms cubic-bezier(0.65,0,0.35,1)',
      }"
    />

    <RouterLink
      v-for="item in items"
      :key="item.to"
      :ref="(el) => registerLink(item.to, el)"
      :to="item.to"
      class="header-nav-item group relative flex items-center gap-2.5 h-full px-4
             text-[12.5px] font-semibold tracking-tight transition-colors duration-200"
      :class="isActive(item.to) ? 'text-ink' : 'text-ink-3 hover:text-ink'"
      @mouseenter="onEnterItem(item.to)"
    >
      <component
        :is="item.icon"
        :size="14"
        :stroke-width="1.85"
        class="relative transition-all duration-200 group-hover:scale-110"
        :class="isActive(item.to)
          ? 'text-cyan'
          : 'text-ink-3 group-hover:text-cyan/70'"
      />
      <span class="relative whitespace-nowrap">{{ item.label }}</span>
    </RouterLink>

    <span
      v-if="showHoverPreview"
      class="absolute bottom-0 h-[2px] bg-bg-line-strong rounded-full"
      :style="{
        left:  `${hoverBox.left + 16}px`,
        width: `${hoverBox.width - 32}px`,
        opacity: 0.85,
        transition: 'left 220ms ease-out, width 220ms ease-out, opacity 200ms',
      }"
    />

    <span
      class="absolute bottom-0 h-[2px] bg-cyan rounded-full"
      :style="{
        left:    `${(activeBox?.left ?? 0) + 16}px`,
        width:   `${(activeBox?.width ?? 0) - 32}px`,
        opacity: activeBox ? 1 : 0,
        boxShadow:
          '0 0 14px 0 rgba(45, 212, 191, 0.55), ' +
          '0 0 4px  0 rgba(45, 212, 191, 0.85)',
        transition:
          'left 320ms cubic-bezier(0.65,0,0.35,1), ' +
          'width 460ms cubic-bezier(0.34, 1.4, 0.64, 1), ' +
          'opacity 200ms ease',
      }"
    />
  </nav>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { useRoute, RouterLink } from 'vue-router'

defineProps({
  items: { type: Array, required: true },
})

const route = useRoute()
const isActive = (to) => route.path === to || route.path.startsWith(to + '/')

const navEl      = ref(null)
const linkRefs   = reactive({})
const activeBox  = ref(null)
const hoverBox   = ref(null)
const hoveredKey = ref(null)

const showHoverPreview = computed(() =>
  hoverBox.value &&
  hoveredKey.value &&
  !isActive(hoveredKey.value)
)

const registerLink = (key, el) => {
  const node = el?.$el ?? el
  if (node) linkRefs[key] = node
}

const measure = (el) => {
  if (!el || !navEl.value) return null
  const navRect  = navEl.value.getBoundingClientRect()
  const itemRect = el.getBoundingClientRect()
  return { left: itemRect.left - navRect.left, width: itemRect.width }
}

const boxFor = (to) => measure(linkRefs[to])

const updateActive = async () => {
  await nextTick()
  const activeKey = Object.keys(linkRefs).find(k => isActive(k))
  activeBox.value = activeKey ? measure(linkRefs[activeKey]) : null
}

watch(() => route.path, updateActive)

const onEnterItem = (to) => {
  hoveredKey.value = to
  hoverBox.value = boxFor(to)
}
const onLeaveNav = () => {
  hoveredKey.value = null
  hoverBox.value = null
}

let ro = null
onMounted(() => {

  setTimeout(updateActive,  60)
  setTimeout(updateActive, 320)

  if (typeof ResizeObserver !== 'undefined' && navEl.value) {
    ro = new ResizeObserver(() => updateActive())
    ro.observe(navEl.value)
  }
})
onBeforeUnmount(() => { ro?.disconnect() })
</script>
