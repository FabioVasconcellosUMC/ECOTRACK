<template>
  <div class="flex flex-col gap-6">

    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-text-primary">Empresas</h1>
        <p class="text-sm text-text-secondary mt-1">Gerencie as empresas cadastradas no sistema</p>
      </div>
      <button @click="abrirModal"
        class="flex items-center gap-2 px-4 py-2 rounded-lg bg-brand text-text-primary text-sm font-semibold hover:opacity-90 transition-opacity">
        <Plus :size="16" />
        Nova Empresa
      </button>
    </div>

    <div class="rounded-xl border border-bg-border bg-bg-surface overflow-hidden">
      <table class="w-full">
        <thead>
          <tr class="border-b border-bg-border">
            <th class="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-text-secondary">Nome</th>
            <th class="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-text-secondary">CNPJ</th>
            <th class="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-text-secondary">Tipo</th>
            <th class="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-text-secondary">Status</th>
            <th class="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-text-secondary">Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="carregando">
            <td colspan="5" class="text-center py-8 text-text-secondary">Carregando...</td>
          </tr>
          <tr v-else-if="empresas.length === 0">
            <td colspan="5" class="text-center py-8 text-text-secondary">Nenhuma empresa cadastrada</td>
          </tr>
          <tr v-for="empresa in empresas" :key="empresa.id"
            class="border-b border-bg-border hover:bg-bg-border transition-colors">
            <td class="px-4 py-3 text-sm text-text-primary">{{ empresa.razaoSocial }}</td>
            <td class="px-4 py-3 text-sm font-mono text-text-secondary">{{ empresa.cnpj }}</td>
            <td class="px-4 py-3">
              <span class="px-2 py-1 rounded-full text-xs font-semibold border"
                :class="tipoBadge(empresa.tipo)">
                {{ empresa.tipo }}
              </span>
            </td>
            <td class="px-4 py-3">
              <span class="flex items-center gap-1.5 text-xs font-semibold"
                :class="empresa.ativa ? 'text-accent' : 'text-danger'">
                <span class="w-1.5 h-1.5 rounded-full"
                  :class="empresa.ativa ? 'bg-accent' : 'bg-danger'"></span>
                {{ empresa.ativa ? 'Ativa' : 'Inativa' }}
              </span>
            </td>
            <td class="px-4 py-3">
              <div class="flex items-center gap-2">
                <button @click="verEmpresa(empresa)" class="p-1.5 rounded-lg hover:bg-bg-border transition-colors">
                  <Eye :size="15" class="text-text-secondary" />
                </button>
                <button @click="editarEmpresa(empresa)" class="p-1.5 rounded-lg hover:bg-bg-border transition-colors">
                  <Pencil :size="15" class="text-text-secondary" />
                </button>
                <button class="p-1.5 rounded-lg hover:bg-bg-border transition-colors">
                  <Trash2 :size="15" class="text-danger" />
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="modalAberto" class="fixed inset-0 z-50 flex items-center justify-center bg-black/60">
      <div class="w-full max-w-lg rounded-xl border border-bg-border bg-bg-surface p-6 flex flex-col gap-5">
        
        <div class="flex items-center justify-between">
          <h2 class="text-lg font-bold text-text-primary">Nova Empresa</h2>
          <button @click="fecharModal">
            <X :size="20" class="text-text-secondary hover:text-text-primary" />
          </button>
        </div>

        <div class="flex flex-col gap-4">
          <div class="flex flex-col gap-1">
            <label class="text-xs font-semibold uppercase tracking-wider text-text-secondary">Razão Social</label>
            <input v-model="form.razaoSocial" type="text" placeholder="Nome da empresa"
              class="px-3 py-2.5 rounded-lg border border-bg-border bg-bg-primary text-sm text-text-primary outline-none focus:border-brand" />
          </div>

          <div class="flex flex-col gap-1">
            <label class="text-xs font-semibold uppercase tracking-wider text-text-secondary">CNPJ</label>
            <input v-model="form.cnpj" type="text" placeholder="00.000.000/0000-00"
              class="px-3 py-2.5 rounded-lg border border-bg-border bg-bg-primary text-sm text-text-primary outline-none focus:border-brand" />
          </div>

          <div class="flex flex-col gap-1">
            <label class="text-xs font-semibold uppercase tracking-wider text-text-secondary">Tipo</label>
            <select v-model="form.tipo"
              class="px-3 py-2.5 rounded-lg border border-bg-border bg-bg-primary text-sm text-text-primary outline-none focus:border-brand">
              <option value="">Selecione o tipo</option>
              <option value="GERADORA">Geradora</option>
              <option value="TRANSPORTADORA">Transportadora</option>
              <option value="RECEPTORA">Receptora</option>
            </select>
          </div>

          <div class="flex flex-col gap-1">
            <label class="text-xs font-semibold uppercase tracking-wider text-text-secondary">Email</label>
            <input v-model="form.email" type="email" placeholder="contato@empresa.com"
              class="px-3 py-2.5 rounded-lg border border-bg-border bg-bg-primary text-sm text-text-primary outline-none focus:border-brand" />
          </div>

          <div class="flex flex-col gap-1">
            <label class="text-xs font-semibold uppercase tracking-wider text-text-secondary">Telefone</label>
            <input v-model="form.telefone" type="text" placeholder="(11) 99999-9999"
              class="px-3 py-2.5 rounded-lg border border-bg-border bg-bg-primary text-sm text-text-primary outline-none focus:border-brand" />
          </div>

          <div class="flex flex-col gap-1">
            <label class="text-xs font-semibold uppercase tracking-wider text-text-secondary">Endereço</label>
            <input v-model="form.endereco" type="text" placeholder="Rua, número - Cidade/UF"
              class="px-3 py-2.5 rounded-lg border border-bg-border bg-bg-primary text-sm text-text-primary outline-none focus:border-brand" />
          </div>
        </div>

        <div v-if="erro" class="text-sm text-danger text-center">{{ erro }}</div>

        <div class="flex gap-3 justify-end">
          <button @click="fecharModal"
            class="px-4 py-2 rounded-lg border border-bg-border text-sm text-text-secondary hover:bg-bg-border transition-colors">
            Cancelar
          </button>
          <button @click="salvarEmpresa"
            class="px-4 py-2 rounded-lg bg-brand text-text-primary text-sm font-semibold hover:opacity-90 transition-opacity">
            Salvar
          </button>
        </div>

      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus, Eye, Pencil, Trash2, X } from 'lucide-vue-next'
