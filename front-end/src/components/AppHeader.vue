<template>
  <header class="ecotrack-header fixed top-0 inset-x-0 z-30 border-b border-white/10">
    <div class="h-[72px] flex items-stretch px-5 lg:px-8">
      <RouterLink to="/dashboard" class="flex items-center gap-2.5 pr-5 h-full group">
        <LogoMark :size="34" />
        <div class="leading-tight">
          <p class="font-display text-[19px] text-ink group-hover:text-cyan transition-colors duration-200 brand-title">
            EcoTrack
          </p>
          <p class="eyebrow text-[8.5px] mt-0.5">Gestão de Resíduos</p>
        </div>
      </RouterLink>

      <span class="header-divider self-center hidden sm:inline-block" />

      <div class="ml-2 hidden md:block">
        <HeaderNav :items="NAV_ITEMS" />
      </div>

      <div class="flex-1" />

      <div class="flex items-center gap-3">
        <span class="hidden lg:inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-success-soft border border-success/30 text-success text-[11px] font-semibold">
          <ShieldCheck :size="13" />
          PNRS · Lei 12.305/2010
        </span>

        <div class="relative self-center">
          <button
            ref="bellButton"
            @click="alternarNotificacoes"
            class="relative flex items-center justify-center w-9 h-9 rounded-md bg-bg-elevated border border-bg-line text-ink-2 hover:border-cyan/40 hover:text-cyan transition-colors"
            title="Notificações"
          >
            <Bell :size="15" />
            <span
              v-if="totalNaoLidas > 0"
              class="absolute -right-1 -top-1 min-w-4 h-4 px-1 rounded-full bg-danger text-white text-[9px] font-bold flex items-center justify-center"
            >
              {{ totalNaoLidas }}
            </span>
          </button>
        </div>

        <span class="header-divider self-center hidden lg:inline-block opacity-60" />

        <div class="text-right leading-tight hidden sm:block">
          <p class="text-xs font-semibold text-ink">{{ nomeUsuario }}</p>
          <p class="text-[10px] text-ink-3 capitalize">{{ perfilUsuario }}</p>
        </div>

        <button
          @click="sair"
          class="flex items-center gap-2 h-9 px-3.5 rounded-md bg-bg-elevated border border-bg-line text-ink-2
                 text-[11.5px] font-bold tracking-wider hover:border-cyan/40 hover:text-cyan transition-colors"
          :title="`Sair (${nomeUsuario})`"
        >
          <LogOut :size="14" />
          SAIR
        </button>
      </div>
    </div>
  </header>

  <Teleport to="body">
    <Transition name="notif">
      <div
        v-if="notificacoesAbertas"
        ref="dropdown"
        class="notif-dropdown rounded-2xl bg-bg-panel border border-bg-line-strong shadow-2xl helmet-stripe overflow-hidden"
        :style="estiloDropdown"
      >
        <div class="px-4 py-3 border-b border-bg-line flex items-center justify-between gap-3 shrink-0">
          <div>
            <p class="eyebrow text-cyan">Notificações</p>
            <p class="text-[12px] text-ink-3 mt-1">Eventos importantes do sistema</p>
          </div>
          <button
            @click="marcarTodasComoLidas"
            class="px-2.5 h-7 rounded-md bg-cyan/10 border border-cyan/30 text-cyan text-[10px] font-bold tracking-wider hover:bg-cyan/20 transition-colors shrink-0"
          >
            LER TODAS
          </button>
        </div>

        <div class="overflow-y-auto flex-1">
          <button
            v-for="notificacao in notificacoes"
            :key="notificacao.id"
            @click="marcarComoLida(notificacao.id)"
            class="w-full px-4 py-3 border-b border-bg-line text-left hover:bg-bg-elevated transition-colors flex gap-3"
          >
            <span
              class="mt-1 w-2 h-2 rounded-full shrink-0"
              :class="notificacao.lida ? 'bg-ink-4' : 'bg-cyan live-pulse'"
            />
            <span class="min-w-0">
              <span class="block text-[13px] text-ink font-semibold leading-snug">{{ notificacao.titulo }}</span>
              <span class="block text-[12px] text-ink-3 mt-1 leading-snug">{{ notificacao.descricao }}</span>
              <span class="block mono-tag text-[10px] text-ink-4 mt-1.5">{{ notificacao.tempo }}</span>
            </span>
          </button>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { computed, ref, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import {
  LayoutGrid, Building2, Boxes, LogOut, ShieldCheck, Truck, FileText, Bell,
} from 'lucide-vue-next'
import LogoMark from './LogoMark.vue'
import HeaderNav from './HeaderNav.vue'

const NAV_ITEMS = [
  { to: '/dashboard',   label: 'Dashboard',   icon: LayoutGrid },
  { to: '/empresas',    label: 'Empresas',    icon: Building2 },
  { to: '/lotes',       label: 'Lotes',       icon: Boxes },
  { to: '/transportes', label: 'Transportes', icon: Truck },
  { to: '/relatorios',  label: 'Relatórios',  icon: FileText },
]

const NOTIFICACOES_MOCK = [
  {
    id: 1,
    titulo: 'Lote aguardando coleta',
    descricao: 'O lote LT-2026-0319 foi criado e está pronto para vincular transporte.',
    tempo: 'agora',
    lida: false,
  },
  {
    id: 2,
    titulo: 'Empresa cadastrada',
    descricao: 'TerraVerde Ambiental entrou na base como receptora.',
    tempo: '15 min atrás',
    lida: false,
  },
  {
    id: 3,
    titulo: 'Relatório disponível',
    descricao: 'O resumo demonstrativo do mês pode ser exportado em CSV.',
    tempo: '1 hora atrás',
    lida: true,
  },
]

const LARGURA_DROPDOWN = 340
const MARGEM_VIEWPORT = 12
const ESPACO_BOTAO_DROPDOWN = 8

const router = useRouter()

const nomeUsuario = localStorage.getItem('nome') || 'Operador'
const perfilUsuario = computed(() =>
  (localStorage.getItem('perfil') || 'ADMIN').toLowerCase(),
)

const notificacoes = ref(NOTIFICACOES_MOCK.map(item => ({ ...item })))
const totalNaoLidas = computed(() =>
  notificacoes.value.filter(item => !item.lida).length,
)

const notificacoesAbertas = ref(false)
const bellButton = ref(null)
const dropdown = ref(null)
const estiloDropdown = ref({})

const calcularPosicaoDropdown = () => {
  if (!bellButton.value) return
  const rect = bellButton.value.getBoundingClientRect()
  const viewportWidth = window.innerWidth
  const viewportHeight = window.innerHeight

  let left = rect.right - LARGURA_DROPDOWN
  if (left + LARGURA_DROPDOWN + MARGEM_VIEWPORT > viewportWidth) {
    left = viewportWidth - LARGURA_DROPDOWN - MARGEM_VIEWPORT
  }
  if (left < MARGEM_VIEWPORT) left = MARGEM_VIEWPORT

  const top = rect.bottom + ESPACO_BOTAO_DROPDOWN
  const maxHeight = viewportHeight - top - MARGEM_VIEWPORT

  estiloDropdown.value = {
    position: 'fixed',
    top: `${top}px`,
    left: `${left}px`,
    width: `${LARGURA_DROPDOWN}px`,
    maxHeight: `${maxHeight}px`,
    zIndex: '9999',
    display: 'flex',
    flexDirection: 'column',
  }
}

const alternarNotificacoes = async () => {
  notificacoesAbertas.value = !notificacoesAbertas.value
  if (notificacoesAbertas.value) {
    await nextTick()
    calcularPosicaoDropdown()
  }
}

const fecharNotificacoes = () => {
  notificacoesAbertas.value = false
}

const marcarComoLida = (id) => {
  const item = notificacoes.value.find(n => n.id === id)
  if (item) item.lida = true
}

const marcarTodasComoLidas = () => {
  notificacoes.value.forEach(item => { item.lida = true })
}

const sair = () => {
  localStorage.clear()
  router.push('/')
}

const cliqueForaDoDropdown = (event) => {
  if (!notificacoesAbertas.value) return
  const clicouNoBotao = bellButton.value?.contains(event.target)
  const clicouNoDropdown = dropdown.value?.contains(event.target)
  if (!clicouNoBotao && !clicouNoDropdown) fecharNotificacoes()
}

const reposicionarSeAberto = () => {
  if (notificacoesAbertas.value) calcularPosicaoDropdown()
}

onMounted(() => {
  document.addEventListener('mousedown', cliqueForaDoDropdown)
  window.addEventListener('resize', reposicionarSeAberto)
  window.addEventListener('scroll', reposicionarSeAberto, true)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', cliqueForaDoDropdown)
  window.removeEventListener('resize', reposicionarSeAberto)
  window.removeEventListener('scroll', reposicionarSeAberto, true)
})
</script>

<style scoped>
.ecotrack-header {
  background: rgba(7, 24, 30, 0.78);
  backdrop-filter: blur(28px) saturate(130%);
  -webkit-backdrop-filter: blur(28px) saturate(130%);
  box-shadow: 0 20px 48px -24px rgba(0, 0, 0, 0.7);
}

.brand-title {
  font-weight: 600;
  letter-spacing: -0.035em;
}

.header-divider {
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

<style>
.notif-enter-active,
.notif-leave-active {
  transition: opacity 180ms ease, transform 180ms ease;
}
.notif-enter-from,
.notif-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
