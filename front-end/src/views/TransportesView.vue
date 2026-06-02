<template>
  <div class="flex flex-col gap-7 pb-6 max-w-[1600px] mx-auto">

    <header class="flex items-end justify-between flex-wrap gap-4 fade-up">
      <div>
        <p class="eyebrow-italic text-cyan mb-2">Operação · Rastreabilidade</p>
        <h1 class="display-title text-[48px] leading-[0.98]">
          Transportes
        </h1>
        <p class="text-ink-3 text-[13px] mt-2 mono-tag">
          {{ totalTransportes }} transporte{{ totalTransportes === 1 ? '' : 's' }} cadastrado{{ totalTransportes === 1 ? '' : 's' }} · ciclo PNRS rastreado
        </p>
      </div>

      <div class="flex items-center gap-2">
        <button
          @click="exportarLista"
          class="flex items-center gap-2 h-10 px-4 rounded-md bg-bg-elevated border border-bg-line text-ink-2
                 text-[11.5px] font-bold tracking-wider hover:border-cyan/40 hover:text-cyan transition-colors"
        >
          <Download :size="14" />
          EXPORTAR CSV
        </button>

        <button
          v-if="podeCriarTransporte"
          @click="abrirModalCadastro"
          class="flex items-center gap-2 h-10 px-4 rounded-md bg-cyan text-bg-base
                 text-[11.5px] font-bold tracking-wider hover:bg-cyan-strong transition-colors"
        >
          <Plus :size="14" />
          NOVO TRANSPORTE
        </button>
      </div>
    </header>

    <section class="grid grid-cols-12 gap-3 fade-up-1">
      <button
        v-for="chip in CHIPS"
        :key="chip.valor"
        @click="filtroAtivo = chip.valor"
        class="col-span-12 sm:col-span-6 lg:col-span-3 rounded-2xl border p-5 text-left transition-colors helmet-stripe cursor-pointer"
        :class="filtroAtivo === chip.valor
          ? 'bg-bg-panel border-cyan/40'
          : 'bg-bg-panel border-bg-line-strong hover:border-bg-line'"
      >
        <div class="flex items-center justify-between mb-3">
          <p class="eyebrow" :style="{ color: chip.cor }">{{ chip.rotulo }}</p>
          <span class="flex items-center justify-center w-8 h-8 rounded-md" :style="{ backgroundColor: `${chip.cor}1a`, border: `1px solid ${chip.cor}33`, color: chip.cor }">
            <component :is="chip.icone" :size="14" />
          </span>
        </div>
        <p class="scoreboard text-[36px] text-ink leading-none">{{ contagemPorStatus(chip.valor) }}</p>
      </button>
    </section>

    <section
      class="rounded-2xl bg-bg-panel border border-bg-line-strong helmet-stripe overflow-hidden fade-up-2"
    >
      <div class="flex items-center justify-between gap-3 px-5 py-4 border-b border-bg-line">
        <div class="flex items-center gap-2 px-3 h-9 rounded-md bg-bg-base border border-bg-line flex-1 max-w-[420px] focus-within:border-cyan/40 transition-colors">
          <Search :size="14" class="text-ink-3" />
          <input
            v-model="termoBusca"
            placeholder="Buscar por ID, lote, responsável, empresa..."
            class="flex-1 bg-transparent outline-none text-[13px] text-ink placeholder:text-ink-4"
          />
        </div>

        <p class="mono-tag text-ink-3 text-[11px]">
          {{ transportesFiltrados.length }} resultado{{ transportesFiltrados.length === 1 ? '' : 's' }}
        </p>
      </div>

      <div v-if="carregando" class="px-5 py-16 flex flex-col items-center gap-3">
        <Loader2 :size="20" class="text-cyan animate-spin" />
        <p class="mono-tag text-ink-3 text-[11px]">CARREGANDO TRANSPORTES...</p>
      </div>

      <div v-else-if="transportesFiltrados.length === 0" class="px-5 py-16 flex flex-col items-center gap-3 text-center">
        <Truck :size="28" class="text-ink-4" />
        <p class="text-ink-2 text-[13px]">Nenhum transporte encontrado</p>
        <p class="mono-tag text-ink-3 text-[11px] max-w-md">
          {{ termoBusca || filtroAtivo !== 'TODOS' ? 'Ajuste os filtros ou a busca' : 'Cadastre o primeiro transporte clicando no botão acima' }}
        </p>
      </div>

      <div v-else class="overflow-x-auto">
        <table class="w-full text-[13px]">
          <thead class="bg-bg-elevated/40 border-b border-bg-line">
            <tr>
              <th class="text-left px-5 py-3 eyebrow text-[10px]">ID</th>
              <th class="text-left px-3 py-3 eyebrow text-[10px]">Lote</th>
              <th class="text-left px-3 py-3 eyebrow text-[10px]">Transportadora</th>
              <th class="text-left px-3 py-3 eyebrow text-[10px]">Receptora</th>
              <th class="text-left px-3 py-3 eyebrow text-[10px]">Responsável</th>
              <th class="text-left px-3 py-3 eyebrow text-[10px]">Status</th>
              <th class="text-left px-5 py-3 eyebrow text-[10px]">Coleta</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="transporte in transportesFiltrados"
              :key="transporte.id"
              @click="abrirDetalhes(transporte)"
              class="border-b border-bg-line cursor-pointer hover:bg-bg-elevated/40 transition-colors"
            >
              <td class="px-5 py-3 mono-tag text-ink text-[11.5px]">{{ formatarId(transporte) }}</td>
              <td class="px-3 py-3 mono-tag text-ink-2 text-[11.5px]">{{ rotuloLote(transporte.lote) }}</td>
              <td class="px-3 py-3 text-ink-2">{{ rotuloEmpresa(transporte.transportadora) }}</td>
              <td class="px-3 py-3 text-ink-2">{{ rotuloEmpresa(transporte.receptora) }}</td>
              <td class="px-3 py-3 text-ink-2">{{ transporte.responsavel || '—' }}</td>
              <td class="px-3 py-3">
                <span
                  class="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-[11px] font-semibold border"
                  :style="estiloStatus(transporte.status)"
                >
                  <component :is="iconeStatus(transporte.status)" :size="11" />
                  {{ rotuloStatus(transporte.status) }}
                </span>
              </td>
              <td class="px-5 py-3 mono-tag text-ink-3 text-[11px]">{{ formatarData(transporte.dataColeta || transporte.criadoEm) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <Transition name="modal">
      <div
        v-if="transporteSelecionado"
        class="fixed inset-0 z-40 flex items-center justify-center px-4 py-8"
      >
        <div class="absolute inset-0 bg-bg-base/85 backdrop-blur-md" @click="fecharDetalhes" />

        <div class="relative w-full max-w-2xl max-h-[90vh] overflow-y-auto rounded-2xl bg-bg-panel border border-bg-line-strong helmet-stripe shadow-2xl">
          <header class="flex items-start justify-between gap-3 px-7 py-5 border-b border-bg-line">
            <div>
              <p class="eyebrow text-cyan">Detalhes do transporte</p>
              <h2 class="display-title text-[28px] mt-1">{{ formatarId(transporteSelecionado) }}</h2>
              <span
                class="inline-flex items-center gap-1.5 px-2 py-0.5 mt-2 rounded-full text-[11px] font-semibold border"
                :style="estiloStatus(transporteSelecionado.status)"
              >
                <component :is="iconeStatus(transporteSelecionado.status)" :size="11" />
                {{ rotuloStatus(transporteSelecionado.status) }}
              </span>
            </div>
            <button
              @click="fecharDetalhes"
              class="flex items-center justify-center w-8 h-8 rounded-md bg-bg-elevated border border-bg-line text-ink-3 hover:text-ink transition-colors"
            >
              <X :size="14" />
            </button>
          </header>

          <div class="grid grid-cols-2 gap-4 px-7 py-5 border-b border-bg-line">
            <div>
              <p class="eyebrow mb-1.5">Lote</p>
              <p class="mono-tag text-ink text-[13px]">{{ rotuloLote(transporteSelecionado.lote) }}</p>
            </div>
            <div>
              <p class="eyebrow mb-1.5">Responsável</p>
              <p class="text-ink text-[13px]">{{ transporteSelecionado.responsavel || '—' }}</p>
            </div>
            <div>
              <p class="eyebrow mb-1.5">Transportadora</p>
              <p class="text-ink text-[13px]">{{ rotuloEmpresa(transporteSelecionado.transportadora) }}</p>
            </div>
            <div>
              <p class="eyebrow mb-1.5">Receptora</p>
              <p class="text-ink text-[13px]">{{ rotuloEmpresa(transporteSelecionado.receptora) }}</p>
            </div>
          </div>

          <div class="px-7 py-5 border-b border-bg-line">
            <p class="eyebrow mb-4">Linha do tempo</p>
            <ol class="relative pl-6 space-y-4">
              <span class="absolute left-[7px] top-2 bottom-2 w-px bg-bg-line-strong" />
              <li
                v-for="marco in linhaTempo(transporteSelecionado)"
                :key="marco.chave"
                class="relative"
              >
                <span
                  class="absolute -left-6 top-1 w-3.5 h-3.5 rounded-full border-2 border-bg-panel"
                  :style="{ backgroundColor: marco.alcancado ? marco.cor : '#1F4751' }"
                />
                <p class="text-[13px] leading-snug" :class="marco.alcancado ? 'text-ink font-semibold' : 'text-ink-3'">
                  {{ marco.rotulo }}
                </p>
                <p class="mono-tag text-[10.5px] mt-0.5" :class="marco.alcancado ? 'text-ink-3' : 'text-ink-4'">
                  {{ marco.descricao }}
                </p>
              </li>
            </ol>
          </div>

          <div v-if="transporteSelecionado.observacao" class="px-7 py-5 border-b border-bg-line">
            <p class="eyebrow mb-1.5">Observação</p>
            <p class="text-ink-2 text-[13px] leading-relaxed">{{ transporteSelecionado.observacao }}</p>
          </div>

          <footer class="flex items-center justify-end gap-2 px-7 py-4 bg-bg-elevated/30">
            <button
              @click="baixarManifesto(transporteSelecionado)"
              :disabled="baixandoManifesto === transporteSelecionado.id"
              class="flex items-center gap-2 h-10 px-4 rounded-md text-[11.5px] font-bold tracking-wider
                     border border-bg-line text-ink-2 hover:border-cyan/40 hover:text-cyan transition-colors
                     disabled:opacity-50"
            >
              <Loader2 v-if="baixandoManifesto === transporteSelecionado.id" :size="13" class="animate-spin" />
              <Download v-else :size="13" />
              MANIFESTO PDF
            </button>

            <button
              v-for="acao in acoesDoStatus(transporteSelecionado.status)"
              :key="acao.novoStatus"
              @click="abrirConfirmacaoStatus(acao)"
              class="flex items-center gap-2 h-10 px-4 rounded-md text-[11.5px] font-bold tracking-wider border transition-colors"
              :style="estiloAcao(acao)"
            >
              <component :is="acao.icone" :size="13" />
              {{ acao.rotulo }}
            </button>
          </footer>
        </div>
      </div>
    </Transition>

    <Transition name="modal">
      <div
        v-if="modalCadastroAberto"
        class="fixed inset-0 z-50 flex items-center justify-center px-4 py-8"
      >
        <div class="absolute inset-0 bg-bg-base/85 backdrop-blur-md" @click="fecharModalCadastro" />

        <div class="relative w-full max-w-xl max-h-[90vh] overflow-y-auto rounded-2xl bg-bg-panel border border-bg-line-strong helmet-stripe shadow-2xl">
          <header class="flex items-start justify-between gap-3 px-7 py-5 border-b border-bg-line">
            <div>
              <p class="eyebrow text-cyan">Novo transporte</p>
              <h2 class="display-title text-[26px] mt-1">Vincular lote à transportadora</h2>
            </div>
            <button
              @click="fecharModalCadastro"
              class="flex items-center justify-center w-8 h-8 rounded-md bg-bg-elevated border border-bg-line text-ink-3 hover:text-ink transition-colors"
            >
              <X :size="14" />
            </button>
          </header>

          <div class="px-7 py-5 space-y-4">
            <div>
              <label class="eyebrow block mb-1.5">Lote *</label>
              <select
                v-model="formulario.loteId"
                class="w-full h-10 px-3 rounded-md bg-bg-base border border-bg-line text-ink text-[13px] outline-none focus:border-cyan/40 transition-colors"
                :class="{ 'border-danger/60': erros.loteId }"
              >
                <option value="">Selecione um lote aguardando coleta</option>
                <option v-for="lote in lotesDisponiveis" :key="lote.id" :value="lote.id">
                  {{ rotuloLote(lote) }} · {{ lote.quantidade }} {{ lote.unidade }}
                </option>
              </select>
              <p v-if="erros.loteId" class="flex items-center gap-1.5 text-[11px] text-danger mt-1">
                <AlertCircle :size="11" /> {{ erros.loteId }}
              </p>
              <p v-else-if="lotesDisponiveis.length === 0" class="text-[11px] text-ink-3 mt-1">
                Nenhum lote aguardando coleta no momento.
              </p>
            </div>

            <div>
              <label class="eyebrow block mb-1.5">Transportadora *</label>
              <select
                v-model="formulario.transportadoraId"
                class="w-full h-10 px-3 rounded-md bg-bg-base border border-bg-line text-ink text-[13px] outline-none focus:border-cyan/40 transition-colors"
                :class="{ 'border-danger/60': erros.transportadoraId }"
              >
                <option value="">Selecione uma transportadora</option>
                <option v-for="empresa in transportadoras" :key="empresa.id" :value="empresa.id">
                  {{ empresa.razaoSocial }}
                </option>
              </select>
              <p v-if="erros.transportadoraId" class="flex items-center gap-1.5 text-[11px] text-danger mt-1">
                <AlertCircle :size="11" /> {{ erros.transportadoraId }}
              </p>
            </div>

            <div>
              <label class="eyebrow block mb-1.5">Receptora *</label>
              <select
                v-model="formulario.receptoraId"
                class="w-full h-10 px-3 rounded-md bg-bg-base border border-bg-line text-ink text-[13px] outline-none focus:border-cyan/40 transition-colors"
                :class="{ 'border-danger/60': erros.receptoraId }"
              >
                <option value="">Selecione uma receptora</option>
                <option v-for="empresa in receptoras" :key="empresa.id" :value="empresa.id">
                  {{ empresa.razaoSocial }}
                </option>
              </select>
              <p v-if="erros.receptoraId" class="flex items-center gap-1.5 text-[11px] text-danger mt-1">
                <AlertCircle :size="11" /> {{ erros.receptoraId }}
              </p>
            </div>

            <FormField
              v-model="formulario.responsavel"
              label="Responsável *"
              placeholder="Nome do responsável pela coleta"
              :erro="erros.responsavel"
              @update:modelValue="erros.responsavel = ''"
              @blur="validarResponsavel"
            />

            <p v-if="mensagemErro" class="flex items-center gap-1.5 text-[12px] text-danger bg-danger-soft border border-danger/30 rounded-md px-3 py-2">
              <AlertCircle :size="13" /> {{ mensagemErro }}
            </p>
          </div>

          <footer class="flex items-center justify-end gap-2 px-7 py-4 bg-bg-elevated/30 border-t border-bg-line">
            <button
              @click="fecharModalCadastro"
              class="h-10 px-4 rounded-md bg-bg-elevated border border-bg-line text-ink-2
                     text-[11.5px] font-bold tracking-wider hover:text-ink transition-colors"
            >
              CANCELAR
            </button>
            <button
              @click="salvarTransporte"
              :disabled="salvando"
              class="flex items-center gap-2 h-10 px-4 rounded-md bg-cyan text-bg-base
                     text-[11.5px] font-bold tracking-wider hover:bg-cyan-strong transition-colors disabled:opacity-50"
            >
              <Loader2 v-if="salvando" :size="13" class="animate-spin" />
              <Save v-else :size="13" />
              {{ salvando ? 'SALVANDO...' : 'CADASTRAR' }}
            </button>
          </footer>
        </div>
      </div>
    </Transition>

    <Transition name="modal">
      <div
        v-if="confirmacaoStatus"
        class="fixed inset-0 z-50 flex items-center justify-center px-4 py-8"
      >
        <div class="absolute inset-0 bg-bg-base/85 backdrop-blur-md" @click="fecharConfirmacaoStatus" />

        <div class="relative w-full max-w-md rounded-2xl bg-bg-panel border border-bg-line-strong shadow-2xl">
          <header class="px-6 py-4 border-b border-bg-line">
            <p class="eyebrow" :style="{ color: confirmacaoStatus.cor }">Confirmar mudança</p>
            <h3 class="display-title text-[22px] mt-1">{{ confirmacaoStatus.tituloConfirmacao }}</h3>
          </header>

          <div class="px-6 py-5 space-y-3">
            <p v-if="confirmacaoStatus.descricaoCascata" class="flex items-start gap-2 text-[12.5px] text-ink-2 leading-relaxed bg-info-soft border border-info/30 rounded-md px-3 py-2.5">
              <Info :size="14" class="text-info shrink-0 mt-0.5" />
              {{ confirmacaoStatus.descricaoCascata }}
            </p>

            <div>
              <label class="eyebrow block mb-1.5">Observação (opcional)</label>
              <textarea
                v-model="observacaoMudanca"
                rows="3"
                placeholder="Registre informações relevantes para auditoria"
                class="w-full px-3 py-2 rounded-md bg-bg-base border border-bg-line text-ink text-[13px] outline-none focus:border-cyan/40 transition-colors resize-none"
              />
            </div>
          </div>

          <footer class="flex items-center justify-end gap-2 px-6 py-4 bg-bg-elevated/30 border-t border-bg-line">
            <button
              @click="fecharConfirmacaoStatus"
              class="h-10 px-4 rounded-md bg-bg-elevated border border-bg-line text-ink-2
                     text-[11.5px] font-bold tracking-wider hover:text-ink transition-colors"
            >
              CANCELAR
            </button>
            <button
              @click="confirmarMudancaStatus"
              :disabled="mudandoStatus"
              class="flex items-center gap-2 h-10 px-4 rounded-md text-[11.5px] font-bold tracking-wider transition-colors disabled:opacity-50"
              :style="{ backgroundColor: confirmacaoStatus.cor, color: '#04141A' }"
            >
              <Loader2 v-if="mudandoStatus" :size="13" class="animate-spin" />
              <component v-else :is="confirmacaoStatus.icone" :size="13" />
              {{ mudandoStatus ? 'PROCESSANDO...' : 'CONFIRMAR' }}
            </button>
          </footer>
        </div>
      </div>
    </Transition>

    <Transition name="toast">
      <div
        v-if="toast"
        class="fixed bottom-6 right-6 z-50 flex items-center gap-3 px-5 py-3.5 rounded-md bg-bg-panel border shadow-2xl"
        :style="{ borderColor: toast.cor }"
      >
        <span class="flex items-center justify-center w-7 h-7 rounded-md" :style="{ backgroundColor: `${toast.cor}1a`, color: toast.cor }">
          <component :is="toast.icone" :size="14" />
        </span>
        <p class="text-[13px] text-ink">{{ toast.mensagem }}</p>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  Plus, Download, Search, X, Save, Loader2, AlertCircle, Truck, Clock,
  Play, CheckCircle, XCircle, Info,
} from 'lucide-vue-next'
import FormField from '../components/ui/FormField.vue'
import api from '../services/api'
import { exportCsv } from '../utils/exportCsv'

const STATUS = {
  PENDENTE:    'PENDENTE',
  EM_TRANSITO: 'EM_TRANSITO',
  CONCLUIDO:   'CONCLUIDO',
  CANCELADO:   'CANCELADO',
}

const CORES_STATUS = {
  PENDENTE:    '#F59E0B',
  EM_TRANSITO: '#38BDF8',
  CONCLUIDO:   '#10B981',
  CANCELADO:   '#DC2626',
}

const ROTULOS_STATUS = {
  PENDENTE:    'Pendente',
  EM_TRANSITO: 'Em trânsito',
  CONCLUIDO:   'Concluído',
  CANCELADO:   'Cancelado',
}

const ICONES_STATUS = {
  PENDENTE:    Clock,
  EM_TRANSITO: Truck,
  CONCLUIDO:   CheckCircle,
  CANCELADO:   XCircle,
}

const CHIPS = [
  { valor: 'TODOS',                rotulo: 'TODOS',                cor: '#2DD4BF', icone: Truck },
  { valor: STATUS.PENDENTE,        rotulo: 'PENDENTES',            cor: CORES_STATUS.PENDENTE,    icone: Clock },
  { valor: STATUS.EM_TRANSITO,     rotulo: 'EM TRÂNSITO',          cor: CORES_STATUS.EM_TRANSITO, icone: Truck },
  { valor: STATUS.CONCLUIDO,       rotulo: 'CONCLUÍDOS',           cor: CORES_STATUS.CONCLUIDO,   icone: CheckCircle },
]

const ACOES_POR_STATUS = {
  [STATUS.PENDENTE]: [
    {
      novoStatus: STATUS.EM_TRANSITO,
      rotulo: 'INICIAR TRÂNSITO',
      icone: Play,
      cor: CORES_STATUS.EM_TRANSITO,
      tituloConfirmacao: 'Iniciar trânsito?',
      descricaoCascata: 'O lote vinculado será atualizado automaticamente para EM_TRANSITO.',
      perfis: ['ADMIN', 'TRANSPORTADORA'],
    },
    {
      novoStatus: STATUS.CANCELADO,
      rotulo: 'CANCELAR',
      icone: XCircle,
      cor: CORES_STATUS.CANCELADO,
      tituloConfirmacao: 'Cancelar transporte?',
      descricaoCascata: '',
      perfis: ['ADMIN', 'TRANSPORTADORA'],
    },
  ],
  [STATUS.EM_TRANSITO]: [
    {
      novoStatus: STATUS.CONCLUIDO,
      rotulo: 'CONFIRMAR RECEBIMENTO',
      icone: CheckCircle,
      cor: CORES_STATUS.CONCLUIDO,
      tituloConfirmacao: 'Confirmar recebimento final?',
      descricaoCascata: 'A receptora confirma que o resíduo chegou ao destino final. O lote será atualizado automaticamente para DESCARTADO.',
      endpoint: 'recebimento-final',
      perfis: ['ADMIN', 'RECEPTORA'],
    },
    {
      novoStatus: STATUS.CANCELADO,
      rotulo: 'CANCELAR',
      icone: XCircle,
      cor: CORES_STATUS.CANCELADO,
      tituloConfirmacao: 'Cancelar transporte?',
      descricaoCascata: 'O lote vinculado voltará para AGUARDANDO_COLETA.',
      perfis: ['ADMIN', 'TRANSPORTADORA'],
    },
  ],
  [STATUS.CONCLUIDO]: [],
  [STATUS.CANCELADO]: [],
}

const formularioVazio = () => ({
  loteId: '',
  transportadoraId: '',
  receptoraId: '',
  responsavel: '',
})

const errosVazio = () => ({
  loteId: '',
  transportadoraId: '',
  receptoraId: '',
  responsavel: '',
})

const transportes = ref([])
const lotes = ref([])
const empresas = ref([])
const carregando = ref(false)

const transporteSelecionado = ref(null)
const modalCadastroAberto = ref(false)
const confirmacaoStatus = ref(null)
const observacaoMudanca = ref('')

const formulario = ref(formularioVazio())
const erros = ref(errosVazio())
const mensagemErro = ref('')
const salvando = ref(false)
const mudandoStatus = ref(false)
const baixandoManifesto = ref(null)

const termoBusca = ref('')
const filtroAtivo = ref('TODOS')

const toast = ref(null)

const perfilUsuario = computed(() =>
  (localStorage.getItem('perfil') || '').toUpperCase(),
)

const totalTransportes = computed(() => transportes.value.length)

const podeCriarTransporte = computed(() =>
  ['ADMIN', 'GERADORA'].includes(perfilUsuario.value),
)

const transportadoras = computed(() =>
  empresas.value.filter(e => e.tipo === 'TRANSPORTADORA'),
)

const receptoras = computed(() =>
  empresas.value.filter(e => e.tipo === 'RECEPTORA'),
)

const lotesDisponiveis = computed(() =>
  lotes.value.filter(l => l.status === 'AGUARDANDO_COLETA'),
)

const transportesFiltrados = computed(() => {
  let lista = [...transportes.value].sort((a, b) =>
    new Date(b.criadoEm) - new Date(a.criadoEm),
  )

  if (filtroAtivo.value !== 'TODOS') {
    lista = lista.filter(t => t.status === filtroAtivo.value)
  }

  const termo = termoBusca.value.trim().toLowerCase()
  if (termo) {
    lista = lista.filter(t => {
      const idTexto = formatarId(t).toLowerCase()
      const lote    = rotuloLote(t.lote).toLowerCase()
      const transp  = rotuloEmpresa(t.transportadora).toLowerCase()
      const rec     = rotuloEmpresa(t.receptora).toLowerCase()
      const resp    = (t.responsavel || '').toLowerCase()
      return idTexto.includes(termo) || lote.includes(termo) ||
             transp.includes(termo)  || rec.includes(termo)  || resp.includes(termo)
    })
  }

  return lista
})

const contagemPorStatus = (status) => {
  if (status === 'TODOS') return transportes.value.length
  return transportes.value.filter(t => t.status === status).length
}

const formatarId = (transporte) => {
  if (!transporte) return ''
  const ano = transporte.criadoEm
    ? new Date(transporte.criadoEm).getFullYear()
    : new Date().getFullYear()
  const numero = String(transporte.id).padStart(4, '0')
  return `TR-${ano}-${numero}`
}

const formatarIdLote = (lote) => {
  if (!lote) return '—'
  const ano = lote.criadoEm
    ? new Date(lote.criadoEm).getFullYear()
    : new Date().getFullYear()
  const numero = String(lote.id).padStart(4, '0')
  return `LT-${ano}-${numero}`
}

const rotuloLote = (lote) => {
  if (!lote) return '—'
  return formatarIdLote(lote)
}

const rotuloEmpresa = (empresa) => {
  if (!empresa) return '—'
  return empresa.razaoSocial || empresa.nome || `Empresa #${empresa.id}`
}

const rotuloStatus = (status) => ROTULOS_STATUS[status] || status

const iconeStatus = (status) => ICONES_STATUS[status] || Clock

const estiloStatus = (status) => {
  const cor = CORES_STATUS[status] || '#94A3B8'
  return {
    backgroundColor: `${cor}1a`,
    borderColor: `${cor}4d`,
    color: cor,
  }
}

const estiloAcao = (acao) => ({
  borderColor: `${acao.cor}66`,
  backgroundColor: `${acao.cor}1a`,
  color: acao.cor,
})

const acoesDoStatus = (status) =>
  (ACOES_POR_STATUS[status] || []).filter(acao =>
    !acao.perfis || acao.perfis.includes(perfilUsuario.value),
  )

const formatarData = (iso) => {
  if (!iso) return '—'
  const data = new Date(iso)
  if (Number.isNaN(data.getTime())) return '—'
  return data.toLocaleDateString('pt-BR', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}

const linhaTempo = (transporte) => {
  if (!transporte) return []

  const cancelado = transporte.status === STATUS.CANCELADO
  const emTransito = transporte.status === STATUS.EM_TRANSITO || transporte.status === STATUS.CONCLUIDO
  const concluido = transporte.status === STATUS.CONCLUIDO

  const marcos = [
    {
      chave: 'criado',
      rotulo: 'Transporte criado',
      descricao: formatarData(transporte.criadoEm),
      alcancado: true,
      cor: '#94A3B8',
    },
    {
      chave: 'transito',
      rotulo: 'Em trânsito',
      descricao: emTransito && transporte.dataColeta
        ? formatarData(transporte.dataColeta)
        : 'aguardando início',
      alcancado: emTransito,
      cor: CORES_STATUS.EM_TRANSITO,
    },
    {
      chave: 'concluido',
      rotulo: 'Concluído',
      descricao: concluido && transporte.dataEntrega
        ? formatarData(transporte.dataEntrega)
        : 'aguardando conclusão',
      alcancado: concluido,
      cor: CORES_STATUS.CONCLUIDO,
    },
  ]

  if (cancelado) {
    marcos.push({
      chave: 'cancelado',
      rotulo: 'Cancelado',
      descricao: 'Operação interrompida',
      alcancado: true,
      cor: CORES_STATUS.CANCELADO,
    })
  }

  return marcos
}

const abrirDetalhes = (transporte) => { transporteSelecionado.value = transporte }
const fecharDetalhes = () => { transporteSelecionado.value = null }

const abrirModalCadastro = () => {
  formulario.value = formularioVazio()
  erros.value = errosVazio()
  mensagemErro.value = ''
  modalCadastroAberto.value = true
}
const fecharModalCadastro = () => { modalCadastroAberto.value = false }

const abrirConfirmacaoStatus = (acao) => {
  observacaoMudanca.value = ''
  confirmacaoStatus.value = acao
}
const fecharConfirmacaoStatus = () => { confirmacaoStatus.value = null }

const validarResponsavel = () => {
  const valor = formulario.value.responsavel.trim()
  if (valor.length === 0) erros.value.responsavel = 'Responsável é obrigatório.'
  else if (valor.length < 3) erros.value.responsavel = 'Mínimo 3 caracteres.'
  else if (valor.length > 80) erros.value.responsavel = 'Máximo 80 caracteres.'
  else erros.value.responsavel = ''
}

const validarFormularioCompleto = () => {
  erros.value = errosVazio()
  if (!formulario.value.loteId)           erros.value.loteId           = 'Selecione um lote.'
  if (!formulario.value.transportadoraId) erros.value.transportadoraId = 'Selecione uma transportadora.'
  if (!formulario.value.receptoraId)      erros.value.receptoraId      = 'Selecione uma receptora.'
  validarResponsavel()
  return Object.values(erros.value).every(mensagem => mensagem === '')
}

const exibirToast = (mensagem, cor = CORES_STATUS.CONCLUIDO, icone = CheckCircle) => {
  toast.value = { mensagem, cor, icone }
  setTimeout(() => { toast.value = null }, 4000)
}

const carregarDados = async () => {
  carregando.value = true
  try {
    const [respTransportes, respLotes, respEmpresas] = await Promise.all([
      api.get('/transportes'),
      api.get('/lotes'),
      api.get('/empresas'),
    ])
    transportes.value = respTransportes.data
    lotes.value       = respLotes.data
    empresas.value    = respEmpresas.data
  } catch (erro) {
    console.error('Erro ao carregar dados:', erro)
    exibirToast('Falha ao carregar dados do servidor.', CORES_STATUS.CANCELADO, AlertCircle)
  } finally {
    carregando.value = false
  }
}

const salvarTransporte = async () => {
  if (salvando.value) return
  if (!validarFormularioCompleto()) {
    mensagemErro.value = 'Corrija os campos destacados antes de salvar.'
    return
  }
  salvando.value = true
  mensagemErro.value = ''
  try {
    const resposta = await api.post('/transportes', {
      lote:           { id: Number(formulario.value.loteId) },
      transportadora: { id: Number(formulario.value.transportadoraId) },
      receptora:      { id: Number(formulario.value.receptoraId) },
      responsavel:    formulario.value.responsavel.trim(),
    })
    fecharModalCadastro()
    await carregarDados()
    exibirToast(`Transporte ${formatarId(resposta.data)} criado · status PENDENTE`)
  } catch (erro) {
    mensagemErro.value = erro.mensagemAmigavel || 'Erro ao cadastrar transporte. Verifique os dados.'
  } finally {
    salvando.value = false
  }
}

const confirmarMudancaStatus = async () => {
  if (mudandoStatus.value || !confirmacaoStatus.value || !transporteSelecionado.value) return
  mudandoStatus.value = true
  try {
    if (confirmacaoStatus.value.endpoint === 'recebimento-final') {
      await api.patch(
        `/transportes/${transporteSelecionado.value.id}/recebimento-final`,
        null,
        { params: { observacao: observacaoMudanca.value.trim() } },
      )
    } else {
      await api.patch(
        `/transportes/${transporteSelecionado.value.id}/status`,
        null,
        {
          params: {
            novoStatus: confirmacaoStatus.value.novoStatus,
            observacao: observacaoMudanca.value.trim(),
          },
        },
      )
    }
    const novoStatus = confirmacaoStatus.value.novoStatus
    fecharConfirmacaoStatus()
    fecharDetalhes()
    await carregarDados()
    exibirToast(
      `Status atualizado para ${ROTULOS_STATUS[novoStatus]}`,
      CORES_STATUS[novoStatus],
      ICONES_STATUS[novoStatus],
    )
  } catch (erro) {
    console.error('Erro ao mudar status:', erro)
    exibirToast(
      erro.mensagemAmigavel || 'Erro ao atualizar status.',
      CORES_STATUS.CANCELADO,
      AlertCircle,
    )
  } finally {
    mudandoStatus.value = false
  }
}

const transporteParaCsv = (transporte) => ({
  id:             formatarId(transporte),
  lote:           rotuloLote(transporte.lote),
  transportadora: rotuloEmpresa(transporte.transportadora),
  receptora:      rotuloEmpresa(transporte.receptora),
  responsavel:    transporte.responsavel || '',
  status:         rotuloStatus(transporte.status),
  dataColeta:     formatarData(transporte.dataColeta),
  dataEntrega:    formatarData(transporte.dataEntrega),
  observacao:     transporte.observacao || '',
  criadoEm:       formatarData(transporte.criadoEm),
})

const exportarLista = () => {
  exportCsv('ecotrack-transportes.csv', transportesFiltrados.value.map(transporteParaCsv))
}

const baixarManifesto = async (transporte) => {
  if (!transporte?.id || baixandoManifesto.value) return

  baixandoManifesto.value = transporte.id
  try {
    const resposta = await api.get(`/transportes/${transporte.id}/manifesto`, {
      responseType: 'blob',
    })

    const url = URL.createObjectURL(new Blob([resposta.data], { type: 'application/pdf' }))
    const link = document.createElement('a')
    link.href = url
    link.download = `manifesto-${formatarId(transporte).toLowerCase()}.pdf`
    document.body.appendChild(link)
    link.click()
    link.remove()
    URL.revokeObjectURL(url)
    exibirToast('Manifesto PDF gerado com sucesso.')
  } catch (erro) {
    console.error('Erro ao gerar manifesto:', erro)
    exibirToast(
      erro.mensagemAmigavel || 'Erro ao gerar manifesto PDF.',
      CORES_STATUS.CANCELADO,
      AlertCircle,
    )
  } finally {
    baixandoManifesto.value = null
  }
}

onMounted(carregarDados)
</script>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: opacity 200ms ease;
}
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.toast-enter-active,
.toast-leave-active {
  transition: opacity 240ms ease, transform 240ms ease;
}
.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(12px);
}
</style>
