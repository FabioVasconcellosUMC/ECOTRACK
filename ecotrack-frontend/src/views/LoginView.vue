<template>
  <div class="flex h-screen w-screen bg-bg-primary">
    
    <div class="relative w-[45%] flex flex-col items-center justify-center gap-6 bg-[#0C2030]">
      
      <div class="w-20 h-20 rounded-2xl flex items-center justify-center bg-brand shadow-[0_0_32px_rgba(0,76,84,0.5)]">
        <Leaf :size="40" class="text-text-primary" />
      </div>

      <div class="text-center">
        <h1 class="text-4xl font-bold text-text-primary">EcoTrack</h1>
        <p class="text-sm mt-1 text-text-secondary">PNRS Lei 12.305/2010</p>
      </div>

      <div class="flex gap-3">
        <span class="px-3 py-1 rounded-full text-xs border border-bg-border text-accent flex items-center gap-1">
          <ShieldCheck :size="12" /> Certificado IBAMA
        </span>
        <span class="px-3 py-1 rounded-full text-xs border border-bg-border text-accent flex items-center gap-1">
          <CheckCircle :size="12" /> ISO 14001
        </span>
      </div>

      <div class="absolute bottom-0 left-0 w-full h-1 bg-danger"></div>
    </div>

    <div class="w-[55%] flex items-center justify-center">
      <div class="w-full max-w-sm flex flex-col gap-6 px-8">
        
        <div>
          <h2 class="text-3xl font-bold text-text-primary">Bem-vindo</h2>
          <p class="text-sm mt-1 text-text-secondary">Faça login para acessar o sistema</p>
        </div>

        <div class="flex flex-col gap-1">
          <label class="text-xs font-semibold tracking-widest uppercase text-text-secondary">E-mail</label>
          <div class="flex items-center gap-2 px-3 py-3 rounded-lg border border-bg-border bg-bg-surface">
            <Mail :size="16" class="text-text-secondary" />
            <input 
              v-model="email"
              type="email" 
              placeholder="seu@email.com"
              class="bg-transparent outline-none w-full text-sm text-text-primary placeholder:text-text-muted"
            />
          </div>
        </div>

        <div class="flex flex-col gap-1">
          <label class="text-xs font-semibold tracking-widest uppercase text-text-secondary">Senha</label>
          <div class="flex items-center gap-2 px-3 py-3 rounded-lg border border-bg-border bg-bg-surface">
            <Lock :size="16" class="text-text-secondary" />
            <input 
              v-model="senha"
              type="password" 
              placeholder="••••••••"
              class="bg-transparent outline-none w-full text-sm text-text-primary placeholder:text-text-muted"
            />
          </div>
        </div>

        <div class="text-right">
          <a href="#" class="text-sm text-accent">Esqueci a senha</a>
        </div>

        <p v-if="erro" class="text-sm text-danger text-center">{{ erro }}</p>
        <button 
          @click="login"
          class="w-full py-3 rounded-lg font-semibold text-text-primary bg-brand hover:opacity-90 transition-opacity">
          Entrar
        </button>

      </div>
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Leaf, Mail, Lock, ShieldCheck, CheckCircle } from 'lucide-vue-next'
import api from '../services/api'

const email = ref('')
const senha = ref('')
const erro = ref('')
const router = useRouter()

const login = async () => {
  try {
    const response = await api.post('/auth/login', {
      email: email.value,
      senha: senha.value,
    })
    localStorage.setItem('token', response.data.token)
    localStorage.setItem('nome', response.data.nome)
    localStorage.setItem('perfil', response.data.perfil)
    router.push('/dashboard')
  } catch {
    erro.value = 'Email ou senha inválidos.'
  }
}
</script>