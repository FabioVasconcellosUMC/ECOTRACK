<template>
  <div class="flex flex-col gap-7 pb-6 max-w-[1600px] mx-auto">

    <header class="flex items-end justify-between flex-wrap gap-4 fade-up">
      <div>
        <p class="eyebrow-italic text-cyan mb-2">Visão geral</p>
        <h1 class="display-title text-[48px] leading-[0.98]">
          Dashboard operacional
        </h1>
        <p class="text-ink-3 text-[13px] mt-2 mono-tag">
          {{ dataLonga }} · acompanhamento dos principais indicadores
        </p>
      </div>
    </header>

    <section
      class="relative rounded-2xl overflow-hidden helmet-stripe fade-up-1 border border-bg-line-strong"
    >
      <div
        class="absolute inset-0 bg-cover pointer-events-none"
        style="background-image: url('/photos/dashboard-bg.jpg'); background-position: center 35%;"
      />
      <div
        class="absolute inset-0 pointer-events-none"
        style="background: linear-gradient(120deg,
          rgba(7, 24, 30, 0.88) 0%,
          rgba(7, 24, 30, 0.72) 50%,
          rgba(7, 24, 30, 0.85) 100%);"
      />
      <div
        class="absolute inset-0 pointer-events-none"
        style="background: radial-gradient(900px 500px at 50% 50%, rgba(0, 76, 84, 0.25), transparent 65%);"
      />

      <div class="relative grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5 p-9">
        <article
          v-for="kpi in kpisDashboard"
          :key="kpi.chave"
          class="card-vidro flex flex-col gap-5 p-6 rounded-xl"
        >
          <div class="flex items-center justify-between">
            <p class="eyebrow text-ink-2">{{ kpi.rotulo }}</p>
            <span class="flex items-center justify-center w-8 h-8 rounded-md bg-cyan/15 border border-cyan/30 text-cyan">
              <component :is="kpi.icone" :size="14" />
            </span>
          </div>
          <div class="flex items-baseline gap-2">
            <span class="scoreboard text-[56px] text-ink leading-none tabular-nums kpi-numero">{{ kpi.valor }}</span>
            <span v-if="kpi.unidade" class="font-display text-cyan text-[15px] kpi-unidade">{{ kpi.unidade }}</span>
          </div>
          <p class="mono-tag text-ink-3 text-[11px]">{{ kpi.descricao }}</p>
        </article>
      </div>
    </section>

    <section class="fade-up-2">
      <div
        class="rounded-2xl bg-bg-panel border border-bg-line-strong p-7 helmet-stripe"
      >
        <div class="flex items-end justify-between mb-6">
          <div>
            <p class="eyebrow">Volume · últimos 6 meses</p>
            <h3 class="section-title text-[26px] mt-2">
              Lotes por mês
            </h3>
          </div>
          <div class="flex items-center gap-3 text-[11px]">
            <span class="flex items-center gap-1.5 text-ink-2">
              <span class="w-2 h-2 rounded-sm bg-cyan" /> Volume
            </span>
            <span class="flex items-center gap-1.5 text-ink-3">
              <span class="w-2 h-2 rounded-sm bg-brand" /> Meta
            </span>
          </div>
        </div>
        <div class="h-[420px]">
          <Bar :data="dadosGrafico" :options="opcoesGrafico" />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Bar } from 'vue-chartjs'
import {
  Chart as ChartJS, CategoryScale, LinearScale, BarController, BarElement, Tooltip, LineController, LineElement, PointElement,
} from 'chart.js'
import {
  Building2, Boxes, Truck, ScrollText,
} from 'lucide-vue-next'
import { buscarComCache } from '../services/dataCache'

ChartJS.register(CategoryScale, LinearScale, BarController, BarElement, Tooltip, LineController, LineElement, PointElement)

const DURACAO_ANIMACAO_BASE = 1100
const DURACAO_ANIMACAO_HERO = 1400

