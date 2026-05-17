<template>
  <div class="flex flex-col gap-7 pb-6 max-w-[1600px] mx-auto">

    <header class="flex items-end justify-between flex-wrap gap-4 fade-up">
      <div>
        <p class="eyebrow-italic text-cyan mb-2">Análise · Auditoria</p>
        <h1 class="display-title text-[48px] leading-[0.98]">
          Relatórios
        </h1>
        <p class="text-ink-3 text-[13px] mt-2 mono-tag">
          {{ dataLonga }} · indicadores agregados em tempo real
        </p>
      </div>

      <div class="flex items-center gap-2">
        <button
          @click="exportarConsolidado"
          :disabled="carregando"
          class="flex items-center gap-2 h-10 px-4 rounded-md bg-cyan text-bg-base
                 text-[11.5px] font-bold tracking-wider hover:bg-cyan-strong transition-colors
                 disabled:opacity-50 cursor-pointer"
        >
          <Download :size="14" />
          EXPORTAR CONSOLIDADO
        </button>
      </div>
    </header>

    <div v-if="carregando" class="flex flex-col items-center gap-3 py-20 fade-up-1">
      <Loader2 :size="22" class="text-cyan animate-spin" />
      <p class="mono-tag text-ink-3 text-[11px]">CARREGANDO INDICADORES...</p>
    </div>

    <template v-else>
      <section class="grid grid-cols-12 gap-4 fade-up-1">
        <div
          v-for="cartao in cartoesGerais"
          :key="cartao.rotulo"
          class="col-span-12 sm:col-span-6 lg:col-span-3 rounded-2xl bg-bg-panel border border-bg-line-strong p-6 helmet-stripe"
        >
          <div class="flex items-center justify-between mb-3">
            <p class="eyebrow">{{ cartao.rotulo }}</p>
            <span class="flex items-center justify-center w-8 h-8 rounded-md" :style="estiloIcone(cartao.cor)">
              <component :is="cartao.icone" :size="14" />
            </span>
          </div>
          <p class="scoreboard text-[40px] text-ink leading-none tabular-nums">{{ cartao.valor }}</p>
          <p v-if="cartao.descricao" class="mono-tag text-ink-3 text-[11px] mt-2">{{ cartao.descricao }}</p>
        </div>
      </section>

      <section class="grid grid-cols-12 gap-5 fade-up-2">
        <div class="col-span-12 lg:col-span-6 rounded-2xl bg-bg-panel border border-bg-line-strong p-7 helmet-stripe">
          <div class="flex items-center justify-between mb-5">
            <div>
              <p class="eyebrow">Distribuição</p>
              <h3 class="section-title text-[22px] mt-1">Empresas por tipo</h3>
            </div>
            <span class="mono-tag text-ink-3 text-[11px]">{{ empresas.length }} total</span>
          </div>

          <div class="space-y-4">
            <div
              v-for="distribuicao in empresasPorTipo"
              :key="distribuicao.tipo"
              class="space-y-2"
            >
              <div class="flex items-center justify-between text-[12px]">
                <span class="flex items-center gap-2 text-ink">
                  <span class="w-2 h-2 rounded-sm" :style="{ backgroundColor: distribuicao.cor }" />
                  {{ distribuicao.rotulo }}
                </span>
                <span class="mono-tag text-ink-3">{{ distribuicao.total }} · {{ distribuicao.percentual }}%</span>
              </div>
              <div class="h-2 rounded-full bg-bg-base overflow-hidden">
                <div
                  class="h-full rounded-full transition-all duration-500"
                  :style="{ width: `${distribuicao.percentual}%`, backgroundColor: distribuicao.cor }"
                />
              </div>
            </div>
          </div>
        </div>

        <div class="col-span-12 lg:col-span-6 rounded-2xl bg-bg-panel border border-bg-line-strong p-7 helmet-stripe">
          <div class="flex items-center justify-between mb-5">
            <div>
              <p class="eyebrow">Operação</p>
              <h3 class="section-title text-[22px] mt-1">Lotes por status</h3>
            </div>
            <span class="mono-tag text-ink-3 text-[11px]">{{ lotes.length }} total</span>
          </div>

          <div class="space-y-4">
            <div
              v-for="distribuicao in lotesPorStatus"
              :key="distribuicao.status"
              class="space-y-2"
            >
              <div class="flex items-center justify-between text-[12px]">
                <span class="flex items-center gap-2 text-ink">
                  <span class="w-2 h-2 rounded-sm" :style="{ backgroundColor: distribuicao.cor }" />
                  {{ distribuicao.rotulo }}
                </span>
                <span class="mono-tag text-ink-3">{{ distribuicao.total }} · {{ distribuicao.percentual }}%</span>
              </div>
              <div class="h-2 rounded-full bg-bg-base overflow-hidden">
                <div
                  class="h-full rounded-full transition-all duration-500"
                  :style="{ width: `${distribuicao.percentual}%`, backgroundColor: distribuicao.cor }"
                />
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="grid grid-cols-12 gap-5 fade-up-3">
        <div class="col-span-12 lg:col-span-6 rounded-2xl bg-bg-panel border border-bg-line-strong helmet-stripe overflow-hidden">
          <header class="px-6 py-4 border-b border-bg-line">
            <p class="eyebrow text-cyan">Ranking</p>
            <h3 class="section-title text-[20px] mt-1">Top 5 transportadoras</h3>
          </header>
          <div v-if="rankingTransportadoras.length === 0" class="px-6 py-10 text-center">
            <p class="mono-tag text-ink-3 text-[11px]">Nenhum transporte cadastrado ainda</p>
          </div>
          <ul v-else class="divide-y divide-bg-line">
            <li
              v-for="(item, indice) in rankingTransportadoras"
              :key="item.id"
              class="flex items-center gap-4 px-6 py-3.5"
            >
              <span class="flex items-center justify-center w-7 h-7 rounded-md bg-bg-elevated border border-bg-line mono-tag text-ink-2 text-[11px]">
                {{ String(indice + 1).padStart(2, '0') }}
              </span>
              <p class="flex-1 text-[13px] text-ink truncate">{{ item.razaoSocial }}</p>
              <span class="mono-tag text-cyan text-[12px]">{{ item.totalTransportes }} transporte{{ item.totalTransportes === 1 ? '' : 's' }}</span>
            </li>
          </ul>
        </div>

        <div class="col-span-12 lg:col-span-6 rounded-2xl bg-bg-panel border border-bg-line-strong helmet-stripe overflow-hidden">
          <header class="px-6 py-4 border-b border-bg-line">
            <p class="eyebrow text-cyan">Ranking</p>
            <h3 class="section-title text-[20px] mt-1">Top 5 geradoras</h3>
          </header>
          <div v-if="rankingGeradoras.length === 0" class="px-6 py-10 text-center">
            <p class="mono-tag text-ink-3 text-[11px]">Nenhum lote cadastrado ainda</p>
          </div>
          <ul v-else class="divide-y divide-bg-line">
            <li
              v-for="(item, indice) in rankingGeradoras"
              :key="item.id"
              class="flex items-center gap-4 px-6 py-3.5"
            >
              <span class="flex items-center justify-center w-7 h-7 rounded-md bg-bg-elevated border border-bg-line mono-tag text-ink-2 text-[11px]">
                {{ String(indice + 1).padStart(2, '0') }}
              </span>
              <p class="flex-1 text-[13px] text-ink truncate">{{ item.razaoSocial }}</p>
              <span class="mono-tag text-cyan text-[12px]">{{ item.totalLotes }} lote{{ item.totalLotes === 1 ? '' : 's' }}</span>
            </li>
          </ul>
        </div>
      </section>

      <section class="rounded-2xl bg-bg-panel border border-bg-line-strong p-7 helmet-stripe fade-up-4">
        <div class="flex items-center justify-between mb-5">
          <div>
            <p class="eyebrow">Exportações</p>
            <h3 class="section-title text-[22px] mt-1">Bases para auditoria</h3>
          </div>
        </div>

        <div class="grid grid-cols-12 gap-4">
          <button
            v-for="base in BASES_EXPORTACAO"
            :key="base.chave"
            @click="exportarBase(base.chave)"
            class="col-span-12 sm:col-span-4 flex items-start gap-3 p-4 rounded-md bg-bg-elevated border border-bg-line hover:border-cyan/40 transition-colors text-left cursor-pointer"
          >
            <span class="flex items-center justify-center w-9 h-9 rounded-md bg-cyan/10 border border-cyan/30 text-cyan shrink-0">
              <component :is="base.icone" :size="14" />
            </span>
            <div class="flex-1">
              <p class="text-[13px] font-semibold text-ink">{{ base.rotulo }}</p>
              <p class="text-[11px] text-ink-3 mt-0.5">{{ base.descricao }}</p>
              <p class="mono-tag text-cyan text-[10.5px] mt-2">{{ contarRegistros(base.chave) }} registros</p>
            </div>
            <Download :size="13" class="text-ink-3 mt-1 shrink-0" />
          </button>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  Download, Loader2, Building2, Boxes, Truck, ScrollText, FileSpreadsheet,
} from 'lucide-vue-next'
import api from '../services/api'
import { exportCsv } from '../utils/exportCsv'

