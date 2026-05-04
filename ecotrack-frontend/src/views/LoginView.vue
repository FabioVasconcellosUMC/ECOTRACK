<template>
  <div class="relative min-h-screen w-full overflow-hidden bg-bg-base">

    <div
      class="absolute inset-0 bg-cover bg-center pointer-events-none"
      :style="{ backgroundImage: `url(${bgUrl})` }"
    />
    <div
      class="absolute inset-0 pointer-events-none"
      style="background: linear-gradient(110deg,
        rgba(4, 20, 26, 0.94) 0%,
        rgba(4, 20, 26, 0.85) 45%,
        rgba(4, 20, 26, 0.55) 75%,
        rgba(4, 20, 26, 0.70) 100%);"
    />
    <div
      class="absolute inset-0 pointer-events-none"
      style="background: radial-gradient(800px 600px at 15% 50%, rgba(0, 76, 84, 0.40), transparent 65%);"
    />
    <div class="absolute inset-0 wing-pattern opacity-20 pointer-events-none" />
    <div
      class="absolute inset-x-0 bottom-0 h-32 pointer-events-none"
      style="background: linear-gradient(to top, rgba(4, 20, 26, 0.85), transparent);"
    />

    <header class="relative z-10 flex items-center justify-between px-10 py-7">
      <div class="flex items-center gap-3">
        <LogoMark :size="38" />
        <div class="leading-tight">
          <p class="font-display text-[19px] text-ink"
             style="font-weight: 600; letter-spacing: -0.035em;">EcoTrack</p>
          <p class="eyebrow text-[9.5px] mt-1">PNRS · Lei 12.305/2010</p>
        </div>
      </div>
      <div class="hidden sm:flex items-center gap-2 mono-tag text-ink-2 text-[10.5px]
                  px-3 py-1.5 rounded-full bg-bg-base/55 backdrop-blur-md
                  border border-white/10">
        <span class="relative flex h-1.5 w-1.5">
          <span class="absolute inline-flex h-full w-full rounded-full bg-cyan opacity-75 live-pulse" />
          <span class="relative inline-flex h-1.5 w-1.5 rounded-full bg-cyan" />
        </span>
        Sistema operacional · API 42ms
      </div>
    </header>

    <div class="relative z-10 grid grid-cols-1 lg:grid-cols-12 gap-10 px-10 pb-12 mt-4">

      <section class="lg:col-span-7 flex flex-col justify-center max-w-2xl fade-up">
        <p class="eyebrow text-cyan mb-5">Conformidade PNRS · Centro de Operações</p>
        <h1
          class="font-display text-[88px] sm:text-[112px] leading-[0.88] text-ink"
          style="
            font-weight: 700;
            letter-spacing: -0.05em;
            text-shadow: 0 2px 24px rgba(0, 0, 0, 0.4);
            font-feature-settings: 'ss01';
          "
        >
          Rastreie.<br />
          Registre.<br />
          <span
            class="text-cyan"
            style="font-weight: 600; font-style: italic; letter-spacing: -0.045em;"
          >Regulamente.</span>
        </h1>
        <p
          class="editorial mt-7 text-[16px] max-w-lg"
          style="text-shadow: 0 1px 8px rgba(0, 0, 0, 0.6);"
        >
          Plataforma de controle de resíduos sólidos em conformidade com a Política Nacional
          de Resíduos Sólidos. Cada lote, transporte e descarte registrado, auditado e inviolável.
        </p>

        <dl class="mt-11 grid grid-cols-3 gap-5 max-w-xl">
          <div class="border-l-2 border-brand pl-4 fade-up-2">
            <dt class="eyebrow">Empresas</dt>
            <dd class="scoreboard text-[40px] text-ink mt-1">84</dd>
          </div>
          <div class="border-l-2 border-cyan pl-4 fade-up-3">
            <dt class="eyebrow">Lotes ativos</dt>
            <dd class="scoreboard text-[40px] text-ink mt-1">217</dd>
          </div>
          <div class="border-l-2 border-silver pl-4 fade-up-4">
            <dt class="eyebrow">Em trânsito</dt>
            <dd class="scoreboard text-[40px] text-ink mt-1">43</dd>
          </div>
        </dl>

        <div class="mt-10 flex flex-wrap items-center gap-2 fade-up-5">
          <span
            v-for="b in badges"
            :key="b.label"
            class="inline-flex items-center gap-2 px-3 py-1.5 rounded-full
                   bg-bg-base/40 backdrop-blur-md border border-white/10
                   text-[11px] text-ink-2"
          >
            <component :is="b.icon" :size="12" class="text-cyan" />
            {{ b.label }}
          </span>
        </div>
      </section>

      <section class="lg:col-span-5 flex items-center fade-up-2">
        <div
          class="relative w-full max-w-md mx-auto rounded-2xl helmet-stripe
                 bg-bg-panel/55 backdrop-blur-2xl
                 border border-white/10 p-8"
          style="box-shadow: 0 32px 64px -16px rgba(0, 0, 0, 0.7),
                             0 1px 0 0 rgba(255, 255, 255, 0.08) inset;"
        >
          <div class="flex gap-1 p-1 rounded-md bg-bg-base/60 border border-white/5 mb-6">
            <button
              @click="mode = 'login'"
              class="flex-1 py-2 rounded-[5px] text-[11.5px] font-bold tracking-wider transition-colors"
              :class="mode === 'login' ? 'bg-bg-elevated text-ink' : 'text-ink-3 hover:text-ink'"
            >
              ENTRAR
            </button>
            <button
              @click="mode = 'register'"
              class="flex-1 py-2 rounded-[5px] text-[11.5px] font-bold tracking-wider transition-colors"
              :class="mode === 'register' ? 'bg-bg-elevated text-ink' : 'text-ink-3 hover:text-ink'"
            >
              CRIAR CONTA
            </button>
          </div>

          <div v-if="mode === 'login'" class="flex flex-col gap-4">
            <h2 class="section-title text-[24px]">Acesso ao sistema</h2>

            <FieldInput v-model="email" label="E-mail operacional" placeholder="seu@email.com" type="email" :icon="Mail" />
            <FieldInput v-model="senha" label="Senha" placeholder="••••••••" type="password" :icon="Lock" @keyup.enter="login" />

            <p
              v-if="erro"
              class="flex items-center gap-2 text-[12px] text-danger
                     bg-danger-soft border border-danger/30 rounded-md px-3 py-2"
            >
              <AlertCircle :size="14" /> {{ erro }}
            </p>

            <button
              @click="login"
              :disabled="loading"
              class="relative w-full py-3 mt-1 rounded-md font-bold text-[12.5px] tracking-[0.1em]
                     bg-cyan text-bg-base hover:bg-cyan/90 transition-colors
                     flex items-center justify-center gap-2 disabled:opacity-50"
            >
              <Loader2 v-if="loading" :size="15" class="animate-spin" />
              <ArrowRight v-else :size="15" />
              {{ loading ? 'AUTENTICANDO...' : 'ENTRAR NO SISTEMA' }}
            </button>

            <p class="mono-tag text-ink-3 text-center text-[10px] mt-1">
              Sessão protegida · JWT · TLS 1.3
            </p>
          </div>

          <div v-else class="flex flex-col gap-3.5">
            <h2 class="section-title text-[24px]">Solicitar credenciais</h2>

            <FieldInput v-model="cadNome"  label="Nome completo" placeholder="Seu nome"   :icon="User" />
            <FieldInput v-model="cadEmail" label="E-mail"        placeholder="seu@email.com" type="email" :icon="Mail" />
            <FieldInput v-model="cadSenha" label="Senha"         placeholder="Mínimo 6 caracteres" type="password" :icon="Lock" />

            <div>
              <label class="eyebrow block mb-1.5">Perfil</label>
              <div class="grid grid-cols-2 gap-1.5">
                <button
                  v-for="p in perfis"
                  :key="p.value"
                  @click="cadPerfil = p.value"
                  class="px-3 py-2 rounded-md text-[11.5px] font-semibold border transition-colors text-left"
                  :class="cadPerfil === p.value
                    ? 'bg-cyan/10 border-cyan/40 text-cyan'
                    : 'bg-bg-elevated/60 border-white/10 text-ink-2 hover:border-bg-line-strong'"
                >
                  {{ p.label }}
                </button>
              </div>
            </div>

            <p
              v-if="erroCadastro"
              class="flex items-center gap-2 text-[12px] text-danger
                     bg-danger-soft border border-danger/30 rounded-md px-3 py-2"
            >
              <AlertCircle :size="14" /> {{ erroCadastro }}
            </p>
            <p
              v-if="sucessoCadastro"
              class="flex items-center gap-2 text-[12px] text-success
                     bg-success-soft border border-success/30 rounded-md px-3 py-2"
            >
              <CheckCircle :size="14" /> {{ sucessoCadastro }}
            </p>

            <button
              @click="cadastrar"
              :disabled="loading"
              class="w-full py-3 mt-1 rounded-md font-bold text-[12.5px] tracking-[0.1em]
                     bg-cyan text-bg-base hover:bg-cyan/90 transition-colors
                     flex items-center justify-center gap-2 disabled:opacity-50"
            >
              <Loader2 v-if="loading" :size="15" class="animate-spin" />
              <UserPlus v-else :size="15" />
              {{ loading ? 'CADASTRANDO...' : 'SOLICITAR ACESSO' }}
            </button>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, h } from 'vue'
