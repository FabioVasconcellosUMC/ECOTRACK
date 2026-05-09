<template>
  <div class="flex h-screen w-screen overflow-hidden bg-bg-primary">
    <AppSidebar />
    <div class="flex flex-col flex-1 ml-[228px] min-w-0">
      <AppTopbar />
      <main class="flex-1 overflow-y-auto p-6 mt-[52px]">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup>
import AppSidebar from '../components/AppSidebar.vue'
import AppTopbar from '../components/AppTopbar.vue'
</script>