<template>
  <div class="min-h-screen w-full">
    <AppHeader />

    <div ref="sweepEl" class="route-sweep" />

    <main class="mt-[84px] min-h-[calc(100vh-84px)] px-8 lg:px-10 py-7 relative">
      <RouterView v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </RouterView>
    </main>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '../components/AppHeader.vue'

const sweepEl = ref(null)
const route   = useRoute()

watch(() => route.path, () => {
  const el = sweepEl.value
  if (!el) return

  el.classList.remove('is-sweeping')
  void el.offsetWidth
  el.classList.add('is-sweeping')
})
</script>