import { useRouter } from 'vue-router'
import {
  Mail, Lock, User, ArrowRight, Loader2, UserPlus,
  AlertCircle, CheckCircle, ShieldCheck, FileCheck2, Fingerprint,
} from 'lucide-vue-next'
import LogoMark from '../components/LogoMark.vue'
import api from '../services/api'

const router = useRouter()
const bgUrl  = '/photos/login-bg.jpg'

const mode    = ref('login')
const loading = ref(false)

const email = ref('')
const senha = ref('')
const erro  = ref('')

const cadNome   = ref('')
const cadEmail  = ref('')
const cadSenha  = ref('')
const cadPerfil = ref('GERADORA')
const erroCadastro    = ref('')
const sucessoCadastro = ref('')

const perfis = [
  { value: 'GERADORA',       label: 'Geradora' },
  { value: 'TRANSPORTADORA', label: 'Transportadora' },
  { value: 'RECEPTORA',      label: 'Receptora' },
  { value: 'ADMIN',          label: 'Administrador' },
]

const badges = [
  { label: 'ISO 14001',         icon: FileCheck2 },
  { label: 'Certificado IBAMA', icon: ShieldCheck },
  { label: 'JWT · TLS 1.3',     icon: Fingerprint },
]

const login = async () => {
  if (loading.value) return
  loading.value = true
  erro.value = ''
  try {
    const r = await api.post('/auth/login', { email: email.value, senha: senha.value })
    localStorage.setItem('token',  r.data.token)
    localStorage.setItem('nome',   r.data.nome)
    localStorage.setItem('perfil', r.data.perfil)
    localStorage.setItem('email',  email.value)
    router.push('/dashboard')
  } catch {
    erro.value = 'E-mail ou senha inválidos.'
  } finally {
    loading.value = false
  }
}

