<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-white">Lotes</h1>
      <button @click="abrirModal" class="bg-teal-600 hover:bg-teal-700 text-white px-4 py-2 rounded-lg flex items-center gap-2">
        + Novo Lote
      </button>
    </div>

    <!-- Filtros -->
    <div class="flex gap-2 mb-4 flex-wrap">
      <button v-for="filtro in filtros" :key="filtro.valor" @click="filtroAtivo = filtro.valor"
        class="px-3 py-1 rounded-full text-xs font-medium border transition-colors"
        :class="filtroAtivo === filtro.valor ? 'bg-teal-600 text-white border-teal-600' : 'text-gray-400 border-gray-600 hover:border-teal-500'">
        {{ filtro.label }}
      </button>
    </div>

    <!-- Tabela -->
    <div class="bg-gray-800 rounded-xl overflow-hidden">
      <table class="w-full text-sm text-left text-gray-300">
        <thead class="text-xs uppercase bg-gray-700 text-gray-400">
          <tr>
            <th class="px-6 py-3">ID</th>
            <th class="px-6 py-3">Descrição</th>
            <th class="px-6 py-3">Tipo Resíduo</th>
            <th class="px-6 py-3">Quantidade</th>
            <th class="px-6 py-3">Status</th>
            <th class="px-6 py-3">Data</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="carregando">
            <td colspan="6" class="px-6 py-4 text-center text-gray-500">Carregando...</td>
          </tr>
          <tr v-else-if="lotesFiltrados.length === 0">
            <td colspan="6" class="px-6 py-4 text-center text-gray-500">Nenhum lote encontrado</td>
          </tr>
          <tr v-for="lote in lotesFiltrados" :key="lote.id" class="border-b border-gray-700 hover:bg-gray-700">
            <td class="px-6 py-4 font-mono text-teal-400">#{{ lote.id }}</td>
            <td class="px-6 py-4">{{ lote.descricao }}</td>
            <td class="px-6 py-4">
              <span class="px-2 py-1 rounded-full text-xs font-medium bg-blue-900 text-blue-300">{{ lote.tipoResiduo }}</span>
            </td>
            <td class="px-6 py-4">{{ lote.quantidade }} {{ lote.unidade }}</td>
            <td class="px-6 py-4">
              <span class="px-2 py-1 rounded-full text-xs font-medium" :class="statusBadge(lote.status)">{{ lote.status }}</span>
            </td>
            <td class="px-6 py-4 text-gray-400">{{ new Date(lote.criadoEm).toLocaleDateString('pt-BR') }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal -->
    <div v-if="modalAberto" class="fixed inset-0 bg-black bg-opacity-60 flex items-center justify-center z-50">
      <div class="bg-gray-800 rounded-xl p-6 w-full max-w-lg">
        <h2 class="text-xl font-bold text-white mb-4">Novo Lote</h2>
        <div class="space-y-4">
          <div>
            <label class="text-gray-400 text-sm">Descrição</label>
            <input v-model="form.descricao" class="w-full bg-gray-700 text-white rounded-lg px-3 py-2 text-sm" />
          </div>
          <div>
            <label class="text-gray-400 text-sm">Tipo de Resíduo</label>
            <input v-model="form.tipoResiduo" class="w-full bg-gray-700 text-white rounded-lg px-3 py-2 text-sm" placeholder="Ex: Químico, Eletrônico, Orgânico" />
          </div>
          <div class="flex gap-3">
            <div class="flex-1">
              <label class="text-gray-400 text-sm">Quantidade</label>
              <input v-model="form.quantidade" type="number" class="w-full bg-gray-700 text-white rounded-lg px-3 py-2 text-sm" />
            </div>
            <div class="flex-1">
              <label class="text-gray-400 text-sm">Unidade</label>
              <select v-model="form.unidade" class="w-full bg-gray-700 text-white rounded-lg px-3 py-2 text-sm">
                <option value="KG">KG</option>
                <option value="TON">TON</option>
                <option value="L">L</option>
              </select>
            </div>
          </div>
          <div>
            <label class="text-gray-400 text-sm">Empresa Geradora (ID)</label>
            <input v-model="form.empresaGeradoraId" type="number" class="w-full bg-gray-700 text-white rounded-lg px-3 py-2 text-sm" />
          </div>
        </div>
        <div v-if="erro" class="mt-3 text-red-400 text-sm">{{ erro }}</div>
        <div class="flex justify-end gap-3 mt-6">
          <button @click="fecharModal" class="bg-gray-600 hover:bg-gray-500 text-white px-4 py-2 rounded-lg text-sm">Cancelar</button>
          <button @click="salvarLote" class="bg-teal-600 hover:bg-teal-700 text-white px-4 py-2 rounded-lg text-sm">Salvar</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
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
  descricao: '',
  tipoResiduo: '',
  quantidade: '',
  unidade: 'KG',
  empresaGeradoraId: ''
})

const lotesFiltrados = computed(() => {
  if (filtroAtivo.value === 'TODOS') return lotes.value
  return lotes.value.filter(l => l.status === filtroAtivo.value)
})

const statusBadge = (status) => {
  const map = {
    AGUARDANDO_COLETA: 'bg-yellow-900 text-yellow-300',
    EM_TRANSITO: 'bg-blue-900 text-blue-300',
    DESCARTADO: 'bg-green-900 text-green-300',
    CANCELADO: 'bg-red-900 text-red-300',
  }
  return map[status] || 'bg-gray-700 text-gray-300'
}

const carregarLotes = async () => {
  carregando.value = true
  try {
    const response = await api.get('/lotes')
    lotes.value = response.data
  } catch (e) {
    console.error('Erro ao carregar lotes:', e)
  } finally {
    carregando.value = false
  }
}

const abrirModal = () => {
  modalAberto.value = true
  erro.value = ''
  form.value = { descricao: '', tipoResiduo: '', quantidade: '', unidade: 'KG', empresaGeradoraId: '' }
}

const fecharModal = () => {
  modalAberto.value = false
}

const salvarLote = async () => {
  try {
    await api.post('/lotes', {
      descricao: form.value.descricao,
      tipoResiduo: form.value.tipoResiduo,
      quantidade: form.value.quantidade,
      unidade: form.value.unidade,
      empresaGeradora: { id: form.value.empresaGeradoraId }
    })
    fecharModal()
    carregarLotes()
  } catch (e) {
    erro.value = 'Erro ao salvar lote. Verifique os dados.'
  }
}

onMounted(carregarLotes)
</script>