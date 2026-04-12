<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-white">Empresas</h1>
      <button @click="abrirModal" class="bg-teal-600 hover:bg-teal-700 text-white px-4 py-2 rounded-lg flex items-center gap-2">
        + Nova Empresa
      </button>
    </div>

    <div class="bg-gray-800 rounded-xl overflow-hidden">
      <table class="w-full text-sm text-left text-gray-300">
        <thead class="text-xs uppercase bg-gray-700 text-gray-400">
          <tr>
            <th class="px-6 py-3">Razão Social</th>
            <th class="px-6 py-3">CNPJ</th>
            <th class="px-6 py-3">Tipo</th>
            <th class="px-6 py-3">Email</th>
            <th class="px-6 py-3">Status</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="empresas.length === 0">
            <td colspan="5" class="px-6 py-4 text-center text-gray-500">Nenhuma empresa cadastrada</td>
          </tr>
          <tr v-for="empresa in empresas" :key="empresa.id" class="border-b border-gray-700 hover:bg-gray-700">
            <td class="px-6 py-4">{{ empresa.razaoSocial }}</td>
            <td class="px-6 py-4">{{ empresa.cnpj }}</td>
            <td class="px-6 py-4">
              <span :class="tipoBadge(empresa.tipo)" class="px-2 py-1 rounded-full text-xs font-medium">{{ empresa.tipo }}</span>
            </td>
            <td class="px-6 py-4">{{ empresa.email }}</td>
            <td class="px-6 py-4">
              <span :class="empresa.ativa ? 'text-green-400' : 'text-red-400'">{{ empresa.ativa ? 'Ativa' : 'Inativa' }}</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="modalAberto" class="fixed inset-0 bg-black bg-opacity-60 flex items-center justify-center z-50">
      <div class="bg-gray-800 rounded-xl p-6 w-full max-w-lg">
        <h2 class="text-xl font-bold text-white mb-4">Nova Empresa</h2>
        <div class="space-y-4">
          <div>
            <label class="text-gray-400 text-sm">CNPJ</label>
            <div class="flex gap-2">
              <input v-model="form.cnpj" placeholder="00.000.000/0000-00" class="flex-1 bg-gray-700 text-white rounded-lg px-3 py-2 text-sm" />
              <button @click="consultarCnpj" class="bg-teal-600 hover:bg-teal-700 text-white px-3 py-2 rounded-lg text-sm">Buscar</button>
            </div>
          </div>
          <div>
            <label class="text-gray-400 text-sm">Razão Social</label>
            <input v-model="form.razaoSocial" class="w-full bg-gray-700 text-white rounded-lg px-3 py-2 text-sm" />
          </div>
          <div>
            <label class="text-gray-400 text-sm">Tipo</label>
            <select v-model="form.tipo" class="w-full bg-gray-700 text-white rounded-lg px-3 py-2 text-sm">
              <option value="GERADORA">Geradora</option>
              <option value="TRANSPORTADORA">Transportadora</option>
              <option value="RECEPTORA">Receptora</option>
            </select>
          </div>
          <div>
            <label class="text-gray-400 text-sm">Email</label>
            <input v-model="form.email" class="w-full bg-gray-700 text-white rounded-lg px-3 py-2 text-sm" />
          </div>
          <div>
            <label class="text-gray-400 text-sm">Telefone</label>
            <input v-model="form.telefone" class="w-full bg-gray-700 text-white rounded-lg px-3 py-2 text-sm" />
          </div>
          <div>
            <label class="text-gray-400 text-sm">Endereço</label>
            <input v-model="form.endereco" class="w-full bg-gray-700 text-white rounded-lg px-3 py-2 text-sm" />
          </div>
        </div>
        <div v-if="erro" class="mt-3 text-red-400 text-sm">{{ erro }}</div>
        <div class="flex justify-end gap-3 mt-6">
          <button @click="fecharModal" class="bg-gray-600 hover:bg-gray-500 text-white px-4 py-2 rounded-lg text-sm">Cancelar</button>
          <button @click="salvarEmpresa" class="bg-teal-600 hover:bg-teal-700 text-white px-4 py-2 rounded-lg text-sm">Salvar</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../services/api'

const empresas = ref([])
const modalAberto = ref(false)
const erro = ref('')

const form = ref({
  cnpj: '',
  razaoSocial: '',
  tipo: 'GERADORA',
  email: '',
  telefone: '',
  endereco: ''
})

const tipoBadge = (tipo) => {
  const classes = {
    GERADORA: 'bg-blue-900 text-blue-300',
    TRANSPORTADORA: 'bg-yellow-900 text-yellow-300',
    RECEPTORA: 'bg-green-900 text-green-300'
  }
  return classes[tipo] || 'bg-gray-700 text-gray-300'
}

const carregarEmpresas = async () => {
  try {
    const response = await api.get('/empresas')
    empresas.value = response.data
  } catch (e) {
    console.error('Erro ao carregar empresas:', e)
  }
}

const consultarCnpj = async () => {
  try {
    const cnpj = form.value.cnpj.replace(/\D/g, '')
    const response = await api.get(`/empresas/cnpj/${cnpj}`)
    form.value.razaoSocial = response.data.razao_social || ''
    form.value.endereco = `${response.data.logradouro || ''}, ${response.data.municipio || ''}`
  } catch (e) {
    erro.value = 'CNPJ não encontrado'
  }
}

const abrirModal = () => {
  modalAberto.value = true
  erro.value = ''
  form.value = { cnpj: '', razaoSocial: '', tipo: 'GERADORA', email: '', telefone: '', endereco: '' }
}

const fecharModal = () => {
  modalAberto.value = false
}

const salvarEmpresa = async () => {
  try {
    await api.post('/empresas', form.value)
    fecharModal()
    carregarEmpresas()
  } catch (e) {
    erro.value = 'Erro ao salvar empresa. Verifique os dados.'
  }
}

onMounted(carregarEmpresas)
</script>