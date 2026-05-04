<template>
  <div class="flex flex-col gap-7 pb-6 max-w-[1600px] mx-auto">

    <header class="flex items-end justify-between flex-wrap gap-4 fade-up">
      <div>
        <p class="eyebrow-italic text-cyan mb-2">Briefing diário</p>
        <h1 class="display-title text-[48px] leading-[0.98]">
          Centro de Operações
        </h1>
        <p class="text-ink-3 text-[13px] mt-2 mono-tag">
          {{ dateLong }} · turno em curso · sincronizado
        </p>
      </div>

      <div class="flex items-center gap-2">
        <div class="flex p-1 rounded-md bg-bg-elevated border border-bg-line">
          <button
            v-for="p in periodos"
            :key="p"
            @click="periodo = p"
            class="px-3 py-1.5 rounded-[5px] text-[11.5px] font-bold tracking-wider transition-colors"
            :class="periodo === p
              ? 'bg-bg-panel text-cyan'
              : 'text-ink-3 hover:text-ink'"
          >
            {{ p.toUpperCase() }}
          </button>
        </div>
        <button class="ml-1 flex items-center gap-2 px-3.5 h-8 rounded-md text-[11.5px] font-bold tracking-wider
                       bg-bg-elevated border border-bg-line text-ink-2 hover:border-bg-line-strong transition-colors">
          <Download :size="13" /> EXPORTAR
        </button>
      </div>
    </header>

    <section
      class="relative grid grid-cols-12 rounded-2xl overflow-hidden helmet-stripe fade-up-1
             border border-bg-line-strong"
    >
      <div
        class="absolute inset-0 bg-cover pointer-events-none"
        style="background-image: url('/photos/dashboard-bg.jpg'); background-position: center 30%;"
      />
      <div
        class="absolute inset-0 pointer-events-none"
        style="background: linear-gradient(105deg,
          rgba(7, 24, 30, 0.96) 0%,
          rgba(7, 24, 30, 0.86) 35%,
          rgba(7, 24, 30, 0.78) 65%,
          rgba(7, 24, 30, 0.88) 100%);"
      />
      <div
        class="absolute inset-0 pointer-events-none"
        style="background: radial-gradient(700px 500px at 12% 50%, rgba(0, 76, 84, 0.32), transparent 60%);"
      />
      <div class="absolute inset-0 wing-pattern opacity-25 pointer-events-none" />
      <div
        class="absolute inset-0 pointer-events-none"
        style="background: radial-gradient(400px 300px at 88% 110%, rgba(45, 212, 191, 0.10), transparent 70%);"
      />

      <div class="relative col-span-12 lg:col-span-5 p-7 border-r border-bg-line/60">
        <p class="eyebrow text-cyan">Total de resíduos rastreados</p>
        <div class="flex items-baseline gap-3 mt-3">
          <span
            class="scoreboard text-[112px] text-ink leading-[0.85]"
            style="text-shadow: 0 2px 24px rgba(0, 0, 0, 0.5);"
          >{{ animated.total }}</span>
          <span class="font-display text-cyan text-[24px]"
                style="font-weight: 500; font-style: italic; letter-spacing: 0.04em;">TON</span>
        </div>
        <div class="flex items-center gap-3 mt-3.5">
          <span
            class="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full
                   bg-success-soft border border-success/30 text-success
                   text-[11px] font-semibold backdrop-blur-md"
          >
            <TrendingUp :size="11" /> +5,2% vs período anterior
          </span>
          <span class="mono-tag text-ink-2 text-[11px]">atualizado há 2 min</span>
        </div>

        <svg viewBox="0 0 200 50" class="mt-6 w-full h-12" preserveAspectRatio="none">
          <defs>
            <linearGradient id="sparkfill" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%"   stop-color="#2DD4BF" stop-opacity="0.5" />
              <stop offset="100%" stop-color="#2DD4BF" stop-opacity="0"   />
            </linearGradient>
          </defs>
          <path d="M0 38 L25 32 L50 35 L75 22 L100 26 L125 18 L150 22 L175 12 L200 8 L200 50 L0 50 Z" fill="url(#sparkfill)" />
          <path d="M0 38 L25 32 L50 35 L75 22 L100 26 L125 18 L150 22 L175 12 L200 8" stroke="#2DD4BF" stroke-width="1.5" fill="none" />
        </svg>
      </div>

      <div class="relative col-span-12 lg:col-span-7 grid grid-cols-3 divide-x divide-bg-line/60">
        <SupportStat
          v-for="(s, i) in supportingKpis"
          :key="s.label"
          :label="s.label"
          :value="animatedSupport[i]"
          :unit="s.unit"
          :delta="s.delta"
          :icon="s.icon"
          :trend="s.trend"
        />
      </div>
    </section>

    <section class="grid grid-cols-12 gap-5">
      <div
        class="col-span-12 lg:col-span-8 rounded-2xl bg-bg-panel border border-bg-line-strong
               p-7 helmet-stripe fade-up-2"
      >
        <div class="flex items-end justify-between mb-6">
          <div>
            <p class="eyebrow">Volume · últimos 6 meses</p>
            <h3 class="section-title text-[26px] mt-2">
              Transportes por mês
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
        <div class="h-[280px]">
          <Bar :data="chartData" :options="chartOptions" />
        </div>
      </div>

      <div
        class="col-span-12 lg:col-span-4 rounded-2xl bg-bg-panel border border-bg-line-strong
               p-7 helmet-stripe fade-up-3"
      >
        <div class="flex items-center justify-between mb-6">
          <div>
            <p class="eyebrow">Play-by-play</p>
            <h3 class="section-title text-[26px] mt-2">
              Feed operacional
            </h3>
          </div>
          <span class="flex items-center gap-1.5 px-2 py-0.5 rounded-full bg-danger-soft border border-danger/30">
            <span class="relative flex h-1.5 w-1.5">
              <span class="absolute inline-flex h-full w-full rounded-full bg-danger opacity-75 live-pulse" />
              <span class="relative inline-flex h-1.5 w-1.5 rounded-full bg-danger" />
            </span>
            <span class="eyebrow text-danger text-[9px]">Ao vivo</span>
          </span>
        </div>

        <ol class="relative pl-5 space-y-4 max-h-[260px] overflow-y-auto pr-1">
          <span class="absolute left-[5px] top-1 bottom-1 w-px bg-bg-line-strong" />
          <li
            v-for="(a, i) in atividades"
            :key="i"
            class="relative flex flex-col gap-0.5 fade-up"
            :style="{ animationDelay: `${i * 60}ms` }"
          >
            <span
              class="absolute -left-5 top-1.5 w-2.5 h-2.5 rounded-full border-2 border-bg-panel"
              :style="{ backgroundColor: a.cor }"
            />
            <p class="text-[13px] text-ink leading-snug">{{ a.texto }}</p>
            <p class="mono-tag text-ink-3 text-[10.5px]">{{ a.tempo }}</p>
          </li>
        </ol>
      </div>
    </section>

    <section class="grid grid-cols-12 gap-5 fade-up-4">
      <Meter
        v-for="m in meters"
        :key="m.label"
        :label="m.label"
        :value="m.value"
        :hint="m.hint"
        :tone="m.tone"
      />
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, h } from 'vue'
import { Bar } from 'vue-chartjs'
import {
  Chart as ChartJS, CategoryScale, LinearScale, BarElement, Tooltip, LineElement, PointElement,
} from 'chart.js'
import {
  Building2, Boxes, Truck, TrendingUp, TrendingDown, Download,
} from 'lucide-vue-next'

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, LineElement, PointElement)

