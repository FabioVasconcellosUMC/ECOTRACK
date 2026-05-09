<template>
  <header class="fixed top-0 left-[228px] right-0 h-[52px] flex items-center justify-between px-6 bg-bg-surface border-b border-bg-border z-10">
    
    <span class="text-text-secondary text-sm">{{ breadcrumb }}</span>

    <div class="flex items-center gap-4">
      <div class="relative">
        <Bell :size="20" class="text-text-secondary cursor-pointer hover:text-text-primary transition-colors" />
        <span class="absolute -top-1 -right-1 w-2 h-2 rounded-full bg-danger"></span>
      </div>
      <div class="w-8 h-8 rounded-full flex items-center justify-center bg-brand text-text-primary text-sm font-bold cursor-pointer">
        {{ inicial }}
      </div>
    </div>

  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Bell } from 'lucide-vue-next'

const route = useRoute()

const nome = localStorage.getItem('nome') || 'Usuário'
const inicial = computed(() => nome.charAt(0).toUpperCase())

const breadcrumb = computed(() => {
  const map = {
    '/dashboard': 'Dashboard',
    '/empresas': 'Empresas',
    '/lotes': 'Lotes',
    '/transportes': 'Transportes',
    '/relatorios': 'Relatórios',
  }
  return map[route.path] || ''
})
</script>