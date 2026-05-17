<template>
  <div class="relative min-h-screen w-full overflow-hidden bg-bg-base">

    <div
      class="absolute inset-0 bg-cover bg-center pointer-events-none"
      :style="{ backgroundImage: `url(${URL_FUNDO})` }"
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
          <p class="font-display text-[19px] text-ink brand-title">EcoTrack</p>
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
        Sistema de gestão ambiental
      </div>
    </header>

    <div class="relative z-10 grid grid-cols-1 lg:grid-cols-12 gap-10 px-10 pb-12 mt-4">

      <section class="lg:col-span-7 flex flex-col justify-center max-w-2xl fade-up">
        <p class="eyebrow text-cyan mb-5">Conformidade PNRS · Gestão de resíduos</p>
        <h1 class="font-display text-[88px] sm:text-[112px] leading-[0.88] text-ink hero-title">
          Rastreie.<br />
          Registre.<br />
          <span class="text-cyan hero-title-accent">Regularize.</span>
        </h1>
        <p class="editorial mt-7 text-[16px] max-w-lg hero-paragraph">
          Plataforma de controle de resíduos sólidos em conformidade com a Política Nacional
          de Resíduos Sólidos. Empresas, lotes e descartes organizados em uma interface clara e rastreável.
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
            v-for="badge in BADGES"
            :key="badge.label"
            class="inline-flex items-center gap-2 px-3 py-1.5 rounded-full
                   bg-bg-base/40 backdrop-blur-md border border-white/10
                   text-[11px] text-ink-2"
          >
            <component :is="badge.icon" :size="12" class="text-cyan" />
            {{ badge.label }}
          </span>
        </div>
      </section>

      <section class="lg:col-span-5 flex items-center fade-up-2">
        <div class="card-login w-full max-w-md mx-auto rounded-2xl helmet-stripe
                    bg-bg-panel/55 backdrop-blur-2xl border border-white/10 p-8">
          <div class="flex gap-1 p-1 rounded-md bg-bg-base/60 border border-white/5 mb-6">
            <button
              @click="modo = 'login'"
              class="flex-1 py-2 rounded-[5px] text-[11.5px] font-bold tracking-wider transition-colors"
              :class="modo === 'login' ? 'bg-bg-elevated text-ink' : 'text-ink-3 hover:text-ink'"
            >
              ENTRAR
            </button>
            <button
              @click="modo = 'cadastro'"
              class="flex-1 py-2 rounded-[5px] text-[11.5px] font-bold tracking-wider transition-colors"
              :class="modo === 'cadastro' ? 'bg-bg-elevated text-ink' : 'text-ink-3 hover:text-ink'"
            >
              CRIAR CONTA
            </button>
          </div>

          <div v-if="modo === 'login'" class="flex flex-col gap-4">
            <h2 class="section-title text-[24px]">Acesso ao sistema</h2>

            <FormField
              v-model="email"
              label="E-mail operacional"
              placeholder="seu@email.com"
              type="email"
            >
              <template #prefix>
                <Mail :size="14" class="text-ink-3" />
              </template>
            </FormField>

            <FormField
              v-model="senha"
              label="Senha"
              placeholder="••••••••"
              type="password"
              @keyup.enter="login"
            >
              <template #prefix>
                <Lock :size="14" class="text-ink-3" />
              </template>
            </FormField>

            <p v-if="erro" class="alerta-erro">
              <AlertCircle :size="14" /> {{ erro }}
            </p>

            <button
              @click="login"
              :disabled="carregando"
              class="botao-principal"
            >
              <Loader2 v-if="carregando" :size="15" class="animate-spin" />
              <ArrowRight v-else :size="15" />
              {{ carregando ? 'AUTENTICANDO...' : 'ENTRAR NO SISTEMA' }}
            </button>

            <p class="mono-tag text-ink-3 text-center text-[10px] mt-1">
              Sessão protegida por autenticação JWT
            </p>
          </div>

          <div v-else class="flex flex-col gap-3.5">
            <h2 class="section-title text-[24px]">Solicitar credenciais</h2>

            <FormField v-model="cadastroNome" label="Nome completo" placeholder="Seu nome">
              <template #prefix>
                <User :size="14" class="text-ink-3" />
              </template>
            </FormField>

            <FormField
              v-model="cadastroEmail"
              label="E-mail"
              placeholder="seu@email.com"
              type="email"
            >
              <template #prefix>
                <Mail :size="14" class="text-ink-3" />
              </template>
            </FormField>

            <FormField
              v-model="cadastroSenha"
              label="Senha"
              placeholder="Mínimo 6 caracteres"
              type="password"
            >
              <template #prefix>
                <Lock :size="14" class="text-ink-3" />
              </template>
            </FormField>

            <div>
              <label class="eyebrow block mb-1.5">Perfil</label>
              <div class="grid grid-cols-2 gap-1.5">
                <button
                  v-for="opcaoPerfil in PERFIS"
                  :key="opcaoPerfil.valor"
                  @click="cadastroPerfil = opcaoPerfil.valor"
                  class="px-3 py-2 rounded-md text-[11.5px] font-semibold border transition-colors text-left"
                  :class="cadastroPerfil === opcaoPerfil.valor
                    ? 'bg-cyan/10 border-cyan/40 text-cyan'
                    : 'bg-bg-elevated/60 border-white/10 text-ink-2 hover:border-bg-line-strong'"
                >
                  {{ opcaoPerfil.rotulo }}
                </button>
              </div>
            </div>

            <p v-if="erroCadastro" class="alerta-erro">
              <AlertCircle :size="14" /> {{ erroCadastro }}
            </p>
            <p v-if="sucessoCadastro" class="alerta-sucesso">
              <CheckCircle :size="14" /> {{ sucessoCadastro }}
            </p>

            <button
              @click="cadastrar"
              :disabled="carregando"
              class="botao-principal"
            >
              <Loader2 v-if="carregando" :size="15" class="animate-spin" />
              <UserPlus v-else :size="15" />
              {{ carregando ? 'CADASTRANDO...' : 'SOLICITAR ACESSO' }}
            </button>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  Mail, Lock, User, ArrowRight, Loader2, UserPlus,
  AlertCircle, CheckCircle, ShieldCheck, FileCheck2, Fingerprint,
} from 'lucide-vue-next'
import LogoMark from '../components/LogoMark.vue'
import FormField from '../components/ui/FormField.vue'
import api from '../services/api'