const periodo  = ref('30d')
const periodos = ['7d', '30d', '90d', '12m']

const supportingKpis = [
  { label: 'Empresas', value: 84,  unit: '',          delta: '+12', trend: 'up', icon: Building2 },
  { label: 'Lotes',    value: 217, unit: 'ativos',    delta: '+8',  trend: 'up', icon: Boxes },
  { label: 'Trânsito', value: 43,  unit: 'em campo',  delta: '+15', trend: 'up', icon: Truck },
]

const meters = [
  { label: 'Capacidade operacional', value: 78, hint: 'Aterro autorizado · 78% ocupado',  tone: 'amber'   },
  { label: 'Cobertura regional',     value: 92, hint: '23 municípios · 92% mapeados',      tone: 'cyan'    },
  { label: 'Conformidade PNRS',      value: 96, hint: 'Auditoria semanal · 96% conformes', tone: 'success' },
]

const atividades = [
  { texto: 'Lote LT-2026-0318 iniciou transporte para tratamento térmico', tempo: '2 min atrás',   cor: '#38BDF8' },
  { texto: 'Lote LT-2026-0315 confirmado em destino final',                tempo: '15 min atrás',  cor: '#10B981' },
  { texto: 'Novo lote LT-2026-0319 criado · 480 KG · resíduo químico',     tempo: '1 hora atrás',  cor: '#F59E0B' },
  { texto: 'Transporte TR-2026-0142 cancelado por motorista',              tempo: '2 horas atrás', cor: '#DC2626' },
  { texto: 'Lote LT-2026-0312 processado com sucesso',                     tempo: '3 horas atrás', cor: '#10B981' },
  { texto: 'Empresa TerraVerde Ambiental cadastrada',                      tempo: '5 horas atrás', cor: '#2DD4BF' },
]