const cadastrar = async () => {
  if (loading.value) return
  loading.value = true
  erroCadastro.value = ''
  sucessoCadastro.value = ''
  try {
    await api.post('/auth/cadastro', {
      nome:   cadNome.value,
      email:  cadEmail.value,
      senha:  cadSenha.value,
      perfil: cadPerfil.value,
    })
    sucessoCadastro.value = 'Cadastro confirmado. Redirecionando ao login...'
    setTimeout(() => {
      mode.value = 'login'
      email.value = cadEmail.value
      sucessoCadastro.value = ''
    }, 1800)
  } catch (e) {
    erroCadastro.value = e.response?.data?.erro || 'Erro ao cadastrar. Tente novamente.'
  } finally {
    loading.value = false
  }
}

const FieldInput = (props, { emit }) => h('div', { class: 'flex flex-col gap-1.5' }, [
  h('label', { class: 'eyebrow' }, props.label),
  h('div', {
    class: 'flex items-center gap-2 px-3 h-11 rounded-md bg-bg-base/60 ' +
           'border border-white/10 focus-within:border-cyan/50 ' +
           'focus-within:bg-bg-elevated/80 transition-colors',
  }, [
    props.icon ? h(props.icon, { size: 14, class: 'text-ink-3 shrink-0' }) : null,
    h('input', {
      type:  props.type || 'text',
      placeholder: props.placeholder,
      value: props.modelValue,
      onInput: (e) => emit('update:modelValue', e.target.value),
      onKeyup: (e) => emit('keyup', e),
      class: 'flex-1 bg-transparent outline-none text-[13.5px] text-ink placeholder:text-ink-4',
    }),
  ]),
])
FieldInput.props = ['modelValue', 'label', 'placeholder', 'type', 'icon']
FieldInput.emits = ['update:modelValue', 'keyup']
</script>