const CORES_TIPO = {
  GERADORA:       '#38BDF8',
  TRANSPORTADORA: '#F59E0B',
  RECEPTORA:      '#10B981',
}

const ROTULOS_TIPO = {
  GERADORA:       'Geradoras',
  TRANSPORTADORA: 'Transportadoras',
  RECEPTORA:      'Receptoras',
}

const CORES_STATUS_LOTE = {
  AGUARDANDO_COLETA: '#F59E0B',
  EM_TRANSITO:       '#38BDF8',
  DESCARTADO:        '#10B981',
  CANCELADO:         '#DC2626',
}

const ROTULOS_STATUS_LOTE = {
  AGUARDANDO_COLETA: 'Aguardando coleta',
  EM_TRANSITO:       'Em trânsito',
  DESCARTADO:        'Descartado',
  CANCELADO:         'Cancelado',
}

const BASES_EXPORTACAO = [
  { chave: 'empresas',    rotulo: 'Empresas',    descricao: 'Cadastro completo das empresas do ecossistema',     icone: Building2 },
  { chave: 'lotes',       rotulo: 'Lotes',       descricao: 'Registro dos lotes de resíduos com status atual',   icone: Boxes },
  { chave: 'transportes', rotulo: 'Transportes', descricao: 'Histórico operacional dos transportes registrados', icone: Truck },
]

