<template>
  <div class="min-h-screen w-full">
    <AppHeader />

    <main class="mt-[72px] min-h-[calc(100vh-72px)] px-5 sm:px-8 lg:px-10 py-7 relative">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import AppHeader from '../components/AppHeader.vue'
</script>
