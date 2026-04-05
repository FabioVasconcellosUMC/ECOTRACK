<template>
  <aside class="fixed top-0 left-0 h-screen w-[228px] flex flex-col bg-bg-surface border-r border-bg-border">
    
    <div class="flex items-center gap-3 px-5 py-4 border-b border-bg-border">
      <div class="w-8 h-8 rounded-lg flex items-center justify-center bg-brand shadow-[0_0_12px_rgba(0,76,84,0.4)]">
        <Leaf :size="16" class="text-text-primary" />
      </div>
      <span class="text-text-primary font-bold text-lg">EcoTrack</span>
    </div>

    <nav class="flex flex-col flex-1 px-3 py-4 gap-1">
      <p class="text-xs font-semibold tracking-widest uppercase text-text-muted px-2 mb-2">Principal</p>
      
      <RouterLink to="/dashboard" class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors"
        :class="isActive('/dashboard') ? 'bg-brand text-text-primary' : 'text-text-secondary hover:bg-bg-border hover:text-text-primary'">
        <LayoutDashboard :size="18" />
        Dashboard
      </RouterLink>

      <p class="text-xs font-semibold tracking-widest uppercase text-text-muted px-2 mt-4 mb-2">Gestão</p>

      <RouterLink to="/empresas" class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors"
        :class="isActive('/empresas') ? 'bg-brand text-text-primary' : 'text-text-secondary hover:bg-bg-border hover:text-text-primary'">
        <Building2 :size="18" />
        Empresas
      </RouterLink>

      <RouterLink to="/lotes" class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors"
        :class="isActive('/lotes') ? 'bg-brand text-text-primary' : 'text-text-secondary hover:bg-bg-border hover:text-text-primary'">
        <Package :size="18" />
        Lotes
      </RouterLink>

      <RouterLink to="/transportes" class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors"
        :class="isActive('/transportes') ? 'bg-brand text-text-primary' : 'text-text-secondary hover:bg-bg-border hover:text-text-primary'">
        <Truck :size="18" />
        Transportes
      </RouterLink>

      <RouterLink to="/relatorios" class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors"
        :class="isActive('/relatorios') ? 'bg-brand text-text-primary' : 'text-text-secondary hover:bg-bg-border hover:text-text-primary'">
        <FileText :size="18" />
        Relatórios
      </RouterLink>
    </nav>

    <div class="border-t-2 border-danger mx-4 mb-3"></div>

    <div class="flex items-center gap-3 px-4 py-4">
      <div class="w-8 h-8 rounded-full flex items-center justify-center bg-brand text-text-primary text-sm font-bold">
        {{ inicial }}
      </div>
      <div class="flex flex-col">
        <span class="text-text-primary text-sm font-semibold">{{ nome }}</span>
        <span class="text-text-muted text-xs">{{ email }}</span>
      </div>
    </div>

  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Leaf, LayoutDashboard, Building2, Package, Truck, FileText } from 'lucide-vue-next'

const route = useRoute()

const nome = localStorage.getItem('nome') || 'Usuário'
const email = localStorage.getItem('email') || ''
const inicial = computed(() => nome.charAt(0).toUpperCase())

const isActive = (path) => route.path === path
</script>