import api from '../services/api'

const empresas = ref([])
const carregando = ref(false)
const modalAberto = ref(false)
const erro = ref('')

const form = ref({
  razaoSocial: '',
  cnpj: '',
  tipo: '',
  email: '',
  telefone: '',
  endereco: '',
})

const tipoBadge = (tipo) => {
  const map = {
    GERADORA: 'text-accent border-accent/30 bg-accent/10',
    TRANSPORTADORA: 'text-blue-400 border-blue-400/30 bg-blue-400/10',
    RECEPTORA: 'text-yellow-400 border-yellow-400/30 bg-yellow-400/10',
  }
  return map[tipo] || 'text-text-secondary border-bg-border'
}

const carregarEmpresas = async () => {
  carregando.value = true
  try {
    const response = await api.get('/empresas')
    empresas.value = response.data
  } catch {
    erro.value = 'Erro ao carregar empresas.'
  } finally {
    carregando.value = false
  }
}

const abrirModal = () => {
  modalAberto.value = true
  erro.value = ''
}

const fecharModal = () => {
  modalAberto.value = false
  form.value = { razaoSocial: '', cnpj: '', tipo: '', email: '', telefone: '', endereco: '' }
  erro.value = ''
}

const salvarEmpresa = async () => {
  try {
    await api.post('/empresas', form.value)
    await carregarEmpresas()
    fecharModal()
  } catch {
    erro.value = 'Erro ao salvar empresa.'
  }
}

const verEmpresa = (empresa) => {
  console.log('Ver empresa:', empresa)
}

const editarEmpresa = (empresa) => {
  console.log('Editar empresa:', empresa)
}

onMounted(carregarEmpresas)
</script>