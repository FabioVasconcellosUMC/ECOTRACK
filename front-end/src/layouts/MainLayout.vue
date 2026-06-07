<template>
  <div class="min-h-screen w-full">
    <AppHeader />

    <main class="mt-[72px] min-h-[calc(100vh-72px)] px-5 sm:px-8 lg:px-10 py-7 relative">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '../components/AppHeader.vue'

const router = useRouter()

const encerrarSessaoSemToken = () => {
  if (!localStorage.getItem('token')) {
    localStorage.clear()
    router.replace('/')
  }
}

const verificarSessaoAoRetomarTela = () => {
  if (document.visibilityState === 'visible') {
    encerrarSessaoSemToken()
  }
}

onMounted(() => {
  window.addEventListener('focus', encerrarSessaoSemToken)
  window.addEventListener('pageshow', encerrarSessaoSemToken)
  window.addEventListener('storage', encerrarSessaoSemToken)
  document.addEventListener('visibilitychange', verificarSessaoAoRetomarTela)
})

onBeforeUnmount(() => {
  window.removeEventListener('focus', encerrarSessaoSemToken)
  window.removeEventListener('pageshow', encerrarSessaoSemToken)
  window.removeEventListener('storage', encerrarSessaoSemToken)
  document.removeEventListener('visibilitychange', verificarSessaoAoRetomarTela)
})
</script>
