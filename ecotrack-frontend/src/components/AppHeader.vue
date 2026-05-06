<template>
  <header
    class="fixed top-0 inset-x-0 z-30
           border-b border-white/10"
    style="
      background: rgba(7, 24, 30, 0.55);
      backdrop-filter: blur(40px) saturate(140%);
      -webkit-backdrop-filter: blur(40px) saturate(140%);
      box-shadow:
        inset 0 1px 0 0 rgba(255, 255, 255, 0.08),
        inset 0 1px 0 0 rgba(45, 212, 191, 0.06),
        0 1px 0 0 rgba(45, 212, 191, 0.06),
        0 0 24px -4px rgba(45, 212, 191, 0.04),
        0 32px 64px -16px rgba(0, 0, 0, 0.55);
    "
  >

    <div
      class="h-7 flex items-center justify-between px-6 mono-tag text-[10.5px]"
      style="
        background: rgba(4, 20, 26, 0.30);
        border-bottom: 1px solid rgba(255, 255, 255, 0.06);
      "
    >

      <div class="flex items-center gap-3 text-ink-3">
        <span class="flex items-center gap-1.5">
          <span class="w-1 h-1 rounded-full bg-cyan/70" />
          <span class="text-cyan">v1.0</span>
        </span>

        <span class="header-divider" />
        <span class="text-ink-2">PNRS · Lei 12.305/2010</span>

        <span class="header-divider hidden md:inline-block" />
        <span class="hidden md:flex items-center gap-1.5">
          <span class="w-1 h-1 rounded-full bg-amber" />
          <span>PROD</span>
        </span>

        <span class="header-divider hidden lg:inline-block" />
        <span class="hidden lg:flex items-center gap-1.5 text-ink-2">
          <ChevronRight :size="9" :stroke-width="2" class="text-ink-4" />
          {{ pageMeta }}
        </span>
      </div>

      <div class="flex items-center gap-3 text-ink-3">

        <span
          class="flex items-center gap-1.5 px-2 py-0.5 rounded-full"
          style="background: rgba(16, 185, 129, 0.08);
                 border: 1px solid rgba(16, 185, 129, 0.22);"
        >
          <span class="relative flex h-1.5 w-1.5">
            <span class="absolute inline-flex h-full w-full rounded-full bg-success opacity-75 live-pulse" />
            <span class="relative inline-flex h-1.5 w-1.5 rounded-full bg-success" />
          </span>
          <span class="text-success text-[10px] font-semibold tracking-wider">
            SINCRONIZADO
          </span>
        </span>

        <span class="header-divider hidden md:inline-block" />

        <span class="hidden md:flex items-center gap-2">
          <Activity :size="10" :stroke-width="2" class="text-ink-4" />
          <span>API</span>

          <span class="flex items-end gap-[2px] h-3" aria-hidden="true">
            <span
              v-for="(h, i) in latencyBars"
              :key="i"
              class="w-[2px] rounded-sm bg-cyan/70 transition-all duration-300 ease-out"
              :style="{
                height:  `${h}px`,
                opacity: 0.55 + (i / latencyBars.length) * 0.45,
              }"
            />
          </span>

          <span class="text-ink-2 tabular-nums">{{ latency }}ms</span>
        </span>

        <span class="header-divider" />

        <span class="flex items-center gap-2 tabular-nums">
          <span class="hidden sm:inline text-ink-3">{{ dateLabel }}</span>
          <span class="text-ink-2">{{ clock }}</span>
          <span class="text-ink-4 text-[9px]">BRT</span>
        </span>
      </div>
    </div>

    <div class="h-14 flex items-stretch px-6">

      <RouterLink
        to="/dashboard"
        class="flex items-center gap-2.5 pr-6 h-full group"
      >
        <LogoMark :size="34" />

        <div class="leading-tight">
          <p class="flex items-center gap-1.5
                    font-display text-[19px] text-ink
                    group-hover:text-cyan transition-colors duration-200"
             style="font-weight: 600; letter-spacing: -0.035em;">
            EcoTrack

            <span class="relative flex h-1 w-1">
              <span class="absolute inline-flex h-full w-full rounded-full bg-success opacity-70 live-pulse" />
              <span class="relative inline-flex h-1 w-1 rounded-full bg-success" />
            </span>
          </p>
          <p class="eyebrow text-[8.5px] mt-0.5">Centro de Operações</p>
        </div>
      </RouterLink>

      <span class="header-divider-tall self-center" />

      <div class="ml-2">
        <HeaderNav :items="navItems" />
      </div>

      <div class="flex-1" />

      <div class="flex items-center gap-1.5">

        <button
          class="relative w-9 h-9 rounded-md flex items-center justify-center
                 text-ink-3 hover:text-ink hover:bg-bg-elevated transition-colors group"
          aria-label="Alertas"
        >
          <Bell
            :size="16"
            :stroke-width="1.75"
            class="transition-transform duration-200 group-hover:scale-110"
          />
          <span
            class="absolute -top-0.5 -right-0.5 min-w-[16px] h-[16px] px-1
                   rounded-full bg-cyan text-bg-base text-[9px] font-bold leading-none
                   flex items-center justify-center border-2 border-bg-panel"
            style="box-shadow: 0 0 8px rgba(45, 212, 191, 0.5);"
          >
            3
          </span>
        </button>

        <span class="header-divider-tall self-center ml-1 opacity-60" />

        <div class="flex items-center gap-2.5 pl-3">
          <div class="text-right leading-tight hidden lg:block">
            <p class="text-xs font-semibold text-ink">{{ nome }}</p>
            <p class="text-[10px] text-ink-3 capitalize flex items-center gap-1 justify-end">
              <span class="w-1 h-1 rounded-full bg-cyan" />
              {{ perfilLabel }}
            </p>
          </div>

          <button
            @click="logout"
            class="relative w-9 h-9 rounded-md flex items-center justify-center
                   bg-gradient-to-br from-brand-bright to-brand-deep
                   text-ink text-[13px] font-bold
                   hover:border-cyan/40 transition-colors"
            style="border: 1px solid rgba(255, 255, 255, 0.12);"
            :title="`Sair (${nome})`"
          >
            {{ inicial }}

            <span
              class="absolute -bottom-0.5 -right-0.5 w-2.5 h-2.5 rounded-full bg-success
                     border-2 border-bg-panel"
            />
          </button>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import {
  LayoutGrid, Building2, Boxes, Truck, FileText, Bell,
  Activity, ChevronRight,
} from 'lucide-vue-next'
import LogoMark   from './LogoMark.vue'
import HeaderNav  from './HeaderNav.vue'

