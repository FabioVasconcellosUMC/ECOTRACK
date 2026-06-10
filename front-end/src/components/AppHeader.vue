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

      <div class="flex items-stretch h-10 self-center rounded-md bg-bg-elevated border border-bg-line overflow-hidden">
        <button
          ref="bellButton"
          @click="alternarNotificacoes"
          class="relative flex items-center justify-center w-10 text-ink-2 hover:text-cyan hover:bg-bg-base/40 transition-colors"
          :class="{ 'text-cyan': notificacoesAbertas }"
          title="Notificações"
        >
          <Bell :size="15" />
          <span
            v-if="totalNaoLidas > 0"
            class="absolute top-1.5 right-1.5 min-w-[16px] h-[16px] px-1 rounded-full bg-danger text-white text-[9px] font-bold flex items-center justify-center border-2 border-bg-elevated"
          >
            {{ totalNaoLidas }}
          </span>
        </button>

        <span class="w-px self-stretch bg-bg-line my-2" />

        <div class="flex items-center px-3.5 leading-tight">
          <div class="flex flex-col items-end">
            <span class="text-[12px] font-semibold text-ink">{{ nomeUsuario }}</span>
            <span class="text-[10px] text-ink-3 capitalize">{{ perfilUsuario }}</span>
          </div>
        </div>

        <span v-if="podeGerenciarUsuarios || podeExcluirPropriaConta" class="w-px self-stretch bg-bg-line my-2" />

        <button
          v-if="podeGerenciarUsuarios"
          @click="abrirGerenciamentoUsuarios"
          class="flex items-center justify-center w-10 text-ink-2 hover:text-cyan hover:bg-bg-base/40 transition-colors"
          title="Gerenciar usuários"
        >
          <Users :size="14" />
        </button>

        <span v-if="podeGerenciarUsuarios && podeExcluirPropriaConta" class="w-px self-stretch bg-bg-line my-2" />

        <button
          v-if="podeExcluirPropriaConta"
          @click="abrirConfirmacaoExclusao"
          class="flex items-center justify-center w-10 text-ink-2 hover:text-danger hover:bg-danger-soft transition-colors"
          :title="`Excluir conta (${nomeUsuario})`"
        >
          <Trash2 :size="14" />
        </button>

        <span class="w-px self-stretch bg-bg-line my-2" />

        <button
          @click="sair"
          class="flex items-center justify-center w-10 text-ink-2 hover:text-cyan hover:bg-bg-base/40 transition-colors"
          :title="`Sair (${nomeUsuario})`"
        >
          <LogOut :size="14" />
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
        <div class="px-4 py-3 border-b border-bg-line shrink-0">
          <p class="eyebrow text-cyan">Notificações</p>
          <p class="text-[12px] text-ink-3 mt-1">Central interna do sistema</p>
        </div>

        <div class="px-5 py-8 text-center flex-1">
          <Bell :size="22" class="mx-auto text-ink-4 mb-3" />
          <p class="text-[13px] text-ink font-semibold">Sem notificações internas</p>
          <p class="text-[11px] text-ink-3 mt-1 leading-relaxed">
            Os avisos operacionais reais são enviados por e-mail quando necessário.
          </p>
        </div>
      </div>
    </Transition>
  </Teleport>

  <Teleport to="body">
    <Transition name="modal">
      <div
        v-if="confirmacaoExclusaoAberta"
        class="fixed inset-0 z-[10000] flex items-center justify-center px-4 bg-black/65 backdrop-blur-sm"
        @click.self="fecharConfirmacaoExclusao"
      >
        <section class="w-full max-w-md rounded-2xl bg-bg-panel border border-bg-line-strong shadow-2xl helmet-stripe overflow-hidden">
          <header class="flex items-start justify-between gap-4 px-6 py-5 border-b border-bg-line">
            <div class="flex items-start gap-3">
              <span class="flex items-center justify-center w-10 h-10 rounded-md bg-danger-soft border border-danger/30 text-danger shrink-0">
                <AlertTriangle :size="18" />
              </span>
              <div>
                <p class="section-title text-[20px]">Excluir conta</p>
                <p class="text-[12px] text-ink-3 mt-1 leading-relaxed">
                  Seu acesso será desativado e seus dados pessoais serão criptografados.
                </p>
              </div>
            </div>
            <button
              @click="fecharConfirmacaoExclusao"
              class="w-8 h-8 rounded-md text-ink-3 hover:text-ink hover:bg-bg-elevated transition-colors shrink-0"
              title="Fechar"
            >
              <X :size="16" class="mx-auto" />
            </button>
          </header>

          <div class="px-6 py-5 space-y-4">
            <p
              v-if="erroExclusao"
              class="flex items-center gap-2 text-[12px] text-danger bg-danger-soft border border-danger/30 rounded-md px-3 py-2"
            >
              <AlertTriangle :size="14" /> {{ erroExclusao }}
            </p>

            <div class="flex justify-end gap-2">
              <button
                @click="fecharConfirmacaoExclusao"
                :disabled="excluindoConta"
                class="px-4 h-10 rounded-md border border-bg-line text-[12px] font-bold tracking-wider text-ink-2 hover:text-ink hover:bg-bg-elevated transition-colors disabled:opacity-50"
              >
                CANCELAR
              </button>
              <button
                @click="excluirMinhaConta"
                :disabled="excluindoConta"
                class="px-4 h-10 rounded-md bg-danger text-white text-[12px] font-bold tracking-wider hover:bg-danger/90 transition-colors disabled:opacity-50 flex items-center gap-2"
              >
                <Loader2 v-if="excluindoConta" :size="14" class="animate-spin" />
                <Trash2 v-else :size="14" />
                {{ excluindoConta ? 'EXCLUINDO...' : 'EXCLUIR CONTA' }}
              </button>
            </div>
          </div>
        </section>
      </div>
    </Transition>
  </Teleport>

  <Teleport to="body">
    <Transition name="modal">
      <div
        v-if="gerenciamentoUsuariosAberto"
        class="fixed inset-0 z-[10000] flex items-center justify-center px-4 bg-black/65 backdrop-blur-sm"
        @click.self="fecharGerenciamentoUsuarios"
      >
        <section class="w-full max-w-3xl max-h-[82vh] rounded-2xl bg-bg-panel border border-bg-line-strong shadow-2xl helmet-stripe overflow-hidden flex flex-col">
          <header class="flex items-start justify-between gap-4 px-6 py-5 border-b border-bg-line">
            <div class="flex items-start gap-3">
              <span class="flex items-center justify-center w-10 h-10 rounded-md bg-cyan-soft border border-cyan/30 text-cyan shrink-0">
                <Users :size="18" />
              </span>
              <div>
                <p class="section-title text-[20px]">Gerenciar usuários</p>
                <p class="text-[12px] text-ink-3 mt-1 leading-relaxed">
                  Exclusão lógica disponível apenas para usuários não administradores.
                </p>
              </div>
            </div>
            <button
              @click="fecharGerenciamentoUsuarios"
              class="w-8 h-8 rounded-md text-ink-3 hover:text-ink hover:bg-bg-elevated transition-colors shrink-0"
              title="Fechar"
            >
              <X :size="16" class="mx-auto" />
            </button>
          </header>

          <div class="px-6 py-5 overflow-y-auto space-y-4">
            <p
              v-if="erroUsuarios"
              class="flex items-center gap-2 text-[12px] text-danger bg-danger-soft border border-danger/30 rounded-md px-3 py-2"
            >
              <AlertTriangle :size="14" /> {{ erroUsuarios }}
            </p>

            <div v-if="carregandoUsuarios" class="flex items-center justify-center gap-2 py-10 text-ink-3 text-[12px] tracking-wider">
              <Loader2 :size="16" class="animate-spin text-cyan" />
              CARREGANDO USUÁRIOS...
            </div>

            <div v-else-if="usuarios.length === 0" class="py-10 text-center text-[13px] text-ink-3">
              Nenhum usuário ativo encontrado.
            </div>

            <div v-else class="space-y-2">
              <article
                v-for="usuario in usuarios"
                :key="usuario.publicId"
                class="flex items-center justify-between gap-4 rounded-md border border-bg-line bg-bg-base/45 px-4 py-3"
              >
                <div class="min-w-0">
                  <div class="flex items-center gap-2">
                    <p class="text-[13px] font-semibold text-ink truncate">{{ usuario.nome }}</p>
                    <span class="px-2 py-0.5 rounded-full border border-cyan/25 bg-cyan-soft text-cyan text-[10px] font-bold uppercase">
                      {{ usuario.perfil }}
                    </span>
                  </div>
                  <p class="text-[11px] text-ink-3 mt-1 truncate">
                    {{ usuario.emailMascarado }}<span v-if="usuario.empresa"> · {{ usuario.empresa }}</span>
                  </p>
                </div>

                <button
                  @click="confirmarExclusaoUsuario(usuario)"
                  :disabled="usuario.perfil === 'ADMIN' || excluindoUsuarioId === usuario.publicId"
                  class="h-9 px-3 rounded-md border border-danger/30 text-danger text-[11px] font-bold tracking-wider hover:bg-danger-soft transition-colors disabled:opacity-45 disabled:cursor-not-allowed flex items-center gap-2 shrink-0"
                  :title="usuario.perfil === 'ADMIN' ? 'Contas administradoras não podem ser excluídas' : 'Excluir usuário'"
                >
                  <Loader2 v-if="excluindoUsuarioId === usuario.publicId" :size="13" class="animate-spin" />
                  <Trash2 v-else :size="13" />
                  EXCLUIR
                </button>
              </article>
            </div>
          </div>
        </section>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { computed, ref, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import {
  LayoutGrid, Building2, Boxes, LogOut, Truck, FileText, Bell,
  Trash2, AlertTriangle, X, Loader2, Users,
} from 'lucide-vue-next'
import LogoMark from './LogoMark.vue'
import HeaderNav from './HeaderNav.vue'
import api from '../services/api'

const NAV_ITEMS = [
  { to: '/dashboard',   label: 'Dashboard',   icon: LayoutGrid },
  { to: '/empresas',    label: 'Empresas',    icon: Building2 },
  { to: '/lotes',       label: 'Lotes',       icon: Boxes },
  { to: '/transportes', label: 'Transportes', icon: Truck },
  { to: '/relatorios',  label: 'Relatórios',  icon: FileText },
]

const LARGURA_DROPDOWN = 340
const MARGEM_VIEWPORT = 12
const ESPACO_BOTAO_DROPDOWN = 8

const router = useRouter()

const nomeUsuario = localStorage.getItem('nome') || 'Operador'
const perfilUsuario = computed(() =>
  (localStorage.getItem('perfil') || 'ADMIN').toLowerCase(),
)
const podeExcluirPropriaConta = computed(() => perfilUsuario.value !== 'admin')
const podeGerenciarUsuarios = computed(() => perfilUsuario.value === 'admin')

const totalNaoLidas = 0

const estiloOculto = () => ({
  position: 'fixed',
  top: '-9999px',
  left: '-9999px',
  width: `${LARGURA_DROPDOWN}px`,
  zIndex: '9999',
  visibility: 'hidden',
  display: 'flex',
  flexDirection: 'column',
})

const notificacoesAbertas = ref(false)
const bellButton = ref(null)
const dropdown = ref(null)
const estiloDropdown = ref(estiloOculto())
const confirmacaoExclusaoAberta = ref(false)
const excluindoConta = ref(false)
const erroExclusao = ref('')
const gerenciamentoUsuariosAberto = ref(false)
const usuarios = ref([])
const carregandoUsuarios = ref(false)
const excluindoUsuarioId = ref(null)
const erroUsuarios = ref('')

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
    visibility: 'visible',
    display: 'flex',
    flexDirection: 'column',
  }
}