const totalEmpresas    = ref(0)
const totalLotes       = ref(0)
const totalEmTransito  = ref(0)
const totalToneladas   = ref(0)

// Dados reais do gráfico, calculados a partir dos lotes (/lotes)
const graficoLabels = ref([])
const graficoValores = ref([])

const animados = ref({ toneladas: 0, empresas: 0, lotes: 0, emTransito: 0 })

const dataLonga = new Date().toLocaleDateString('pt-BR', {
  weekday: 'long', day: '2-digit', month: 'long', year: 'numeric',
}).toUpperCase()

const kpisDashboard = computed(() => [
  {
    chave:     'toneladas',
    rotulo:    'Resíduos rastreados',
    valor:     animados.value.toneladas,
    unidade:   'TON',
    descricao: 'consolidado dos lotes',
    icone:     ScrollText,
  },
  {
    chave:     'empresas',
    rotulo:    'Empresas',
    valor:     animados.value.empresas,
    unidade:   '',
    descricao: 'cadastradas no ecossistema',
    icone:     Building2,
  },
  {
    chave:     'lotes',
    rotulo:    'Lotes',
    valor:     animados.value.lotes,
    unidade:   '',
    descricao: 'registrados no sistema',
    icone:     Boxes,
  },
  {
    chave:     'emTransito',
    rotulo:    'Em trânsito',
    valor:     animados.value.emTransito,
    unidade:   '',
    descricao: 'transportes em curso',
    icone:     Truck,
  },
])

const animar = (valorAlvo, chave, duracao = DURACAO_ANIMACAO_BASE) => {
  const inicio = performance.now()
  const easing = (t) => 1 - Math.pow(1 - t, 3)
  const tick = (agora) => {
    const t = Math.min(1, (agora - inicio) / duracao)
    const valor = Math.round(valorAlvo * easing(t))
    animados.value[chave] = valor
    if (t < 1) requestAnimationFrame(tick)
  }
  requestAnimationFrame(tick)
}

const NOMES_MESES = ['JAN', 'FEV', 'MAR', 'ABR', 'MAI', 'JUN', 'JUL', 'AGO', 'SET', 'OUT', 'NOV', 'DEZ']

// Conta quantos lotes foram criados em cada um dos últimos 6 meses
const calcularLotesPorMes = (lotes) => {
  const hoje = new Date()
  const baldes = []

  // Monta os 6 meses, do mais antigo (5 meses atrás) até o atual
  for (let i = 5; i >= 0; i--) {
    const data = new Date(hoje.getFullYear(), hoje.getMonth() - i, 1)
    baldes.push({
      ano: data.getFullYear(),
      mes: data.getMonth(),       // 0 = janeiro
      rotulo: NOMES_MESES[data.getMonth()],
      total: 0,
    })
  }

  // Pra cada lote, descobre em qual mês ele foi criado e incrementa
  lotes.forEach((lote) => {
    if (!lote.criadoEm) return
    const dataLote = new Date(lote.criadoEm)
    const balde = baldes.find(
      (b) => b.ano === dataLote.getFullYear() && b.mes === dataLote.getMonth(),
    )
    if (balde) balde.total++
  })

  graficoLabels.value  = baldes.map((b) => b.rotulo)
  graficoValores.value = baldes.map((b) => b.total)
}

