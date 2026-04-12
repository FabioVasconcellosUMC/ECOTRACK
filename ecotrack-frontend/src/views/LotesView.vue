<template>
  <div class="flex flex-col gap-6">

    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-text-primary">Lotes</h1>
        <p class="text-sm text-text-secondary mt-1">Gerencie os lotes de resíduos sólidos</p>
      </div>
      <button @click="abrirModal"
        class="flex items-center gap-2 px-4 py-2 rounded-lg bg-brand text-text-primary text-sm font-semibold hover:opacity-90 transition-opacity">
        <Plus :size="16" />
        Novo Lote
      </button>
    </div>

    <div class="flex items-center gap-3 flex-wrap">
      <button v-for="filtro in filtros" :key="filtro.valor" @click="filtroAtivo = filtro.valor"
        class="px-4 py-1.5 rounded-full text-xs font-semibold border transition-colors"
        :class="filtroAtivo === filtro.valor
          ? 'bg-brand text-text-primary border-brand'
          : 'text-text-secondary border-bg-border hover:border-accent hover:text-accent'">
        {{ filtro.label }}
      </button>
    </div>

    <div class="rounded-xl border border-bg-border bg-bg-surface overflow-hidden">
      <table class="w-full">
        <thead>
          <tr class="border-b border-bg-border">
            <th class="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-text-secondary">Código</th>
            <th class="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-text-secondary">Tipo Resíduo</th>
            <th class="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-text-secondary">Empresa</th>
            <th class="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-text-secondary">Status</th>
            <th class="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-text-secondary">Data</th>
            <th class="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-text-secondary">Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="carregando">
            <td colspan="6" class="text-center py-8 text-text-secondary">Carregando...</td>
          </tr>
          <tr v-else-if="lotesFiltrados.length === 0">
            <td colspan="6" class="text-center py-8 text-text-secondary">Nenhum lote encontrado</td>
          </tr>
          <tr v-for="lote in lotesFiltrados" :key="lote.id"
            class="border-b border-bg-border hover:bg-bg-border transition-colors">
            <td class="px-4 py-3 text-sm font-mono text-accent">{{ lote.codigo }}</td>
            <td class="px-4 py-3">
              <span class="px-2 py-1 rounded-full text-xs font-semibold border" :class="tipoBadge(lote.tipoResiduo)">
                {{ lote.tipoResiduo }}
              </span>
            </td>
            <td class="px-4 py-3 text-sm text-text-primary">{{ lote.empresaId }}</td>
            <td class="px-4 py-3">
              <span class="flex items-center gap-1.5 text-xs font-semibold" :class="statusClasse(lote.status)">
                <span class="w-1.5 h-1.5 rounded-full" :class="statusDot(lote.status)"></span>
                {{ lote.status }}
              </span>
            </td>
            <td class="px-4 py-3 text-sm font-mono text-text-secondary">
              {{ new Date(lote.criadoEm).toLocaleDateString('pt-BR') }}
            </td>
            <td class="px-4 py-3">
              <div class="flex items-center gap-2">
                <button class="p-1.5 rounded-lg hover:bg-bg-border transition-colors">
                  <Eye :size="15" class="text-text-secondary" />
                </button>
                <button class="p-1.5 rounded-lg hover:bg-bg-border transition-colors">
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
          <h2 class="text-lg font-bold text-text-primary">Novo Lote</h2>
          <button @click="fecharModal">
            <X :size="20" class="text-text-secondary hover:text-text-primary" />
          </button>
        </div>

        <div class="flex flex-col gap-4">
          <div class="flex flex-col gap-1">
            <label class="text-xs font-semibold uppercase tracking-wider text-text-secondary">Tipo de Resíduo</label>
            <select v-model="form.tipoResiduo"
              class="px-3 py-2.5 rounded-lg border border-bg-border bg-bg-primary text-sm text-text-primary outline-none focus:border-brand">
              <option value="">Selecione o tipo</option>
              <option value="RECICLAVEL">Reciclável</option>
              <option value="ORGANICO">Orgânico</option>
              <option value="ELETRONICO">Eletrônico</option>
              <option value="PERIGOSO">Perigoso</option>
              <option value="HOSPITALAR">Hospitalar</option>
            </select>
          </div>

          <div class="flex flex-col gap-1">
            <label class="text-xs font-semibold uppercase tracking-wider text-text-secondary">Quantidade (kg)</label>
            <input v-model="form.quantidadeKg" type="number" placeholder="0"
              class="px-3 py-2.5 rounded-lg border border-bg-border bg-bg-primary text-sm text-text-primary outline-none focus:border-brand" />
          </div>

          <div class="flex flex-col gap-1">
            <label class="text-xs font-semibold uppercase tracking-wider text-text-secondary">Empresa ID</label>
            <input v-model="form.empresaId" type="number" placeholder="ID da empresa"
              class="px-3 py-2.5 rounded-lg border border-bg-border bg-bg-primary text-sm text-text-primary outline-none focus:border-brand" />
          </div>

          <div class="flex flex-col gap-1">
            <label class="text-xs font-semibold uppercase tracking-wider text-text-secondary">Descrição</label>
            <input v-model="form.descricao" type="text" placeholder="Descrição do lote"
              class="px-3 py-2.5 rounded-lg border border-bg-border bg-bg-primary text-sm text-text-primary outline-none focus:border-brand" />
          </div>
        </div>

        <div v-if="erro" class="text-sm text-danger text-center">{{ erro }}</div>

        <div class="flex gap-3 justify-end">
          <button @click="fecharModal"
            class="px-4 py-2 rounded-lg border border-bg-border text-sm text-text-secondary hover:bg-bg-border transition-colors">
            Cancelar
          </button>
          <button @click="salvarLote"
            class="px-4 py-2 rounded-lg bg-brand text-text-primary text-sm font-semibold hover:opacity-90 transition-opacity">
            Salvar
          </button>
        </div>

      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Plus, Eye, Pencil, Trash2, X } from 'lucide-vue-next'