const empresas    = ref([])
const lotes       = ref([])
const transportes = ref([])
const carregando  = ref(false)

const dataLonga = new Date().toLocaleDateString('pt-BR', {
  weekday: 'long', day: '2-digit', month: 'long', year: 'numeric',
}).toUpperCase()

const totalToneladas = computed(() => {
  const soma = lotes.value.reduce((acc, lote) => {
    const quantidade = Number(lote.quantidade) || 0
    const unidade = (lote.unidade || '').toUpperCase()
    if (unidade === 'TON') return acc + quantidade
    if (unidade === 'KG')  return acc + quantidade / 1000
    return acc
  }, 0)
  return Math.round(soma)
})

const transportesConcluidos = computed(() =>
  transportes.value.filter(t => t.status === 'CONCLUIDO').length,
)

const transportesEmAndamento = computed(() =>
  transportes.value.filter(t => t.status === 'PENDENTE' || t.status === 'EM_TRANSITO').length,
)

const cartoesGerais = computed(() => [
  {
    rotulo: 'Resíduos rastreados',
    valor: `${totalToneladas.value} TON`,
    descricao: 'soma consolidada dos lotes',
    icone: ScrollText,
    cor: '#2DD4BF',
  },
  {
    rotulo: 'Empresas no ecossistema',
    valor: empresas.value.length,
    descricao: 'parceiros cadastrados',
    icone: Building2,
    cor: '#38BDF8',
  },
  {
    rotulo: 'Transportes concluídos',
    valor: transportesConcluidos.value,
    descricao: 'ciclos finalizados',
    icone: Truck,
    cor: '#10B981',
  },
  {
    rotulo: 'Em andamento',
    valor: transportesEmAndamento.value,
    descricao: 'pendentes ou em trânsito',
    icone: Boxes,
    cor: '#F59E0B',
  },
])

const empresasPorTipo = computed(() => {
  const total = empresas.value.length
  return ['GERADORA', 'TRANSPORTADORA', 'RECEPTORA'].map(tipo => {
    const totalTipo = empresas.value.filter(e => e.tipo === tipo).length
    return {
      tipo,
      rotulo:     ROTULOS_TIPO[tipo],
      cor:        CORES_TIPO[tipo],
      total:      totalTipo,
      percentual: total === 0 ? 0 : Math.round((totalTipo / total) * 100),
    }
  })
})

const lotesPorStatus = computed(() => {
  const total = lotes.value.length
  return ['AGUARDANDO_COLETA', 'EM_TRANSITO', 'DESCARTADO', 'CANCELADO'].map(status => {
    const totalStatus = lotes.value.filter(l => l.status === status).length
    return {
      status,
      rotulo:     ROTULOS_STATUS_LOTE[status],
      cor:        CORES_STATUS_LOTE[status],
      total:      totalStatus,
      percentual: total === 0 ? 0 : Math.round((totalStatus / total) * 100),
    }
  })
})