const dateLong = new Date().toLocaleDateString('pt-BR', {
  weekday: 'long', day: '2-digit', month: 'long', year: 'numeric',
}).toUpperCase()

const animated        = ref({ total: 0 })
const animatedSupport = ref([0, 0, 0])

const animate = (target, key, dur = 1200) => {
  const start = performance.now()
  const ease  = (t) => 1 - Math.pow(1 - t, 3)
  const tick  = (now) => {
    const t = Math.min(1, (now - start) / dur)
    const v = Math.round(target * ease(t))
    if (typeof key === 'number') animatedSupport.value[key] = v
    else                          animated.value[key]      = v
    if (t < 1) requestAnimationFrame(tick)
  }
  requestAnimationFrame(tick)
}

onMounted(() => {
  animate(1840, 'total', 1400)
  supportingKpis.forEach((s, i) => animate(s.value, i, 1100 + i * 120))
})

const chartData = computed(() => ({
  labels: ['OUT', 'NOV', 'DEZ', 'JAN', 'FEV', 'MAR'],
  datasets: [
    {
      label: 'Volume',
      data: [44, 50, 46, 62, 55, 68],
      backgroundColor: (ctx) => {
        const { ctx: c, chartArea } = ctx.chart
        if (!chartArea) return '#2DD4BF'
        const g = c.createLinearGradient(0, chartArea.bottom, 0, chartArea.top)
        g.addColorStop(0, 'rgba(0, 109, 120, 0.6)')
        g.addColorStop(1, 'rgba(45, 212, 191, 0.95)')
        return g
      },
      hoverBackgroundColor: '#2DD4BF',
      borderRadius:  { topLeft: 6, topRight: 6, bottomLeft: 0, bottomRight: 0 },
      borderSkipped: false,
      barThickness:  28,
    },
    {
      label: 'Meta',
      data: [55, 55, 55, 55, 55, 55],
      type: 'line',
      borderColor: 'rgba(0, 76, 84, 0.7)',
      borderDash:  [4, 4],
      borderWidth: 1.5,
      pointRadius: 0,
      tension:     0,
    },
  ],
}))

const chartOptions = {
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

const SupportStat = (props) => h('div', { class: 'p-7 flex flex-col justify-between min-h-[180px] relative' }, [
  h('div', { class: 'flex items-center justify-between' }, [
    h('p', { class: 'eyebrow' }, props.label),
    h(props.icon, { size: 16, class: 'text-ink-3' }),
  ]),
  h('div', {}, [
    h('p', {
      class: 'scoreboard text-[60px] text-ink mt-3 leading-none tabular-nums',
      style: 'text-shadow: 0 2px 12px rgba(0, 0, 0, 0.4);',
    }, props.value),
    props.unit ? h('p', { class: 'mono-tag text-ink-2 mt-1.5 text-[11px]' }, props.unit) : null,
  ]),
  h('div', { class: 'flex items-center gap-1.5' }, [
    h('span', {
      class: [
        'inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-semibold border backdrop-blur-md',
        props.trend === 'up'
          ? 'bg-success-soft border-success/30 text-success'
          : 'bg-danger-soft  border-danger/30  text-danger',
      ],
    }, [
      h(props.trend === 'up' ? TrendingUp : TrendingDown, { size: 11 }),
      ` ${props.delta}%`,
    ]),
  ]),
])
SupportStat.props = ['label', 'value', 'unit', 'delta', 'icon', 'trend']

const Meter = (props) => {
  const toneMap = {
    cyan:    { bar: 'bg-cyan',    text: 'text-cyan'    },
    amber:   { bar: 'bg-amber',   text: 'text-amber'   },
    success: { bar: 'bg-success', text: 'text-success' },
  }[props.tone] || { bar: 'bg-cyan', text: 'text-cyan' }

  return h('div', {
    class: 'col-span-12 md:col-span-4 rounded-xl bg-bg-panel border border-bg-line-strong p-6',
  }, [
    h('div', { class: 'flex items-center justify-between mb-3.5' }, [
      h('p', { class: 'eyebrow' }, props.label),
      h('p', { class: ['scoreboard text-[24px] tabular-nums', toneMap.text] }, `${props.value}%`),
    ]),
    h('div', { class: 'h-1.5 rounded-full bg-bg-base overflow-hidden' }, [
      h('div', {
        class: ['h-full rounded-full transition-all duration-700', toneMap.bar],
        style: `width: ${props.value}%`,
      }),
    ]),
    h('p', { class: 'mono-tag text-ink-3 text-[10.5px] mt-3' }, props.hint),
  ])
}
Meter.props = ['label', 'value', 'hint', 'tone']
</script>