const alternarNotificacoes = async () => {
  notificacoesAbertas.value = !notificacoesAbertas.value
  if (notificacoesAbertas.value) {
    estiloDropdown.value = estiloOculto()
    await nextTick()
    calcularPosicaoDropdown()
  }
}

const fecharNotificacoes = () => {
  notificacoesAbertas.value = false
}


const sair = () => {
  localStorage.clear()
  router.push('/')
}

const abrirConfirmacaoExclusao = () => {
  if (!podeExcluirPropriaConta.value) return
  erroExclusao.value = ''
  confirmacaoExclusaoAberta.value = true
}

const fecharConfirmacaoExclusao = () => {
  if (excluindoConta.value) return
  confirmacaoExclusaoAberta.value = false
  erroExclusao.value = ''
}

const abrirGerenciamentoUsuarios = async () => {
  if (!podeGerenciarUsuarios.value) return
  gerenciamentoUsuariosAberto.value = true
  await carregarUsuarios()
}

const fecharGerenciamentoUsuarios = () => {
  if (excluindoUsuarioId.value) return
  gerenciamentoUsuariosAberto.value = false
  erroUsuarios.value = ''
}

const carregarUsuarios = async () => {
  carregandoUsuarios.value = true
  erroUsuarios.value = ''
  try {
    const resposta = await api.get('/usuarios')
    usuarios.value = resposta.data || []
  } catch (e) {
    erroUsuarios.value = e.response?.data?.erro || 'Não foi possível carregar os usuários.'
  } finally {
    carregandoUsuarios.value = false
  }
}