const route  = useRoute()
const router = useRouter()

const navItems = [
  { to: '/dashboard',   label: 'Dashboard',   icon: LayoutGrid },
  { to: '/empresas',    label: 'Empresas',    icon: Building2  },
  { to: '/lotes',       label: 'Lotes',       icon: Boxes      },
  { to: '/transportes', label: 'Transportes', icon: Truck      },
  { to: '/relatorios',  label: 'Relatórios',  icon: FileText   },
]

const pageMetaMap = {
  '/dashboard':   'Centro de Operações',
  '/empresas':    'Cadastro de Empresas',
  '/lotes':       'Gestão de Lotes',
  '/transportes': 'Manifestos de Transporte',
  '/relatorios':  'Relatórios e Auditoria',
}
const pageMeta = computed(() => pageMetaMap[route.path] || 'Sistema')

const nome    = localStorage.getItem('nome')   || 'Operador'
const perfil  = localStorage.getItem('perfil') || 'ADMIN'
const inicial = computed(() => nome.charAt(0).toUpperCase())
const perfilLabel = computed(() => perfil.toLowerCase())

const logout = () => {
  localStorage.clear()
  router.push({ name: 'login' })
}

const now = ref(new Date())
let clockTimer = null

const clock = computed(() =>
  now.value.toLocaleTimeString('pt-BR', {
    hour: '2-digit', minute: '2-digit', second: '2-digit',
  })
)

const dateLabel = computed(() => {
  const d = now.value
  const wd = d.toLocaleDateString('pt-BR', { weekday: 'short' })
              .replace(/[.,]/g, '')
              .slice(0, 3)
              .toUpperCase()
  const day = String(d.getDate()).padStart(2, '0')
  const mo = d.toLocaleDateString('pt-BR', { month: 'short' })
              .replace(/[.,]/g, '')
              .toUpperCase()
  return `${wd} ${day} ${mo}`
})

const latency = ref(42)
const latencyBars = ref([4, 6, 5, 7, 4, 6])
let latencyTimer = null

const latencyToHeight = (ms) => {
  const clamped = Math.max(28, Math.min(72, ms))
  return Math.max(2, Math.min(12, Math.round((clamped - 24) / 4)))
}

const updateLatency = () => {
  const delta = (Math.random() - 0.5) * 7
  const next = Math.max(32, Math.min(56, latency.value + delta))
  latency.value = Math.round(next)
  latencyBars.value = [
    ...latencyBars.value.slice(1),
    latencyToHeight(next),
  ]
}

onMounted(() => {
  clockTimer   = setInterval(() => (now.value = new Date()), 1000)
  latencyTimer = setInterval(updateLatency, 2200)
})
onUnmounted(() => {
  clearInterval(clockTimer)
  clearInterval(latencyTimer)
})
</script>

<style scoped>

.header-divider {
  display: inline-block;
  width: 1px;
  height: 10px;
  flex-shrink: 0;
  background: linear-gradient(
    to bottom,
    transparent,
    rgba(255, 255, 255, 0.10) 25%,
    rgba(255, 255, 255, 0.10) 75%,
    transparent
  );
}
.header-divider-tall {
  display: inline-block;
  width: 1px;
  height: 28px;
  flex-shrink: 0;
  background: linear-gradient(
    to bottom,
    transparent,
    rgba(255, 255, 255, 0.10) 20%,
    rgba(255, 255, 255, 0.10) 80%,
    transparent
  );
}
</style>