const carregarIndicadores = async () => {
  try {
    const [respostaEmpresas, respostaLotes, respostaTransportes] = await Promise.all([
      buscarComCache('/empresas'),
      buscarComCache('/lotes'),
      buscarComCache('/transportes'),
    ])

    totalEmpresas.value = respostaEmpresas.length
    totalLotes.value    = respostaLotes.length
    totalEmTransito.value = respostaTransportes.filter(
      transporte => transporte.status === 'EM_TRANSITO',
    ).length

    totalToneladas.value = respostaLotes.reduce((soma, lote) => {
      const quantidade = Number(lote.quantidade) || 0
      const unidade = (lote.unidade || '').toUpperCase()
      if (unidade === 'TON') return soma + quantidade
      if (unidade === 'KG')  return soma + quantidade / 1000
      return soma
    }, 0)

    // Calcula a distribuição de lotes nos últimos 6 meses
    calcularLotesPorMes(respostaLotes)

    animar(Math.round(totalToneladas.value), 'toneladas',  DURACAO_ANIMACAO_HERO)
    animar(totalEmpresas.value,                'empresas',  DURACAO_ANIMACAO_BASE)
    animar(totalLotes.value,                   'lotes',     DURACAO_ANIMACAO_BASE + 120)
    animar(totalEmTransito.value,              'emTransito', DURACAO_ANIMACAO_BASE + 240)
  } catch (erro) {
    console.error('Erro ao carregar indicadores do dashboard:', erro)
  }
}

onMounted(carregarIndicadores)

const dadosGrafico = computed(() => {
  // Linha de meta = média dos volumes dos 6 meses (arredondada)
  const valores = graficoValores.value
  const media = valores.length
    ? Math.round(valores.reduce((soma, v) => soma + v, 0) / valores.length)
    : 0

  return {
    labels: graficoLabels.value,
    datasets: [
      {
        label: 'Volume',
        data: graficoValores.value,
        backgroundColor: (ctx) => {
          const { ctx: contexto, chartArea } = ctx.chart
          if (!chartArea) return '#2DD4BF'
          const gradiente = contexto.createLinearGradient(0, chartArea.bottom, 0, chartArea.top)
          gradiente.addColorStop(0, 'rgba(0, 109, 120, 0.6)')
          gradiente.addColorStop(1, 'rgba(45, 212, 191, 0.95)')
          return gradiente
        },
        hoverBackgroundColor: '#2DD4BF',
        borderRadius:  { topLeft: 6, topRight: 6, bottomLeft: 0, bottomRight: 0 },
        borderSkipped: false,
        barThickness:  64,
      },
      {
        label: 'Meta',
        data: graficoValores.value.map(() => media),
        type: 'line',
        borderColor: 'rgba(0, 76, 84, 0.7)',
        borderDash:  [4, 4],
        borderWidth: 1.5,
        pointRadius: 0,
        tension:     0,
      },
    ],
  }
})

const opcoesGrafico = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend:  { display: false },
    tooltip: {
      backgroundColor: '#0B2026',
      borderColor: '#1F4751',
      borderWidth: 1,
      padding: 12,
      titleColor: '#F1F5F4',
      bodyColor:  '#B8C2C5',
      titleFont: { family: 'DM Sans', weight: '600', size: 11 },
      bodyFont:  { family: 'JetBrains Mono', size: 11 },
    },
  },
  scales: {
    x: {
      ticks:  { color: '#6E7779', font: { family: 'JetBrains Mono', size: 10, weight: '500' } },
      grid:   { display: false },
      border: { color: '#173640' },
    },
    y: {
      ticks:  { color: '#6E7779', font: { family: 'JetBrains Mono', size: 10 }, stepSize: 20 },
      grid:   { color: 'rgba(23, 54, 64, 0.6)', drawBorder: false },
      border: { display: false },
    },
  },
}
</script>

<style scoped>
.card-vidro {
  background: rgba(7, 24, 30, 0.55);
  backdrop-filter: blur(18px) saturate(140%);
  -webkit-backdrop-filter: blur(18px) saturate(140%);
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 8px 24px -12px rgba(0, 0, 0, 0.6),
              0 1px 0 0 rgba(255, 255, 255, 0.04) inset;
  transition: border-color 200ms;
}

.card-vidro:hover {
  border-color: rgba(45, 212, 191, 0.25);
}

.kpi-numero {
  text-shadow: 0 2px 16px rgba(0, 0, 0, 0.45);
}

.kpi-unidade {
  font-weight: 500;
  font-style: italic;
  letter-spacing: 0.04em;
}
</style>
