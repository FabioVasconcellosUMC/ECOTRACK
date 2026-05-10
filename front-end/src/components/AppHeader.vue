<template>
  <header
    class="fixed top-0 inset-x-0 z-30 border-b border-white/10"
    style="
      background: rgba(7, 24, 30, 0.78);
      backdrop-filter: blur(28px) saturate(130%);
      -webkit-backdrop-filter: blur(28px) saturate(130%);
      box-shadow: 0 20px 48px -24px rgba(0, 0, 0, 0.7);
    "
  >
    <div class="h-[72px] flex items-stretch px-5 lg:px-8">
      <RouterLink to="/dashboard" class="flex items-center gap-2.5 pr-5 h-full group">
        <LogoMark :size="34" />
        <div class="leading-tight">
          <p class="font-display text-[19px] text-ink group-hover:text-cyan transition-colors duration-200" style="font-weight: 600; letter-spacing: -0.035em;">
            EcoTrack
          </p>
          <p class="eyebrow text-[8.5px] mt-0.5">Gestão de Resíduos</p>
        </div>
      </RouterLink>

      <span class="header-divider-tall self-center hidden sm:inline-block" />

      <div class="ml-2 hidden md:block">
        <HeaderNav :items="navItems" />
      </div>

      <div class="flex-1" />

      <div class="flex items-center gap-3">
        <span class="hidden lg:inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-success-soft border border-success/30 text-success text-[11px] font-semibold">
          <ShieldCheck :size="13" />
          PNRS · Lei 12.305/2010
        </span>

        <div class="relative">
          <button
            @click="toggleNotificacoes"
            class="relative flex items-center justify-center w-9 h-9 rounded-md bg-bg-elevated border border-bg-line text-ink-2 hover:border-cyan/40 hover:text-cyan transition-colors"
            title="Notificações"
          >
            <Bell :size="15" />
            <span
              v-if="notificacoesNaoLidas > 0"
              class="absolute -right-1 -top-1 min-w-4 h-4 px-1 rounded-full bg-danger text-white text-[9px] font-bold flex items-center justify-center"
            >
              {{ notificacoesNaoLidas }}
            </span>
          </button>

          <Transition name="page">
            <div
              v-if="notificacoesAbertas"
              class="absolute right-0 top-12 w-[340px] rounded-2xl bg-bg-panel border border-bg-line-strong shadow-2xl overflow-hidden helmet-stripe"
            >
              <div class="px-4 py-3 border-b border-bg-line flex items-center justify-between gap-3">
                <div>
                  <p class="eyebrow text-cyan">Notificações</p>
                  <p class="text-[12px] text-ink-3 mt-1">Eventos importantes do sistema</p>
                </div>
                <button
                  @click="marcarComoLidas"
                  class="px-2.5 h-7 rounded-md bg-cyan/10 border border-cyan/30 text-cyan text-[10px] font-bold tracking-wider hover:bg-cyan/20 transition-colors"
                >
                  LER TODAS
                </button>
              </div>

              <div class="max-h-[300px] overflow-y-auto">
                <button
                  v-for="notificacao in notificacoes"
                  :key="notificacao.id"
                  @click="abrirNotificacao(notificacao.id)"
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
        </div>

        <span class="header-divider-tall self-center hidden lg:inline-block opacity-60" />

        <div class="text-right leading-tight hidden sm:block">
          <p class="text-xs font-semibold text-ink">{{ nome }}</p>
          <p class="text-[10px] text-ink-3 capitalize">{{ perfilLabel }}</p>
        </div>

        <button
          @click="logout"
          class="flex items-center gap-2 h-9 px-3 rounded-md bg-bg-elevated border border-bg-line text-ink-2 hover:border-cyan/40 hover:text-cyan transition-colors"
          :title="`Sair (${nome})`"
        >
          <span class="w-6 h-6 rounded-md flex items-center justify-center bg-gradient-to-br from-brand-bright to-brand-deep text-ink text-[11px] font-bold">
            {{ inicial }}
          </span>
          <LogOut :size="14" />
        </button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { LayoutGrid, Building2, Boxes, LogOut, ShieldCheck, Truck, FileText, Bell } from 'lucide-vue-next'
import LogoMark from './LogoMark.vue'
import HeaderNav from './HeaderNav.vue'

const router = useRouter()

const navItems = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutGrid },
  { to: '/empresas', label: 'Empresas', icon: Building2 },
  { to: '/lotes', label: 'Lotes', icon: Boxes },
  { to: '/transportes', label: 'Transportes', icon: Truck },
  { to: '/relatorios', label: 'Relatórios', icon: FileText },
]

const nome = localStorage.getItem('nome') || 'Operador'
const perfil = localStorage.getItem('perfil') || 'ADMIN'
const inicial = computed(() => nome.charAt(0).toUpperCase())
const perfilLabel = computed(() => perfil.toLowerCase())

const notificacoesAbertas = ref(false)
const notificacoes = ref([
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
])

const notificacoesNaoLidas = computed(() => notificacoes.value.filter((item) => !item.lida).length)

const toggleNotificacoes = () => {
  notificacoesAbertas.value = !notificacoesAbertas.value
}

const abrirNotificacao = (id) => {
  const item = notificacoes.value.find((notificacao) => notificacao.id === id)
  if (item) item.lida = true
}

const marcarComoLidas = () => {
  notificacoes.value = notificacoes.value.map((item) => ({ ...item, lida: true }))
}

const logout = () => {
  localStorage.clear()
  router.push('/')
}
</script>

<style scoped>
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