const rankingTransportadoras = computed(() => {
  const contagem = new Map()
  transportes.value.forEach(transporte => {
    const empresa = transporte.transportadora
    if (!empresa?.id) return
    const atual = contagem.get(empresa.id) || { id: empresa.id, razaoSocial: empresa.razaoSocial || `Empresa #${empresa.id}`, totalTransportes: 0 }
    atual.totalTransportes += 1
    contagem.set(empresa.id, atual)
  })
  return Array.from(contagem.values())
    .sort((a, b) => b.totalTransportes - a.totalTransportes)
    .slice(0, 5)
})

const rankingGeradoras = computed(() => {
  const contagem = new Map()
  lotes.value.forEach(lote => {
    const empresa = lote.empresaGeradora || lote.empresa
    if (!empresa?.id) return
    const atual = contagem.get(empresa.id) || { id: empresa.id, razaoSocial: empresa.razaoSocial || `Empresa #${empresa.id}`, totalLotes: 0 }
    atual.totalLotes += 1
    contagem.set(empresa.id, atual)
  })
  return Array.from(contagem.values())
    .sort((a, b) => b.totalLotes - a.totalLotes)
    .slice(0, 5)
})

const estiloIcone = (cor) => ({
  backgroundColor: `${cor}1a`,
  border: `1px solid ${cor}33`,
  color: cor,
})

const carregarDados = async () => {
  carregando.value = true
  try {
    const [respEmpresas, respLotes, respTransportes] = await Promise.all([
      api.get('/empresas'),
      api.get('/lotes'),
      api.get('/transportes'),
    ])
    empresas.value    = respEmpresas.data
    lotes.value       = respLotes.data
    transportes.value = respTransportes.data
  } catch (erro) {
    console.error('Erro ao carregar relatórios:', erro)
  } finally {
    carregando.value = false
  }
}

const contarRegistros = (chave) => {
  if (chave === 'empresas')    return empresas.value.length
  if (chave === 'lotes')       return lotes.value.length
  if (chave === 'transportes') return transportes.value.length
  return 0
}

const empresaParaCsv = (empresa) => ({
  id:          empresa.id,
  razaoSocial: empresa.razaoSocial,
  cnpj:        empresa.cnpj,
  tipo:        empresa.tipo,
  email:       empresa.email || '',
  telefone:    empresa.telefone || '',
  endereco:    empresa.endereco || '',
})

const loteParaCsv = (lote) => ({
  id:         lote.id,
  geradora:   lote.empresaGeradora?.razaoSocial || lote.empresa?.razaoSocial || '',
  quantidade: lote.quantidade,
  unidade:    lote.unidade,
  status:     lote.status,
  criadoEm:   lote.criadoEm,
})

const transporteParaCsv = (transporte) => ({
  id:             transporte.id,
  lote:           transporte.lote?.id || '',
  transportadora: transporte.transportadora?.razaoSocial || '',
  receptora:      transporte.receptora?.razaoSocial || '',
  responsavel:    transporte.responsavel || '',
  status:         transporte.status,
  dataColeta:     transporte.dataColeta || '',
  dataEntrega:    transporte.dataEntrega || '',
})

const exportarBase = (chave) => {
  if (chave === 'empresas') {
    exportCsv('ecotrack-empresas.csv', empresas.value.map(empresaParaCsv))
  } else if (chave === 'lotes') {
    exportCsv('ecotrack-lotes.csv', lotes.value.map(loteParaCsv))
  } else if (chave === 'transportes') {
    exportCsv('ecotrack-transportes.csv', transportes.value.map(transporteParaCsv))
  }
}

const exportarConsolidado = () => {
  const resumo = [
    {
      indicador:  'Resíduos rastreados (TON)',
      valor:      totalToneladas.value,
      observacao: 'Soma consolidada de todos os lotes',
    },
    {
      indicador:  'Empresas no ecossistema',
      valor:      empresas.value.length,
      observacao: `${empresasPorTipo.value.map(d => `${d.total} ${d.rotulo.toLowerCase()}`).join(' · ')}`,
    },
    {
      indicador:  'Lotes registrados',
      valor:      lotes.value.length,
      observacao: lotesPorStatus.value.map(d => `${d.total} ${d.rotulo.toLowerCase()}`).join(' · '),
    },
    {
      indicador:  'Transportes registrados',
      valor:      transportes.value.length,
      observacao: `${transportesConcluidos.value} concluídos · ${transportesEmAndamento.value} em andamento`,
    },
  ]
  exportCsv('ecotrack-consolidado.csv', resumo)
}

onMounted(carregarDados)
</script>
