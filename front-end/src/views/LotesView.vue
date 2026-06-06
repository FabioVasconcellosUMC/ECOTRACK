<template>
  <div class="flex flex-col gap-6 pb-6 max-w-[1600px] mx-auto fade-up">

    <header class="flex items-end justify-between flex-wrap gap-4">
      <div>
        <p class="eyebrow-italic text-cyan mb-2">Operação · Resíduos</p>
        <h1 class="display-title text-[48px] leading-[0.98]">Lotes de resíduos</h1>
        <p class="text-ink-3 text-[13px] mt-2 mono-tag">
          {{ lotes.length }} lotes · {{ contagens.AGUARDANDO_COLETA }} aguardando ·
          {{ contagens.EM_TRANSITO }} em trânsito · {{ contagens.DESCARTADO }} descartados
        </p>
      </div>

      <div class="flex items-center gap-2">
        <div class="flex items-center bg-bg-elevated border border-bg-line rounded-md p-0.5">
          <button
            v-for="opcao in OPCOES_VISTA"
            :key="opcao.valor"
            @click="vistaAtiva = opcao.valor"
            class="flex items-center gap-1.5 px-3 h-9 rounded text-[11px] font-bold tracking-wider transition-colors"
            :class="vistaAtiva === opcao.valor
              ? 'bg-cyan/10 text-cyan'
              : 'text-ink-2 hover:text-ink'"
          >
            <component :is="opcao.icone" :size="13" />
            {{ opcao.rotulo }}
          </button>
        </div>

        <button
          @click="exportarTodos"
          class="flex items-center gap-2 h-10 px-4 rounded-md bg-bg-elevated border border-bg-line text-ink-2 text-[12.5px] font-bold tracking-[0.08em] hover:border-cyan/40 hover:text-cyan transition-colors"
        >
          <Download :size="15" /> EXPORTAR
        </button>

        <button
          @click="abrirModalCadastro"
          class="flex items-center gap-2 h-10 px-4 rounded-md bg-cyan text-bg-base text-[12.5px] font-bold tracking-[0.08em] hover:bg-cyan/90 transition-colors"
        >
          <Plus :size="16" /> NOVO LOTE
        </button>
      </div>
    </header>

    <section class="rounded-2xl bg-bg-panel border border-bg-line-strong p-4 helmet-stripe">
      <div class="relative max-w-xl">
        <Search :size="18" class="absolute left-4 top-1/2 -translate-y-1/2 text-ink-3" />
        <input
          v-model="termoBusca"
          type="search"
          class="w-full h-12 rounded-md bg-bg-base border border-bg-line pl-11 pr-4 text-[14px] text-ink placeholder:text-ink-4 focus:outline-none focus:border-cyan/60"
          placeholder="Buscar por descrição ou tipo de resíduo..."
        >
      </div>
    </section>

    <section v-if="vistaAtiva === 'kanban'" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <div
        v-for="coluna in COLUNAS_STATUS"
        :key="coluna.status"
        class="rounded-2xl bg-bg-panel border border-bg-line-strong helmet-stripe overflow-hidden flex flex-col"
      >
        <header class="px-4 py-3 border-b border-bg-line flex items-center justify-between">
          <div class="flex items-center gap-2">
            <span class="w-2 h-2 rounded-full" :style="{ backgroundColor: coluna.cor }" />
            <p class="eyebrow text-[10px]" :style="{ color: coluna.cor }">{{ coluna.label }}</p>
          </div>
          <span class="mono-tag text-ink-3 text-[11px]">
            {{ contagens[coluna.status] || 0 }}
          </span>
        </header>

        <div class="p-3 flex-1 space-y-2.5 min-h-[200px]">
          <div
            v-if="lotesPorStatus(coluna.status).length === 0"
            class="text-center py-8 text-ink-4 text-[11px] mono-tag"
          >
            sem lotes
          </div>

          <article
            v-for="lote in lotesPorStatus(coluna.status)"
            :key="chavePublica(lote)"
            @click="abrirDetalhes(lote)"
            class="rounded-xl bg-bg-elevated border border-bg-line p-3.5 hover:border-cyan/40 transition-colors cursor-pointer"
          >
            <div class="flex items-start justify-between mb-3">
              <span class="mono-tag text-cyan text-[12px]">#{{ formatarIdentificador(lote) }}</span>
              <span class="mono-tag text-ink-3 text-[10px]">{{ formatarData(lote.criadoEm) }}</span>
            </div>

            <p class="text-[13px] text-ink font-semibold leading-snug line-clamp-2 mb-1">
              {{ lote.descricao || 'Sem descrição' }}
            </p>
            <p class="eyebrow text-[9.5px] mb-3">{{ lote.tipoResiduo || 'Tipo não informado' }}</p>

            <div class="flex items-baseline gap-1.5 pt-3 border-t border-bg-line">
              <span class="scoreboard text-[30px] text-ink leading-none tabular-nums">{{ lote.quantidade }}</span>
              <span class="font-display text-cyan text-[15px] unidade-italic">{{ lote.unidade }}</span>
            </div>
          </article>
        </div>
      </div>
    </section>

    <section
      v-else
      class="rounded-2xl bg-bg-panel border border-bg-line-strong overflow-hidden helmet-stripe fade-up"
    >
      <table class="w-full text-left">
        <thead class="bg-bg-elevated border-b border-bg-line">
          <tr>
            <th class="px-5 py-3 eyebrow">#</th>
            <th class="px-5 py-3 eyebrow">ID</th>
            <th class="px-5 py-3 eyebrow">Descrição</th>
            <th class="px-5 py-3 eyebrow">Resíduo</th>
            <th class="px-5 py-3 eyebrow text-right">Qtd</th>
            <th class="px-5 py-3 eyebrow">Status</th>
            <th class="px-5 py-3 eyebrow">Data</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="lotes.length === 0">
            <td colspan="7" class="px-6 py-16 text-center">
              <Package :size="28" class="mx-auto text-ink-4 mb-2" />
              <p class="mono-tag text-ink-3">Nenhum lote registrado</p>
            </td>
          </tr>
          <tr
            v-for="(lote, indice) in lotes"
            :key="chavePublica(lote)"
            @click="abrirDetalhes(lote)"
            class="border-b border-bg-line hover:bg-bg-elevated transition-colors cursor-pointer"
          >
            <td class="px-5 py-3.5 mono-tag text-ink-4 text-[11px]">{{ formatarIndice(indice) }}</td>
            <td class="px-5 py-3.5 mono-tag text-cyan text-[12px]">#{{ formatarIdentificador(lote) }}</td>
            <td class="px-5 py-3.5 text-[13px] text-ink">{{ lote.descricao || '—' }}</td>
            <td class="px-5 py-3.5">
              <span class="px-2 py-0.5 rounded-full text-[10.5px] font-bold tracking-wider bg-info-soft border border-info/30 text-info">
                {{ lote.tipoResiduo || '—' }}
              </span>
            </td>
            <td class="px-5 py-3.5 text-right">
              <span class="scoreboard text-[20px] text-ink tabular-nums">{{ lote.quantidade }}</span>
              <span class="mono-tag text-ink-3 ml-1">{{ lote.unidade }}</span>
            </td>
            <td class="px-5 py-3.5">
              <span
                class="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-[10.5px] font-bold tracking-wider border"
                :style="estiloChipStatus(lote.status)"
              >
                <span class="w-1.5 h-1.5 rounded-full" :style="{ backgroundColor: corStatus(lote.status) }" />
                {{ rotuloStatus(lote.status) }}
              </span>
            </td>
            <td class="px-5 py-3.5 mono-tag text-ink-3 text-[11px]">{{ formatarData(lote.criadoEm) }}</td>
          </tr>
        </tbody>
      </table>
    </section>

    <div
      v-if="temMaisLotes || carregandoMais"
      class="flex items-center justify-center"
    >
      <button
        @click="carregarMaisLotes"
        :disabled="carregandoMais"
        class="flex items-center gap-2 h-10 px-4 rounded-md bg-bg-elevated border border-bg-line text-ink-2 text-[11.5px] font-bold tracking-wider hover:border-cyan/40 hover:text-cyan transition-colors disabled:opacity-50"
      >
        <Loader2 v-if="carregandoMais" :size="14" class="animate-spin" />
        <Plus v-else :size="14" />
        {{ carregandoMais ? 'CARREGANDO...' : 'CARREGAR MAIS LOTES' }}
      </button>
    </div>

    <Transition name="page">
      <div
        v-if="loteSelecionado"
        class="fixed inset-0 z-50 bg-bg-base/80 backdrop-blur-sm flex items-center justify-center p-6"
        @click.self="fecharDetalhes"
      >
        <div class="w-full max-w-2xl rounded-2xl bg-bg-panel border border-bg-line-strong p-7 helmet-stripe fade-up">
          <div class="flex items-start justify-between gap-5">
            <div class="flex-1 min-w-0">
              <p class="eyebrow text-cyan">Detalhe do lote</p>
              <h2 class="section-title text-[36px] mt-1.5">#{{ formatarIdentificador(loteSelecionado) }}</h2>
              <p class="text-[13px] text-ink-2 mt-2">{{ loteSelecionado.descricao || 'Sem descrição' }}</p>
            </div>

            <button
              @click="fecharDetalhes"
              class="shrink-0 w-9 h-9 rounded-md flex items-center justify-center text-ink-3 hover:text-ink hover:bg-bg-elevated transition-colors"
            >
              <X :size="18" />
            </button>
          </div>

          <div class="grid grid-cols-2 gap-4 mt-7">
            <div class="rounded-xl bg-bg-elevated border border-bg-line p-4">
              <p class="eyebrow">Tipo de resíduo</p>
              <p class="text-[13px] text-ink mt-2">{{ loteSelecionado.tipoResiduo || '—' }}</p>
            </div>
            <div class="rounded-xl bg-bg-elevated border border-bg-line p-4">
              <p class="eyebrow">Status</p>
              <span
                class="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-[10.5px] font-bold tracking-wider border mt-2"
                :style="estiloChipStatus(loteSelecionado.status)"
              >
                <span class="w-1.5 h-1.5 rounded-full" :style="{ backgroundColor: corStatus(loteSelecionado.status) }" />
                {{ rotuloStatus(loteSelecionado.status) }}
              </span>
            </div>
            <div class="rounded-xl bg-bg-elevated border border-bg-line p-4">
              <p class="eyebrow">Quantidade</p>
              <p class="mt-2 flex items-baseline gap-1.5">
                <span class="scoreboard text-[28px] text-ink leading-none tabular-nums">
                  {{ loteSelecionado.quantidade }}
                </span>
                <span class="font-display text-cyan text-[14px] unidade-italic">{{ loteSelecionado.unidade }}</span>
              </p>
            </div>
            <div class="rounded-xl bg-bg-elevated border border-bg-line p-4">
              <p class="eyebrow">Criado em</p>
              <p class="mono-tag text-[13px] text-ink mt-2">{{ formatarData(loteSelecionado.criadoEm) }}</p>
            </div>
          </div>

          <div class="mt-7 pt-5 border-t border-bg-line flex items-center justify-between gap-4">
            <p class="mono-tag text-ink-3 text-[11px]">
              Identificador publico · #{{ formatarIdentificador(loteSelecionado) }}
            </p>
            <button
              @click="exportarSelecionado"
              class="shrink-0 flex items-center gap-2 h-9 px-3 rounded-md bg-bg-elevated border border-bg-line text-ink-2 text-[11px] font-bold tracking-wider hover:border-cyan/40 hover:text-cyan transition-colors"
            >
              <Download :size="14" /> EXPORTAR LOTE
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <Transition name="page">
      <div
        v-if="modalCadastroAberto"
        class="fixed inset-0 z-50 bg-bg-base/80 backdrop-blur-sm flex items-center justify-center p-6"
        @click.self="fecharModalCadastro"
      >
        <div class="w-full max-w-xl rounded-2xl bg-bg-panel border border-bg-line-strong p-7 helmet-stripe fade-up">
          <div class="flex items-center justify-between mb-6">
            <div>
              <p class="eyebrow text-cyan">Novo registro</p>
              <h2 class="section-title text-[28px] mt-2">Cadastrar lote</h2>
            </div>
            <button
              @click="fecharModalCadastro"
              class="w-9 h-9 rounded-md flex items-center justify-center text-ink-3 hover:text-ink hover:bg-bg-elevated transition-colors"
            >
              <X :size="18" />
            </button>
          </div>

          <div class="space-y-4">
            <FormField
              v-model="formulario.descricao"
              label="Descrição *"
              placeholder="Descrição resumida do lote"
              :erro="erros.descricao"
              @update:modelValue="limparErro('descricao')"
              @blur="validarCampo('descricao')"
            />
            <FormField
              v-model="formulario.tipoResiduo"
              label="Tipo resíduo *"
              placeholder="Ex: Eletrônico, Plástico, Orgânico"
              :erro="erros.tipoResiduo"
              @update:modelValue="limparErro('tipoResiduo')"
              @blur="validarCampo('tipoResiduo')"
            />

            <div class="grid grid-cols-2 gap-3">
              <FormField
                v-model="formulario.quantidade"
                label="Quantidade *"
                placeholder="0"
                type="number"
                :erro="erros.quantidade"
                @update:modelValue="limparErro('quantidade')"
                @blur="validarCampo('quantidade')"
              />

              <div>
                <label class="eyebrow block mb-1.5">Unidade</label>
                <div class="grid grid-cols-3 gap-1.5">
                  <button
                    v-for="unidade in UNIDADES"
                    :key="unidade"
                    @click="formulario.unidade = unidade"
                    class="px-2 py-2.5 rounded-md text-[11px] font-bold tracking-wider border transition-colors"
                    :class="formulario.unidade === unidade
                      ? 'border-cyan/40 text-cyan bg-cyan/10'
                      : 'bg-bg-elevated border-bg-line text-ink-2 hover:border-bg-line-strong'"
                  >
                    {{ unidade }}
                  </button>
                </div>
              </div>
            </div>

            <div>
              <label class="eyebrow block mb-1.5">Empresa geradora *</label>
              <div
                class="flex items-center gap-2 px-3 h-10 rounded-md bg-bg-base border border-bg-line focus-within:border-cyan/40 transition-colors"
                :class="{ 'border-danger/60': erros.empresaGeradoraId }"
              >
                <Building2 :size="14" class="text-ink-3" />
                <select
                  v-model="formulario.empresaGeradoraId"
                  @change="limparErro('empresaGeradoraId')"
                  class="flex-1 bg-transparent outline-none text-[13px] text-ink"
                >
                  <option value="">Selecione uma empresa geradora</option>
                  <option v-for="empresa in empresasGeradoras" :key="chavePublica(empresa)" :value="chavePublica(empresa)">
                    {{ empresa.razaoSocial }}
                  </option>
                </select>
              </div>
              <p v-if="erros.empresaGeradoraId" class="flex items-center gap-1.5 text-[11px] text-danger mt-1">
                <AlertCircle :size="11" /> {{ erros.empresaGeradoraId }}
              </p>
              <p v-else-if="empresasGeradoras.length === 0" class="text-[11px] text-ink-3 mt-1">
                Cadastre uma empresa do tipo GERADORA antes de criar lotes.
              </p>
            </div>
          </div>

          <p
            v-if="mensagemErro"
            class="flex items-center gap-2 text-[12px] text-danger bg-danger-soft border border-danger/30 rounded-md px-3 py-2 mt-4"
          >
            <AlertCircle :size="14" /> {{ mensagemErro }}
          </p>

          <div class="flex justify-end gap-2 mt-6 pt-5 border-t border-bg-line">
            <button
              @click="fecharModalCadastro"
              class="px-4 h-10 rounded-md text-[11.5px] font-bold tracking-wider bg-bg-elevated border border-bg-line text-ink-2 hover:border-bg-line-strong"
            >
              CANCELAR
            </button>
            <button
              @click="salvarLote"
              :disabled="salvando"
              class="flex items-center gap-2 px-5 h-10 rounded-md text-[11.5px] font-bold tracking-wider bg-cyan text-bg-base hover:bg-cyan/90 transition-colors disabled:opacity-50"
            >
              <Loader2 v-if="salvando" :size="13" class="animate-spin" />
              <Check v-else :size="13" />
              {{ salvando ? 'SALVANDO...' : 'CRIAR LOTE' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import {
  Plus, Package, X, Check, AlertCircle, Loader2, Building2,
  Columns3, Rows3, Download, Search,
} from 'lucide-vue-next'
import api from '../services/api'
import { buscarComCache, invalidarCacheDados } from '../services/dataCache'
import FormField from '../components/ui/FormField.vue'
import { exportCsv } from '../utils/exportCsv'
import { validarTamanhoMinimo } from '../composables/useValidacao'

const STATUS = {
  AGUARDANDO_COLETA: 'AGUARDANDO_COLETA',
  EM_TRANSITO:       'EM_TRANSITO',
  DESCARTADO:        'DESCARTADO',
  CANCELADO:         'CANCELADO',
}

const COLUNAS_STATUS = [
  { status: STATUS.AGUARDANDO_COLETA, label: 'Aguardando',  cor: '#F59E0B' },
  { status: STATUS.EM_TRANSITO,       label: 'Em trânsito', cor: '#38BDF8' },
  { status: STATUS.DESCARTADO,        label: 'Descartado',  cor: '#10B981' },
  { status: STATUS.CANCELADO,         label: 'Cancelado',   cor: '#DC2626' },
]

const OPCOES_VISTA = [
  { valor: 'kanban', rotulo: 'KANBAN', icone: Columns3 },
  { valor: 'tabela', rotulo: 'TABELA', icone: Rows3 },
]

const UNIDADES = ['KG', 'TON', 'L']
const COR_PADRAO = '#A5ACAF'
const TAMANHO_ID = 4
const TAMANHO_INDICE = 2
const TIPO_EMPRESA_GERADORA = 'GERADORA'
const LIMITE_LISTAGEM = 20
const ATRASO_BUSCA_MS = 350
let timeoutBusca = null

const formularioVazio = () => ({
  descricao: '',
  tipoResiduo: '',
  quantidade: '',
  unidade: 'KG',
  empresaGeradoraId: '',
})

const errosVazio = () => ({
  descricao: '',
  tipoResiduo: '',
  quantidade: '',
  empresaGeradoraId: '',
})

const lotes = ref([])
const empresas = ref([])
const paginaAtual = ref(0)
const temMaisLotes = ref(false)
const carregandoMais = ref(false)
const loteSelecionado = ref(null)
const modalCadastroAberto = ref(false)
const salvando = ref(false)
const mensagemErro = ref('')
const vistaAtiva = ref('kanban')
const formulario = ref(formularioVazio())
const erros = ref(errosVazio())
const termoBusca = ref('')

const empresasGeradoras = computed(() =>
  empresas.value.filter(empresa => empresa.tipo === TIPO_EMPRESA_GERADORA),
)

const contagens = computed(() => {
  const contagem = {
    [STATUS.AGUARDANDO_COLETA]: 0,
    [STATUS.EM_TRANSITO]: 0,
    [STATUS.DESCARTADO]: 0,
    [STATUS.CANCELADO]: 0,
  }
  lotes.value.forEach(lote => {
    if (contagem[lote.status] !== undefined) contagem[lote.status]++
  })
  return contagem
})

const lotesPorStatus = (status) => lotes.value.filter(lote => lote.status === status)

const corStatus = (status) =>
  COLUNAS_STATUS.find(coluna => coluna.status === status)?.cor || COR_PADRAO

const rotuloStatus = (status) =>
  COLUNAS_STATUS.find(coluna => coluna.status === status)?.label || status

const estiloChipStatus = (status) => {
  const cor = corStatus(status)
  return { backgroundColor: `${cor}1A`, color: cor, borderColor: `${cor}30` }
}

const formatarData = (data) =>
  data ? new Date(data).toLocaleDateString('pt-BR', { day: '2-digit', month: 'short' }) : '—'

const chavePublica = (registro) => registro?.publicId || registro?.id || ''
const formatarIdentificador = (registro) => {
  const valor = chavePublica(registro)
  if (!valor) return ''.padStart(TAMANHO_ID, '0')
  if (typeof valor === 'string' && valor.includes('-')) return valor.slice(0, 8).toUpperCase()
  return String(valor).padStart(TAMANHO_ID, '0')
}
const formatarIndice = (indice) => String(indice + 1).padStart(TAMANHO_INDICE, '0')

const limparErro = (campo) => { erros.value[campo] = '' }

const validarCampo = (campo) => {
  const valor = formulario.value[campo]

  if (campo === 'descricao') {
    erros.value.descricao = validarTamanhoMinimo(valor, 3, 'Descrição')
  }

  if (campo === 'tipoResiduo') {
    erros.value.tipoResiduo = validarTamanhoMinimo(valor, 3, 'Tipo de resíduo')
  }

  if (campo === 'quantidade') {
    const quantidade = Number(valor)
    erros.value.quantidade = quantidade > 0 ? '' : 'Quantidade deve ser maior que zero.'
  }

  if (campo === 'empresaGeradoraId') {
    erros.value.empresaGeradoraId = valor ? '' : 'Selecione uma empresa geradora.'
  }
}

const validarFormularioCompleto = () => {
  validarCampo('descricao')
  validarCampo('tipoResiduo')
  validarCampo('quantidade')
  validarCampo('empresaGeradoraId')
  return Object.values(erros.value).every(mensagem => mensagem === '')
}

const loteParaCsv = (lote) => ({
  id: chavePublica(lote),
  descricao: lote.descricao || '',
  tipoResiduo: lote.tipoResiduo || '',
  quantidade: lote.quantidade || '',
  unidade: lote.unidade || '',
  status: rotuloStatus(lote.status),
  criadoEm: formatarData(lote.criadoEm),
  empresaGeradoraId: chavePublica(lote.empresaGeradora) || lote.empresaGeradoraId || '',
})

const exportarTodos = () => {
  exportCsv('ecotrack-lotes.csv', lotes.value.map(loteParaCsv))
}

const exportarSelecionado = () => {
  if (!loteSelecionado.value) return
  exportCsv(
    `ecotrack-lote-${chavePublica(loteSelecionado.value)}.csv`,
    [loteParaCsv(loteSelecionado.value)],
  )
}

const normalizarPagina = (resposta) => {
  if (Array.isArray(resposta)) {
    return { itens: resposta, hasNext: false, page: 0 }
  }

  return {
    itens: resposta?.itens || [],
    hasNext: Boolean(resposta?.hasNext),
    page: Number(resposta?.page) || 0,
  }
}

const carregarDados = async ({ acrescentar = false } = {}) => {
  const pagina = acrescentar ? paginaAtual.value + 1 : 0
  if (acrescentar) carregandoMais.value = true

  try {
    const [respostaLotes, respostaEmpresas] = await Promise.all([
      buscarComCache('/lotes', {
        limit: LIMITE_LISTAGEM,
        page: pagina,
        q: termoBusca.value.trim() || undefined,
      }),
      buscarComCache('/empresas'),
    ])
    const paginaLotes = normalizarPagina(respostaLotes)
    lotes.value = acrescentar
      ? [...lotes.value, ...paginaLotes.itens]
      : paginaLotes.itens
    paginaAtual.value = paginaLotes.page
    temMaisLotes.value = paginaLotes.hasNext
    empresas.value = respostaEmpresas
  } catch (erro) {
    console.error('Erro ao carregar dados de lotes:', erro)
  } finally {
    carregandoMais.value = false
  }
}

const carregarMaisLotes = () => carregarDados({ acrescentar: true })

const abrirDetalhes = (lote) => { loteSelecionado.value = lote }
const fecharDetalhes = () => { loteSelecionado.value = null }

const abrirModalCadastro = () => {
  modalCadastroAberto.value = true
  mensagemErro.value = ''
  formulario.value = formularioVazio()
  erros.value = errosVazio()
}

const fecharModalCadastro = () => { modalCadastroAberto.value = false }

const salvarLote = async () => {
  if (salvando.value) return
  mensagemErro.value = ''

  if (!validarFormularioCompleto()) {
    mensagemErro.value = 'Corrija os campos destacados antes de salvar.'
    return
  }

  salvando.value = true
  try {
    await api.post('/lotes', {
      descricao:       formulario.value.descricao.trim(),
      tipoResiduo:     formulario.value.tipoResiduo.trim(),
      quantidade:      Number(formulario.value.quantidade),
      unidade:         formulario.value.unidade,
      empresaGeradora: { publicId: formulario.value.empresaGeradoraId },
    })
    invalidarCacheDados('/lotes', '/transportes')
    fecharModalCadastro()
    await carregarDados()
  } catch (erro) {
    mensagemErro.value = erro.mensagemAmigavel || 'Erro ao salvar lote. Verifique os dados.'
  } finally {
    salvando.value = false
  }
}

watch(termoBusca, () => {
  clearTimeout(timeoutBusca)
  paginaAtual.value = 0
  temMaisLotes.value = false
  timeoutBusca = setTimeout(carregarDados, ATRASO_BUSCA_MS)
})

onMounted(carregarDados)
</script>

<style scoped>
.unidade-italic {
  font-weight: 500;
  font-style: italic;
  letter-spacing: 0.04em;
}
</style>