const URL_FUNDO = '/photos/login-bg.jpg'

const PERFIS = [
  { valor: 'GERADORA',       rotulo: 'Geradora' },
  { valor: 'TRANSPORTADORA', rotulo: 'Transportadora' },
  { valor: 'RECEPTORA',      rotulo: 'Receptora' },
  { valor: 'ADMIN',          rotulo: 'Administrador' },
]

const BADGES = [
  { label: 'PNRS',            icon: FileCheck2 },
  { label: 'Rastreabilidade', icon: ShieldCheck },
  { label: 'Acesso seguro',   icon: Fingerprint },
]

const router = useRouter()

const modo = ref('login')
const carregando = ref(false)

const email = ref('')
const senha = ref('')
const erro  = ref('')

const cadastroNome   = ref('')
const cadastroEmail  = ref('')
const cadastroSenha  = ref('')
const cadastroPerfil = ref('GERADORA')
const erroCadastro    = ref('')
const sucessoCadastro = ref('')

const login = async () => {
  if (carregando.value) return
  carregando.value = true
  erro.value = ''
  try {
    const resposta = await api.post('/auth/login', {
      email: email.value,
      senha: senha.value,
    })
    localStorage.setItem('token',  resposta.data.token)
    localStorage.setItem('nome',   resposta.data.nome)
    localStorage.setItem('perfil', resposta.data.perfil)
    localStorage.setItem('email',  email.value)
    router.push('/dashboard')
  } catch {
    erro.value = 'E-mail ou senha inválidos.'
  } finally {
    carregando.value = false
  }
}

const cadastrar = async () => {
  if (carregando.value) return
  carregando.value = true
  erroCadastro.value = ''
  sucessoCadastro.value = ''
  try {
    await api.post('/auth/cadastro', {
      nome:   cadastroNome.value,
      email:  cadastroEmail.value,
      senha:  cadastroSenha.value,
      perfil: cadastroPerfil.value,
    })
    sucessoCadastro.value = 'Cadastro confirmado. Redirecionando ao login...'
    setTimeout(() => {
      modo.value = 'login'
      email.value = cadastroEmail.value
      sucessoCadastro.value = ''
    }, 1800)
  } catch (e) {
    erroCadastro.value = e.response?.data?.erro || 'Erro ao cadastrar. Tente novamente.'
  } finally {
    carregando.value = false
  }
}
</script>

<style scoped>
.brand-title {
  font-weight: 600;
  letter-spacing: -0.035em;
}

.hero-title {
  font-weight: 700;
  letter-spacing: -0.05em;
  text-shadow: 0 2px 24px rgba(0, 0, 0, 0.4);
  font-feature-settings: 'ss01';
}

.hero-title-accent {
  font-weight: 600;
  font-style: italic;
  letter-spacing: -0.045em;
}

.hero-paragraph {
  text-shadow: 0 1px 8px rgba(0, 0, 0, 0.6);
}

.card-login {
  box-shadow: 0 32px 64px -16px rgba(0, 0, 0, 0.7),
              0 1px 0 0 rgba(255, 255, 255, 0.08) inset;
}

.alerta-erro {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 12px;
  color: rgb(248 113 113);
  background-color: rgba(248, 113, 113, 0.1);
  border: 1px solid rgba(248, 113, 113, 0.3);
  border-radius: 6px;
  padding: 8px 12px;
}

.alerta-sucesso {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 12px;
  color: rgb(74 222 128);
  background-color: rgba(74, 222, 128, 0.1);
  border: 1px solid rgba(74, 222, 128, 0.3);
  border-radius: 6px;
  padding: 8px 12px;
}

.botao-principal {
  position: relative;
  width: 100%;
  padding: 12px 0;
  margin-top: 4px;
  border-radius: 6px;
  font-weight: 700;
  font-size: 12.5px;
  letter-spacing: 0.1em;
  background-color: #2DD4BF;
  color: rgb(7 24 30);
  transition: background-color 200ms;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.botao-principal:hover:not(:disabled) {
  background-color: rgba(45, 212, 191, 0.9);
}

.botao-principal:disabled {
  opacity: 0.5;
}
</style>