const confirmarExclusaoUsuario = async (usuario) => {
  if (!podeGerenciarUsuarios.value || usuario.perfil === 'ADMIN') return
  const ok = window.confirm(`Excluir logicamente o usuário ${usuario.nome}?`)
  if (!ok) return

  excluindoUsuarioId.value = usuario.publicId
  erroUsuarios.value = ''
  try {
    await api.delete(`/usuarios/${usuario.publicId}`)
    usuarios.value = usuarios.value.filter((item) => item.publicId !== usuario.publicId)
  } catch (e) {
    erroUsuarios.value = e.response?.data?.erro || 'Não foi possível excluir o usuário.'
  } finally {
    excluindoUsuarioId.value = null
  }
}

const excluirMinhaConta = async () => {
  if (!podeExcluirPropriaConta.value) {
    erroExclusao.value = 'A conta administradora principal não pode ser excluída por esta tela.'
    return
  }
  if (excluindoConta.value) return
  excluindoConta.value = true
  erroExclusao.value = ''
  try {
    await api.delete('/usuarios/me')
    localStorage.clear()
    router.push('/')
  } catch (e) {
    erroExclusao.value = e.response?.data?.erro || 'Não foi possível excluir sua conta.'
  } finally {
    excluindoConta.value = false
  }
}

const cliqueForaDoDropdown = (evento) => {
  if (!notificacoesAbertas.value) return
  const clicouNoBotao = bellButton.value?.contains(evento.target)
  const clicouNoDropdown = dropdown.value?.contains(evento.target)
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

.modal-enter-active,
.modal-leave-active {
  transition: opacity 180ms ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
</style>
