<template>
  <aside class="fixed top-0 left-0 h-screen w-[228px] bg-bg-surface border-r border-bg-border flex flex-col z-20">

    <!-- Logo -->
    <div class="flex items-center gap-3 px-5 py-4 border-b border-bg-border">
      <div class="w-8 h-8 rounded-lg bg-brand flex items-center justify-center">
        <Leaf :size="18" class="text-white" />
      </div>
      <span class="text-text-primary font-bold text-lg">EcoTrack</span>
    </div>

    <!-- Menu -->
    <nav class="flex-1 px-3 py-4 space-y-1">
      <p class="text-text-secondary text-xs uppercase px-2 mb-2">Principal</p>

      <RouterLink to="/dashboard" class="nav-item" :class="{ 'nav-item-active': route.path === '/dashboard' }">
        <LayoutDashboard :size="18" />
        <span>Dashboard</span>
      </RouterLink>

      <p class="text-text-secondary text-xs uppercase px-2 mt-4 mb-2">Gestão</p>

      <RouterLink to="/empresas" class="nav-item" :class="{ 'nav-item-active': route.path === '/empresas' }">
        <Building2 :size="18" />
        <span>Empresas</span>
      </RouterLink>

      <RouterLink to="/lotes" class="nav-item" :class="{ 'nav-item-active': route.path === '/lotes' }">
        <Package :size="18" />
        <span>Lotes</span>
      </RouterLink>

      <RouterLink to="/transportes" class="nav-item" :class="{ 'nav-item-active': route.path === '/transportes' }">
        <Truck :size="18" />
        <span>Transportes</span>
      </RouterLink>

      <RouterLink to="/relatorios" class="nav-item" :class="{ 'nav-item-active': route.path === '/relatorios' }">
        <FileText :size="18" />
        <span>Relatórios</span>
      </RouterLink>
    </nav>

    <!-- Usuário -->
    <div class="px-4 py-4 border-t border-bg-border flex items-center gap-3">
      <div class="w-8 h-8 rounded-full bg-brand flex items-center justify-center text-white text-sm font-bold">
        {{ inicial }}
      </div>
      <div class="flex-1 min-w-0">
        <p class="text-text-primary text-sm font-medium truncate">{{ nome }}</p>
        <p class="text-text-secondary text-xs truncate">{{ perfil }}</p>
      </div>
    </div>

  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { LayoutDashboard, Building2, Package, Truck, FileText, Leaf } from 'lucide-vue-next'

const route = useRoute()

const nome = localStorage.getItem('nome') || 'Usuário'
const perfil = localStorage.getItem('perfil') || ''
const inicial = computed(() => nome.charAt(0).toUpperCase())
</script>

<style scoped>
.nav-item {
  @apply flex items-center gap-3 px-3 py-2 rounded-lg text-sm text-gray-400 hover:bg-gray-700 hover:text-white transition-colors cursor-pointer;
}
.nav-item-active {
  @apply bg-teal-900 text-teal-300;
}
</style>