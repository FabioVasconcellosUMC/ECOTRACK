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

    <div class="relative z-10 min-h-screen flex items-center px-10 py-12">
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-12 w-full max-w-[1400px] mx-auto">

        <section class="lg:col-span-7 flex flex-col justify-center gap-10 min-h-[560px] fade-up">

          <div class="max-w-3xl">
            <div class="flex items-center gap-5 mb-10">
              <LogoMark
                :size="76"
                class-name="shadow-[0_0_34px_rgba(45,212,191,0.22)]"
              />
              <div class="leading-tight">
                <p
                  class="font-display text-[38px] text-ink"
                  style="font-weight: 700; letter-spacing: 0;"
                >
                  EcoTrack
                </p>
                <p class="eyebrow text-[11.5px] mt-2">Gestão de resíduos</p>
              </div>
            </div>

            <h1
              class="font-display text-ink"
              style="
                font-size: clamp(50px, 5.4vw, 72px);
                line-height: 0.98;
                font-weight: 700;
                letter-spacing: 0;
                text-shadow: 0 2px 24px rgba(0, 0, 0, 0.4);
              "
            >
              <span
                class="text-cyan block"
                style="font-weight: 600; font-style: italic; letter-spacing: 0;"
              >Rastreabilidade</span>
              <span
                class="block text-ink-2 mt-2"
                style="font-size: 0.55em; font-weight: 500; letter-spacing: 0;"
              >de resíduos sólidos</span>
            </h1>
          </div>

          <div class="flex flex-wrap items-center gap-2.5 fade-up-2">
            <span
              v-for="b in badges"
              :key="b.label"
              class="inline-flex items-center gap-2 px-3.5 py-2 rounded-full
                     bg-bg-base/40 backdrop-blur-md border border-white/10
                     text-[12px] text-ink-2"
            >
              <component :is="b.icon" :size="13" class="text-cyan" />
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
                Sessão protegida por autenticação JWT
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
]

const badges = [
  { label: 'Controle interno', icon: FileCheck2 },
  { label: 'Rastreabilidade', icon: ShieldCheck },
  { label: 'Acesso seguro',   icon: Fingerprint },
]

const login = async () => {
  if (loading.value) return
  loading.value = true
  erro.value = ''
  try {
    const emailNormalizado = email.value.trim()
    const r = await api.post('/auth/login', { email: emailNormalizado, senha: senha.value })
    localStorage.setItem('token',  r.data.token)
    localStorage.setItem('nome',   r.data.nome)
    localStorage.setItem('perfil', r.data.perfil)
    localStorage.setItem('email',  emailNormalizado)
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
    const emailNormalizado = cadEmail.value.trim()
    await api.post('/auth/cadastro', {
      nome:   cadNome.value,
      email:  emailNormalizado,
      senha:  cadSenha.value,
      perfil: cadPerfil.value,
    })
    sucessoCadastro.value = 'Cadastro confirmado. Redirecionando ao login...'
    setTimeout(() => {
      mode.value = 'login'
      email.value = emailNormalizado
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