import api from '../services/api'

const lotes = ref([])
const carregando = ref(false)
const modalAberto = ref(false)
const erro = ref('')
const filtroAtivo = ref('TODOS')

const filtros = [
  { label: 'Todos', valor: 'TODOS' },
  { label: 'Aguardando Coleta', valor: 'AGUARDANDO_COLETA' },
  { label: 'Em Trânsito', valor: 'EM_TRANSITO' },
  { label: 'Descartado', valor: 'DESCARTADO' },
  { label: 'Cancelado', valor: 'CANCELADO' },
]

const form = ref({
  tipoResiduo: '',
  quantidadeKg: '',
  empresaId: '',
  descricao: '',
})

const lotesFiltrados = computed(() => {
  if (filtroAtivo.value === 'TODOS') return lotes.value
  return lotes.value.filter(l => l.status === filtroAtivo.value)
})

const tipoBadge = (tipo) => {
  const map = {
    RECICLAVEL: 'text-accent border-accent/30 bg-accent/10',
    ORGANICO: 'text-yellow-400 border-yellow-400/30 bg-yellow-400/10',
    ELETRONICO: 'text-purple-400 border-purple-400/30 bg-purple-400/10',
    PERIGOSO: 'text-danger border-danger/30 bg-danger/10',
    HOSPITALAR: 'text-orange-400 border-orange-400/30 bg-orange-400/10',
  }
  return map[tipo] || 'text-text-secondary border-bg-border'
}

const statusClasse = (status) => {
  const map = {
    AGUARDANDO_COLETA: 'text-yellow-400',
    EM_TRANSITO: 'text-blue-400',
    DESCARTADO: 'text-accent',
    CANCELADO: 'text-danger',
  }
  return map[status] || 'text-text-secondary'
}

const statusDot = (status) => {
  const map = {
    AGUARDANDO_COLETA: 'bg-yellow-400',
    EM_TRANSITO: 'bg-blue-400',
    DESCARTADO: 'bg-accent',
    CANCELADO: 'bg-danger',
  }
  return map[status] || 'bg-text-secondary'
}

const carregarLotes = async () => {
  carregando.value = true
  try {
    const response = await api.get('/lotes')
    lotes.value = response.data
  } catch {
    erro.value = 'Erro ao carregar lotes.'
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
  form.value = { tipoResiduo: '', quantidadeKg: '', empresaId: '', descricao: '' }
  erro.value = ''
}

const salvarLote = async () => {
  try {
    await api.post('/lotes', form.value)
    await carregarLotes()
    fecharModal()
  } catch {
    erro.value = 'Erro ao salvar lote.'
  }
}

onMounted(carregarLotes)
</script>